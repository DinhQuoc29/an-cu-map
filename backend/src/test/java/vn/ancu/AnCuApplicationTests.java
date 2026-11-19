package vn.ancu;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import vn.ancu.flood.FloodService;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test: khởi động Spring context với PostGIS thật (Testcontainers).
 * Cần Docker chạy.
 */
@SpringBootTest
@Testcontainers
class AnCuApplicationTests {
    @Autowired JdbcTemplate jdbc;
    @Autowired FloodService floodService;
    @Autowired org.springframework.cache.CacheManager cacheManager;
    @Autowired vn.ancu.airquality.OpenMeteoClient airClient;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:16-3.4-alpine")
                    .asCompatibleSubstituteFor("postgres")
    )
            .withDatabaseName("ancu_test")
            .withUsername("ancu")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // Tắt Valhalla/external calls trong test
        registry.add("valhalla.url", () -> "http://localhost:9999");
        registry.add("openaq.api-key", () -> "test-key");
    }

    @Test
    void contextLoads() {
        // Nếu test này pass, Spring context khởi động thành công với Flyway + PostGIS
    }

    @Test
    void annualAirQualityCacheSupportsReactiveResults() {
        var cache = cacheManager.getCache("airQualityCache");
        var expected = new vn.ancu.airquality.AirQualityResponseDto(
                new vn.ancu.common.DataField<>(25.0, "µg/m³", vn.ancu.common.SourceType.MODELED,
                        "Test", null, java.time.Instant.now()), null);
        cache.put("21.0:105.8", expected);
        assertThat(airClient.fetchAvgPm25(21.0, 105.8).block()).isEqualTo(expected);
        cache.clear();
    }

    @Test
    @Transactional
    void onlyReviewedRealUnexpiredHistoricalEvidenceIsPublished() {
        String insert = """
                INSERT INTO flood_point (geom, source_type, source_name, source_url, event_date,
                  location_label, location_precision_m, is_demo, reviewed, expires_at)
                VALUES (ST_MakePoint(105.85,21.03)::geography, 'HISTORICAL', 'Test', ?,
                  '2024-08-13', ?, 100, ?, ?, ?)
                """;
        jdbc.update(insert, "https://example.com/demo", "Demo", true, true, null);
        jdbc.update(insert, "https://example.com/pending", "Pending", false, false, null);
        jdbc.update(insert, "https://example.com/", "Homepage only", false, true, null);
        jdbc.update(insert, "https://example.com/expired", "Expired", false, true, java.sql.Timestamp.valueOf("2020-01-01 00:00:00"));
        jdbc.update(insert, "https://example.com/article", "Sourced intersection", false, true, null);
        var response = floodService.calcRisk(21.03, 105.85);
        assertThat(response.riskScore()).isNull();
        assertThat(response.evidence()).hasSize(1);
        assertThat(response.evidence().getFirst().locationLabel()).isEqualTo("Sourced intersection");
    }
}


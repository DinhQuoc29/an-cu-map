package vn.ancu;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Smoke test: khởi động Spring context với PostGIS thật (Testcontainers).
 * Cần Docker chạy.
 */
@SpringBootTest
@Testcontainers
class AnCuApplicationTests {

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
}


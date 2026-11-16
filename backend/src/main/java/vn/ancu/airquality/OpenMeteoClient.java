package vn.ancu.airquality;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import vn.ancu.common.DataField;
import vn.ancu.common.SourceType;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Client gọi Open-Meteo Air Quality API (CAMS ~10 km, không cần key).
 * Tính trung bình PM2.5 trong 365 ngày gần nhất.
 *
 * @see <a href="https://open-meteo.com/en/docs/air-quality-api">Open-Meteo Docs</a>
 */
@Service
public class OpenMeteoClient {

    private static final Logger log = LoggerFactory.getLogger(OpenMeteoClient.class);
    private static final String PM25_SOURCE = "Open-Meteo CAMS (~10 km)";

    private final WebClient webClient;
    private final int timeoutSeconds;

    public OpenMeteoClient(
            WebClient.Builder builder,
            @Value("${open-meteo.base-url}") String baseUrl,
            @Value("${open-meteo.timeout-seconds:8}") int timeoutSeconds
    ) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.timeoutSeconds = timeoutSeconds;
    }

    /**
     * Lấy trung bình PM2.5 theo mô hình CAMS 12 tháng.
     *
     * @return AirQualityResponseDto hoặc empty nếu lỗi
     */
    public Mono<AirQualityResponseDto> fetchAvgPm25(double lat, double lng) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/air-quality")
                        .queryParam("latitude", lat)
                        .queryParam("longitude", lng)
                        .queryParam("hourly", "pm2_5")
                        .queryParam("past_days", 365)
                        .queryParam("forecast_days", 0)
                        .queryParam("timezone", "Asia/Bangkok")
                        .build())
                .retrieve()
                .bodyToMono(OpenMeteoResponse.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .map(resp -> {
                    double avg = computeAvg(resp.hourly().pm2_5());
                    var field = new DataField<>(
                            avg,
                            "µg/m³",
                            SourceType.MODELED,
                            PM25_SOURCE,
                            0.6,                // độ tin cậy mô hình: trung bình
                            Instant.now()
                    );
                    return new AirQualityResponseDto(field, null);
                })
                .onErrorResume(ex -> {
                    log.warn("Open-Meteo error (lat={}, lng={}): {}", lat, lng, ex.getMessage());
                    return Mono.empty();
                });
    }

    /** Tính trung bình danh sách double, bỏ qua null */
    private double computeAvg(List<Double> values) {
        if (values == null || values.isEmpty()) return 0.0;
        double sum = 0.0;
        int count = 0;
        for (Double v : values) {
            if (v != null) {
                sum += v;
                count++;
            }
        }
        return count == 0 ? 0.0 : Math.round((sum / count) * 10.0) / 10.0;
    }

    // ─── Internal response records ───────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    record OpenMeteoResponse(Hourly hourly) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Hourly(@JsonProperty("pm2_5") List<Double> pm2_5) {}
}


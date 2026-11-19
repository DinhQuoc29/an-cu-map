package vn.ancu.airquality;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import vn.ancu.common.DataField;
import vn.ancu.common.SourceType;

import java.time.*;
import java.util.*;

/** Annual PM2.5 from CAMS Global, about 45 km spatial resolution over Hanoi. */
@Service
public class OpenMeteoClient {
    private static final Logger log = LoggerFactory.getLogger(OpenMeteoClient.class);
    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private final WebClient webClient;
    private final int timeoutSeconds;

    public OpenMeteoClient(WebClient.Builder builder,
            @Value("${open-meteo.base-url}") String baseUrl,
            @Value("${open-meteo.timeout-seconds:8}") int timeoutSeconds) {
        this.webClient = builder.baseUrl(baseUrl)
                .codecs(config -> config.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)).build();
        this.timeoutSeconds = timeoutSeconds;
    }

    @Cacheable(value = "airQualityCache", key = "T(java.lang.String).format(T(java.util.Locale).ROOT, '%.1f:%.1f', #lat, #lng)")
    public Mono<AirQualityResponseDto> fetchAvgPm25(double lat, double lng) {
        LocalDate end = LocalDate.now(ZONE).minusDays(1);
        LocalDate start = end.minusDays(364);
        return webClient.get().uri(uri -> uri.path("/v1/air-quality")
                        .queryParam("latitude", lat).queryParam("longitude", lng)
                        .queryParam("hourly", "pm2_5").queryParam("domains", "cams_global")
                        .queryParam("start_date", start).queryParam("end_date", end)
                        .queryParam("timezone", ZONE.getId()).build())
                .retrieve().bodyToMono(OpenMeteoResponse.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .flatMap(resp -> toAnnualAverage(resp, start, end))
                .onErrorResume(ex -> {
                    log.warn("Open-Meteo annual data unavailable: {}", ex.getClass().getSimpleName());
                    return Mono.empty();
                });
    }

    Mono<AirQualityResponseDto> toAnnualAverage(OpenMeteoResponse resp, LocalDate start, LocalDate end) {
        if (resp.hourly() == null || resp.hourly().time() == null || resp.hourly().pm2_5() == null) {
            return Mono.empty();
        }
        var times = resp.hourly().time();
        var values = resp.hourly().pm2_5();
        int expected = Math.toIntExact((java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1) * 24);
        Set<LocalDateTime> seen = new HashSet<>();
        double sum = 0;
        for (int i = 0; i < Math.min(times.size(), values.size()); i++) {
            Double value = values.get(i);
            if (value == null || !Double.isFinite(value) || value < 0 || times.get(i) == null) continue;
            LocalDateTime time = LocalDateTime.parse(times.get(i));
            if (time.getMinute() != 0 || time.toLocalDate().isBefore(start)
                    || time.toLocalDate().isAfter(end) || !seen.add(time)) continue;
            sum += value;
        }
        // Sparse data is insufficient to represent an annual mean.
        if (seen.size() < Math.ceil(expected * 0.9)) return Mono.empty();
        double average = Math.round(sum / seen.size() * 10) / 10.0;
        var field = new DataField<>(average, "µg/m³", SourceType.MODELED,
                "Open-Meteo / CAMS Global (~45 km)", null, Instant.now());
        return Mono.just(new AirQualityResponseDto(field, null, start, end, seen.size(), expected));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record OpenMeteoResponse(Hourly hourly) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Hourly(List<String> time, @JsonProperty("pm2_5") List<Double> pm2_5) {}
}

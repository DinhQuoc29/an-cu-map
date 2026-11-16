package vn.ancu.routing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Client gọi Valhalla routing engine.
 * Fallback: trả empty khi timeout / circuit open.
 */
@Service
public class ValhallaClient {

    private static final Logger log = LoggerFactory.getLogger(ValhallaClient.class);

    private final WebClient webClient;
    private final int timeoutSeconds;

    public ValhallaClient(
            WebClient.Builder builder,
            @Value("${valhalla.url}") String valhallaUrl,
            @Value("${valhalla.timeout-seconds:10}") int timeoutSeconds
    ) {
        this.webClient = builder.baseUrl(valhallaUrl).build();
        this.timeoutSeconds = timeoutSeconds;
    }

    /**
     * Gọi Valhalla POST /route.
     *
     * @param oLat origin latitude
     * @param oLng origin longitude
     * @param dLat destination latitude
     * @param dLng destination longitude
     * @param mode Valhalla costing: motor_scooter | auto | bicycle | pedestrian
     * @return RouteResultDto hoặc empty nếu lỗi
     */
    public Mono<RouteResultDto> route(double oLat, double oLng,
                                       double dLat, double dLng,
                                       String mode) {
        // Valhalla request body
        var body = Map.of(
                "locations", List.of(
                        Map.of("lat", oLat, "lon", oLng, "type", "break"),
                        Map.of("lat", dLat, "lon", dLng, "type", "break")
                ),
                "costing", mode,
                "units", "km",
                "directions_options", Map.of("language", "vi-VN")
        );

        return webClient.post()
                .uri("/route")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(ValhallaResponse.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .map(resp -> {
                    var leg = resp.trip().legs().get(0).summary();
                    return new RouteResultDto(mode,
                            (int) Math.round(leg.time()),
                            (int) Math.round(leg.length() * 1000));
                })
                .onErrorResume(ex -> {
                    log.warn("Valhalla route error (mode={}): {}", mode, ex.getMessage());
                    return Mono.empty();
                });
    }

    // ─── Internal response records ───────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ValhallaResponse(Trip trip) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Trip(List<Leg> legs) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Leg(Summary summary) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Summary(
            double length,  // km
            double time     // seconds
    ) {}
}


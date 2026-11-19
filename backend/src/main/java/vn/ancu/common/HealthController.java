package vn.ancu.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Duration;
import java.util.Map;

@RestController
public class HealthController {
    private final JdbcTemplate jdbc;
    private final WebClient valhalla;

    public HealthController(JdbcTemplate jdbc, WebClient.Builder builder, @Value("${valhalla.url}") String url) {
        this.jdbc = jdbc;
        this.valhalla = builder.baseUrl(url).build();
    }

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, String>> health() {
        boolean databaseUp;
        boolean routingUp;
        try { databaseUp = Integer.valueOf(1).equals(jdbc.queryForObject("SELECT 1", Integer.class)); }
        catch (Exception ex) { databaseUp = false; }
        try {
            routingUp = Boolean.TRUE.equals(valhalla.get().uri("/status").retrieve().toBodilessEntity()
                    .map(response -> response.getStatusCode().is2xxSuccessful())
                    .timeout(Duration.ofSeconds(2)).onErrorReturn(false).block());
        } catch (Exception ex) { routingUp = false; }
        var body = Map.of("status", databaseUp && routingUp ? "UP" : "DEGRADED",
                "database", databaseUp ? "UP" : "DOWN", "routing", routingUp ? "UP" : "DOWN");
        return ResponseEntity.status(databaseUp && routingUp ? 200 : 503).body(body);
    }
}

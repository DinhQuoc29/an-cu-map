package vn.ancu.geocode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

/**
 * Client gọi Nominatim (OSM Geocoding).
 * Cache kết quả 1 giờ bằng Caffeine để không vi phạm rate limit 1 req/s.
 *
 * @see <a href="https://nominatim.org/release-docs/latest/api/Search/">Nominatim Search API</a>
 */
@Service
public class NominatimClient {

    private static final Logger log = LoggerFactory.getLogger(NominatimClient.class);
    private static final String NOMINATIM_BASE = "https://nominatim.openstreetmap.org";
    // Bounding box Hà Nội — ưu tiên kết quả trong vùng này
    private static final String HANOI_VIEWBOX = "105.28,20.85,106.05,21.40";

    private final WebClient webClient;

    public NominatimClient(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl(NOMINATIM_BASE)
                .defaultHeader("User-Agent", "AnCuMap/1.0 (contact@ancu.vn)")
                .defaultHeader("Accept-Language", "vi,en;q=0.8")
                .build();
    }

    /**
     * Tìm địa chỉ theo chuỗi truy vấn.
     * Kết quả được cache trong Caffeine (xem AppConfig — tên "geocodeCache").
     */
    @Cacheable(value = "geocodeCache", key = "#query.toLowerCase().trim()")
    public List<GeocodeResultDto> search(String query) {
        if (query == null || query.isBlank()) return List.of();

        List<NominatimItem> raw = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("q", query)
                        .queryParam("format", "jsonv2")
                        .queryParam("limit", 5)
                        .queryParam("viewbox", HANOI_VIEWBOX)
                        .queryParam("bounded", 0)
                        .queryParam("countrycodes", "vn")
                        .build())
                .retrieve()
                .bodyToFlux(NominatimItem.class)
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(ex -> {
                    log.warn("Nominatim error for '{}': {}", query, ex.getMessage());
                    return Flux.empty();
                })
                .collectList()
                .block();

        if (raw == null) return List.of();

        return raw.stream().map(item -> {
            double[] bbox = null;
            if (item.boundingbox() != null && item.boundingbox().length == 4) {
                bbox = new double[]{
                        Double.parseDouble(item.boundingbox()[0]),
                        Double.parseDouble(item.boundingbox()[1]),
                        Double.parseDouble(item.boundingbox()[2]),
                        Double.parseDouble(item.boundingbox()[3]),
                };
            }
            return new GeocodeResultDto(
                    String.valueOf(item.place_id()),
                    item.display_name(),
                    Double.parseDouble(item.lat()),
                    Double.parseDouble(item.lon()),
                    bbox
            );
        }).toList();
    }

    // ─── Internal records ─────────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    record NominatimItem(
            @JsonProperty("place_id") long place_id,
            @JsonProperty("display_name") String display_name,
            String lat,
            String lon,
            String[] boundingbox
    ) {}
}


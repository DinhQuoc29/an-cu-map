package vn.ancu.geocode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import vn.ancu.amenity.AmenityGeocodeRow;
import vn.ancu.amenity.AmenityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/** Search-as-you-type geocoder backed by Photon, with local OSM amenities as a supplement. */
@Service
public class PhotonClient {

    private static final Logger log = LoggerFactory.getLogger(PhotonClient.class);
    private static final String HANOI_BBOX = "105.28,20.85,106.05,21.40";
    private static final int MAX_RESULTS = 15;

    private final WebClient webClient;
    private final AmenityRepository amenityRepository;

    public PhotonClient(
            WebClient.Builder builder,
            AmenityRepository amenityRepository,
            @Value("${geocoding.photon.base-url:https://photon.komoot.io}") String baseUrl
    ) {
        this.amenityRepository = amenityRepository;
        this.webClient = builder.baseUrl(baseUrl)
                .defaultHeader("User-Agent", "AnCuMap/1.0 (contact@ancu.vn)")
                .build();
    }

    /**
     * Searches all OSM place types in Hanoi and supplements those results with
     * amenities already imported into the local database.
     */
    @Cacheable(value = "geocodeCache", key = "#query.toLowerCase().trim()",
            unless = "#result.isEmpty() || #result[0].placeId().startsWith('amenity-')")
    public List<GeocodeResultDto> search(String query) {
        if (query == null || query.trim().length() < 3) return List.of();

        String normalizedQuery = query.trim();
        List<GeocodeResultDto> photonResults = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api")
                        .queryParam("q", normalizedQuery)
                        .queryParam("limit", 10)
                        .queryParam("bbox", HANOI_BBOX)
                        .queryParam("countrycode", "VN")
                        .build())
                .retrieve()
                .bodyToMono(PhotonFeatureCollection.class)
                .timeout(Duration.ofSeconds(5))
                .map(this::toGeocodeResults)
                .onErrorResume(ex -> {
                    log.warn("Photon geocoding failed: {}", ex.getMessage());
                    return Mono.empty();
                })
                .defaultIfEmpty(List.of())
                .block();

        List<GeocodeResultDto> localResults = searchLocalAmenities(normalizedQuery);
        Map<String, GeocodeResultDto> uniqueResults = new LinkedHashMap<>();
        Stream.concat((photonResults == null ? Stream.<GeocodeResultDto>empty() : photonResults.stream()),
                        localResults.stream())
                .forEach(result -> uniqueResults.putIfAbsent(locationKey(result), result));

        return uniqueResults.values().stream().limit(MAX_RESULTS).toList();
    }

    private List<GeocodeResultDto> toGeocodeResults(PhotonFeatureCollection collection) {
        if (collection == null || collection.features() == null) return List.of();

        return collection.features().stream()
                .filter(feature -> feature != null
                        && feature.geometry() != null
                        && feature.geometry().coordinates() != null
                        && feature.geometry().coordinates().length >= 2)
                .map(feature -> {
                    double[] coordinates = feature.geometry().coordinates();
                    double lng = coordinates[0];
                    double lat = coordinates[1];
                    PhotonProperties properties = feature.properties();
                    String displayName = formatDisplayName(properties);
                    String placeId = properties != null && properties.osmId() != null
                            ? properties.osmType() + "-" + properties.osmId()
                            : "photon-" + locationKey(lat, lng);
                    return new GeocodeResultDto(placeId, displayName, lat, lng, null);
                })
                .toList();
    }

    private String formatDisplayName(PhotonProperties properties) {
        if (properties == null) return "Địa điểm tại Hà Nội";

        Set<String> parts = new LinkedHashSet<>();
        addPart(parts, properties.name());

        String streetAddress = joinNonBlank(properties.houseNumber(), properties.street());
        addPart(parts, streetAddress);
        addPart(parts, properties.locality());
        addPart(parts, properties.district());
        addPart(parts, properties.city());
        addPart(parts, properties.county());
        addPart(parts, properties.state());

        String displayName = String.join(", ", parts);
        return displayName.isBlank() ? "Địa điểm tại Hà Nội" : displayName;
    }

    private void addPart(Set<String> parts, String value) {
        if (value == null || value.isBlank()) return;
        boolean alreadyPresent = parts.stream().anyMatch(part -> part.equalsIgnoreCase(value.trim()));
        if (!alreadyPresent) parts.add(value.trim());
    }

    private String joinNonBlank(String first, String second) {
        if (first == null || first.isBlank()) return second == null ? "" : second.trim();
        if (second == null || second.isBlank()) return first.trim();
        return first.trim() + " " + second.trim();
    }

    private List<GeocodeResultDto> searchLocalAmenities(String query) {
        return amenityRepository.searchSuggestions(query, 5).stream()
                .map(row -> {
                    String name = row.getName();
                    String address = row.getAddress();
                    String displayName = name == null || name.isBlank()
                            ? address
                            : address == null || address.isBlank()
                                    ? name
                                    : name + " — " + address;
                    return new GeocodeResultDto(
                            "amenity-" + row.getId(),
                            displayName,
                            row.getLat(),
                            row.getLng(),
                            null
                    );
                })
                .toList();
    }

    private String locationKey(GeocodeResultDto result) {
        return locationKey(result.lat(), result.lng());
    }

    private String locationKey(double lat, double lng) {
        return String.format(Locale.ROOT, "%.5f:%.5f", lat, lng);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PhotonFeatureCollection(List<PhotonFeature> features) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PhotonFeature(PhotonProperties properties, PhotonGeometry geometry) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PhotonGeometry(double[] coordinates) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PhotonProperties(
            String name,
            @JsonProperty("housenumber") String houseNumber,
            String street,
            String locality,
            String district,
            String city,
            String county,
            String state,
            @JsonProperty("osm_type") String osmType,
            @JsonProperty("osm_id") Long osmId
    ) {}
}

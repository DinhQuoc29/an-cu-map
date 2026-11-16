package vn.ancu.amenity;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AmenityService {

    private static final double DEFAULT_RADIUS_M = 2000.0;
    private static final int DEFAULT_LIMIT = 3;

    private static final List<String> CATEGORIES = Arrays.asList(
            "hospital", "clinic", "school", "market", "supermarket"
    );

    private final AmenityRepository repository;

    public AmenityService(AmenityRepository repository) {
        this.repository = repository;
    }

    public AmenitiesResponseDto findNearby(double lat, double lng) {
        List<AmenityItemDto> items = CATEGORIES.parallelStream()
                .flatMap(category ->
                        repository.findNearby(lat, lng, DEFAULT_RADIUS_M, category, DEFAULT_LIMIT)
                                .stream()
                                .map(row -> new AmenityItemDto(
                                        row.getId(),
                                        row.getCategory(),
                                        row.getSubcategory(),
                                        row.getName(),
                                        row.getLat(),
                                        row.getLng(),
                                        row.getDistanceM()
                                ))
                )
                .sorted(Comparator.comparingDouble(AmenityItemDto::distanceM))
                .collect(Collectors.toList());

        return new AmenitiesResponseDto(items, DEFAULT_RADIUS_M);
    }
}

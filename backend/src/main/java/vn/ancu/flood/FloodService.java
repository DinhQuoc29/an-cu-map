package vn.ancu.flood;

import org.springframework.stereotype.Service;

/** Historical evidence is not a calibrated flood probability or a current warning. */
@Service
public class FloodService {
    private static final double SEARCH_RADIUS_M = 500;
    private final FloodPointRepository repository;

    public FloodService(FloodPointRepository repository) {
        this.repository = repository;
    }

    public FloodRiskResponseDto calcRisk(double lat, double lng) {
        var evidence = repository.findActiveNearby(lat, lng, SEARCH_RADIUS_M).stream()
                .map(row -> new FloodEvidenceDto(row.getId(), row.getLocationLabel(), row.getEventDate(),
                        row.getSourceName(), row.getSourceUrl(), row.getDepthCm(),
                        row.getLocationPrecisionM(), row.getDistanceM()))
                .toList();
        return new FloodRiskResponseDto(null, evidence.size(), evidence,
                evidence.isEmpty() ? "INSUFFICIENT_DATA" : "HISTORICAL_EVIDENCE", SEARCH_RADIUS_M);
    }
}

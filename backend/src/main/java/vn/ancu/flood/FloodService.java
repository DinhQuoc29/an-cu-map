package vn.ancu.flood;

import org.springframework.stereotype.Service;
import vn.ancu.common.DataField;
import vn.ancu.common.SourceType;

import java.time.Instant;
import java.util.List;

/**
 * Service tính nguy cơ ngập.
 *
 * <p>Công thức:
 * <pre>
 *   weightedSum = Σ(severity_i × confidence_i)  cho điểm trong 500 m
 *   riskScore   = min(5, max(1, ceil(weightedSum / 2)))
 * </pre>
 * Nếu không có điểm ngập nào → riskScore = 1 (rất thấp).
 */
@Service
public class FloodService {

    private static final double SEARCH_RADIUS_M = 500.0;
    // Độ tin cậy dữ liệu lịch sử báo chí — thấp do chưa xác minh hoàn toàn
    private static final double BASE_CONFIDENCE = 0.4;
    private static final String SOURCE_NAME = "Dữ liệu ngập lịch sử Hà Nội";

    private final FloodPointRepository repository;

    public FloodService(FloodPointRepository repository) {
        this.repository = repository;
    }

    /**
     * Tính điểm nguy cơ ngập tại tọa độ.
     */
    public FloodRiskResponseDto calcRisk(double lat, double lng) {
        List<FloodScoreRow> rows = repository.findActiveNearby(lat, lng, SEARCH_RADIUS_M);

        int score = computeScore(rows);
        double confidence = rows.isEmpty() ? 0.2 : BASE_CONFIDENCE;

        var field = new DataField<>(
                score,
                "",
                SourceType.HISTORICAL,
                SOURCE_NAME,
                confidence,
                Instant.now()
        );

        return new FloodRiskResponseDto(field, rows.size());
    }

    /**
     * Tính điểm nguy cơ từ danh sách điểm ngập gần nhất.
     */
    int computeScore(List<FloodScoreRow> rows) {
        if (rows.isEmpty()) return 1;

        double weightedSum = rows.stream()
                .mapToDouble(row -> {
                    double conf = (row.getConfidence() != null) ? row.getConfidence() : 0.5;
                    return row.getSeverity() * conf;
                })
                .sum();

        int score = (int) Math.ceil(weightedSum / 2.0);
        return Math.min(5, Math.max(1, score));
    }
}


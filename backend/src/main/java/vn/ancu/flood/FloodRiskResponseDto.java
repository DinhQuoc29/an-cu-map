package vn.ancu.flood;

import vn.ancu.common.DataField;
import java.util.List;

/** DTO response nguy cơ ngập */
public record FloodRiskResponseDto(
        DataField<Integer> riskScore,   // null until a flood risk model is calibrated
        int nearbyPointsCount,
        List<FloodEvidenceDto> evidence,
        String assessmentStatus,
        double radiusM
) {
    public FloodRiskResponseDto(DataField<Integer> riskScore, int nearbyPointsCount) {
        this(riskScore, nearbyPointsCount, List.of(), "INSUFFICIENT_DATA", 500);
    }
}


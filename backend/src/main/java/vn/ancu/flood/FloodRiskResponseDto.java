package vn.ancu.flood;

import vn.ancu.common.DataField;

/** DTO response nguy cơ ngập */
public record FloodRiskResponseDto(
        DataField<Integer> riskScore,   // 1–5
        int nearbyPointsCount
) {}


package vn.ancu.flood;

/** Projection cho native query tính điểm ngập */
public interface FloodScoreRow {
    Integer getSeverity();
    Double getConfidence();
}


package vn.ancu.flood;

import java.time.LocalDate;

public interface FloodEvidenceRow {
    Long getId();
    String getLocationLabel();
    LocalDate getEventDate();
    String getSourceName();
    String getSourceUrl();
    Integer getDepthCm();
    Integer getLocationPrecisionM();
    Double getDistanceM();
}

package vn.ancu.airquality;

import vn.ancu.common.DataField;
import vn.ancu.common.SourceType;
import java.time.LocalDate;

/** DTO response chất lượng không khí */
public record AirQualityResponseDto(
        DataField<Double> pm25,
        Double nearestStationKm,
        LocalDate periodStart,
        LocalDate periodEnd,
        int validHours,
        int expectedHours
) {
    public AirQualityResponseDto(DataField<Double> pm25, Double nearestStationKm) {
        this(pm25, nearestStationKm, null, null, 0, 0);
    }
}


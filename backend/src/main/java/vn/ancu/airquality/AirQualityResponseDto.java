package vn.ancu.airquality;

import vn.ancu.common.DataField;
import vn.ancu.common.SourceType;

/** DTO response chất lượng không khí */
public record AirQualityResponseDto(
        DataField<Double> pm25,
        Double nearestStationKm   // null khi dùng mô hình (không có trạm đo)
) {}


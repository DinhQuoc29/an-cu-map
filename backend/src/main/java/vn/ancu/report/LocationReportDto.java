package vn.ancu.report;

import vn.ancu.amenity.AmenitiesResponseDto;
import vn.ancu.airquality.AirQualityResponseDto;
import vn.ancu.flood.FloodRiskResponseDto;
import vn.ancu.routing.RouteResultDto;

import java.util.List;

/**
 * DTO báo cáo tổng hợp vị trí.
 * Mỗi field có thể null nếu nguồn dữ liệu tương ứng không khả dụng.
 */
public record LocationReportDto(
        double lat,
        double lng,
        AmenitiesResponseDto amenities,
        AirQualityResponseDto airQuality,
        FloodRiskResponseDto floodRisk,
        List<RouteResultDto> commute
) {}


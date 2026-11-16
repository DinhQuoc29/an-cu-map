package vn.ancu.amenity;

import java.util.List;

/** DTO response danh sách tiện ích */
public record AmenitiesResponseDto(
        List<AmenityItemDto> items,
        double radiusM
) {}


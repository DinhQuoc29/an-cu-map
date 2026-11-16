package vn.ancu.amenity;

import java.util.List;

/** DTO kết quả tiện ích gần nhất */
public record AmenityItemDto(
        long id,
        String category,
        String subcategory,
        String name,
        String address,
        double lat,
        double lng,
        double distanceM
) {}


package vn.ancu.amenity;

/**
 * Projection cho native query tìm tiện ích gần nhất.
 * Dùng interface projection của Spring Data JPA.
 */
public interface AmenityNearbyRow {
    Long getId();
    String getCategory();
    String getSubcategory();
    String getName();
    Double getLat();
    Double getLng();
    Double getDistanceM();
}


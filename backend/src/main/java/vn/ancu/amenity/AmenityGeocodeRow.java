package vn.ancu.amenity;

/** Projection for address suggestions backed by locally imported OSM amenities. */
public interface AmenityGeocodeRow {
    Long getId();
    String getCategory();
    String getName();
    String getAddress();
    Double getLat();
    Double getLng();
}

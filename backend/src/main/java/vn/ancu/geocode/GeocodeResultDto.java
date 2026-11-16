package vn.ancu.geocode;

/** DTO kết quả geocode — khớp GeocodeResult frontend */
public record GeocodeResultDto(
        String placeId,
        String displayName,
        double lat,
        double lng,
        double[] boundingBox   // [minLat, maxLat, minLng, maxLng], có thể null
) {}


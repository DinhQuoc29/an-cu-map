package vn.ancu.flood;

import java.time.LocalDate;

public record FloodEvidenceDto(
        Long id, String locationLabel, LocalDate eventDate,
        String sourceName, String sourceUrl, Integer depthCm,
        Integer locationPrecisionM, double distanceM
) {}

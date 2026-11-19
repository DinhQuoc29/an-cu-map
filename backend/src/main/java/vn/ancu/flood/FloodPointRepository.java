package vn.ancu.flood;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FloodPointRepository extends JpaRepository<FloodPoint, Long> {

    /** Return only reviewed, sourced historical evidence, excluding demo and expired rows. */
    @Query(value = """
            SELECT
                id, location_label AS locationLabel, event_date AS eventDate,
                source_name AS sourceName, source_url AS sourceUrl,
                depth_cm AS depthCm, location_precision_m AS locationPrecisionM,
                ST_Distance(geom, ST_MakePoint(:lng, :lat)::geography) AS distanceM
            FROM flood_point
            WHERE status = 'ACTIVE'
              AND NOT is_demo AND reviewed
              AND source_type = 'HISTORICAL'
              AND NULLIF(TRIM(location_label), '') IS NOT NULL
              AND NULLIF(TRIM(source_name), '') IS NOT NULL
              AND event_date IS NOT NULL AND event_date <= CURRENT_DATE
              AND source_url ~ '^https?://[^/]+/.+'
              AND (expires_at IS NULL OR expires_at > NOW())
              AND ST_DWithin(geom, ST_MakePoint(:lng, :lat)::geography, :radiusM)
            ORDER BY event_date DESC, id
            """, nativeQuery = true)
    List<FloodEvidenceRow> findActiveNearby(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusM") double radiusM
    );
}


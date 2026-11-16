package vn.ancu.flood;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FloodPointRepository extends JpaRepository<FloodPoint, Long> {

    /**
     * Tìm điểm ngập ACTIVE trong bán kính radiusM.
     * Chỉ lấy severity và confidence để tính điểm nguy cơ.
     */
    @Query(value = """
            SELECT
                severity,
                confidence
            FROM flood_point
            WHERE status = 'ACTIVE'
              AND ST_DWithin(geom, ST_MakePoint(:lng, :lat)::geography, :radiusM)
            """, nativeQuery = true)
    List<FloodScoreRow> findActiveNearby(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusM") double radiusM
    );
}


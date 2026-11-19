package vn.ancu.amenity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AmenityRepository extends JpaRepository<Amenity, Long> {

    @Query(value = """
            SELECT
                id,
                category,
                subcategory,
                name,
                address,
                ST_Y(geom::geometry)                              AS lat,
                ST_X(geom::geometry)                              AS lng,
                ST_Distance(geom, ST_MakePoint(:lng, :lat)::geography) AS distance_m
            FROM amenity
            WHERE ST_DWithin(geom, ST_MakePoint(:lng, :lat)::geography, :radiusM)
              AND category = :category
            ORDER BY geom <-> ST_MakePoint(:lng, :lat)::geography
            LIMIT :lim
            """, nativeQuery = true)
    List<AmenityNearbyRow> findNearby(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusM") double radiusM,
            @Param("category") String category,
            @Param("lim") int limit
    );

    @Query(value = """
            SELECT id, category, name, address,
                   ST_Y(geom::geometry) AS lat,
                   ST_X(geom::geometry) AS lng
            FROM amenity
            WHERE (name ILIKE CONCAT('%', :query, '%')
                   OR address ILIKE CONCAT('%', :query, '%'))
            ORDER BY CASE
                         WHEN name ILIKE CONCAT(:query, '%') THEN 0
                         WHEN address ILIKE CONCAT(:query, '%') THEN 1
                         ELSE 2
                     END,
                     name NULLS LAST
            LIMIT :lim
            """, nativeQuery = true)
    List<AmenityGeocodeRow> searchSuggestions(
            @Param("query") String query,
            @Param("lim") int limit
    );
}


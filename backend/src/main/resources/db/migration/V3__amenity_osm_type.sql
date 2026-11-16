-- OSM IDs are unique within each object type. Keep the type so node, way,
-- and relation objects can safely coexist in amenity.
ALTER TABLE amenity
    ADD COLUMN osm_type VARCHAR(16) NOT NULL DEFAULT 'node';

DROP INDEX idx_amenity_osm_id;

CREATE UNIQUE INDEX idx_amenity_osm_object
    ON amenity (osm_type, osm_id);

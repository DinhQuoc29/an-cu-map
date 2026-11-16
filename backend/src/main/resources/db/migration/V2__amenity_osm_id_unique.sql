-- V2__amenity_osm_id_unique.sql
-- Thêm unique index trên osm_id để import OSM idempotent:
-- mỗi lần nạp lại OSM dùng ON CONFLICT (osm_id) DO NOTHING thay vì nhân bản.

CREATE UNIQUE INDEX idx_amenity_osm_id ON amenity (osm_id);

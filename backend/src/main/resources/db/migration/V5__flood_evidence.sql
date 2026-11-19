-- Existing flood rows are the demo dataset confirmed by the project owner.
-- Keep them for development, but exclude them from real assessments.
ALTER TABLE flood_point ADD COLUMN is_demo BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE flood_point ALTER COLUMN is_demo SET DEFAULT FALSE;
ALTER TABLE flood_point ALTER COLUMN severity DROP NOT NULL;
ALTER TABLE flood_point ADD COLUMN location_label TEXT;
ALTER TABLE flood_point ADD COLUMN location_precision_m INTEGER CHECK (location_precision_m > 0);
ALTER TABLE flood_point ADD COLUMN depth_cm INTEGER CHECK (depth_cm >= 0);
ALTER TABLE flood_point ADD COLUMN reviewed BOOLEAN NOT NULL DEFAULT FALSE;
CREATE UNIQUE INDEX idx_flood_evidence_source
    ON flood_point (source_url, event_date, location_label);

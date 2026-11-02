-- V1__init.sql
-- Giai đoạn 0: Khởi tạo schema PostGIS cho An Cư
-- Mọi thay đổi schema PHẢI qua Flyway migration, không sửa DB bằng tay.

-- Kích hoạt extension PostGIS (nếu chưa có)
CREATE EXTENSION IF NOT EXISTS postgis;

-- ============================================================
-- Bảng: amenity — Tiện ích từ OSM
-- category: hospital | clinic | school | market | supermarket
-- ============================================================
CREATE TABLE amenity (
    id          BIGSERIAL PRIMARY KEY,
    osm_id      BIGINT,
    category    VARCHAR(32)  NOT NULL,
    subcategory VARCHAR(64),
    name        VARCHAR(255),
    geom        geography(Point, 4326) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Chỉ mục GiST cho truy vấn không gian (KNN, ST_DWithin)
CREATE INDEX idx_amenity_geom ON amenity USING GIST (geom);
CREATE INDEX idx_amenity_category ON amenity (category);

-- ============================================================
-- Bảng: air_station — Trạm đo chất lượng không khí (OpenAQ)
-- source: openaq | iqair | purpleair
-- ============================================================
CREATE TABLE air_station (
    id          BIGSERIAL PRIMARY KEY,
    source      VARCHAR(32)  NOT NULL,
    external_id VARCHAR(128) NOT NULL,
    name        VARCHAR(255),
    geom        geography(Point, 4326) NOT NULL,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    UNIQUE (source, external_id)
);

CREATE INDEX idx_air_station_geom ON air_station USING GIST (geom);

-- ============================================================
-- Bảng: air_measurement — Đo lường chất lượng không khí
-- ============================================================
CREATE TABLE air_measurement (
    id          BIGSERIAL PRIMARY KEY,
    station_id  BIGINT       NOT NULL REFERENCES air_station (id) ON DELETE CASCADE,
    measured_at TIMESTAMPTZ  NOT NULL,
    pm25        NUMERIC(8,2),
    pm10        NUMERIC(8,2),
    aqi         INTEGER,
    source_type VARCHAR(16)  NOT NULL DEFAULT 'OBSERVED'
        CHECK (source_type IN ('OBSERVED', 'MODELED', 'HISTORICAL', 'USER_REPORTED', 'VERIFIED'))
);

CREATE INDEX idx_air_measurement_station_time ON air_measurement (station_id, measured_at DESC);

-- ============================================================
-- Bảng: flood_point — Điểm ngập lịch sử (nhập thủ công/CSV)
-- source_type phân biệt loại nguồn theo chuẩn CLAUDE.md
-- ============================================================
CREATE TABLE flood_point (
    id          BIGSERIAL PRIMARY KEY,
    geom        geography(Point, 4326) NOT NULL,
    severity    SMALLINT     NOT NULL CHECK (severity BETWEEN 1 AND 5),
    source_type VARCHAR(16)  NOT NULL
        CHECK (source_type IN ('OBSERVED', 'MODELED', 'HISTORICAL', 'USER_REPORTED', 'VERIFIED')),
    source_name VARCHAR(255),
    source_url  TEXT,
    event_date  DATE,
    confidence  NUMERIC(4,3) CHECK (confidence BETWEEN 0 AND 1),
    status      VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'EXPIRED', 'REJECTED')),
    expires_at  TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_flood_point_geom ON flood_point USING GIST (geom);
CREATE INDEX idx_flood_point_status ON flood_point (status);

-- ============================================================
-- Bảng: flood_report — Báo cáo ngập do người dùng
-- KHÔNG lưu định danh người báo (privacy-first)
-- ============================================================
CREATE TABLE flood_report (
    id            BIGSERIAL PRIMARY KEY,
    geom          geography(Point, 4326) NOT NULL,
    severity      SMALLINT     NOT NULL CHECK (severity BETWEEN 1 AND 5),
    photo_key     VARCHAR(255),                 -- key S3/object storage, không phải URL
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    confirm_count INTEGER      NOT NULL DEFAULT 0,
    status        VARCHAR(16)  NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'VERIFIED', 'REJECTED', 'EXPIRED')),
    expires_at    TIMESTAMPTZ  NOT NULL DEFAULT (NOW() + INTERVAL '24 hours')
);

CREATE INDEX idx_flood_report_geom ON flood_report USING GIST (geom);
CREATE INDEX idx_flood_report_status_expires ON flood_report (status, expires_at);

-- ============================================================
-- Bảng: route_cache — Cache kết quả routing Valhalla
-- origin_cell / dest_cell: lưới ~500 m (geohash hoặc H3)
-- ============================================================
CREATE TABLE route_cache (
    id           BIGSERIAL PRIMARY KEY,
    origin_cell  VARCHAR(16)  NOT NULL,
    dest_cell    VARCHAR(16)  NOT NULL,
    mode         VARCHAR(32)  NOT NULL,     -- motor_scooter | auto | bicycle | pedestrian
    depart_bucket SMALLINT    NOT NULL,     -- bucket giờ trong ngày (0-23)
    duration_s   INTEGER      NOT NULL,
    distance_m   INTEGER      NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (origin_cell, dest_cell, mode, depart_bucket)
);

CREATE INDEX idx_route_cache_lookup ON route_cache (origin_cell, dest_cell, mode, depart_bucket);


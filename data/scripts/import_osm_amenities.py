#!/usr/bin/env python3
"""
import_osm_amenities.py — Nạp tiện ích (amenity/school/market/supermarket/clinic/hospital)
từ file OSM (PBF) vào bảng `amenity` PostGIS, lọc theo bounding box Hà Nội.

Thay thế osm2pgsql flex khi không có tool native. Dùng Python osmium bindings
(libosmium) để đọc PBF và psycopg2 để chèn — khớp pattern data/scripts/.

Yêu cầu:
    pip install osmium psycopg2-binary

Chạy (từ thư mục gốc an-cu-map/):
    python data/scripts/import_osm_amenities.py \\
        --pbf infra/valhalla/data/vietnam-latest.osm.pbf

Nhập node và khu vực OSM (closed way/multipolygon relation). Khu vực được chuyển
thành điểm nằm bên trong ranh giới bằng ST_PointOnSurface. Khóa gồm loại và ID OSM.

Bounding box Hà Nội (ước lượng thô, kiểm tra lại khi dùng thật):
    lat 20.85–21.40, lng 105.28–106.05
"""

import argparse
import os
import sys
from pathlib import Path

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

try:
    import psycopg2
    from psycopg2.extras import execute_values
except ImportError:
    print("ERROR: psycopg2 chưa được cài. Chạy: pip install psycopg2-binary")
    sys.exit(1)

try:
    import osmium
except ImportError:
    print("ERROR: osmium (Python bindings) chưa được cài. Chạy: pip install osmium")
    sys.exit(1)

try:
    from dotenv import load_dotenv
    backend_env = Path(__file__).resolve().parents[2] / "backend" / ".env"
    root_env = Path(__file__).resolve().parents[2] / ".env"
    # Không override: biến môi trường thật (DB_NAME/DB_USER...) ưu tiên hơn .env
    if backend_env.exists():
        load_dotenv(backend_env)
        print(f"Đã nạp .env từ: {backend_env} (biến môi trường hiện có được giữ)")
    elif root_env.exists():
        load_dotenv(root_env)
        print(f"Đã nạp .env từ: {root_env}")
except ImportError:
    pass  # dotenv không bắt buộc

# Bounding box Hà Nội (đúng với isInsideHanoi trong frontend/src/lib/geo.ts)
HANOI_MIN_LAT = 20.85
HANOI_MAX_LAT = 21.40
HANOI_MIN_LNG = 105.28
HANOI_MAX_LNG = 106.05

BATCH_SIZE = 5000

# Ánh xạ tag OSM → (category, subcategory) — khớp amenity.lua (osm2pgsql)
AMENITY_TAGS = {
    "hospital": ("hospital", None),
    "clinic": ("clinic", None),
    "doctors": ("clinic", "doctors"),
    "school": ("school", None),
    "university": ("school", "university"),
    "college": ("school", "college"),
    "kindergarten": ("school", "kindergarten"),
}
SHOP_TAGS = {
    "supermarket": ("supermarket", None),
    "convenience": ("market", "convenience"),
}
AMENITY_TAGS["marketplace"] = ("market", None)


class AmenityHandler(osmium.SimpleHandler):
    """Đọc điểm và khu vực OSM có tag tiện ích nằm trong bbox Hà Nội."""

    def __init__(self):
        super().__init__()
        self.rows = []  # (osm_type, osm_id, category, subcategory, name, address, WKB hex)
        self.wkb_factory = osmium.geom.WKBFactory()

    @staticmethod
    def classify(tags):
        if tags.get("amenity") in AMENITY_TAGS:
            return AMENITY_TAGS[tags["amenity"]]
        if tags.get("shop") in SHOP_TAGS:
            return SHOP_TAGS[tags["shop"]]
        return None

    @staticmethod
    def address_from(tags):
        """Ghép các tag địa chỉ OSM thường gặp thành một dòng dễ đọc."""
        full_address = tags.get("addr:full") or tags.get("addr:street_address")
        if full_address:
            return full_address

        parts = [
            tags.get("addr:housenumber"),
            tags.get("addr:street") or tags.get("addr:place"),
            tags.get("addr:quarter") or tags.get("addr:suburb"),
            tags.get("addr:district"),
            tags.get("addr:city") or tags.get("addr:province"),
        ]
        address = ", ".join(dict.fromkeys(part.strip() for part in parts if part and part.strip()))
        return address or None

    def node(self, n):
        tags = n.tags
        classified = self.classify(tags)
        if classified is None or not n.location.valid():
            return
        category, subcategory = classified

        # Node trong bbox Hà Nội (loose check — đủ cho import)
        if not (HANOI_MIN_LAT <= n.location.lat <= HANOI_MAX_LAT
                and HANOI_MIN_LNG <= n.location.lon <= HANOI_MAX_LNG):
            return

        name = tags.get("name") or tags.get("name:vi")
        self.rows.append((
            "node",
            n.id,
            category,
            subcategory,
            name,
            self.address_from(tags),
            self.wkb_factory.create_point(n),
        ))

    def area(self, area):
        """Nhập closed way và multipolygon relation thành điểm đại diện."""
        classified = self.classify(area.tags)
        if classified is None:
            return
        category, subcategory = classified
        osm_type = "way" if area.from_way() else "relation"
        self.rows.append((
            osm_type,
            area.orig_id(),
            category,
            subcategory,
            area.tags.get("name") or area.tags.get("name:vi"),
            self.address_from(area.tags),
            self.wkb_factory.create_multipolygon(area),
        ))


def flush(conn, rows):
    """Upsert batch theo loại và ID đối tượng OSM."""
    if not rows:
        return 0
    sql = """
        WITH incoming AS (
            SELECT osm_type, osm_id, category, subcategory, name, address,
                   ST_PointOnSurface(ST_GeomFromWKB(decode(wkb_hex, 'hex'), 4326)) AS geom,
                   min_lat, max_lat, min_lng, max_lng
            FROM (VALUES %s) AS batch(
                osm_type, osm_id, category, subcategory, name, address, wkb_hex,
                min_lat, max_lat, min_lng, max_lng
            )
        )
        INSERT INTO amenity (osm_type, osm_id, category, subcategory, name, address, geom)
        SELECT osm_type, osm_id, category, subcategory, name, address, geom::geography
        FROM incoming
        WHERE ST_Y(geom) BETWEEN min_lat AND max_lat
          AND ST_X(geom) BETWEEN min_lng AND max_lng
        ON CONFLICT (osm_type, osm_id) DO UPDATE SET
            category = EXCLUDED.category,
            subcategory = EXCLUDED.subcategory,
            name = EXCLUDED.name,
            address = EXCLUDED.address,
            geom = EXCLUDED.geom
    """
    params = [row + (HANOI_MIN_LAT, HANOI_MAX_LAT, HANOI_MIN_LNG, HANOI_MAX_LNG) for row in rows]
    with conn.cursor() as cur:
        execute_values(
            cur,
            sql,
            params,
            template="(%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)",
            page_size=BATCH_SIZE,
        )
    conn.commit()
    return len(rows)


def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("--pbf", required=True, help="Đường dẫn file .osm.pbf (Việt Nam)")
    args = ap.parse_args()

    pbf = Path(args.pbf)
    if not pbf.exists():
        print(f"ERROR: Không tìm thấy file PBF: {pbf}")
        sys.exit(1)

    handler = AmenityHandler()
    print(f"Đang đọc {pbf.name} và lọc tiện ích trong bbox Hà Nội...")
    # Locations cho way và area từ closed ways/multipolygon relations.
    handler.apply_file(str(pbf), locations=True, idx="flex_mem")

    total = len(handler.rows)
    print(f"Đã tìm thấy {total} tiện ích hợp lệ trong Hà Nội.")
    if total == 0:
        print("Không có dữ liệu để nạp. Dừng lại.")
        return

    host = os.environ.get("DB_HOST", "localhost")
    port = os.environ.get("DB_PORT", "5432")
    db = os.environ.get("DB_NAME", "ancu")
    user = os.environ.get("DB_USER", "ancu")
    pwd = os.environ.get("DB_PASSWORD", "changeme")

    print(f"Nối PostgreSQL {user}@{host}:{port}/{db} ...")
    conn = psycopg2.connect(host=host, port=port, dbname=db, user=user, password=pwd)

    rows = handler.rows
    # Chèn từng batch (flush tự commit)
    for i in range(0, len(rows), BATCH_SIZE):
        batch = rows[i:i + BATCH_SIZE]
        flush(conn, batch)
        print(f"  Đã nạp {min(i + BATCH_SIZE, len(rows))}/{len(rows)}")

    conn.close()
    print("Hoàn tất. Kiểm tra:")
    print("  SELECT category, COUNT(*) FROM amenity GROUP BY category ORDER BY 1 DESC;")


if __name__ == "__main__":
    main()

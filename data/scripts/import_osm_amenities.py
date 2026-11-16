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

Idempotent: dùng ON CONFLICT (osm_id) DO NOTHING nhờ unique index từ V2 migration,
nên chạy lại nhiều lần không nhân bản dữ liệu.

Bounding box Hà Nội (ước lượng thô, kiểm tra lại khi dùng thật):
    lat 20.85–21.40, lng 105.28–106.05
"""

import argparse
import os
import sys
from pathlib import Path

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
    "marketplace": ("market", None),
}


class AmenityHandler(osmium.SimpleHandler):
    """Đọc node OSM, giữ lại những node có tag tiện ích nằm trong bbox Hà Nội."""

    def __init__(self):
        super().__init__()
        self.rows = []  # (osm_id, category, subcategory, name, lat, lng)

    def node(self, n):
        tags = n.tags
        category = None
        subcategory = None
        if tags.get("amenity") in AMENITY_TAGS:
            category, subcategory = AMENITY_TAGS[tags["amenity"]]
        elif tags.get("shop") in SHOP_TAGS:
            category, subcategory = SHOP_TAGS[tags["shop"]]

        if category is None:
            return

        # Node trong bbox Hà Nội (loose check — đủ cho import)
        if not (HANOI_MIN_LAT <= n.location.lat <= HANOI_MAX_LAT
                and HANOI_MIN_LNG <= n.location.lon <= HANOI_MAX_LNG):
            return

        name = tags.get("name") or tags.get("name:vi")
        self.rows.append((
            n.id,
            category,
            subcategory,
            name,
            round(n.location.lat, 6),
            round(n.location.lon, 6),
        ))


def flush(conn, rows, table="amenity"):
    """Chèn batch vào bảng amenity, idempotent theo osm_id (V2 unique index)."""
    if not rows:
        return 0
    sql = f"""
        INSERT INTO {table} (osm_id, category, subcategory, name, geom)
        VALUES %s
        ON CONFLICT (osm_id) DO NOTHING
    """
    params = [
        (oid, cat, sub, name,
         f"SRID=4326;POINT({lng} {lat})")  # PostGIS point WKT (lng lat)
        for (oid, cat, sub, name, lat, lng) in rows
    ]
    execute_values(conn, sql, params, page_size=BATCH_SIZE)
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
    # pyosmium: io.Reader đọc PBF/OSM (tự nhận format), chỉ cần NODE (tiện ích là point).
    # osmium.apply(reader, handler) gọi handler.node() cho từng node.
    reader = osmium.io.Reader(str(pbf), types=osmium.osm.NODE)
    osmium.apply(reader, handler)
    reader.close()

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

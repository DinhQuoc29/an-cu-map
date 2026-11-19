#!/usr/bin/env python3
"""
seed_flood.py — Nạp điểm ngập lịch sử vào bảng flood_point.

Yêu cầu:
    pip install psycopg2-binary python-dotenv

Chạy:
    # Từ thư mục gốc dự án (an-cu-map/)
    python data/scripts/seed_flood.py

    # Hoặc chỉ định file CSV khác:
    python data/scripts/seed_flood.py --csv data/flood_history/hanoi_flood_2024.csv

Idempotent: dùng ON CONFLICT DO NOTHING (kiểm tra theo lat+lng+event_date).
"""

import csv
import os
import sys
import argparse
from pathlib import Path

try:
    import psycopg2
    from psycopg2.extras import execute_values
except ImportError:
    print("ERROR: psycopg2 chưa được cài. Chạy: pip install psycopg2-binary")
    sys.exit(1)

try:
    from dotenv import load_dotenv
    # Ưu tiên backend/.env (nơi Spring Boot cấu hình), sau đó đến root .env
    backend_env = Path(__file__).resolve().parents[2] / "backend" / ".env"
    root_env = Path(__file__).resolve().parents[2] / ".env"
    if backend_env.exists():
        load_dotenv(backend_env, override=False)
        print(f"Đã nạp .env từ: {backend_env}")
    elif root_env.exists():
        load_dotenv(root_env, override=False)
        print(f"Đã nạp .env từ: {root_env}")
    else:
        load_dotenv()
except ImportError:
    pass  # python-dotenv không bắt buộc nếu biến môi trường đã được set

# ─── Cấu hình từ biến môi trường ───────────────────────────────────────────────

DB_CONFIG = {
    "host":     os.getenv("DB_HOST", "localhost"),
    "port":     int(os.getenv("DB_PORT", "5432")),
    "dbname":   os.getenv("DB_NAME", "an_cu_map"),
    "user":     os.getenv("DB_USER", "postgres"),
    "password": os.getenv("DB_PASSWORD", ""),
}

INSERT_SQL = """
INSERT INTO flood_point (geom, severity, source_type, source_name, source_url, event_date, confidence, status, is_demo, reviewed)
VALUES %s
ON CONFLICT DO NOTHING
"""

# Template cho execute_values: (geography point, ...)
ROW_TEMPLATE = "(ST_MakePoint(%s, %s)::geography, %s, %s, %s, %s, %s, %s, 'ACTIVE', TRUE, FALSE)"


def load_csv(csv_path: Path) -> list[tuple]:
    rows = []
    with open(csv_path, newline="", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for i, row in enumerate(reader, 1):
            try:
                lat = float(row["lat"])
                lng = float(row["lng"])
                severity = int(row["severity"])
                source_type = row["source_type"].strip()
                source_name = row["source_name"].strip()
                source_url = row.get("source_url", "").strip() or None
                event_date = row.get("event_date", "").strip() or None
                confidence = float(row["confidence"]) if row.get("confidence") else None

                rows.append((lng, lat, severity, source_type, source_name, source_url, event_date, confidence))
            except (ValueError, KeyError) as e:
                print(f"  WARN: Bỏ qua dòng {i}: {e} — {row}")
    return rows


def seed(csv_path: Path):
    print(f"\n=== An Cư — Seed dữ liệu ngập lịch sử ===")
    print(f"File CSV: {csv_path}")
    print(f"Database: {DB_CONFIG['user']}@{DB_CONFIG['host']}:{DB_CONFIG['port']}/{DB_CONFIG['dbname']}\n")

    rows = load_csv(csv_path)
    if not rows:
        print("Không có dữ liệu hợp lệ để nạp.")
        return

    print(f"Đọc được {len(rows)} bản ghi hợp lệ.")

    conn = None
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        with conn.cursor() as cur:
            # Kiểm tra bảng tồn tại
            cur.execute("SELECT COUNT(*) FROM flood_point")
            existing = cur.fetchone()[0]
            print(f"Hiện tại bảng flood_point có {existing} bản ghi.")

            cur.execute("SELECT ST_X(geom::geometry), ST_Y(geom::geometry), event_date FROM flood_point WHERE is_demo")
            imported = {(lng, lat, str(event_date)) for lng, lat, event_date in cur.fetchall()}
            rows = [row for row in rows if (row[0], row[1], str(row[6])) not in imported]

            # Nạp dữ liệu
            if rows:
                execute_values(cur, INSERT_SQL, rows, template=ROW_TEMPLATE, page_size=100)
            conn.commit()

            cur.execute("SELECT COUNT(*) FROM flood_point")
            after = cur.fetchone()[0]

        print(f"\n✅ Hoàn thành!")
        print(f"   Thêm mới: {after - existing} bản ghi")
        print(f"   Tổng sau seed: {after} bản ghi")

    except psycopg2.OperationalError as e:
        print(f"\n❌ Không kết nối được database: {e}")
        print("   Hãy chắc chắn docker compose up -d db đang chạy.")
        sys.exit(1)
    finally:
        if conn:
            conn.close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Seed flood history data vào PostGIS")
    parser.add_argument(
        "--csv",
        default=str(Path(__file__).resolve().parents[2] / "data/flood_history/demo/hanoi_flood_2024.csv"),
        help="Đường dẫn tới file CSV dữ liệu ngập"
    )
    parser.add_argument('--demo', action='store_true', help='Explicitly acknowledge that these are fake demo records')
    args = parser.parse_args()
    if not args.demo:
        parser.error('This script only imports FAKE demo data. Pass --demo, or use import_flood_history.py for real sources.')
    if 'dev' not in os.getenv('SPRING_PROFILES_ACTIVE', 'dev').split(','):
        parser.error('Demo data may only be imported with the dev profile.')

    csv_file = Path(args.csv)
    if not csv_file.exists():
        print(f"❌ File không tồn tại: {csv_file}")
        sys.exit(1)

    seed(csv_file)


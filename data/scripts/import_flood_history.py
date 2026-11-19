#!/usr/bin/env python3
"""Validate sourced flood history; preview by default, write only with --apply."""
import argparse
import csv
import math
import os
from datetime import date
from pathlib import Path
from urllib.parse import urlparse


def validate_row(row):
    if row.get('reviewed', '').lower() != 'true':
        return None
    location = row['location_label'].strip()
    source = row['source_name'].strip()
    url = row['source_url'].strip()
    parsed = urlparse(url)
    if not location or not source or parsed.scheme not in ('http', 'https') or not parsed.netloc or parsed.path in ('', '/'):
        raise ValueError('Reviewed evidence requires a location, source and direct article URL')
    event_date = date.fromisoformat(row['event_date'])
    if event_date > date.today():
        raise ValueError('Historical evidence cannot be dated in the future')
    lat, lng = float(row['lat']), float(row['lng'])
    if not (math.isfinite(lat) and math.isfinite(lng) and 20.85 <= lat <= 21.40 and 105.28 <= lng <= 106.05):
        raise ValueError('Coordinates must be within the Hanoi MVP area')
    precision = int(row['location_precision_m'])
    if not 0 < precision <= 500 or not row.get('geocode_method', '').strip():
        raise ValueError('Reviewed location requires precision 1..500 m and geocoding provenance')
    depth = int(row['depth_cm']) if row.get('depth_cm') else None
    if depth is not None and depth < 0:
        raise ValueError('Depth cannot be negative')
    return lng, lat, location, event_date, source, url, depth, precision


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--csv', type=Path, default=Path(__file__).resolve().parents[1] / 'flood_history/historical_evidence.csv')
    parser.add_argument('--apply', action='store_true')
    args = parser.parse_args()
    with args.csv.open(encoding='utf-8', newline='') as source:
        candidates = list(csv.DictReader(source))
    # Validate the whole file before opening a transaction.
    rows = [result for row in candidates if (result := validate_row(row)) is not None]
    print(f'Reviewed: {len(rows)}; pending location review: {len(candidates) - len(rows)}')
    if not args.apply:
        print('Preview only. Pass --apply after reviewing the CSV to import.')
        return
    from dotenv import load_dotenv
    import psycopg2
    load_dotenv(Path(__file__).resolve().parents[2] / 'backend/.env')
    with psycopg2.connect(host=os.getenv('DB_HOST', 'localhost'), port=os.getenv('DB_PORT', '5432'),
                          dbname=os.environ['DB_NAME'], user=os.environ['DB_USER'], password=os.environ['DB_PASSWORD']) as conn:
        with conn.cursor() as cursor:
            for row in rows:
                cursor.execute('''
                    INSERT INTO flood_point (geom, location_label, event_date, source_name, source_url,
                        depth_cm, location_precision_m, source_type, status, is_demo, reviewed)
                    VALUES (ST_MakePoint(%s,%s)::geography,%s,%s,%s,%s,%s,%s,'HISTORICAL','ACTIVE',FALSE,TRUE)
                    ON CONFLICT (source_url, event_date, location_label) DO UPDATE SET
                        geom=EXCLUDED.geom, depth_cm=EXCLUDED.depth_cm,
                        location_precision_m=EXCLUDED.location_precision_m,
                        source_name=EXCLUDED.source_name, reviewed=TRUE
                ''', row)
    print(f'Imported {len(rows)} sourced records (repeat imports update the same records).')


if __name__ == '__main__':
    main()

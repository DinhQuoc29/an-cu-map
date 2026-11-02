# An Cư Map — Ghi chú dữ liệu Valhalla

Thư mục này chứa cấu hình và dữ liệu cho Valhalla routing engine.

## Cấu trúc

```
infra/valhalla/
├── data/            ← Đặt file .osm.pbf ở đây (KHÔNG commit file lớn)
│   └── file_hashes.txt
└── README.md
```

## Hướng dẫn khởi động lần đầu

### 1. Tải dữ liệu OSM Việt Nam

```bash
wget https://download.geofabrik.de/asia/vietnam-latest.osm.pbf \
  -P infra/valhalla/data
```

### 2. Cắt bounding box Hà Nội (~tọa độ ước lượng, cần kiểm tra lại)

```bash
# Cần cài osmium-tool
osmium extract \
  -b 105.28,20.85,106.05,21.40 \
  infra/valhalla/data/vietnam-latest.osm.pbf \
  -o infra/valhalla/data/hanoi.osm.pbf
```

> **Lưu ý:** Bounding box `105.28,20.85,106.05,21.40` là ước lượng thô.
> Kiểm tra và điều chỉnh trước khi dùng thật.

### 3. Build tiles Valhalla

```bash
# Khởi động container với PBF, để Valhalla tự build tiles
docker compose run --rm valhalla \
  valhalla_build_tiles -c /custom_files/valhalla.json \
  /custom_files/data/hanoi.osm.pbf
```

### 4. Chạy dịch vụ

```bash
docker compose up -d valhalla
# Kiểm tra: curl http://localhost:8002/status
```

## Profiles được dùng

- `motor_scooter` — xe máy (phổ biến nhất tại Hà Nội)
- `auto` — ô tô
- `bicycle` — xe đạp
- `pedestrian` — đi bộ

> ⚠️ **Thời gian di chuyển từ Valhalla là thời gian lý tưởng, chưa tính kẹt xe.**
> Luôn ghi chú điều này trong UI và API response.


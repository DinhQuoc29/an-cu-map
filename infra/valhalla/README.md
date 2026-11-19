# An Cư Map — Ghi chú dữ liệu Valhalla

Thư mục này chứa cấu hình và dữ liệu cho Valhalla routing engine.

## Cấu trúc

```
infra/valhalla/
├── valhalla.json    ← Cấu hình build và chạy Valhalla
├── data/            ← PBF gốc và PBF Hà Nội (không commit file lớn)
├── valhalla_tiles/  ← Graph tiles sinh tự động (không commit)
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
# Tạo thư mục đầu ra và build graph từ extract Hà Nội
mkdir -p infra/valhalla/valhalla_tiles
docker compose run --rm --no-deps \
  --entrypoint valhalla_build_tiles valhalla \
  -c /custom_files/valhalla.json -j 2 \
  /custom_files/data/hanoi.osm.pbf
```

`valhalla.json` đã được tạo sẵn cho mount `/custom_files`. Tùy chọn `-j 2`
giới hạn số luồng để build ổn định trên máy phát triển; có thể tăng nếu máy có
nhiều tài nguyên hơn.

### 4. Chạy dịch vụ

```bash
docker compose up -d valhalla
# Kiểm tra: curl http://localhost:8002/status
```

Nếu cần dựng lại graph, dừng service trước, chạy lại lệnh build ở trên, rồi khởi
động service. Không cần build lại khi chỉ restart container.

## Profiles được dùng

- `motor_scooter` — xe máy (phổ biến nhất tại Hà Nội)
- `auto` — ô tô
- `bicycle` — xe đạp
- `pedestrian` — đi bộ

> ⚠️ **Thời gian di chuyển từ Valhalla là thời gian lý tưởng, chưa tính kẹt xe.**
> Luôn ghi chú điều này trong UI và API response.


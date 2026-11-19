# An Cư Map

> **"Xem trước nơi ở tại Hà Nội"**

Web bản đồ giúp người thuê nhà / người mua nhà tại Hà Nội đánh giá nhanh một vị trí trước khi quyết định.

## Tính năng

Bấm vào một điểm trên bản đồ và nhận **báo cáo vị trí** gồm:

1. 🌊 **Nguy cơ ngập** (ước tính, có độ tin cậy)
2. 💨 **Chất lượng không khí** (trung bình dài hạn + nguồn dữ liệu)
3. 🛵 **Thời gian di chuyển** đến nơi làm/học (xe máy, ô tô, xe đạp, đi bộ)
4. 🏥 **Khoảng cách đến tiện ích**: bệnh viện, trường học, chợ, siêu thị

## Tech Stack

| Tầng | Công nghệ |
|---|---|
| Backend | Java 21, Spring Boot 3.x, PostGIS 3, Flyway |
| Frontend | React 18 + TypeScript + Vite, MapLibre GL JS |
| Routing | Valhalla (self-hosted, OSM Hà Nội) |
| CSDL | PostgreSQL 16 + PostGIS 3 |

## Bắt đầu nhanh

### Yêu cầu

- Docker & Docker Compose
- Java 21 (cho backend dev)
- Node.js 20+ (cho frontend dev)

### 1. Cấu hình môi trường

```bash
cp .env.example .env
# Chỉnh sửa .env theo môi trường local
```

### 2. Khởi động hạ tầng

```bash
docker compose up -d db
# Valhalla cần build tiles trước — xem infra/valhalla/README.md
```

Sau khi đã build tiles, mỗi lần làm việc cần mở Docker Desktop và chạy:

```powershell
docker compose up -d db valhalla
docker compose ps
curl.exe http://localhost:8002/status
```

Chỉ chạy backend sẽ không tự khởi động Valhalla. Nếu gặp `Connection refused`
ở cổng 8002, kiểm tra Docker và service `valhalla` trước.

Backend chạy từ thư mục `backend` đọc `backend/.env`; sao chép `.env.example`
vào đó và đặt thông tin database đang dùng. File `.env` ở gốc phục vụ Compose.

### 3. Chạy backend

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
# Backend: http://localhost:8080
```

### 4. Chạy frontend

```bash
cd frontend
npm install
npm run dev
# Frontend: http://localhost:5173
```

## Cấu trúc dự án

```
an-cu-map/
├── CLAUDE.md          ← Hướng dẫn cho AI coding assistant
├── docker-compose.yml
├── .env.example
├── infra/
│   ├── valhalla/      ← Config + data routing
│   └── osm2pgsql/     ← Script nạp tiện ích OSM
├── backend/           ← Spring Boot (Java 21)
├── frontend/          ← React + Vite + TypeScript
├── data/
│   ├── flood_history/ ← CSV điểm ngập lịch sử
│   └── scripts/
└── docs/
```

## Chạy bản đóng gói Docker

Sau khi có tiles Valhalla và file `.env` tại gốc:

```powershell
docker compose --profile app up -d --build
docker compose --profile app ps
curl.exe http://localhost:8088/api/health
```

Mở http://localhost:8088. Nginx phục vụ frontend và chuyển `/api` tới backend.
Compose đợi database và Valhalla khỏe trước khi chạy ứng dụng.
`WEB_PORT` đổi cổng web (mặc định 8088).

Database trong Compose dùng volume `pgdata`, có thể khác database PostgreSQL
cài trực tiếp trên Windows. Bản đóng gói không tự sao chép dữ liệu từ database
Windows; cần nhập OSM và dữ liệu ngập vào database mà ứng dụng kết nối.

Để dùng lại database Windows đang có dữ liệu, đặt trong `.env` ở gốc:

```dotenv
APP_DB_HOST=host.docker.internal
APP_DB_PORT=5432
```

Các biến `DB_NAME`, `DB_USER`, `DB_PASSWORD` phải trùng với thông tin kết nối
trong `backend/.env`. Đặt `APP_DB_HOST=db` để dùng database của Compose.
Nếu volume Compose đã tồn tại, thay đổi `POSTGRES_PASSWORD` trong môi trường
container không tự đổi mật khẩu đã lưu trong PostgreSQL.

## Dữ liệu và kiểm tra MVP

- Tìm kiếm địa chỉ/địa điểm: Photon, cấu hình `PHOTON_BASE_URL`.
- Ngập: dữ liệu giả nằm tại `data/flood_history/demo/`, bị loại khỏi báo cáo thực.
  Bộ lịch sử có nguồn và cách nhập: [data/flood_history/README.md](data/flood_history/README.md).
- Không khí: trung bình 365 ngày của CAMS Global (~45 km), ghi rõ kỳ dữ liệu và
  độ phủ. Không gán nhãn AQI hiện tại cho giá trị trung bình năm.
- Kiểm tra tự động và luồng sử dụng: [docs/mvp-validation.md](docs/mvp-validation.md).

## Phạm vi

**MVP: chỉ Hà Nội.** Không mở rộng sang tỉnh khác trước khi MVP ổn định.

## Nguồn dữ liệu & Attribution

- Bản đồ nền: © [OpenStreetMap contributors](https://www.openstreetmap.org/copyright)
- Tiện ích: OpenStreetMap (Geofabrik)
- Chất lượng không khí: Open-Meteo (CAMS model), OpenAQ
- Routing: Valhalla + OpenStreetMap
- Tìm kiếm địa điểm: Photon + OpenStreetMap

## Lưu ý quan trọng

- ⚠️ Thời gian di chuyển **chưa tính kẹt xe**
- ⚠️ Nguy cơ ngập là **ước tính**, không phải dự báo chính xác
- 🔒 Không lưu vị trí chính xác người dùng lâu dài


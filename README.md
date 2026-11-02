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

## Phạm vi

**MVP: chỉ Hà Nội.** Không mở rộng sang tỉnh khác trước khi MVP ổn định.

## Nguồn dữ liệu & Attribution

- Bản đồ nền: © [OpenStreetMap contributors](https://www.openstreetmap.org/copyright)
- Tiện ích: OpenStreetMap (Geofabrik)
- Chất lượng không khí: Open-Meteo (CAMS model), OpenAQ
- Routing: Valhalla + OpenStreetMap

## Lưu ý quan trọng

- ⚠️ Thời gian di chuyển **chưa tính kẹt xe**
- ⚠️ Nguy cơ ngập là **ước tính**, không phải dự báo chính xác
- 🔒 Không lưu vị trí chính xác người dùng lâu dài


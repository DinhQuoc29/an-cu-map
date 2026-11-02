# CLAUDE.md — An Cư

Hướng dẫn cho Claude Code khi làm việc trong repo này. Đọc kỹ trước khi viết code.

**Tên dự án:** An Cư (từ "an cư lạc nghiệp"). Tagline: "Xem trước nơi ở tại Hà Nội". Tên repo: `an-cu-map`. Package Java gốc: `vn.ancu`. Trong code, tài liệu và UI dùng tên "An Cư" (không viết "Hanoi Living Map").

## 1. Tổng quan dự án

Web bản đồ giúp **người thuê nhà / người mua nhà tại Hà Nội** đánh giá nhanh một vị trí trước khi quyết định.

Người dùng bấm vào một điểm trên bản đồ (hoặc tìm theo tên khu vực/địa chỉ) và nhận **báo cáo vị trí** gồm:

1. **Nguy cơ ngập** (ước tính, có độ tin cậy)
2. **Chất lượng không khí** (trung bình dài hạn + nguồn dữ liệu)
3. **Thời gian di chuyển** từ vị trí đó đến nơi làm/học do người dùng chọn (xe máy, ô tô, xe đạp, đi bộ)
4. **Khoảng cách đến tiện ích**: bệnh viện, trường học, chợ, siêu thị

Phạm vi MVP: **chỉ Hà Nội**. Không mở rộng sang tỉnh khác trước khi MVP ổn định.

## 2. Nguyên tắc cốt lõi (quan trọng nhất)

- **Trung thực về dữ liệu.** Mọi con số hiển thị phải đi kèm *nguồn* và *mức độ tin cậy*. Không khẳng định chắc chắn khi dữ liệu là ước tính (đặc biệt ngập và không khí).
- **Phân biệt rõ loại dữ liệu** trên UI và API: `OBSERVED` (đo thực tế), `MODELED` (mô hình), `HISTORICAL` (báo chí/lịch sử), `USER_REPORTED` (người dùng báo, chưa xác nhận), `VERIFIED`.
- **Thời gian di chuyển từ Valhalla là thời gian lý tưởng, chưa tính kẹt xe.** Luôn ghi chú điều này trong response và UI.
- **Tuân thủ điều khoản dữ liệu.** Không cache/lưu dữ liệu từ Google, Goong, IQAir... nếu điều khoản cấm. Luôn ghi attribution: `© OpenStreetMap contributors`.
- **Quyền riêng tư.** Không lưu vị trí chính xác của người dùng lâu dài. Tuân thủ Nghị định 13/2023/NĐ-CP về bảo vệ dữ liệu cá nhân. Không log tọa độ gắn với IP/định danh.
- **Không scrape Facebook/Zalo** hay nguồn cấm scraping trong điều khoản.
- Không hard-code API key, mật khẩu. Dùng biến môi trường.

## 3. Tech stack

| Tầng | Công nghệ |
|---|---|
| Backend | Java 21, Spring Boot 3.x, Spring Web, Spring Data JPA, **Hibernate Spatial**, Spring Validation, Spring Scheduling |
| CSDL | PostgreSQL 16 + **PostGIS 3**, Flyway (migration) |
| Frontend | **React 18 + TypeScript + Vite**; bản đồ dùng **MapLibre GL JS** qua `react-map-gl` (`react-map-gl/maplibre`); **TanStack Query** gọi API; **React Router**; CSS: Tailwind CSS (hoặc CSS Modules, chọn một và thống nhất) |
| Routing | **Valhalla** (tự host, Docker) với dữ liệu OSM Hà Nội; profile `motor_scooter`, `auto`, `bicycle`, `pedestrian` |
| HTTP client | Spring `WebClient` (có timeout, retry, circuit breaker bằng Resilience4j) |
| Cache | Caffeine (in-memory) ở MVP; Redis nếu cần |
| Build | Maven (hoặc Gradle, thống nhất một loại) |
| Test | JUnit 5, Mockito, **Testcontainers** (PostGIS) |
| Triển khai | Docker Compose; frontend build tĩnh và phục vụ qua **nginx** (reverse proxy `/api` → backend, cùng origin nên không cần CORS ở prod) |

## 4. Nguồn dữ liệu

| Lớp | Nguồn | Ghi chú |
|---|---|---|
| Tiện ích | OSM (Geofabrik `vietnam-latest.osm.pbf`, cắt Hà Nội) qua `osm2pgsql` | Thiếu chợ dân sinh/phòng khám nhỏ; cho phép người dùng đề xuất bổ sung |
| Đường đi, thời gian | Valhalla trên OSM Hà Nội | Không tính kẹt xe |
| Không khí (nền) | Open-Meteo Air Quality API (mô hình CAMS, ~10 km) | `MODELED` |
| Không khí (đo) | OpenAQ (cần API key) | `OBSERVED`, trạm thưa; luôn trả khoảng cách tới trạm gần nhất |
| Địa hình | Copernicus DEM 30 m | Tính độ cao tương đối, vùng trũng |
| Ngập lịch sử | Tổng hợp từ báo chí + cơ quan thoát nước, nhập thủ công/CSV, geocode | `HISTORICAL`, **bắt buộc ghi nguồn + ngày** |
| Ngập người dùng báo | Chính ứng dụng | `USER_REPORTED` → `VERIFIED` khi đủ xác nhận; tự hết hạn |
| Ranh giới hành chính | OSM `admin_level` hoặc data.gov.vn | Kiểm tra phiên bản đơn vị hành chính mới nhất |

Bổ sung tùy chọn (sau MVP): IQAir/PurpleAir, Goong/Google/TomTom Distance Matrix cho giờ cao điểm. Chỉ dùng khi đã đọc điều khoản.

## 5. Cấu trúc thư mục

```
an-cu-map/
├── CLAUDE.md
├── docker-compose.yml
├── .env.example
├── infra/
│   ├── valhalla/            # config + data (.osm.pbf, tiles) — không commit file lớn
│   └── osm2pgsql/           # style Lua nạp tiện ích
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/java/vn/ancu/
│       │   ├── config/              # WebClient, cache, CORS, Scheduling
│       │   ├── amenity/             # entity, repository, service, controller
│       │   ├── airquality/          # client Open-Meteo, OpenAQ; service nội suy
│       │   ├── flood/               # điểm ngập, báo cáo người dùng, risk score
│       │   ├── routing/             # client Valhalla, cache
│       │   ├── report/              # tổng hợp báo cáo vị trí (facade)
│       │   ├── geocode/             # tìm kiếm địa chỉ/khu vực
│       │   └── common/              # exception, DTO chung, util geo
│       ├── main/resources/
│       │   ├── application.yml
│       │   └── db/migration/        # Flyway: V1__init.sql, ...
│       └── test/java/...
├── frontend/
│   ├── package.json
│   ├── vite.config.ts               # proxy /api → http://localhost:8080 khi dev
│   ├── tsconfig.json
│   └── src/
│       ├── main.tsx
│       ├── App.tsx
│       ├── api/                     # client gọi backend, kiểu dữ liệu (types) khớp DTO
│       ├── components/
│       │   ├── map/                 # MapView, marker, lớp tiện ích, isochrone
│       │   ├── report/              # ReportPanel, FloodCard, AirQualityCard, CommuteCard, AmenityCard
│       │   └── common/              # SourceBadge, ConfidenceBadge, Loading, ErrorState
│       ├── hooks/                   # useReport, useAmenities, useGeocode...
│       ├── pages/                   # MapPage, About/DataSources
│       ├── lib/                     # util: định dạng khoảng cách/thời gian, geo
│       └── styles/
├── data/
│   ├── flood_history/               # CSV điểm ngập lịch sử (có cột source, source_url, date)
│   └── scripts/                     # script nạp/geocode dữ liệu
└── docs/
```

(Package gốc là `vn.ancu`; mọi class mới phải nằm trong package này.)

## 6. Mô hình dữ liệu chính (PostGIS)

Dùng `geography(Point, 4326)` cho điểm; luôn tạo chỉ mục **GiST**. Tọa độ API: `lat`, `lng` (WGS84).

- `amenity(id, osm_id, category, subcategory, name, geom)` — category: `hospital | clinic | school | market | supermarket`
- `air_station(id, source, external_id, name, geom)` và `air_measurement(station_id, measured_at, pm25, pm10, aqi, ...)`
- `flood_point(id, geom, severity, source_type, source_name, source_url, event_date, confidence, status, expires_at)`
- `flood_report(id, geom, severity, photo_key, created_at, confirm_count, status)` — không lưu định danh người báo; chống spam bằng rate limit
- `route_cache(origin_cell, dest_cell, mode, depart_bucket, duration_s, distance_m, created_at)` — cell lưới ~500 m

Mọi thay đổi schema **phải qua Flyway migration**, không sửa DB bằng tay.

## 7. API (dự kiến)

- `GET /api/report?lat=&lng=&destLat=&destLng=&mode=motor_scooter` → báo cáo tổng hợp
- `GET /api/amenities/nearby?lat=&lng=&limit=3`
- `GET /api/air-quality?lat=&lng=`
- `GET /api/flood/risk?lat=&lng=`
- `POST /api/flood/reports` (kèm validate vị trí nằm trong Hà Nội, rate limit)
- `GET /api/routing/route?...` và `/api/routing/isochrone?...`
- `GET /api/geocode?q=`

Response báo cáo luôn có cấu trúc: `value`, `unit`, `sourceType`, `sourceName`, `confidence`, `updatedAt`. Lỗi trả theo RFC 7807 (`ProblemDetail`).

## 8. Quy ước code

- Java: dùng `record` cho DTO; Constructor injection; không field injection; Lombok chỉ dùng nếu đã thống nhất.
- Phân tầng: Controller → Service → Repository/Client. Controller không chứa logic nghiệp vụ.
- Truy vấn không gian: dùng **native query** với `ST_DWithin`, `<->` (KNN), `ST_Distance` trên `geography`. Không tính khoảng cách bằng Java.
- Gọi dịch vụ ngoài: bắt buộc timeout, retry có giới hạn, fallback (trả "không có dữ liệu" thay vì lỗi 500 cho toàn báo cáo). Một nguồn lỗi **không được làm hỏng cả báo cáo**.
- Cấu hình qua `application.yml` + biến môi trường; có profile `dev`, `test`, `prod`.
- Log: không log tọa độ người dùng ở mức INFO; không log API key.
- Tên bảng/cột: `snake_case`. Tên class/biến: Java chuẩn.
- Comment và commit message: tiếng Việt hoặc tiếng Anh đều được, nhưng **nhất quán trong một PR**. Commit theo Conventional Commits (`feat:`, `fix:`, `docs:`...).
- Frontend:
  - Bắt buộc **TypeScript strict**; không dùng `any`. Kiểu dữ liệu API đặt trong `src/api/types.ts`, khớp DTO backend (cập nhật cả hai phía khi đổi API).
  - Function component + hooks; dữ liệu từ server quản lý bằng **TanStack Query** (không tự `useEffect` + `fetch` rải rác); state UI cục bộ dùng `useState`/`useReducer`, chỉ thêm thư viện state toàn cục (Zustand) khi thật cần.
  - Mỗi thẻ trong báo cáo (ngập, không khí, di chuyển, tiện ích) là một component riêng, luôn hiển thị `SourceBadge` (loại nguồn) và `ConfidenceBadge` (độ tin cậy), kèm trạng thái loading/lỗi/không có dữ liệu. Một thẻ lỗi không được làm sập cả báo cáo (dùng Error Boundary).
  - Bản đồ: tọa độ nội bộ là `{ lat, lng }`; chú ý MapLibre dùng thứ tự `[lng, lat]`. Luôn hiển thị attribution `© OpenStreetMap contributors`. Dùng tile style không cần key trả phí ở MVP (hoặc tự host vector tiles).
  - Không nhúng API key trả phí vào JS; mọi lời gọi dịch vụ có key đi qua backend.
  - Ngôn ngữ giao diện: **tiếng Việt** (tách chuỗi vào một file riêng để dễ sửa/thêm ngôn ngữ). Giao diện phải dùng tốt trên **mobile** (phần lớn người dùng mở bằng điện thoại): báo cáo dạng bottom sheet trên màn hình nhỏ.
  - Lint/format: ESLint + Prettier; test: Vitest + React Testing Library.

## 9. Lệnh thường dùng

```bash
# Khởi động hạ tầng (PostGIS, Valhalla)
docker compose up -d db valhalla

# Chạy backend
cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Test (có Testcontainers PostGIS, cần Docker)
cd backend && ./mvnw test

# Build backend
cd backend && ./mvnw clean package

# Frontend (React + Vite)
cd frontend && npm install
cd frontend && npm run dev        # http://localhost:5173, proxy /api → :8080
cd frontend && npm run lint
cd frontend && npm run test
cd frontend && npm run build      # xuất ra frontend/dist

# Tải và cắt dữ liệu OSM Hà Nội (cần osmium-tool)
wget https://download.geofabrik.de/asia/vietnam-latest.osm.pbf -P infra/valhalla/data
osmium extract -b 105.28,20.85,106.05,21.40 infra/valhalla/data/vietnam-latest.osm.pbf \
  -o infra/valhalla/data/hanoi.osm.pbf

# Nạp tiện ích vào PostGIS
osm2pgsql -d $DB_NAME -U $DB_USER -H $DB_HOST --output=flex \
  --style=infra/osm2pgsql/amenity.lua infra/valhalla/data/hanoi.osm.pbf
```

(Bounding box trên là ước lượng thô quanh Hà Nội, cần kiểm tra và điều chỉnh khi làm thật.)

## 10. Biến môi trường (xem `.env.example`)

```
DB_HOST= DB_PORT=5432 DB_NAME= DB_USER= DB_PASSWORD=
VALHALLA_URL=http://localhost:8002
OPENAQ_API_KEY=
OPEN_METEO_BASE_URL=https://air-quality-api.open-meteo.com
```

## 11. Lộ trình triển khai

**Giai đoạn 0 — Nền tảng (tuần 1)**
- Khởi tạo Spring Boot, Docker Compose (PostGIS + Valhalla), Flyway, Testcontainers
- Nạp OSM Hà Nội vào `amenity`

**Giai đoạn 1 — MVP (tuần 2–5)**
1. Khởi tạo frontend React + Vite + TS, bản đồ MapLibre (`react-map-gl`) + click lấy tọa độ + tìm kiếm địa chỉ
2. Tiện ích gần nhất (KNN) + đếm trong bán kính
3. Thời gian di chuyển qua Valhalla (+ cache theo ô lưới, ghi chú "chưa tính kẹt xe")
4. Chất lượng không khí: Open-Meteo + OpenAQ, trung bình 12 tháng
5. Ngập: nhập 50–100 điểm lịch sử có nguồn, hiển thị mức nguy cơ ước tính
6. Endpoint `/api/report` tổng hợp + UI báo cáo

**Giai đoạn 2**
- Báo cáo ngập do người dùng + xác nhận chéo + tự hết hạn
- Lớp isochrone, so sánh nhiều vị trí
- Điểm tiện ích tổng hợp (kèm số liệu gốc)

**Giai đoạn 3**
- Mô hình nguy cơ ngập từ DEM + mưa + lịch sử
- Job cập nhật định kỳ, cảnh báo, API công khai

## 12. Những điều Claude KHÔNG được làm

- Không bịa dữ liệu mẫu rồi trình bày như dữ liệu thật; dữ liệu giả phải gắn nhãn `sample` và nằm trong profile `dev`.
- Không đổi stack hoặc thêm dependency lớn mà không hỏi (ví dụ: không tự chuyển sang Next.js, không thêm UI framework nặng).
- Không commit file `.osm.pbf`, tile, `.env`, API key.
- Không chạy migration phá hủy (`DROP`, `TRUNCATE`) trên DB không phải local.
- Không bỏ qua attribution/điều khoản của nguồn dữ liệu để "cho nhanh".
- Không trình bày kết quả ước tính (ngập, không khí, thời gian di chuyển) như sự thật tuyệt đối.

## 13. Khi bắt đầu một tác vụ

1. Đọc mục liên quan trong file này và `docs/`.
2. Nếu thay đổi schema: viết migration Flyway trước.
3. Viết/ cập nhật test (ưu tiên Testcontainers cho truy vấn PostGIS).
4. Chạy `./mvnw test` (backend) và `npm run lint && npm run test` (frontend) trước khi báo hoàn thành.
5. Cập nhật `CLAUDE.md` nếu quyết định kiến trúc thay đổi.

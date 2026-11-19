# Kiểm tra MVP

## Hạ tầng và chạy lại

Mở Docker Desktop, sau đó chạy từ gốc dự án:

```powershell
docker compose up -d db valhalla
docker compose ps
curl.exe http://localhost:8002/status
```

Khởi động backend và frontend theo README. `GET /api/health` trả trạng thái
database và routing. HTTP 503 nghĩa là một dịch vụ phụ thuộc chưa sẵn sàng.
Geocoder và Open-Meteo được kiểm tra bằng yêu cầu thực tế riêng vì phụ thuộc mạng ngoài.

## Luồng cần kiểm tra trên trình duyệt

| Bước | Kết quả mong đợi |
|---|---|
| Gõ tên đường hoặc địa điểm tối thiểu 3 ký tự | Có gợi ý sau khoảng 800 ms ngừng gõ |
| Chọn gợi ý | Bản đồ di chuyển, mở báo cáo vị trí |
| Chọn điểm đến trong thẻ di chuyển | Thời gian và khoảng cách hiện ra |
| Chuyển lần lượt 4 phương tiện | Kết quả phù hợp phương tiện đang chọn |
| Sửa chữ trong điểm đến đã chọn | Kết quả cũ bị xóa, cần chọn lại một gợi ý |
| Dịch vụ routing lỗi | Thông báo chưa tính được tuyến, có nút thử tính lại |
| Dịch vụ được phục hồi, nhấn thử lại | Tính lại ngay, không phải đợi cache frontend hết hạn |
| Kiểm tra ngập ở nơi chưa có dữ liệu | Chưa đủ dữ liệu, không hiển thị mức nguy cơ thấp |
| Kiểm tra gần giao Tạ Hiện – Lương Ngọc Quyến | Ghi nhận ngày 2024-08-13, nguồn cụ thể, vị trí ước lượng |
| Kiểm tra không khí | Trung bình 365 ngày, thời gian bao phủ, nguồn CAMS Global khoảng 45 km |
| Mạng ngoài bị lỗi | Thẻ tương ứng báo thiếu dữ liệu, các thẻ khác vẫn hoạt động |
| Màn hình điện thoại 390 × 844 | Ô địa chỉ và báo cáo không tràn chiều ngang |

## Kiểm tra tự động

```powershell
cd backend
mvn test
cd ../frontend
npm run lint
npm run test -- --run
npm run build
```

Kiểm tra quy tắc nhập lịch sử ngập từ gốc dự án:

```powershell
python -m unittest discover -s data/scripts -p "test_*.py"
```

Lượt kiểm tra ngày 17/11/2026 đã chạy luồng điện thoại 390 × 844 trên Chrome
headless với API thực, gồm tìm địa chỉ, chọn điểm đến Phố Tràng Thi, đổi bốn
phương tiện, giả lập một phản hồi lỗi tuyến và nhấn thử lại thành công. Không phát
hiện lỗi JavaScript hoặc tràn chiều ngang trong luồng này.

API không khí trả 8.760/8.760 giờ cho kỳ 2025-11-17 đến 2026-11-16.
Database thực giữ 60 bản ghi demo bị loại và 5 ghi nhận lịch sử đã duyệt.
Nhập lại CSV không tăng số ghi nhận đã duyệt.

Testcontainers 1.21.4 dùng để tương thích Docker mới. Kiểm tra tích hợp dùng
database tạm, không sửa database phát triển.

## Giới hạn còn lại

- 20 ghi nhận lịch sử có nguồn: 5 giao lộ đã đối chiếu tọa độ OSM; 15 ghi nhận
  chỉ nêu đường/khu vực, chờ kiểm tra vị trí trước khi nhập.
- Không có mô hình xác suất ngập đã hiệu chỉnh. `riskScore=null` có chủ đích.
- Chưa tích hợp dữ liệu hiện trạng HSDC/iHanoi hoặc OpenAQ.
- Thời gian Valhalla chưa tính ùn tắc. Dữ liệu graph hiện là extract Hà Nội.
- Endpoint Photon công khai phục vụ giai đoạn phát triển; có thể đổi qua
  `PHOTON_BASE_URL` khi triển khai cho nhiều người dùng.

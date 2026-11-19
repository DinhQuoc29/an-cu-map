# Dữ liệu ngập lịch sử thực tế

`historical_evidence.csv` có 20 ghi nhận từ ba bài báo cụ thể. Năm giao lộ đã
đối chiếu tên đường và node giao nhau trong extract OSM Hà Nội; 15 mục còn lại
chờ kiểm tra vị trí, không được đưa vào báo cáo.

## Nhập dữ liệu

Khởi động backend mới để Flyway chạy migration V5 trước khi nhập.
Các script đọc `backend/.env`; biến môi trường thật được ưu tiên.

```powershell
pip install psycopg2-binary python-dotenv
python data/scripts/import_flood_history.py
python data/scripts/import_flood_history.py --apply
```

Lệnh đầu chỉ kiểm tra file. Lệnh `--apply` chỉ nhập dòng `reviewed=true`.
Nhập lại cập nhật cùng bản ghi theo URL nguồn, ngày sự kiện và nhãn vị trí.
Mọi dòng được kiểm tra trước khi mở giao dịch; nếu có dòng đã duyệt không hợp lệ,
không nhập một phần file.

## Ý nghĩa dữ liệu

- `location_label`: địa điểm nguồn thực sự nêu, không suy rộng toàn bộ tuyến đường.
- `event_date`: ngày xảy ra sự kiện, không phải ngày thu thập.
- `source_url`: URL bài viết cụ thể, không phải trang chủ tờ báo.
- `depth_cm`: chỉ điền khi nguồn gắn con số với đúng vị trí; không suy từ số liệu chung cả thành phố.
- `lat`, `lng`: điểm đại diện đã kiểm tra, theo WGS84.
- `location_precision_m`: phạm vi vị trí tham khảo do người nhập ước lượng,
  không phải sai số đo đạc được bảo đảm. Các giao lộ ban đầu dùng khoảng 100 m.
- `geocode_method`: cách xác định vị trí, ID node OSM và ngày đối chiếu.
- `reviewed`: chỉ true sau khi đối chiếu nguồn và vị trí.

Không điền mức severity 1–5 khi nguồn chỉ xác nhận có ngập. Các ghi nhận lịch sử
không tự tạo ra xác suất ngập, hiện trạng ngập hay kết luận an toàn tại một căn nhà.
Nhiều bài cùng ghi nhận một sự kiện tại một địa điểm cần được gộp khi biên tập.

## Nguồn đã đọc

1. [Phố cổ Hà Nội ngập sau trận mưa lớn, 13/8/2024](https://vnexpress.net/pho-co-ha-noi-ngap-sau-tran-mua-lon-4781156.html).
2. [Hà Nội mưa giông 20 phút, một số tuyến phố ngập, 11/10/2025](https://vnexpress.net/ha-noi-mua-giong-20-phut-mot-so-tuyen-pho-ngap-4950072.html).
3. [Hà Nội mưa lớn, ngập nhiều tuyến phố, 5/6/2024](https://vnexpress.net/ha-noi-mua-lon-ngap-nhieu-tuyen-pho-4754549.html).

CSV chỉ lưu dữ kiện địa điểm/ngày và dẫn nguồn, không sao chép toàn văn hay ảnh báo.
Các bản tin lịch sử giữ tên địa giới hành chính tại thời điểm sự kiện.
Dữ liệu demo cũ được giữ riêng trong [demo/](demo/README.md).

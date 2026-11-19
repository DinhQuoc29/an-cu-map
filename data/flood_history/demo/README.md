# Dữ liệu demo

`hanoi_flood_2024.csv` chứa 60 bản ghi giả, được chủ dự án xác nhận chỉ dùng demo.
Tên báo, ngày và tọa độ trong file không phải bằng chứng về một sự kiện thực tế.

Migration V5 giữ lại dữ liệu cũ và đánh dấu `is_demo=true`, `reviewed=false`.
API báo cáo luôn loại chúng, kể cả khi ứng dụng chạy profile `dev`.
Muốn nạp lại vào database phát triển: `python data/scripts/seed_flood.py --demo`.

# Dữ liệu ngập lịch sử

File CSV trong thư mục này chứa các điểm ngập lịch sử được nhập thủ công.

## Định dạng bắt buộc

```csv
lat,lng,severity,source_name,source_url,event_date,notes
```

| Cột | Mô tả | Bắt buộc |
|---|---|---|
| `lat` | Vĩ độ WGS84 | ✅ |
| `lng` | Kinh độ WGS84 | ✅ |
| `severity` | Mức độ ngập 1–5 (1=nhẹ, 5=nặng) | ✅ |
| `source_name` | Tên nguồn (ví dụ: "Báo Hà Nội Mới 15/8/2023") | ✅ |
| `source_url` | Link bài báo / tài liệu gốc | ✅ |
| `event_date` | Ngày xảy ra ngập (YYYY-MM-DD) | ✅ |
| `notes` | Ghi chú thêm | ❌ |

> ⚠️ **Mọi điểm ngập PHẢI có nguồn và ngày cụ thể.**
> Không nhập điểm không có nguồn rõ ràng (theo nguyên tắc CLAUDE.md).

## Quy trình nhập dữ liệu

```bash
# Chạy script nhập từ thư mục data/scripts/
cd data/scripts
python import_flood_history.py --csv ../flood_history/hanoi_floods.csv
```

## Nguồn dữ liệu được phép dùng

- Báo chí: Hà Nội Mới, VnExpress, Tuổi Trẻ, Dân Trí (có ghi attribution)
- Cơ quan nhà nước: Công ty TNHH MTV Thoát nước Hà Nội
- OSM: không có dữ liệu ngập lịch sử tích hợp sẵn


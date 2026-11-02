package vn.ancu.common;

import java.time.Instant;

/**
 * DTO chung cho một trường dữ liệu có kèm nguồn và độ tin cậy.
 * Dùng record (Java 21) — immutable, không cần boilerplate.
 *
 * @param value      Giá trị (số, chuỗi, ...)
 * @param unit       Đơn vị (ví dụ: "µg/m³", "phút", "km")
 * @param sourceType Loại nguồn dữ liệu
 * @param sourceName Tên nguồn cụ thể (ví dụ: "Open-Meteo CAMS", "Báo Hà Nội Mới")
 * @param confidence Độ tin cậy 0.0–1.0 (null = không xác định)
 * @param updatedAt  Thời điểm cập nhật dữ liệu
 */
public record DataField<T>(
        T value,
        String unit,
        SourceType sourceType,
        String sourceName,
        Double confidence,
        Instant updatedAt
) {}


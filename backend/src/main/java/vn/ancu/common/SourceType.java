package vn.ancu.common;

/**
 * Loại nguồn dữ liệu — dùng thống nhất trong toàn bộ hệ thống.
 * Mọi response API phải chỉ rõ sourceType.
 */
public enum SourceType {
    /** Đo thực tế từ thiết bị/trạm đo */
    OBSERVED,
    /** Kết quả từ mô hình tính toán (CAMS, DEM, ...) */
    MODELED,
    /** Ghi nhận từ báo chí, tài liệu lịch sử */
    HISTORICAL,
    /** Do người dùng báo cáo, chưa xác nhận */
    USER_REPORTED,
    /** Đã xác nhận qua cross-check */
    VERIFIED
}


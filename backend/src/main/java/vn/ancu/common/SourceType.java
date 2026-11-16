package vn.ancu.common;

public enum SourceType {
    OBSERVED,
    MODELED,
    /** Ghi nhận từ báo chí, tài liệu lịch sử */
    HISTORICAL,
    /** Do người dùng báo cáo, chưa xác nhận */
    USER_REPORTED,
    /** Đã xác nhận qua cross-check */
    VERIFIED
}


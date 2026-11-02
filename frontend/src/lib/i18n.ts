/**
 * Chuỗi giao diện tiếng Việt — tập trung tại đây để dễ sửa / thêm ngôn ngữ.
 * KHÔNG dùng chuỗi cứng trong component.
 */
export const vi = {
  app: {
    name: 'An Cư',
    tagline: 'Xem trước nơi ở tại Hà Nội',
  },
  map: {
    attribution: '© OpenStreetMap contributors',
    clickHint: 'Nhấn vào bản đồ để xem báo cáo vị trí',
    searching: 'Đang tìm kiếm…',
    searchPlaceholder: 'Tìm địa chỉ tại Hà Nội…',
    noResults: 'Không tìm thấy kết quả',
    errorSearch: 'Lỗi tìm kiếm, thử lại sau',
  },
  report: {
    title: 'Báo cáo vị trí',
    loading: 'Đang tải dữ liệu…',
    noData: 'Không có dữ liệu',
    close: 'Đóng',
    coords: (lat: number, lng: number) =>
      `${lat.toFixed(5)}, ${lng.toFixed(5)}`,
  },
  flood: {
    title: 'Nguy cơ ngập',
    disclaimer: 'Đây là ước tính dựa trên lịch sử, không phải dự báo chính xác.',
    levels: {
      1: 'Rất thấp',
      2: 'Thấp',
      3: 'Trung bình',
      4: 'Cao',
      5: 'Rất cao',
    },
  },
  airQuality: {
    title: 'Chất lượng không khí',
    unit: 'µg/m³',
    disclaimer: 'Trung bình 12 tháng từ mô hình CAMS (~10 km). Không phải đo thực tế tại điểm.',
    pm25Label: 'PM2.5',
  },
  commute: {
    title: 'Thời gian di chuyển',
    disclaimer: 'Thời gian lý tưởng, chưa tính kẹt xe.',
    modes: {
      motor_scooter: 'Xe máy',
      auto: 'Ô tô',
      bicycle: 'Xe đạp',
      pedestrian: 'Đi bộ',
    },
    noDestination: 'Chọn điểm đến để xem thời gian di chuyển',
    setDestination: 'Đặt điểm đến',
  },
  amenity: {
    title: 'Tiện ích gần nhất',
    categories: {
      hospital: 'Bệnh viện',
      clinic: 'Phòng khám',
      school: 'Trường học',
      market: 'Chợ',
      supermarket: 'Siêu thị',
    },
    distanceUnit: 'km',
    noData: 'Không tìm thấy tiện ích trong bán kính 3 km',
  },
  source: {
    OBSERVED: 'Đo thực tế',
    MODELED: 'Mô hình',
    HISTORICAL: 'Lịch sử',
    USER_REPORTED: 'Người dùng báo',
    VERIFIED: 'Đã xác nhận',
  },
  confidence: {
    label: 'Độ tin cậy',
    high: 'Cao',
    medium: 'Trung bình',
    low: 'Thấp',
  },
} as const

export type I18nKey = typeof vi


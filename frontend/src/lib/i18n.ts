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
    noResults: 'Không tìm thấy. Thử tên địa điểm hoặc địa chỉ khác.',
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
    insufficient: 'Chưa đủ dữ liệu đánh giá nguy cơ ngập.',
    historicalDisclaimer: 'Ghi nhận lịch sử không phản ánh tình trạng ngập hiện tại hoặc xác suất ngập tại nhà bạn.',
    noEvidence: 'Chưa có ghi nhận đã kiểm tra trong phạm vi này. Điều đó không có nghĩa khu vực không ngập.',
    evidenceCount: (count: number, radius: number) => `${count} ghi nhận ngập có nguồn trong bán kính ${radius} m`,
    sourceLink: 'Xem nguồn',
    approximate: 'Vị trí ước lượng',
    reportedDepth: 'Độ sâu nguồn ghi nhận',
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
    disclaimer: 'Trung bình 365 ngày từ mô hình CAMS Global (~45 km). Không phải số đo tại nhà hoặc AQI hiện tại.',
    annualMean: 'PM2.5 trung bình 365 ngày',
    coverage: (valid: number, expected: number) => `Độ phủ dữ liệu: ${(valid / expected * 100).toFixed(1)}%`,
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
    loading: 'Đang tính thời gian…',
    unavailable: 'Chưa tính được tuyến đường. Dịch vụ có thể đang gián đoạn hoặc vị trí nằm ngoài dữ liệu bản đồ.',
    retry: 'Thử tính lại',
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
    distance: 'Khoảng cách',
    addressUnavailable: 'Chưa có địa chỉ trong dữ liệu OSM',
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


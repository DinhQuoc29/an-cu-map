/**
 * Các hàm tiện ích địa lý / định dạng.
 * Quy ước nội bộ: tọa độ luôn là { lat, lng } (WGS84).
 * MapLibre dùng [lng, lat] — hàm toLngLat() để convert.
 */

export interface LatLng {
  lat: number
  lng: number
}

/** Convert sang format [lng, lat] mà MapLibre yêu cầu */
export function toLngLat(pos: LatLng): [number, number] {
  return [pos.lng, pos.lat]
}

/** Kiểm tra tọa độ nằm trong bounding box Hà Nội (ước lượng) */
export function isInsideHanoi(pos: LatLng): boolean {
  return (
    pos.lat >= 20.85 && pos.lat <= 21.40 &&
    pos.lng >= 105.28 && pos.lng <= 106.05
  )
}

/** Định dạng khoảng cách (m → km nếu >= 1000 m) */
export function formatDistance(meters: number): string {
  if (meters < 1000) return `${Math.round(meters)} m`
  return `${(meters / 1000).toFixed(1)} km`
}

/** Định dạng thời gian (giây → "X phút" hoặc "X giờ Y phút") */
export function formatDuration(seconds: number): string {
  const minutes = Math.round(seconds / 60)
  if (minutes < 60) return `${minutes} phút`
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return mins > 0 ? `${hours} giờ ${mins} phút` : `${hours} giờ`
}

/** Độ tin cậy 0-1 → nhãn text */
export function confidenceLabel(confidence: number): 'high' | 'medium' | 'low' {
  if (confidence >= 0.7) return 'high'
  if (confidence >= 0.4) return 'medium'
  return 'low'
}

/** Trung tâm Hà Nội — vị trí mặc định khi mở bản đồ */
export const HANOI_CENTER: LatLng = { lat: 21.0285, lng: 105.8542 }
export const HANOI_DEFAULT_ZOOM = 13


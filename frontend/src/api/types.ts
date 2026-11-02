/**
 * Kiểu dữ liệu API — khớp với DTO backend (vn.ancu).
 * Cập nhật file này mỗi khi đổi API backend.
 *
 * Tọa độ API: lat, lng (WGS84) — nhất quán với backend.
 */

// ─── Chung ──────────────────────────────────────────────────────────────────

export type SourceType =
  | 'OBSERVED'
  | 'MODELED'
  | 'HISTORICAL'
  | 'USER_REPORTED'
  | 'VERIFIED'

/** Một trường dữ liệu kèm nguồn và độ tin cậy — khớp DataField<T> Java */
export interface DataField<T> {
  value: T
  unit: string
  sourceType: SourceType
  sourceName: string
  confidence: number | null   // 0.0–1.0, null = không xác định
  updatedAt: string           // ISO 8601
}

// ─── Geocoding (Nominatim) ──────────────────────────────────────────────────

export interface GeocodeResult {
  placeId: string
  displayName: string
  lat: number
  lng: number
  boundingBox?: [number, number, number, number] // [minLat, maxLat, minLng, maxLng]
}

// ─── Amenity ────────────────────────────────────────────────────────────────

export type AmenityCategory =
  | 'hospital'
  | 'clinic'
  | 'school'
  | 'market'
  | 'supermarket'

export interface AmenityItem {
  id: number
  category: AmenityCategory
  subcategory: string | null
  name: string | null
  lat: number
  lng: number
  distanceM: number
}

export interface AmenitiesResponse {
  items: AmenityItem[]
  radiusM: number
}

// ─── Air Quality ─────────────────────────────────────────────────────────────

export interface AirQualityResponse {
  pm25: DataField<number>
  nearestStationKm: number | null   // khoảng cách trạm đo gần nhất (km)
}

// ─── Flood ───────────────────────────────────────────────────────────────────

export type FloodSeverity = 1 | 2 | 3 | 4 | 5

export interface FloodRiskResponse {
  riskScore: DataField<FloodSeverity>
  nearbyPointsCount: number
}

// ─── Routing ─────────────────────────────────────────────────────────────────

export type TravelMode = 'motor_scooter' | 'auto' | 'bicycle' | 'pedestrian'

export interface RouteResult {
  mode: TravelMode
  durationS: number
  distanceM: number
  /** Luôn true — Valhalla không tính kẹt xe */
  idealOnly: true
}

// ─── Report (tổng hợp) ───────────────────────────────────────────────────────

export interface LocationReport {
  lat: number
  lng: number
  amenities: AmenitiesResponse | null
  airQuality: AirQualityResponse | null
  floodRisk: FloodRiskResponse | null
  commute: RouteResult[] | null
}


import type { AmenityItem, AmenityCategory, AmenitiesResponse } from './types'

/**
 * Gọi GET /api/amenities/nearby?lat=&lng= (data từ OSM qua PostGIS).
 */
export async function fetchNearbyAmenities(
  lat: number,
  lng: number,
): Promise<AmenitiesResponse> {
  const params = new URLSearchParams({ lat: String(lat), lng: String(lng) })
  const res = await fetch(`/api/amenities/nearby?${params.toString()}`)
  if (!res.ok) {
    throw new Error(`Amenities API lỗi ${String(res.status)}`)
  }
  return (await res.json()) as AmenitiesResponse
}

/**
 * Gọi theo danh sách tiện ích + bán kính để vẽ layer trên bản đồ.
 * Hiện backend trả 3 gần nhất/mỗi loại; layer dùng cùng endpoint.
 */
export function filterByCategories(
  items: AmenityItem[],
  categories: AmenityCategory[],
): AmenityItem[] {
  const allowed = new Set(categories)
  return items.filter((i) => allowed.has(i.category))
}

import type { GeocodeResult } from './types'

/**
 * Tìm kiếm địa chỉ qua backend proxy /api/geocode → Nominatim.
 * Backend xử lý User-Agent, rate limit cache, nên frontend không cần lo.
 */
export async function geocodeSearch(query: string): Promise<GeocodeResult[]> {
  if (!query.trim()) return []

  const params = new URLSearchParams({ q: query })
  const res = await fetch(`/api/geocode?${params.toString()}`)

  if (!res.ok) throw new Error(`Geocode error: ${String(res.status)}`)

  const data = (await res.json()) as Array<{
    placeId: string
    displayName: string
    lat: number
    lng: number
    boundingBox?: number[]
  }>

  return data.map((item) => ({
    placeId: item.placeId,
    displayName: item.displayName,
    lat: item.lat,
    lng: item.lng,
    boundingBox: item.boundingBox as [number, number, number, number] | undefined,
  }))
}

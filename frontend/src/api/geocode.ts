import type { GeocodeResult } from './types'

const NOMINATIM_BASE = 'https://nominatim.openstreetmap.org'

/** Giới hạn kết quả tìm kiếm trong Hà Nội */
const HANOI_VIEWBOX = '105.28,20.85,106.05,21.40'

interface NominatimRawItem {
  place_id: number | string
  display_name: string
  lat: string
  lon: string
  boundingbox?: [string, string, string, string]
}

/**
 * Tìm kiếm địa chỉ / địa điểm qua Nominatim.
 * Rate limit: 1 req/s — TanStack Query sẽ debounce qua enabled flag.
 *
 * @see https://nominatim.org/release-docs/latest/api/Search/
 */
export async function geocodeSearch(query: string): Promise<GeocodeResult[]> {
  if (!query.trim()) return []

  const params = new URLSearchParams({
    q: query,
    format: 'jsonv2',
    limit: '5',
    viewbox: HANOI_VIEWBOX,
    bounded: '0',          // bounded=0: ưu tiên viewbox nhưng không giới hạn cứng
    countrycodes: 'vn',
    'accept-language': 'vi',
  })

  const res = await fetch(`${NOMINATIM_BASE}/search?${params.toString()}`, {
    headers: {
      // Nominatim yêu cầu User-Agent định danh ứng dụng
      'User-Agent': 'AnCuMap/0.1 (https://github.com/ancu-map; contact@ancu.vn)',
    },
  })

  if (!res.ok) throw new Error(`Nominatim error: ${String(res.status)}`)

  const data = (await res.json()) as NominatimRawItem[]

  return data.map((item) => ({
    placeId: String(item.place_id),
    displayName: item.display_name,
    lat: parseFloat(item.lat),
    lng: parseFloat(item.lon),
    boundingBox: item.boundingbox
      ? (item.boundingbox.map((v) => parseFloat(v)) as [number, number, number, number])
      : undefined,
  }))
}

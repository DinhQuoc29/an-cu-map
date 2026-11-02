import { useQuery } from '@tanstack/react-query'
import { geocodeSearch } from '@/api/geocode'
import type { GeocodeResult } from '@/api/types'

/**
 * Hook tìm kiếm địa chỉ qua Nominatim.
 * Chỉ fetch khi query >= 3 ký tự để tránh gọi quá nhiều.
 */
export function useGeocode(query: string) {
  return useQuery<GeocodeResult[]>({
    queryKey: ['geocode', query],
    queryFn: () => geocodeSearch(query),
    enabled: query.trim().length >= 3,
    staleTime: 60 * 1000,       // cache 1 phút
    placeholderData: [],
  })
}

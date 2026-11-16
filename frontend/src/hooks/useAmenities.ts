import { useQuery } from '@tanstack/react-query'
import { fetchNearbyAmenities } from '@/api/amenities'

/**
 * Hook lấy tiện ích gần nhất (tanstack query).
 * Chỉ fetch khi có vị trí (lat/lng không null).
 * Cache 10 phút — dữ liệu OSM gần như tĩnh.
 */
export function useAmenities(lat: number | null, lng: number | null) {
  return useQuery({
    queryKey: ['amenities', lat, lng],
    queryFn: () => fetchNearbyAmenities(lat as number, lng as number),
    enabled: lat !== null && lng !== null,
    staleTime: 10 * 60 * 1000,
    retry: 1,
  })
}

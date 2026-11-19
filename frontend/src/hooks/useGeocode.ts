import { useEffect, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { geocodeSearch } from '@/api/geocode'
import type { GeocodeResult } from '@/api/types'

/**
 * Hook tìm kiếm địa chỉ qua backend geocoder.
 * Chỉ fetch sau khi người dùng ngừng gõ 800 ms và query có ít nhất 3 ký tự.
 */
export function useGeocode(query: string) {
  const normalizedQuery = query.trim()
  const [debouncedQuery, setDebouncedQuery] = useState(normalizedQuery)

  useEffect(() => {
    const timeoutId = window.setTimeout(() => setDebouncedQuery(normalizedQuery), 800)
    return () => window.clearTimeout(timeoutId)
  }, [normalizedQuery])

  const geocodeQuery = useQuery<GeocodeResult[]>({
    queryKey: ['geocode', debouncedQuery],
    queryFn: ({ signal }) => geocodeSearch(debouncedQuery, signal),
    enabled: debouncedQuery.length >= 3,
    staleTime: 60 * 1000,       // cache 1 phút
  })

  return {
    ...geocodeQuery,
    data: geocodeQuery.data ?? [],
    isDebouncing: normalizedQuery.length >= 3 && normalizedQuery !== debouncedQuery,
  }
}

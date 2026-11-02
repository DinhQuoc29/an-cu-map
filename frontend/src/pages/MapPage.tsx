import { useState, useCallback } from 'react'
import MapView from '@/components/map/MapView'
import ReportSheet from '@/components/report/ReportSheet'
import SearchBar from '@/components/common/SearchBar'
import type { LatLng } from '@/lib/geo'
import { vi } from '@/lib/i18n'

/**
 * Trang chính — toàn màn hình bản đồ + thanh tìm kiếm + bottom sheet báo cáo.
 */
export default function MapPage() {
  const [selectedLocation, setSelectedLocation] = useState<LatLng | null>(null)
  const [mapCenter, setMapCenter] = useState<LatLng | null>(null)

  const handleLocationSelect = useCallback((pos: LatLng) => {
    setSelectedLocation(pos)
  }, [])

  const handleSearchSelect = useCallback((pos: LatLng) => {
    setSelectedLocation(pos)
    setMapCenter(pos)
  }, [])

  const handleCloseSheet = useCallback(() => {
    setSelectedLocation(null)
  }, [])

  return (
    <div className="relative w-screen h-screen overflow-hidden">
      {/* Bản đồ toàn màn hình */}
      <MapView
        onLocationSelect={handleLocationSelect}
        selectedLocation={selectedLocation}
        center={mapCenter}
      />

      {/* Thanh tìm kiếm nổi trên bản đồ */}
      <div className="absolute top-4 left-4 right-4 z-10 flex justify-center">
        <div className="w-full max-w-sm">
          <SearchBar onSelect={handleSearchSelect} />
        </div>
      </div>

      {/* Hint click khi chưa chọn vị trí */}
      {!selectedLocation && (
        <div className="absolute bottom-6 left-1/2 -translate-x-1/2 z-10 pointer-events-none">
          <div className="bg-white/90 backdrop-blur-sm rounded-full px-4 py-2 shadow-md">
            <p className="text-sm text-gray-600">{vi.map.clickHint}</p>
          </div>
        </div>
      )}

      {/* Bottom sheet báo cáo */}
      <ReportSheet
        location={selectedLocation}
        onClose={handleCloseSheet}
      />
    </div>
  )
}

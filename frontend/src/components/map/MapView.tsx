import { useState, useCallback, useRef, useEffect } from 'react'
import Map, { NavigationControl, AttributionControl } from 'react-map-gl/maplibre'
import type { MapRef, MapMouseEvent } from 'react-map-gl/maplibre'
import 'maplibre-gl/dist/maplibre-gl.css'
import type { LatLng } from '@/lib/geo'
import { HANOI_CENTER, HANOI_DEFAULT_ZOOM } from '@/lib/geo'
import { vi } from '@/lib/i18n'
import LocationMarker from './LocationMarker'

/**
 * Sử dụng CARTO Voyager style (dựa trên OpenStreetMap data)
 * - Vector tiles miễn phí, không cần key, CDN toàn cầu tốc độ cao.
 * - Hiển thị sắc nét sông hồ, đường sá, tòa nhà tại Hà Nội.
 * Attribution bắt buộc: © OpenStreetMap contributors © CARTO
 */
const MAP_STYLE = 'https://basemaps.cartocdn.com/gl/voyager-gl-style/style.json'

interface MapViewProps {
  onLocationSelect: (pos: LatLng) => void
  selectedLocation: LatLng | null
  center?: LatLng | null
}

export default function MapView({ onLocationSelect, selectedLocation, center }: MapViewProps) {
  const [cursor, setCursor] = useState<'crosshair' | 'grab'>('grab')
  const mapRef = useRef<MapRef | null>(null)

  useEffect(() => {
    if (center && mapRef.current) {
      mapRef.current.flyTo({
        center: [center.lng, center.lat],
        zoom: 15,
        essential: true,
      })
    }
  }, [center])

  const handleClick = useCallback(
    (e: MapMouseEvent) => {
      const { lat, lng } = e.lngLat
      onLocationSelect({ lat, lng })
    },
    [onLocationSelect],
  )

  return (
    <Map
      ref={mapRef}
      initialViewState={{
        longitude: HANOI_CENTER.lng,
        latitude: HANOI_CENTER.lat,
        zoom: HANOI_DEFAULT_ZOOM,
      }}
      style={{ width: '100%', height: '100%' }}
      mapStyle={MAP_STYLE}
      cursor={cursor}
      onClick={handleClick}
      onMouseEnter={() => setCursor('crosshair')}
      onMouseLeave={() => setCursor('grab')}
      attributionControl={false}
    >
      <NavigationControl position="bottom-right" />
      <AttributionControl
        compact
        customAttribution={vi.map.attribution}
        position="bottom-left"
      />

      {selectedLocation && (
        <LocationMarker position={selectedLocation} />
      )}
    </Map>
  )
}

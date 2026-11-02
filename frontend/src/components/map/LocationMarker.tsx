import { Marker } from 'react-map-gl/maplibre'
import type { LatLng } from '@/lib/geo'

interface LocationMarkerProps {
  position: LatLng
}

/**
 * Marker đánh dấu vị trí người dùng đã chọn trên bản đồ.
 * Dùng SVG tùy chỉnh thay vì ảnh để không cần asset.
 */
export default function LocationMarker({ position }: LocationMarkerProps) {
  return (
    <Marker
      longitude={position.lng}
      latitude={position.lat}
      anchor="bottom"
    >
      <svg
        width="32"
        height="40"
        viewBox="0 0 32 40"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        aria-label="Vị trí đã chọn"
      >
        {/* Pin body */}
        <path
          d="M16 0C9.373 0 4 5.373 4 12C4 21 16 40 16 40C16 40 28 21 28 12C28 5.373 22.627 0 16 0Z"
          fill="#2563eb"
          stroke="white"
          strokeWidth="2"
        />
        {/* Dot trong pin */}
        <circle cx="16" cy="12" r="5" fill="white" />
      </svg>
    </Marker>
  )
}


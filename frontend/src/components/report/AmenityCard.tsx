import { vi } from '@/lib/i18n'
import { formatDistance } from '@/lib/geo'
import type { AmenitiesResponse, AmenityCategory } from '@/api/types'
import LoadingSpinner from '@/components/common/LoadingSpinner'

interface AmenityCardProps {
  data: AmenitiesResponse | null
  isLoading: boolean
}

/** Icon cho từng loại tiện ích */
const CATEGORY_ICONS: Record<AmenityCategory, string> = {
  hospital:    '🏥',
  clinic:      '🩺',
  school:      '🏫',
  market:      '🛒',
  supermarket: '🏪',
}

export default function AmenityCard({ data, isLoading }: AmenityCardProps) {
  if (isLoading) {
    return (
      <div className="report-card">
        <h3 className="text-sm font-semibold text-gray-800 mb-1">{vi.amenity.title}</h3>
        <LoadingSpinner size="sm" label="Đang tải dữ liệu tiện ích…" />
      </div>
    )
  }

  if (!data || data.items.length === 0) {
    return (
      <div className="report-card">
        <h3 className="text-sm font-semibold text-gray-800 mb-1">{vi.amenity.title}</h3>
        <p className="text-sm text-gray-400">{vi.amenity.noData}</p>
      </div>
    )
  }

  return (
    <div className="report-card">
      <h3 className="text-sm font-semibold text-gray-800 mb-2">{vi.amenity.title}</h3>

      <ul className="space-y-1.5">
        {data.items.map((item) => (
          <li key={item.id} className="flex items-center gap-2 text-sm">
            <span className="text-base w-5 text-center flex-shrink-0">
              {CATEGORY_ICONS[item.category]}
            </span>
            <div className="flex-1 min-w-0">
              <span className="text-gray-700 truncate block">
                {item.name ?? vi.amenity.categories[item.category]}
              </span>
              <span className="text-xs text-gray-400">
                {vi.amenity.categories[item.category]}
              </span>
            </div>
            <span className="text-xs text-gray-500 flex-shrink-0 font-mono">
              {formatDistance(item.distanceM)}
            </span>
          </li>
        ))}
      </ul>

      <p className="text-xs text-gray-400 mt-2">
        Trong bán kính {(data.radiusM / 1000).toFixed(0)} km · © OpenStreetMap
      </p>
    </div>
  )
}

import { vi } from '@/lib/i18n'
import { formatDuration, formatDistance } from '@/lib/geo'
import type { RouteResult, TravelMode } from '@/api/types'
import type { LatLng } from '@/lib/geo'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import CommuteInput from './CommuteInput'

interface CommuteCardProps {
  data: RouteResult[] | null
  isLoading: boolean
  onDestChange: (dest: LatLng | null) => void
  onModeChange: (mode: TravelMode) => void
  currentMode: TravelMode
  hasDest: boolean
}

/** Icon phương tiện */
const MODE_ICONS: Record<TravelMode, string> = {
  motor_scooter: '🛵',
  auto:          '🚗',
  bicycle:       '🚲',
  pedestrian:    '🚶',
}

export default function CommuteCard({
  data,
  isLoading,
  onDestChange,
  onModeChange,
  currentMode,
  hasDest,
}: CommuteCardProps) {
  return (
    <div className="report-card">
      <h3 className="text-sm font-semibold text-gray-800 mb-2">{vi.commute.title}</h3>

      {/* Luôn hiện CommuteInput để có thể đổi điểm đến */}
      <CommuteInput
        onDestChange={onDestChange}
        onModeChange={onModeChange}
        currentMode={currentMode}
      />

      {/* Kết quả */}
      <div className="mt-2">
        {!hasDest && (
          <p className="text-sm text-gray-400">{vi.commute.noDestination}</p>
        )}

        {hasDest && isLoading && (
          <LoadingSpinner size="sm" label="Đang tính thời gian…" />
        )}

        {hasDest && !isLoading && (!data || data.length === 0) && (
          <p className="text-sm text-gray-400">Không tính được thời gian di chuyển</p>
        )}

        {hasDest && !isLoading && data && data.length > 0 && (
          <ul className="space-y-1 mt-2">
            {data.map((route) => (
              <li key={route.mode} className="flex items-center gap-2 text-sm">
                <span className="text-base w-5 text-center flex-shrink-0">
                  {MODE_ICONS[route.mode]}
                </span>
                <span className="text-gray-600 flex-1">{vi.commute.modes[route.mode]}</span>
                <span className="font-semibold text-gray-800">{formatDuration(route.durationS)}</span>
                <span className="text-xs text-gray-400">{formatDistance(route.distanceM)}</span>              </li>
            ))}
          </ul>
        )}
      </div>

      <p className="text-xs text-gray-400 mt-2 italic">{vi.commute.disclaimer}</p>
    </div>
  )
}

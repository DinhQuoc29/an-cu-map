import { vi } from '@/lib/i18n'
import { formatDuration, formatDistance } from '@/lib/geo'
import type { RouteResult, TravelMode } from '@/api/types'
import type { LatLng } from '@/lib/geo'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import CommuteInput from './CommuteInput'
import SourceBadge from '@/components/common/SourceBadge'
import ConfidenceBadge from '@/components/common/ConfidenceBadge'

interface CommuteCardProps {
  data: RouteResult[] | null
  isLoading: boolean
  onDestChange: (dest: LatLng | null) => void
  onModeChange: (mode: TravelMode) => void
  currentMode: TravelMode
  hasDest: boolean
  onRetry: () => void
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
  onRetry,
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
          <LoadingSpinner size="sm" label={vi.commute.loading} />
        )}

        {hasDest && !isLoading && (!data || data.length === 0) && (
          <div>
            <p className="text-sm text-gray-500" role="status">{vi.commute.unavailable}</p>
            <button type="button" onClick={onRetry} className="mt-2 rounded-lg bg-primary-50 px-3 py-2 text-sm text-primary-700">
              {vi.commute.retry}
            </button>
          </div>
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
      {hasDest && !isLoading && !!data?.length && (
        <div className="mt-2 flex flex-wrap gap-1 text-xs text-gray-500">
          <span>Valhalla / OpenStreetMap</span><SourceBadge sourceType="MODELED" /><ConfidenceBadge confidence={null} />
        </div>
      )}
    </div>
  )
}

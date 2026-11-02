import { vi } from '@/lib/i18n'
import LoadingSpinner from '@/components/common/LoadingSpinner'

/** Placeholder card chất lượng không khí */
export default function AirQualityCard() {
  return (
    <div className="report-card">
      <h3 className="text-sm font-semibold text-gray-800 mb-1">{vi.airQuality.title}</h3>
      <LoadingSpinner size="sm" label="Chờ dữ liệu không khí…" />
      <p className="text-xs text-gray-400 mt-2 italic">{vi.airQuality.disclaimer}</p>
    </div>
  )
}


import { vi } from '@/lib/i18n'
import type { AirQualityResponse } from '@/api/types'
import SourceBadge from '@/components/common/SourceBadge'
import ConfidenceBadge from '@/components/common/ConfidenceBadge'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import ErrorState from '@/components/common/ErrorState'

interface AirQualityCardProps {
  data: AirQualityResponse | null
  isLoading: boolean
}

/** Màu cho PM2.5 theo ngưỡng WHO */
function pm25Color(value: number): string {
  if (value <= 15) return 'text-green-600'
  if (value <= 35) return 'text-yellow-600'
  if (value <= 75) return 'text-orange-600'
  return 'text-red-600'
}

/** Nhãn chất lượng theo ngưỡng WHO 2021 */
function pm25Label(value: number): string {
  if (value <= 15) return 'Tốt'
  if (value <= 35) return 'Trung bình'
  if (value <= 75) return 'Kém'
  return 'Rất kém'
}

export default function AirQualityCard({ data, isLoading }: AirQualityCardProps) {
  if (isLoading) {
    return (
      <div className="report-card">
        <h3 className="text-sm font-semibold text-gray-800 mb-1">{vi.airQuality.title}</h3>
        <LoadingSpinner size="sm" label="Đang tải dữ liệu không khí…" />
      </div>
    )
  }

  if (!data) {
    return (
      <div className="report-card">
        <h3 className="text-sm font-semibold text-gray-800 mb-1">{vi.airQuality.title}</h3>
        <ErrorState message={vi.report.noData} />
      </div>
    )
  }

  const { pm25 } = data
  const colorClass = pm25Color(pm25.value)
  const label = pm25Label(pm25.value)

  return (
    <div className="report-card">
      <div className="flex items-center justify-between mb-1">
        <h3 className="text-sm font-semibold text-gray-800">{vi.airQuality.title}</h3>
        <div className="text-right">
          <span className={`text-xl font-bold ${colorClass}`}>
            {pm25.value.toFixed(1)}
          </span>
          <span className="text-xs text-gray-400 ml-1">{vi.airQuality.unit}</span>
        </div>
      </div>

      <p className={`text-sm font-medium mb-2 ${colorClass}`}>{label}</p>

      <div className="flex gap-1 flex-wrap">
        <SourceBadge sourceType={pm25.sourceType} />
        <ConfidenceBadge confidence={pm25.confidence} />
      </div>

      <p className="text-xs text-gray-400 mt-2 italic">{vi.airQuality.disclaimer}</p>
    </div>
  )
}

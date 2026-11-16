import { vi } from '@/lib/i18n'
import type { FloodRiskResponse, FloodSeverity } from '@/api/types'
import SourceBadge from '@/components/common/SourceBadge'
import ConfidenceBadge from '@/components/common/ConfidenceBadge'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import ErrorState from '@/components/common/ErrorState'

interface FloodCardProps {
  data: FloodRiskResponse | null
  isLoading: boolean
}

/** Màu cho từng mức nguy cơ 1–5 */
const RISK_COLORS: Record<FloodSeverity, string> = {
  1: 'bg-green-100 text-green-800',
  2: 'bg-lime-100 text-lime-800',
  3: 'bg-yellow-100 text-yellow-800',
  4: 'bg-orange-100 text-orange-800',
  5: 'bg-red-100 text-red-800',
}

/** Thanh trực quan mức nguy cơ (1–5 ô màu) */
function RiskMeter({ value }: { value: FloodSeverity }) {
  return (
    <div className="flex gap-1 my-2" aria-label={`Mức nguy cơ: ${value}/5`}>
      {([1, 2, 3, 4, 5] as FloodSeverity[]).map((level) => (
        <div
          key={level}
          className={`h-2 flex-1 rounded-full transition-colors ${level <= value ? RISK_COLORS[value] : 'bg-gray-200'
            }`}
        />
      ))}
    </div>
  )
}

export default function FloodCard({ data, isLoading }: FloodCardProps) {
  if (isLoading) {
    return (
      <div className="report-card">
        <h3 className="text-sm font-semibold text-gray-800 mb-1">{vi.flood.title}</h3>
        <LoadingSpinner size="sm" label="Đang tải dữ liệu ngập…" />
      </div>
    )
  }

  if (!data) {
    return (
      <div className="report-card">
        <h3 className="text-sm font-semibold text-gray-800 mb-1">{vi.flood.title}</h3>
        <ErrorState message={vi.report.noData} />
      </div>
    )
  }

  const { riskScore, nearbyPointsCount } = data
  const level = vi.flood.levels[riskScore.value]

  return (
    <div className="report-card">
      <div className="flex items-center justify-between mb-1">
        <h3 className="text-sm font-semibold text-gray-800">{vi.flood.title}</h3>
        <span className="text-lg font-bold text-gray-700">
          {riskScore.value}/5
        </span>
      </div>

      <RiskMeter value={riskScore.value} />

      <p className="text-sm font-medium text-gray-700 mb-2">{level}</p>

      {nearbyPointsCount > 0 && (
        <p className="text-xs text-gray-500 mb-2">
          {nearbyPointsCount} điểm ngập lịch sử trong bán kính 500 m
        </p>
      )}

      <div className="flex gap-1 flex-wrap">
        <SourceBadge sourceType={riskScore.sourceType} />
        <ConfidenceBadge confidence={riskScore.confidence} />
      </div>

      <p className="text-xs text-gray-400 mt-2 italic">{vi.flood.disclaimer}</p>
    </div>
  )
}

import { vi } from '@/lib/i18n'
import { confidenceLabel } from '@/lib/geo'

interface ConfidenceBadgeProps {
  confidence: number | null
}

const BADGE_COLORS = {
  high:   'bg-green-100 text-green-800',
  medium: 'bg-amber-100 text-amber-800',
  low:    'bg-red-100 text-red-800',
}

/** Badge hiển thị độ tin cậy — bắt buộc trên mọi thẻ báo cáo */
export default function ConfidenceBadge({ confidence }: ConfidenceBadgeProps) {
  if (confidence === null) return null

  const level = confidenceLabel(confidence)
  const pct = Math.round(confidence * 100)

  return (
    <span
      className={`source-badge ${BADGE_COLORS[level]}`}
      title={`${vi.confidence.label}: ${pct}%`}
    >
      {vi.confidence[level]} {pct}%
    </span>
  )
}


import { vi } from '@/lib/i18n'
import type { SourceType } from '@/api/types'

interface SourceBadgeProps {
  sourceType: SourceType
}

const SOURCE_COLORS: Record<SourceType, string> = {
  OBSERVED:      'bg-green-100 text-green-800',
  MODELED:       'bg-blue-100 text-blue-800',
  HISTORICAL:    'bg-amber-100 text-amber-800',
  USER_REPORTED: 'bg-purple-100 text-purple-800',
  VERIFIED:      'bg-teal-100 text-teal-800',
}

/** Badge hiển thị loại nguồn dữ liệu — bắt buộc trên mọi thẻ báo cáo */
export default function SourceBadge({ sourceType }: SourceBadgeProps) {
  return (
    <span className={`source-badge ${SOURCE_COLORS[sourceType]}`}>
      {vi.source[sourceType]}
    </span>
  )
}


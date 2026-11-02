import { vi } from '@/lib/i18n'
import LoadingSpinner from '@/components/common/LoadingSpinner'

/** Placeholder card ngập — sẽ nhận dữ liệu thật từ API ở Giai đoạn 1.5 */
export default function FloodCard() {
  return (
    <div className="report-card">
      <h3 className="text-sm font-semibold text-gray-800 mb-1">{vi.flood.title}</h3>
      <LoadingSpinner size="sm" label="Chờ dữ liệu ngập…" />
      <p className="text-xs text-gray-400 mt-2 italic">{vi.flood.disclaimer}</p>
    </div>
  )
}


import { vi } from '@/lib/i18n'
import LoadingSpinner from '@/components/common/LoadingSpinner'

/** Placeholder card tiện ích gần nhất */
export default function AmenityCard() {
  return (
    <div className="report-card">
      <h3 className="text-sm font-semibold text-gray-800 mb-1">{vi.amenity.title}</h3>
      <LoadingSpinner size="sm" label="Chờ dữ liệu tiện ích…" />
    </div>
  )
}


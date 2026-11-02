import type { LatLng } from '@/lib/geo'
import { vi } from '@/lib/i18n'
import FloodCard from './FloodCard'
import AirQualityCard from './AirQualityCard'
import CommuteCard from './CommuteCard'
import AmenityCard from './AmenityCard'
import CardErrorBoundary from '@/components/common/CardErrorBoundary'

interface ReportSheetProps {
  location: LatLng | null
  onClose: () => void
}

/**
 * Bottom sheet trượt lên khi người dùng click bản đồ.
 * Mỗi card được bọc trong CardErrorBoundary — một card lỗi không sập cả báo cáo.
 */
export default function ReportSheet({ location, onClose }: ReportSheetProps) {
  const isVisible = location !== null

  return (
    <>
      {/* Overlay mờ phía sau (chỉ hiện khi sheet visible) */}
      {isVisible && (
        <div
          className="fixed inset-0 bg-black/20 z-40"
          onClick={onClose}
          aria-hidden="true"
        />
      )}

      <div
        className={`bottom-sheet ${isVisible ? 'bottom-sheet--visible' : 'bottom-sheet--hidden'}`}
        role="dialog"
        aria-modal="true"
        aria-label={vi.report.title}
      >
        {/* Drag handle */}
        <div className="sheet-handle" />

        {/* Header */}
        <div className="flex items-center justify-between px-4 pb-2">
          <div>
            <h2 className="text-base font-bold text-gray-900">{vi.report.title}</h2>
            {location && (
              <p className="text-xs text-gray-400 font-mono mt-0.5">
                {vi.report.coords(location.lat, location.lng)}
              </p>
            )}
          </div>
          <button
            onClick={onClose}
            className="p-2 rounded-full hover:bg-gray-100 transition-colors"
            aria-label={vi.report.close}
          >
            <svg className="h-5 w-5 text-gray-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        {/* Cards báo cáo */}
        {location && (
          <div className="px-4 pb-8 space-y-3">
            <CardErrorBoundary label="nguy cơ ngập">
              <FloodCard />
            </CardErrorBoundary>

            <CardErrorBoundary label="chất lượng không khí">
              <AirQualityCard />
            </CardErrorBoundary>

            <CardErrorBoundary label="thời gian di chuyển">
              <CommuteCard />
            </CardErrorBoundary>

            <CardErrorBoundary label="tiện ích gần nhất">
              <AmenityCard />
            </CardErrorBoundary>
          </div>
        )}
      </div>
    </>
  )
}


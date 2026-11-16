import { useState, useCallback } from 'react'
import type { LatLng } from '@/lib/geo'
import type { TravelMode, ReportParams } from '@/api/types'
import { vi } from '@/lib/i18n'
import { useReport } from '@/hooks/useReport'
import FloodCard from './FloodCard'
import AirQualityCard from './AirQualityCard'
import CommuteCard from './CommuteCard'
import AmenityCard from './AmenityCard'
import CardErrorBoundary from '@/components/common/CardErrorBoundary'

interface ReportSheetProps {
  location: LatLng | null
  isOpen: boolean
  onClose: () => void
}

/**
 * Bottom sheet trượt lên khi người dùng click bản đồ.
 * Wire tất cả card vào /api/report thật.
 * Mỗi card được bọc trong CardErrorBoundary — một card lỗi không sập cả báo cáo.
 */
export default function ReportSheet({ location, isOpen, onClose }: ReportSheetProps) {
  const isVisible = isOpen && location !== null

  // State điểm đến và phương tiện cho commute
  const [commuteDest, setCommuteDest] = useState<LatLng | null>(null)
  const [commuteMode, setCommuteMode] = useState<TravelMode>('motor_scooter')

  // Reset commute khi đổi vị trí
  const handleDestChange = useCallback((dest: LatLng | null) => {
    setCommuteDest(dest)
  }, [])

  const handleModeChange = useCallback((mode: TravelMode) => {
    setCommuteMode(mode)
    // Nếu đã có điểm đến → trigger refetch bằng cách đổi params
    // TanStack Query tự detect queryKey thay đổi
  }, [])

  // Tạo params cho useReport
  const reportParams: ReportParams | null = location
    ? {
        lat: location.lat,
        lng: location.lng,
        ...(commuteDest
          ? { destLat: commuteDest.lat, destLng: commuteDest.lng, mode: commuteMode }
          : {}),
      }
    : null

  const { data, isLoading } = useReport(reportParams)

  return (
    <>
      {/* Overlay mờ phía sau */}
      {isVisible && (
        <div
          className="pointer-events-none fixed inset-0 bg-black/20 z-40"
          onClick={onClose}
          aria-hidden="true"
        />
      )}

      <div
        className={`bottom-sheet ${isVisible ? 'bottom-sheet--visible' : 'bottom-sheet--hidden'}`}
        role={isVisible ? 'dialog' : undefined}
        aria-modal={isVisible ? 'true' : undefined}
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
              <FloodCard data={data?.floodRisk ?? null} isLoading={isLoading} />
            </CardErrorBoundary>

            <CardErrorBoundary label="chất lượng không khí">
              <AirQualityCard data={data?.airQuality ?? null} isLoading={isLoading} />
            </CardErrorBoundary>

            <CardErrorBoundary label="thời gian di chuyển">
              <CommuteCard
                data={data?.commute ?? null}
                isLoading={isLoading && commuteDest !== null}
                onDestChange={handleDestChange}
                onModeChange={handleModeChange}
                currentMode={commuteMode}
                hasDest={commuteDest !== null}
              />
            </CardErrorBoundary>

            <CardErrorBoundary label="tiện ích gần nhất">
              <AmenityCard data={data?.amenities ?? null} isLoading={isLoading} />
            </CardErrorBoundary>
          </div>
        )}
      </div>
    </>
  )
}

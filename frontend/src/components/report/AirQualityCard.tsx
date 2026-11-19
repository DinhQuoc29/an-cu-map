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

export default function AirQualityCard({ data, isLoading }: AirQualityCardProps) {
  return (
    <div className="report-card">
      <h3 className="text-sm font-semibold text-gray-800 mb-2">{vi.airQuality.title}</h3>
      {isLoading ? <LoadingSpinner size="sm" label={vi.report.loading} /> : !data ? (
        <ErrorState message={vi.report.noData} />
      ) : (
        <>
          <p className="text-xs text-gray-500">{vi.airQuality.annualMean}</p>
          <p className="my-2 text-xl font-bold text-gray-800">{data.pm25.value.toFixed(1)} <span className="text-xs font-normal">{vi.airQuality.unit}</span></p>
          {data.periodStart && data.periodEnd && <p className="mb-1 text-xs text-gray-500">{data.periodStart} – {data.periodEnd}</p>}
          {!!data.expectedHours && <p className="mb-2 text-xs text-gray-500">{vi.airQuality.coverage(data.validHours ?? 0, data.expectedHours)}</p>}
          <div className="flex gap-1 flex-wrap">
            <SourceBadge sourceType={data.pm25.sourceType} />
            <ConfidenceBadge confidence={data.pm25.confidence} />
          </div>
          <p className="mt-2 text-xs text-gray-500">{data.pm25.sourceName}</p>
          <a className="text-xs text-primary-700 underline" href="https://open-meteo.com/en/docs/air-quality-api" target="_blank" rel="noopener noreferrer">Open-Meteo / CAMS</a>
          <p className="text-xs text-gray-400 mt-2 italic">{vi.airQuality.disclaimer}</p>
        </>
      )}
    </div>
  )
}

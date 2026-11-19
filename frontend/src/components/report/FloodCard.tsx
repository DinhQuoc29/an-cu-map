import { vi } from '@/lib/i18n'
import type { FloodRiskResponse } from '@/api/types'
import LoadingSpinner from '@/components/common/LoadingSpinner'
import SourceBadge from '@/components/common/SourceBadge'
import ConfidenceBadge from '@/components/common/ConfidenceBadge'

interface FloodCardProps {
  data: FloodRiskResponse | null
  isLoading: boolean
}

export default function FloodCard({ data, isLoading }: FloodCardProps) {
  return (
    <div className="report-card">
      <h3 className="text-sm font-semibold text-gray-800 mb-2">{vi.flood.title}</h3>
      {isLoading ? <LoadingSpinner size="sm" label={vi.report.loading} /> : (
        <>
          <p className="text-sm font-medium text-gray-700">{vi.flood.insufficient}</p>
          {data && data.evidence.length > 0 ? (
            <>
              <p className="my-2 text-xs text-gray-600">{vi.flood.evidenceCount(data.nearbyPointsCount, data.radiusM)}</p>
              <div className="mb-2 flex gap-1"><SourceBadge sourceType="HISTORICAL" /><ConfidenceBadge confidence={null} /></div>
              <ul className="max-h-64 space-y-3 overflow-y-auto text-xs text-gray-600">
                {data.evidence.map((item) => (
                  <li key={item.id}>
                    <p className="font-medium">{item.locationLabel}</p>
                    <p>{item.eventDate} · {item.sourceName}</p>
                    {item.depthCm !== null && <p>{vi.flood.reportedDepth}: {item.depthCm} cm</p>}
                    {item.locationPrecisionM !== null && <p>{vi.flood.approximate} (~{item.locationPrecisionM} m)</p>}
                    {/^https?:\/\//.test(item.sourceUrl) && (
                      <a href={item.sourceUrl} target="_blank" rel="noopener noreferrer" className="text-primary-700 underline">{vi.flood.sourceLink}</a>
                    )}
                  </li>
                ))}
              </ul>
            </>
          ) : <p className="mt-2 text-xs text-gray-500">{vi.flood.noEvidence}</p>}
          <p className="mt-2 text-xs italic text-gray-400">{vi.flood.historicalDisclaimer}</p>
        </>
      )}
    </div>
  )
}

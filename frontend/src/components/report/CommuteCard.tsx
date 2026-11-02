import { vi } from '@/lib/i18n'

/** Placeholder card thời gian di chuyển */
export default function CommuteCard() {
  return (
    <div className="report-card">
      <h3 className="text-sm font-semibold text-gray-800 mb-1">{vi.commute.title}</h3>
      <p className="text-sm text-gray-500">{vi.commute.noDestination}</p>
      <button className="mt-2 w-full py-2 rounded-lg bg-primary-500 text-white text-sm font-medium hover:bg-primary-600 transition-colors">
        {vi.commute.setDestination}
      </button>
      <p className="text-xs text-gray-400 mt-2 italic">{vi.commute.disclaimer}</p>
    </div>
  )
}


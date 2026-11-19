import { useState, useRef, useEffect, type KeyboardEvent } from 'react'
import { useGeocode } from '@/hooks/useGeocode'
import type { GeocodeResult, TravelMode } from '@/api/types'
import type { LatLng } from '@/lib/geo'
import { vi } from '@/lib/i18n'

interface CommuteInputProps {
  onDestChange: (dest: LatLng | null) => void
  onModeChange: (mode: TravelMode) => void
  currentMode: TravelMode
}

/** Danh sách phương tiện theo thứ tự hiển thị */
const MODES: { key: TravelMode; icon: string }[] = [
  { key: 'motor_scooter', icon: '🛵' },
  { key: 'auto',          icon: '🚗' },
  { key: 'bicycle',       icon: '🚲' },
  { key: 'pedestrian',    icon: '🚶' },
]

/**
 * Thanh nhập điểm đến + chọn phương tiện cho CommuteCard.
 * Dùng lại cùng logic tìm kiếm với SearchBar (useGeocode).
 */
export default function CommuteInput({ onDestChange, onModeChange, currentMode }: CommuteInputProps) {
  const [query, setQuery] = useState('')
  const [isOpen, setIsOpen] = useState(false)
  const [activeIndex, setActiveIndex] = useState(-1)
  const inputRef = useRef<HTMLInputElement>(null)

  const { data: results, isLoading, isError, isDebouncing } = useGeocode(query)

  useEffect(() => {
    setActiveIndex(-1)
  }, [query])

  function handleSelect(result: GeocodeResult) {
    setQuery(result.displayName)
    setIsOpen(false)
    onDestChange({ lat: result.lat, lng: result.lng })
    inputRef.current?.blur()
  }

  function handleClear() {
    setQuery('')
    setIsOpen(false)
    onDestChange(null)
  }

  function handleKeyDown(e: KeyboardEvent<HTMLInputElement>) {
    if (!isOpen) return
    if (e.key === 'ArrowDown') {
      e.preventDefault()
      setActiveIndex((i) => Math.min(i + 1, results.length - 1))
    } else if (e.key === 'ArrowUp') {
      e.preventDefault()
      setActiveIndex((i) => Math.max(i - 1, 0))
    } else if (e.key === 'Enter' && activeIndex >= 0) {
      const result = results[activeIndex]
      if (result) handleSelect(result)
    } else if (e.key === 'Escape') {
      setIsOpen(false)
    }
  }

  return (
    <div className="space-y-2">
      {/* Thanh tìm kiếm điểm đến */}
      <div className="relative">
        <div className="flex items-center bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 gap-2">
          <span className="text-gray-400 text-xs flex-shrink-0">📍</span>
          <input
            ref={inputRef}
            type="search"
            value={query}
            onChange={(e) => {
              const value = e.target.value
              setQuery(value)
              setIsOpen(value.trim().length >= 3)
              onDestChange(null)
            }}
            onKeyDown={handleKeyDown}
            onFocus={() => query.trim().length >= 3 && setIsOpen(true)}
            onBlur={() => setIsOpen(false)}
            placeholder="Nhập điểm đến (nơi làm, học…)"
            className="min-w-0 flex-1 text-sm outline-none bg-transparent placeholder-gray-400"
            aria-label="Điểm đến"
          />
          {isLoading && (
            <svg className="h-3 w-3 animate-spin text-gray-400" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
            </svg>
          )}
          {query && (
            <button onClick={handleClear} className="text-gray-400 hover:text-gray-600" aria-label="Xóa">
              <svg className="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          )}
        </div>

        {/* Dropdown kết quả */}
        {isOpen && query.trim().length >= 3 && (
          <ul
            role="listbox"
            className="absolute top-full mt-1 w-full bg-white rounded-lg shadow-lg border border-gray-100 z-50 max-h-48 overflow-y-auto"
          >
            {isDebouncing || isLoading ? (
              <li className="px-3 py-2 text-xs text-gray-500">{vi.map.searching}</li>
            ) : isError ? (
              <li className="px-3 py-2 text-xs text-red-500">{vi.map.errorSearch}</li>
            ) : results.length === 0 ? (
              <li className="px-3 py-2 text-xs text-gray-500">{vi.map.noResults}</li>
            ) : results.map((result, i) => (
              <li
                key={result.placeId}
                role="option"
                aria-selected={i === activeIndex}
                onMouseDown={(e) => e.preventDefault()}
                onClick={() => handleSelect(result)}
                className={`px-3 py-2 text-xs cursor-pointer transition-colors ${
                  i === activeIndex ? 'bg-primary-50 text-primary-700' : 'hover:bg-gray-50 text-gray-700'
                }`}
              >
                <span className="line-clamp-2">{result.displayName}</span>
              </li>
            ))}
          </ul>
        )}
      </div>

      {/* Chọn phương tiện */}
      <div className="flex gap-1">
        {MODES.map(({ key, icon }) => (
          <button
            key={key}
            onClick={() => onModeChange(key)}
            title={vi.commute.modes[key]}
            aria-label={vi.commute.modes[key]}
            aria-pressed={currentMode === key}
            className={`flex-1 py-1.5 rounded-lg text-base transition-colors ${
              currentMode === key
                ? 'bg-primary-500 text-white shadow-sm'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            {icon}
          </button>
        ))}
      </div>
    </div>
  )
}


import { useState, useRef, useEffect, type KeyboardEvent } from 'react'
import { useGeocode } from '@/hooks/useGeocode'
import type { GeocodeResult } from '@/api/types'
import type { LatLng } from '@/lib/geo'
import { vi } from '@/lib/i18n'

interface SearchBarProps {
  onSelect: (pos: LatLng) => void
}

/**
 * Thanh tìm kiếm địa chỉ dùng Nominatim.
 * Debounce: chỉ fetch khi query >= 3 ký tự (xử lý trong useGeocode).
 */
export default function SearchBar({ onSelect }: SearchBarProps) {
  const [query, setQuery] = useState('')
  const [isOpen, setIsOpen] = useState(false)
  const [activeIndex, setActiveIndex] = useState(-1)
  const inputRef = useRef<HTMLInputElement>(null)
  const listRef = useRef<HTMLUListElement>(null)

  const { data: results = [], isLoading, isError } = useGeocode(query)

  // Mở dropdown khi có kết quả
  useEffect(() => {
    setIsOpen(results.length > 0 && query.trim().length >= 3)
    setActiveIndex(-1)
  }, [results, query])

  function handleSelect(result: GeocodeResult) {
    setQuery(result.displayName)
    setIsOpen(false)
    onSelect({ lat: result.lat, lng: result.lng })
    inputRef.current?.blur()
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
    <div className="relative w-full max-w-sm" role="search">
      <div className="flex items-center bg-white rounded-xl shadow-md px-3 py-2 gap-2">
        {/* Icon tìm kiếm */}
        <svg className="h-4 w-4 text-gray-400 flex-shrink-0" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-4.35-4.35M17 11A6 6 0 105 11a6 6 0 0012 0z" />
        </svg>

        <input
          ref={inputRef}
          type="search"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onKeyDown={handleKeyDown}
          onFocus={() => results.length > 0 && setIsOpen(true)}
          placeholder={vi.map.searchPlaceholder}
          className="flex-1 text-sm outline-none bg-transparent placeholder-gray-400"
          aria-label={vi.map.searchPlaceholder}
          aria-autocomplete="list"
          aria-controls="search-results"
          aria-expanded={isOpen}
        />

        {/* Loading indicator */}
        {isLoading && (
          <svg className="h-4 w-4 animate-spin text-primary-500 flex-shrink-0" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
          </svg>
        )}

        {/* Clear button */}
        {query && (
          <button
            onClick={() => { setQuery(''); setIsOpen(false) }}
            className="text-gray-400 hover:text-gray-600"
            aria-label="Xóa tìm kiếm"
          >
            <svg className="h-4 w-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        )}
      </div>

      {/* Dropdown kết quả */}
      {isOpen && (
        <ul
          id="search-results"
          ref={listRef}
          role="listbox"
          className="absolute top-full mt-1 w-full bg-white rounded-xl shadow-lg border border-gray-100 overflow-hidden z-50 max-h-64 overflow-y-auto"
        >
          {isError ? (
            <li className="px-4 py-3 text-sm text-red-500">{vi.map.errorSearch}</li>
          ) : results.length === 0 ? (
            <li className="px-4 py-3 text-sm text-gray-500">{vi.map.noResults}</li>
          ) : (
            results.map((result, i) => (
              <li
                key={result.placeId}
                role="option"
                aria-selected={i === activeIndex}
                onClick={() => handleSelect(result)}
                className={`px-4 py-3 text-sm cursor-pointer transition-colors ${
                  i === activeIndex ? 'bg-primary-50 text-primary-700' : 'hover:bg-gray-50 text-gray-700'
                }`}
              >
                <span className="line-clamp-2">{result.displayName}</span>
              </li>
            ))
          )}
        </ul>
      )}
    </div>
  )
}


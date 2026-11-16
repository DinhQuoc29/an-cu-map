import { useState, type CSSProperties } from 'react'
import { Marker, Popup } from 'react-map-gl/maplibre'
import type { AmenityItem, AmenityCategory } from '@/api/types'
import { vi } from '@/lib/i18n'
import { formatDistance } from '@/lib/geo'

/** Icon (emoji) + màu cho từng loại tiện ích */
const CATEGORY_META: Record<AmenityCategory, { icon: string; color: string }> = {
  hospital:    { icon: '🏥', color: '#dc2626' },
  clinic:      { icon: '🩺', color: '#ea580c' },
  school:      { icon: '🏫', color: '#2563eb' },
  market:      { icon: '🛒', color: '#16a34a' },
  supermarket: { icon: '🏪', color: '#0891b2' },
}

const ALL_CATEGORIES = Object.keys(CATEGORY_META) as AmenityCategory[]

interface AmenityLayerProps {
  items: AmenityItem[]
}

/**
 * Vẽ các tiện ích lên bản đồ bằng marker + filter theo loại + popup chi tiết.
 * Không dùng ảnh ngoài: icon là emoji trong badge SVG (thuần CSS/SVG).
 */
export default function AmenityLayer({ items }: AmenityLayerProps) {
  const [enabled, setEnabled] = useState<Set<AmenityCategory>>(
    () => new Set(ALL_CATEGORIES),
  )
  const [openId, setOpenId] = useState<number | null>(null)

  const toggle = (cat: AmenityCategory) => {
    setEnabled((prev) => {
      const next = new Set(prev)
      if (next.has(cat)) next.delete(cat)
      else next.add(cat)
      return next
    })
  }

  const visible = items.filter((i) => enabled.has(i.category))

  return (
    <>
      {/* Bộ lọc tiện ích nổi trên bản đồ (trên bottom sheet) */}
      <div className="amenity-filters">
        {ALL_CATEGORIES.map((cat) => (
          <button
            key={cat}
            type="button"
            aria-pressed={enabled.has(cat)}
            onClick={() => toggle(cat)}
            className={
              'amenity-filter-chip' +
              (enabled.has(cat) ? ' amenity-filter-chip--on' : '')
            }
          >
            <span aria-hidden>{CATEGORY_META[cat].icon}</span>
            <span>{vi.amenity.categories[cat]}</span>
          </button>
        ))}
      </div>

      {/* Marker tiện ích */}
      {visible.map((item) => {
        const meta = CATEGORY_META[item.category]
        const isOpen = openId === item.id
        return (
          <Marker
            key={item.id}
            longitude={item.lng}
            latitude={item.lat}
            anchor="bottom"
          >
            <button
              type="button"
              className="amenity-marker"
              style={{ '--dot': meta.color } as CSSProperties}
              onClick={() => setOpenId(isOpen ? null : item.id)}
              aria-label={item.name ?? vi.amenity.categories[item.category]}
            >
              {meta.icon}
            </button>
            {isOpen && (
              <Popup
                longitude={item.lng}
                latitude={item.lat}
                anchor="bottom"
                offset={4}
                closeOnClick={false}
              >
                <div className="amenity-popup">
                  <strong>{item.name ?? vi.amenity.categories[item.category]}</strong>
                  <span className="amenity-popup__sub">
                    {vi.amenity.categories[item.category]} ·{' '}
                    {formatDistance(item.distanceM)}
                  </span>
                </div>
              </Popup>
            )}
          </Marker>
        )
      })}
    </>
  )
}

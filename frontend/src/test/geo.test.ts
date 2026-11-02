import { describe, it, expect } from 'vitest'
import { formatDistance, formatDuration, isInsideHanoi, confidenceLabel } from '@/lib/geo'

describe('geo utils', () => {
  it('formatDistance — dưới 1 km trả về mét', () => {
    expect(formatDistance(500)).toBe('500 m')
  })

  it('formatDistance — trên 1 km trả về km', () => {
    expect(formatDistance(2500)).toBe('2.5 km')
  })

  it('formatDuration — dưới 60 phút', () => {
    expect(formatDuration(1800)).toBe('30 phút')
  })

  it('formatDuration — trên 60 phút', () => {
    expect(formatDuration(5400)).toBe('1 giờ 30 phút')
  })

  it('isInsideHanoi — trung tâm Hà Nội', () => {
    expect(isInsideHanoi({ lat: 21.0285, lng: 105.8542 })).toBe(true)
  })

  it('isInsideHanoi — ngoài Hà Nội (TP.HCM)', () => {
    expect(isInsideHanoi({ lat: 10.7769, lng: 106.7009 })).toBe(false)
  })

  it('confidenceLabel — cao khi >= 0.7', () => {
    expect(confidenceLabel(0.85)).toBe('high')
  })

  it('confidenceLabel — thấp khi < 0.4', () => {
    expect(confidenceLabel(0.2)).toBe('low')
  })
})


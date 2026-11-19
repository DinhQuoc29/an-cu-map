import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import FloodCard from '@/components/report/FloodCard'
import AirQualityCard from '@/components/report/AirQualityCard'
import AmenityCard from '@/components/report/AmenityCard'
import type { FloodRiskResponse, AirQualityResponse, AmenitiesResponse } from '@/api/types'

// ─── Helper: bọc component trong QueryClient ─────────────────────────────────

function wrapper({ children }: { children: React.ReactNode }) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return <QueryClientProvider client={qc}>{children}</QueryClientProvider>
}

// ─── FloodCard ────────────────────────────────────────────────────────────────

describe('FloodCard', () => {
  it('shows loading', () => {
    render(<FloodCard data={null} isLoading={true} />, { wrapper })
    expect(screen.getByRole('status')).toBeInTheDocument()
  })

  it('does not infer low risk from missing data', () => {
    const data: FloodRiskResponse = { riskScore: null, nearbyPointsCount: 0, evidence: [], assessmentStatus: 'INSUFFICIENT_DATA', radiusM: 500 }
    render(<FloodCard data={data} isLoading={false} />, { wrapper })
    expect(screen.getByText(/Chưa đủ dữ liệu/)).toBeInTheDocument()
    expect(screen.queryByText('1/5')).not.toBeInTheDocument()
  })

  it('shows source links and dates for historical evidence', () => {
    const data: FloodRiskResponse = { riskScore: null, nearbyPointsCount: 1, assessmentStatus: 'HISTORICAL_EVIDENCE', radiusM: 500,
      evidence: [{ id: 1, locationLabel: 'Tạ Hiện', eventDate: '2024-08-13', sourceName: 'VnExpress', sourceUrl: 'https://vnexpress.net/event.html', depthCm: 50, locationPrecisionM: 100, distanceM: 50 }] }
    render(<FloodCard data={data} isLoading={false} />, { wrapper })
    expect(screen.getByRole('link', { name: 'Xem nguồn' })).toHaveAttribute('href', 'https://vnexpress.net/event.html')
    expect(screen.getByText(/2024-08-13/)).toBeInTheDocument()
    expect(screen.getByText(/50 cm/)).toBeInTheDocument()
  })
})

// ─── AirQualityCard ───────────────────────────────────────────────────────────

describe('AirQualityCard', () => {
  it('hiển thị spinner khi loading', () => {
    render(<AirQualityCard data={null} isLoading={true} />, { wrapper })
    expect(screen.getByRole('status')).toBeInTheDocument()
  })

  it('hiển thị PM2.5 và nhãn chất lượng', () => {
    const data: AirQualityResponse = {
      pm25: {
        value: 28.5,
        unit: 'µg/m³',
        sourceType: 'MODELED',
        sourceName: 'Open-Meteo',
        confidence: 0.6,
        updatedAt: new Date().toISOString(),
      },
      nearestStationKm: null,
    }
    render(<AirQualityCard data={data} isLoading={false} />, { wrapper })
    expect(screen.getByText('28.5')).toBeInTheDocument()
    // "Trung bình" và "Mô hình" xuất hiện nhiều lần — dùng getAllByText
    expect(screen.getByText(/PM2.5 trung bình 365 ngày/)).toBeInTheDocument()
    expect(screen.getAllByText(/mô hình/i).length).toBeGreaterThanOrEqual(1)
  })

  it('hiển thị trung bình năm mà không gán nhãn AQI', () => {
    const data: AirQualityResponse = {
      pm25: { value: 10.0, unit: 'µg/m³', sourceType: 'MODELED', sourceName: 'T', confidence: 0.6, updatedAt: '' },
      nearestStationKm: null,
    }
    render(<AirQualityCard data={data} isLoading={false} />, { wrapper })
    expect(screen.getByText('10.0')).toBeInTheDocument(); expect(screen.queryByText('Tốt')).not.toBeInTheDocument()
  })
})

// ─── AmenityCard ──────────────────────────────────────────────────────────────

describe('AmenityCard', () => {
  it('hiển thị "Không tìm thấy" khi items rỗng', () => {
    const data: AmenitiesResponse = { items: [], radiusM: 2000 }
    render(<AmenityCard data={data} isLoading={false} />, { wrapper })
    expect(screen.getByText(/không tìm thấy/i)).toBeInTheDocument()
  })

  it('hiển thị danh sách tiện ích', () => {
    const data: AmenitiesResponse = {
      radiusM: 2000,
      items: [
        { id: 1, category: 'hospital', subcategory: null, name: 'BV Bạch Mai', address: 'Hà Nội', lat: 21.0, lng: 105.8, distanceM: 450 },
        { id: 2, category: 'school', subcategory: null, name: 'Trường THPT', address: null, lat: 21.01, lng: 105.81, distanceM: 800 },
      ],
    }
    render(<AmenityCard data={data} isLoading={false} />, { wrapper })
    expect(screen.getByText('BV Bạch Mai')).toBeInTheDocument()
    expect(screen.getByText('450 m')).toBeInTheDocument()
    expect(screen.getByText('Trường THPT')).toBeInTheDocument()
    expect(screen.getByText('800 m')).toBeInTheDocument()
  })
})

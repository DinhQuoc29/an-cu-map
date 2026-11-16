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
  it('hiển thị spinner khi isLoading=true', () => {
    render(<FloodCard data={null} isLoading={true} />, { wrapper })
    expect(screen.getByRole('status')).toBeInTheDocument()
  })

  it('hiển thị "Không có dữ liệu" khi data=null và không loading', () => {
    render(<FloodCard data={null} isLoading={false} />, { wrapper })
    expect(screen.getByText(/không có dữ liệu/i)).toBeInTheDocument()
  })

  it('hiển thị mức nguy cơ và nguồn khi có data', () => {
    const data: FloodRiskResponse = {
      riskScore: {
        value: 3,
        unit: '',
        sourceType: 'HISTORICAL',
        sourceName: 'Test',
        confidence: 0.4,
        updatedAt: new Date().toISOString(),
      },
      nearbyPointsCount: 5,
    }
    render(<FloodCard data={data} isLoading={false} />, { wrapper })
    expect(screen.getByText('3/5')).toBeInTheDocument()
    // "Trung bình" và "Lịch sử" xuất hiện nhiều lần (nhãn + badge) — dùng getAllByText
    expect(screen.getAllByText(/trung bình/i).length).toBeGreaterThanOrEqual(1)
    expect(screen.getByText('5 điểm ngập lịch sử trong bán kính 500 m')).toBeInTheDocument()
    expect(screen.getAllByText(/lịch sử/i).length).toBeGreaterThanOrEqual(1)
  })

  it('hiển thị disclaimer luôn luôn', () => {
    const data: FloodRiskResponse = {
      riskScore: { value: 1, unit: '', sourceType: 'HISTORICAL', sourceName: 'T', confidence: 0.2, updatedAt: '' },
      nearbyPointsCount: 0,
    }
    render(<FloodCard data={data} isLoading={false} />, { wrapper })
    expect(screen.getByText(/ước tính/i)).toBeInTheDocument()
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
    expect(screen.getAllByText(/trung bình/i).length).toBeGreaterThanOrEqual(1)
    expect(screen.getAllByText(/mô hình/i).length).toBeGreaterThanOrEqual(1)
  })

  it('hiển thị "Tốt" khi PM2.5 <= 15', () => {
    const data: AirQualityResponse = {
      pm25: { value: 10.0, unit: 'µg/m³', sourceType: 'MODELED', sourceName: 'T', confidence: 0.6, updatedAt: '' },
      nearestStationKm: null,
    }
    render(<AirQualityCard data={data} isLoading={false} />, { wrapper })
    expect(screen.getByText('Tốt')).toBeInTheDocument()
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

import type { LocationReport, ReportParams } from './types'

/**
 * Gọi /api/report để lấy báo cáo tổng hợp vị trí.
 */
export async function fetchReport(params: ReportParams): Promise<LocationReport> {
  const url = new URL('/api/report', window.location.origin)
  url.searchParams.set('lat', String(params.lat))
  url.searchParams.set('lng', String(params.lng))
  if (params.destLat != null) url.searchParams.set('destLat', String(params.destLat))
  if (params.destLng != null) url.searchParams.set('destLng', String(params.destLng))
  if (params.mode) url.searchParams.set('mode', params.mode)

  const res = await fetch(url.toString())
  if (!res.ok) {
    const text = await res.text()
    throw new Error(`Report API lỗi ${String(res.status)}: ${text}`)
  }
  return res.json() as Promise<LocationReport>
}


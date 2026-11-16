import { useQuery } from '@tanstack/react-query'
import { fetchReport } from '@/api/report'
import type { LocationReport, ReportParams } from '@/api/types'

/**
 * Hook lấy báo cáo vị trí từ /api/report.
 * Chỉ fetch khi params không null.
 * Cache 5 phút — dữ liệu địa lý không thay đổi nhanh.
 */
export function useReport(params: ReportParams | null) {
  return useQuery<LocationReport>({
    queryKey: ['report', params],
    queryFn: () => fetchReport(params as ReportParams),
    enabled: params !== null,
    staleTime: 5 * 60 * 1000,  // 5 phút
    retry: 1,
  })
}

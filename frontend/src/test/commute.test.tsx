import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import CommuteCard from '@/components/report/CommuteCard'

describe('commute recovery', () => {
  it('allows retry when routing is unavailable', () => {
    const retry = vi.fn()
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(<QueryClientProvider client={client}>
      <CommuteCard data={null} isLoading={false} hasDest currentMode="auto"
        onDestChange={vi.fn()} onModeChange={vi.fn()} onRetry={retry} />
    </QueryClientProvider>)
    fireEvent.click(screen.getByRole('button', { name: 'Thử tính lại' }))
    expect(retry).toHaveBeenCalledOnce()
    expect(screen.getByText(/chưa tính kẹt xe/)).toBeInTheDocument()
  })
})

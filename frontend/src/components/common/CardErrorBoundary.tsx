import { Component, type ReactNode, type ErrorInfo } from 'react'
import ErrorState from './ErrorState'

interface Props {
  children: ReactNode
  fallback?: ReactNode
  label?: string
}

interface State {
  hasError: boolean
}

/**
 * Error Boundary — ngăn một card lỗi làm sập cả báo cáo.
 * Mỗi card (FloodCard, AirQualityCard, ...) nên bọc trong component này.
 */
export default class CardErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props)
    this.state = { hasError: false }
  }

  static getDerivedStateFromError(): State {
    return { hasError: true }
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    // Log ở mức warn — không log tọa độ người dùng
    console.warn('[CardErrorBoundary]', error.message, info.componentStack)
  }

  render() {
    if (this.state.hasError) {
      return (
        this.props.fallback ?? (
          <div className="report-card">
            <ErrorState
              message={`Không thể hiển thị ${this.props.label ?? 'thông tin này'}.`}
              onRetry={() => this.setState({ hasError: false })}
            />
          </div>
        )
      )
    }
    return this.props.children
  }
}


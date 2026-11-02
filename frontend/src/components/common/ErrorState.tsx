interface ErrorStateProps {
  message?: string
  onRetry?: () => void
}

export default function ErrorState({
  message = 'Không thể tải dữ liệu.',
  onRetry,
}: ErrorStateProps) {
  return (
    <div className="flex flex-col items-center gap-2 py-4 text-center">
      <svg
        className="h-8 w-8 text-red-400"
        xmlns="http://www.w3.org/2000/svg"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
        aria-hidden="true"
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={2}
          d="M12 9v2m0 4h.01M12 3a9 9 0 100 18A9 9 0 0012 3z"
        />
      </svg>
      <p className="text-sm text-gray-600">{message}</p>
      {onRetry && (
        <button
          onClick={onRetry}
          className="mt-1 text-xs text-primary-600 underline hover:text-primary-700"
        >
          Thử lại
        </button>
      )}
    </div>
  )
}


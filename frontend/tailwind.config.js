/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // Màu chủ đạo An Cư
        primary: {
          50:  '#eff6ff',
          100: '#dbeafe',
          500: '#3b82f6',
          600: '#2563eb',
          700: '#1d4ed8',
        },
        flood: {
          low:    '#22c55e',  // xanh lá — nguy cơ thấp
          medium: '#f59e0b',  // vàng — trung bình
          high:   '#ef4444',  // đỏ — cao
        },
        aqi: {
          good:     '#22c55e',
          moderate: '#f59e0b',
          unhealthy:'#ef4444',
        },
      },
      borderRadius: {
        'xl': '1rem',
        '2xl': '1.5rem',
      },
      boxShadow: {
        'sheet': '0 -4px 24px rgba(0,0,0,0.12)',
      },
    },
  },
  plugins: [],
}


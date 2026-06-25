/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        // Pulled directly from the NumberMerge 2048 game theme (MainActivity.kt / GameScreen.kt)
        ink: '#16141f',
        deep: '#0f0d16',
        bg: '#14131b',
        panel: '#23202c',
        panelSoft: '#2a2633',
        board: '#2e2a38',
        empty: '#3a3644',
        orange: '#ffa938',   // OrangePrimary
        green: '#63d883',    // GreenPrimary
        blue: '#69b3ff',     // BlueAccent
        gold: '#f4d544',     // 2048 tile
        amber: '#ddb42c',    // 512 tile
        frozen: '#78d6ff',   // Freeze
        cream: '#f6f2e9',    // TextPrimary
        taupe: '#c0b7aa',    // TextSecondary
      },
      fontFamily: {
        sans: ['Plus Jakarta Sans', 'Inter', 'system-ui', 'sans-serif'],
        display: ['Sora', 'Plus Jakarta Sans', 'system-ui', 'sans-serif'],
      },
      borderRadius: { xl2: '1.5rem', xl3: '2rem' },
      fontWeight: { 400: '400', 500: '500', 600: '600', 700: '700', 800: '800' },
      keyframes: {
        float: { '0%,100%': { transform: 'translateY(0)' }, '50%': { transform: 'translateY(-14px)' } },
        shimmer: { '0%': { backgroundPosition: '200% 0' }, '100%': { backgroundPosition: '-200% 0' } },
      },
      animation: {
        float: 'float 6s ease-in-out infinite',
        shimmer: 'shimmer 6s linear infinite',
      },
    },
  },
  plugins: [],
}

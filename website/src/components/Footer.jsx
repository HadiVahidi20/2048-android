export default function Footer() {
  return (
    <footer className="border-t border-white/10 py-12">
      <div className="container-x">
        <div className="flex flex-col items-center justify-between gap-6 sm:flex-row">
          <div className="flex items-center gap-2.5">
            <img src="/assets/icon.png" alt="" className="h-8 w-8 rounded-lg" />
            <span className="font-display text-sm font-700">NumberMerge 2048</span>
            <span className="text-sm text-cream/35">· by Hadify Studios</span>
          </div>
          <nav className="flex flex-wrap items-center justify-center gap-x-5 gap-y-2 text-sm text-cream/55">
            <a href="/privacy" className="transition hover:text-cream">Privacy</a>
            <a href="/terms" className="transition hover:text-cream">Terms</a>
            <a href="mailto:hadivahidi20@gmail.com" className="transition hover:text-cream">Contact</a>
            <a href="https://hadify.app" className="transition hover:text-cream">More games</a>
          </nav>
        </div>
        <div className="mt-8 text-center text-xs text-cream/30">© 2026 Hadify Studios. All rights reserved.</div>
      </div>
    </footer>
  )
}

import { useEffect, useState } from 'react'
import PlayButton from './PlayButton'

export default function Nav() {
  const [scrolled, setScrolled] = useState(false)
  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 20)
    onScroll()
    window.addEventListener('scroll', onScroll, { passive: true })
    return () => window.removeEventListener('scroll', onScroll)
  }, [])

  return (
    <header className={`fixed inset-x-0 top-0 z-50 transition-all duration-300 ${scrolled ? 'py-2' : 'py-4'}`}>
      <div className="container-x">
        <div className={`flex items-center justify-between rounded-2xl px-3 py-2.5 transition-all duration-300 ${scrolled ? 'border border-white/10 bg-deep/70 backdrop-blur-xl' : 'border border-transparent'}`}>
          <a href="#top" className="flex items-center gap-2.5">
            <img src="/assets/icon.png" alt="" className="h-9 w-9 rounded-xl" />
            <span className="font-display text-[15px] font-700">NumberMerge 2048</span>
          </a>
          <nav className="hidden items-center gap-1 md:flex">
            {[['Features', '#features'], ['How to play', '#how'], ['Daily', '#daily'], ['Gallery', '#gallery']].map(([t, h]) => (
              <a key={h} href={h} className="rounded-full px-3.5 py-2 text-sm text-cream/65 transition hover:bg-white/5 hover:text-cream">{t}</a>
            ))}
          </nav>
          <PlayButton className="btn btn-primary !px-5 !py-2.5 text-[13px]" label="Get the app" />
        </div>
      </div>
    </header>
  )
}

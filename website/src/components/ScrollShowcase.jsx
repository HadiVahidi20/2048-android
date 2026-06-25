import { useRef, useState } from 'react'
import { motion, useScroll, useMotionValueEvent, AnimatePresence } from 'framer-motion'
import PhoneFrame from './PhoneFrame'
import { SHOTS } from '../data'

// A pinned phone that swaps screens as you scroll — the signature effect.
export default function ScrollShowcase() {
  const ref = useRef(null)
  const [active, setActive] = useState(0)
  const { scrollYProgress } = useScroll({ target: ref, offset: ['start start', 'end end'] })

  useMotionValueEvent(scrollYProgress, 'change', (p) => {
    const idx = Math.min(SHOTS.length - 1, Math.floor(p * SHOTS.length))
    setActive(idx)
  })

  return (
    <section id="gallery" ref={ref} className="relative" style={{ height: `${SHOTS.length * 90}vh` }}>
      <div className="sticky top-0 flex h-screen items-center overflow-hidden">
        <div className="blob left-1/4 top-1/3 h-[360px] w-[360px] bg-blue/20" />
        <div className="container-x relative z-10 grid items-center gap-10 lg:grid-cols-2">
          {/* text side */}
          <div className="order-2 lg:order-1">
            <span className="chip">A peek inside</span>
            <h2 className="display mt-5 text-[clamp(2rem,4.5vw,3.2rem)] font-800 leading-tight">
              Polished, dark,<br /><span className="text-grad">made to focus.</span>
            </h2>
            <div className="mt-8 space-y-3">
              {SHOTS.map((s, i) => (
                <button
                  key={s.label}
                  onClick={() => {
                    const el = ref.current
                    if (!el) return
                    const top = el.offsetTop + (i + 0.5) * (el.offsetHeight / SHOTS.length) - window.innerHeight / 2
                    window.scrollTo({ top, behavior: 'smooth' })
                  }}
                  className={`block w-full rounded-2xl border p-4 text-left transition-all duration-300 ${
                    active === i ? 'border-white/15 bg-white/[0.06]' : 'border-transparent bg-transparent opacity-50 hover:opacity-80'
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <span className={`grid h-8 w-8 place-items-center rounded-lg text-[13px] font-700 ${active === i ? 'bg-gradient-to-br from-orange to-amber text-[#1a1208]' : 'bg-white/10 text-cream/60'}`}>{i + 1}</span>
                    <span className="font-600">{s.label}</span>
                  </div>
                  <AnimatePresence>
                    {active === i && (
                      <motion.p
                        initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: 'auto' }} exit={{ opacity: 0, height: 0 }}
                        className="mt-2 pl-11 text-sm text-cream/60"
                      >
                        {s.caption}
                      </motion.p>
                    )}
                  </AnimatePresence>
                </button>
              ))}
            </div>
          </div>

          {/* phone side */}
          <div className="order-1 flex justify-center lg:order-2">
            <div className="relative">
              <div className="absolute inset-0 -z-10 m-auto h-72 w-72 rounded-full bg-orange/25 blur-3xl" />
              <PhoneFrame>
                <AnimatePresence mode="popLayout">
                  <motion.img
                    key={SHOTS[active].src}
                    src={SHOTS[active].src}
                    alt={SHOTS[active].label}
                    className="absolute inset-0 h-full w-full object-cover"
                    initial={{ opacity: 0, scale: 1.04 }}
                    animate={{ opacity: 1, scale: 1 }}
                    exit={{ opacity: 0, scale: 0.98 }}
                    transition={{ duration: 0.5, ease: 'easeOut' }}
                    draggable="false"
                  />
                </AnimatePresence>
              </PhoneFrame>
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}

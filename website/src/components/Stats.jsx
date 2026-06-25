import { useEffect, useRef } from 'react'
import gsap from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'
import { STATS } from '../data'

gsap.registerPlugin(ScrollTrigger)

export default function Stats() {
  const ref = useRef(null)

  useEffect(() => {
    const ctx = gsap.context(() => {
      gsap.utils.toArray('[data-count]').forEach((el) => {
        const end = Number(el.dataset.count)
        const obj = { v: 0 }
        gsap.to(obj, {
          v: end,
          duration: 1.6,
          ease: 'power2.out',
          scrollTrigger: { trigger: el, start: 'top 85%', once: true },
          onUpdate: () => { el.textContent = Math.round(obj.v).toString() },
        })
      })
    }, ref)
    return () => ctx.revert()
  }, [])

  return (
    <section ref={ref} className="relative py-20">
      <div className="container-x relative z-10">
        <div className="card-surface grid grid-cols-2 gap-6 px-6 py-10 sm:grid-cols-4 sm:px-12">
          {STATS.map((s) => (
            <div key={s.label} className="text-center">
              <div className="font-display text-[clamp(2.2rem,5vw,3.4rem)] font-800 leading-none">
                <span data-count={s.value}>0</span><span className="text-grad">{s.suffix}</span>
              </div>
              <div className="mt-2 text-sm text-cream/55">{s.label}</div>
            </div>
          ))}
        </div>
      </div>
    </section>
  )
}

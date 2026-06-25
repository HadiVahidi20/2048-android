import { useRef } from 'react'
import { motion, useScroll, useTransform } from 'framer-motion'
import PhoneFrame from './PhoneFrame'
import MiniBoard from './MiniBoard'
import PlayButton from './PlayButton'

export default function Hero() {
  const ref = useRef(null)
  const { scrollYProgress } = useScroll({ target: ref, offset: ['start start', 'end start'] })
  const phoneY = useTransform(scrollYProgress, [0, 1], [0, 120])
  const phoneRot = useTransform(scrollYProgress, [0, 1], [0, -6])
  const fade = useTransform(scrollYProgress, [0, 0.8], [1, 0])

  const ease = [0.21, 0.6, 0.35, 1]

  return (
    <section id="top" ref={ref} className="relative overflow-hidden pb-24 pt-32 sm:pt-40">
      {/* background */}
      <div className="blob -left-32 -top-24 h-[420px] w-[420px] bg-orange/40" />
      <div className="blob right-[-120px] top-10 h-[360px] w-[360px] bg-blue/30" />
      <div className="absolute inset-0 grid-overlay" />

      <div className="container-x relative z-10 grid items-center gap-14 lg:grid-cols-[1.05fr_0.95fr]">
        {/* copy */}
        <div className="text-center lg:text-left">
          <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.6, ease }}>
            <span className="chip">🎯 The 2048 merge puzzle with real power-ups</span>
          </motion.div>
          <motion.h1
            className="display mt-6 text-[clamp(2.6rem,6vw,4.6rem)] font-800 leading-[1.02]"
            initial={{ opacity: 0, y: 24 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.7, delay: 0.05, ease }}
          >
            Merge the tiles.<br />
            <span className="text-grad">Reach 2048.</span>
          </motion.h1>
          <motion.p
            className="mx-auto mt-6 max-w-xl text-[17px] leading-relaxed text-cream/65 lg:mx-0"
            initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.7, delay: 0.15, ease }}
          >
            Swipe to slide and combine matching tiles all the way to 2048 — then keep going.
            Bend the board with Swap, Undo, Bomb and Freeze, on 4×4, 5×5 or 6×6 grids.
          </motion.p>
          <motion.div
            className="mt-9 flex flex-wrap items-center justify-center gap-3 lg:justify-start"
            initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.7, delay: 0.25, ease }}
          >
            <PlayButton />
            <a href="#how" className="btn btn-ghost">How to play</a>
          </motion.div>
          <motion.div
            className="mt-7 flex items-center justify-center gap-5 text-sm text-cream/45 lg:justify-start"
            initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ duration: 0.8, delay: 0.4 }}
          >
            <span>4 power-ups</span><span className="hidden sm:inline">·</span>
            <span>3 board sizes</span><span className="hidden sm:inline">·</span>
            <span>Daily challenges</span>
          </motion.div>
        </div>

        {/* phone with the live merging board INSIDE the frame */}
        <motion.div className="relative flex justify-center" style={{ y: phoneY, rotate: phoneRot, opacity: fade }}>
          <div className="absolute inset-0 -z-10 m-auto h-72 w-72 rounded-full bg-orange/30 blur-3xl" />
          <motion.div
            initial={{ opacity: 0, y: 40, scale: 0.96 }} animate={{ opacity: 1, y: 0, scale: 1 }}
            transition={{ duration: 0.9, delay: 0.2, ease }}
            className="animate-float"
          >
            <PhoneFrame>
              <div className="flex h-full flex-col bg-bg px-3 pb-3 pt-8">
                <div className="mb-3 flex items-center justify-between">
                  <span className="font-display text-sm font-800 text-cream">2048</span>
                  <div className="flex gap-1.5 text-[10px]">
                    <span className="rounded-md bg-panel px-2 py-1 text-taupe">Score <b className="text-cream">3.2k</b></span>
                    <span className="rounded-md bg-panel px-2 py-1 text-taupe">🏆 <b className="text-gold">2048</b></span>
                  </div>
                </div>
                <div className="flex flex-1 items-center justify-center">
                  <MiniBoard />
                </div>
                <p className="mt-3 text-center text-[11px] text-cream/40">Swipe to merge matching tiles</p>
                <div className="mt-2 grid grid-cols-4 gap-1.5">
                  {['↩', '⇄', '💣', '❄'].map((p, i) => (
                    <div key={i} className="rounded-lg bg-panel py-2 text-center text-[13px] text-cream/80">{p}</div>
                  ))}
                </div>
              </div>
            </PhoneFrame>
          </motion.div>
        </motion.div>
      </div>
    </section>
  )
}

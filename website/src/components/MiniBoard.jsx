import { useEffect, useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { TILES } from '../data'

const colorFor = (v) => TILES.find((t) => t.v === v) || { bg: '#3a3644', fg: '#f6f2e9' }

// A small, animated 4x4 board that keeps merging upward — built from the
// game's real tile colors. Purely decorative.
const FRAMES = [
  [0, 2, 0, 4, 2, 0, 4, 0, 0, 8, 0, 16, 4, 0, 32, 0],
  [0, 0, 4, 4, 0, 4, 0, 8, 8, 0, 16, 16, 0, 32, 0, 64],
  [0, 0, 0, 8, 0, 0, 8, 16, 0, 16, 32, 0, 64, 0, 128, 256],
  [0, 0, 0, 16, 0, 0, 64, 128, 0, 256, 512, 0, 1024, 0, 0, 2048],
]

export default function MiniBoard() {
  const [i, setI] = useState(0)
  useEffect(() => {
    const id = setInterval(() => setI((p) => (p + 1) % FRAMES.length), 1600)
    return () => clearInterval(id)
  }, [])
  const grid = FRAMES[i]

  return (
    <div className="w-full rounded-3xl border border-white/10 bg-board p-3 shadow-2xl">
      <div className="grid grid-cols-4 gap-2">
        {grid.map((v, idx) => {
          const c = colorFor(v)
          return (
            <div key={idx} className="relative aspect-square">
              <div className="absolute inset-0 rounded-[0.7rem] bg-empty" />
              <AnimatePresence>
                {v > 0 && (
                  <motion.div
                    key={`${i}-${idx}-${v}`}
                    className="tile absolute inset-0 text-[clamp(0.8rem,2.4vw,1.15rem)]"
                    style={{ background: c.bg, color: c.fg }}
                    initial={{ scale: 0.4, opacity: 0 }}
                    animate={{ scale: 1, opacity: 1 }}
                    exit={{ scale: 0.4, opacity: 0 }}
                    transition={{ duration: 0.35, ease: [0.21, 0.6, 0.35, 1] }}
                  >
                    {v}
                  </motion.div>
                )}
              </AnimatePresence>
            </div>
          )
        })}
      </div>
    </div>
  )
}

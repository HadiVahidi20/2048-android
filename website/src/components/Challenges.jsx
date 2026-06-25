import Reveal from './Reveal'
import { CHALLENGES, DIFFICULTIES } from '../data'

export default function Challenges() {
  return (
    <section id="daily" className="relative py-24 sm:py-32">
      <div className="blob right-1/4 top-1/4 h-[320px] w-[320px] bg-green/20" />
      <div className="container-x relative z-10">
        <Reveal className="mx-auto max-w-2xl text-center">
          <span className="chip">A fresh run every day</span>
          <h2 className="display mt-5 text-[clamp(2rem,4.5vw,3.2rem)] font-800 leading-tight">
            Daily challenges &<br /><span className="text-grad-cool">difficulty modes.</span>
          </h2>
          <p className="mt-5 text-cream/60">Four tiers of multi-stage challenge chains rotate daily, each with score and tile goals, move caps and power-up limits — clear them for bonus coins.</p>
        </Reveal>

        {/* Daily challenge tiers */}
        <div className="mt-14 grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
          {CHALLENGES.map((c, i) => (
            <Reveal key={c.tier} delay={(i % 4) * 0.07}>
              <div className="card-surface flex h-full flex-col p-6">
                <div className="flex items-center justify-between">
                  <span className="font-display text-lg font-700">{c.tier}</span>
                  <span className="chip !px-2.5 !py-1 text-[11px]">{c.board}</span>
                </div>
                <p className="mt-3 flex-1 text-sm leading-relaxed text-cream/60">{c.text}</p>
                <div className="mt-5 inline-flex items-center gap-2 text-sm font-700 text-gold">
                  <span className="grid h-6 w-6 place-items-center rounded-full bg-gold/20 text-xs">¢</span>
                  +{c.coins} coins
                </div>
              </div>
            </Reveal>
          ))}
        </div>

        {/* Difficulty modes */}
        <Reveal delay={0.1} className="mt-10">
          <div className="card-surface grid gap-px overflow-hidden sm:grid-cols-3">
            {DIFFICULTIES.map((d) => (
              <div key={d.name} className="bg-white/[0.02] p-6">
                <div className="flex items-center gap-2.5">
                  <span className="h-2.5 w-2.5 rounded-full" style={{ background: d.accent }} />
                  <span className="font-display text-base font-700">{d.name}</span>
                </div>
                <p className="mt-2 text-sm leading-relaxed text-cream/55">{d.text}</p>
              </div>
            ))}
          </div>
        </Reveal>
      </div>
    </section>
  )
}

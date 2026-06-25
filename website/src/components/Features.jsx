import Reveal from './Reveal'
import { FEATURES } from '../data'

export default function Features() {
  return (
    <section id="features" className="relative py-24 sm:py-32">
      <div className="container-x relative z-10">
        <Reveal className="mx-auto max-w-2xl text-center">
          <span className="chip">Built around power-ups</span>
          <h2 className="display mt-5 text-[clamp(2rem,4.5vw,3.2rem)] font-800 leading-tight">
            Classic 2048,<br /><span className="text-grad">with a toolkit.</span>
          </h2>
          <p className="mt-5 text-cream/60">Every feature exists to give you one more way out of a tight board — and one more reason to push for the next tile.</p>
        </Reveal>

        <div className="mt-14 grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {FEATURES.map((f, i) => (
            <Reveal key={f.title} delay={(i % 3) * 0.08}>
              <div className="card-surface group h-full p-6 transition-colors duration-300 hover:border-white/20">
                <div
                  className="grid h-12 w-12 place-items-center rounded-2xl text-2xl"
                  style={{ background: `linear-gradient(135deg, ${f.accent}33, ${f.accent}1a)` }}
                >
                  {f.emoji}
                </div>
                <h3 className="mt-5 font-display text-lg font-700">{f.title}</h3>
                <p className="mt-2 text-sm leading-relaxed text-cream/60">{f.text}</p>
              </div>
            </Reveal>
          ))}
        </div>
      </div>
    </section>
  )
}

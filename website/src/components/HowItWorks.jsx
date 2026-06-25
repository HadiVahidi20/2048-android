import Reveal from './Reveal'
import PhoneFrame from './PhoneFrame'
import PlayButton from './PlayButton'
import { STEPS } from '../data'

export default function HowItWorks() {
  return (
    <section id="how" className="relative py-24 sm:py-32">
      <div className="container-x relative z-10 grid items-center gap-14 lg:grid-cols-2">
        <Reveal className="order-2 flex justify-center lg:order-1">
          <div className="relative">
            <div className="absolute inset-0 -z-10 m-auto h-72 w-72 rounded-full bg-green/20 blur-3xl" />
            <PhoneFrame src="/assets/shots/06_howto.png" alt="How to play NumberMerge 2048" className="rotate-[-4deg]" />
          </div>
        </Reveal>

        <div className="order-1 lg:order-2">
          <Reveal>
            <span className="chip">How to play</span>
            <h2 className="display mt-5 text-[clamp(2rem,4.5vw,3.2rem)] font-800 leading-tight">
              Four swipes from<br /><span className="text-grad">2048.</span>
            </h2>
          </Reveal>

          <div className="mt-10 space-y-3">
            {STEPS.map((s, i) => (
              <Reveal key={s.n} delay={i * 0.08}>
                <div className="flex gap-4 rounded-2xl border border-white/10 bg-white/[0.03] p-4">
                  <span className="font-display text-2xl font-800 text-grad">{s.n}</span>
                  <div>
                    <h3 className="font-600">{s.title}</h3>
                    <p className="mt-1 text-sm text-cream/55">{s.text}</p>
                  </div>
                </div>
              </Reveal>
            ))}
          </div>

          <Reveal delay={0.2}>
            <PlayButton className="btn btn-primary mt-9" label="Download free on Google Play" />
          </Reveal>
        </div>
      </div>
    </section>
  )
}

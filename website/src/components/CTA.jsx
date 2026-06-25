import Reveal from './Reveal'
import PlayButton from './PlayButton'

export default function CTA() {
  return (
    <section className="relative py-24 sm:py-32">
      <div className="container-x relative z-10">
        <Reveal>
          <div className="card-surface relative overflow-hidden px-6 py-16 text-center sm:px-12">
            <div className="blob left-1/2 top-0 h-72 w-72 -translate-x-1/2 bg-orange/40" />
            <div className="relative z-10">
              <img src="/assets/icon.png" alt="" className="mx-auto h-20 w-20 rounded-3xl shadow-2xl" />
              <h2 className="display mx-auto mt-7 max-w-2xl text-[clamp(2rem,5vw,3.4rem)] font-800 leading-tight">
                Chase the <span className="text-grad">2048</span> tile today.
              </h2>
              <p className="mx-auto mt-5 max-w-md text-cream/60">Four power-ups, three board sizes, three difficulties and a new daily challenge — all free, all offline.</p>
              <div className="mt-9 flex flex-wrap justify-center gap-3">
                <PlayButton />
                <a href="#features" className="btn btn-ghost">See the power-ups</a>
              </div>
            </div>
          </div>
        </Reveal>
      </div>
    </section>
  )
}

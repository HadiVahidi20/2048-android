import { useLenis } from './lib/useLenis'
import Nav from './components/Nav'
import Hero from './components/Hero'
import Features from './components/Features'
import ScrollShowcase from './components/ScrollShowcase'
import HowItWorks from './components/HowItWorks'
import Challenges from './components/Challenges'
import Stats from './components/Stats'
import CTA from './components/CTA'
import Footer from './components/Footer'

export default function App() {
  useLenis()
  return (
    <div className="relative">
      <Nav />
      <main>
        <Hero />
        <Features />
        <ScrollShowcase />
        <HowItWorks />
        <Challenges />
        <Stats />
        <CTA />
      </main>
      <Footer />
    </div>
  )
}

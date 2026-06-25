import { PLAY_URL } from '../data'

// "Get it on Google Play" — a simple external link (the app is in testing).
export default function PlayButton({ className = 'btn btn-primary', label = 'Get it on Google Play' }) {
  return (
    <a href={PLAY_URL} target="_blank" rel="noopener" className={className}>
      <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
        <path d="M3.6 2.3 13 12 3.6 21.7c-.3-.2-.5-.6-.5-1V3.3c0-.4.2-.8.5-1Zm10.9 7.6 2.9 1.7c1 .6 1 1.9 0 2.5l-2.9 1.7L11.6 12l2.9-2.1ZM5 1.6l9.6 5.6-2.3 2.3L5 1.6Zm0 20.8 7.3-7.9 2.3 2.3L5 22.4Z" />
      </svg>
      {label}
    </a>
  )
}

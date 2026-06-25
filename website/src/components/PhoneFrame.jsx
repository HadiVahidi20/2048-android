// A polished phone mockup. Pass a screenshot src or children.
export default function PhoneFrame({ src, alt = '', className = '', children }) {
  return (
    <div className={`phone-shell relative aspect-[9/19.5] w-[260px] p-3 sm:w-[300px] ${className}`}>
      {/* notch */}
      <div className="absolute left-1/2 top-3 z-20 h-5 w-24 -translate-x-1/2 rounded-full bg-black/70" />
      <div className="phone-screen relative h-full w-full">
        {src ? (
          <img src={src} alt={alt} className="h-full w-full object-cover" draggable="false" />
        ) : (
          children
        )}
      </div>
    </div>
  )
}

export function Spinner({ label }: { label?: string }) {
  return (
    <div className="flex items-center gap-3 py-8 text-ink-soft">
      <span className="h-5 w-5 animate-spin rounded-full border-2 border-ink-faint border-t-brass-deep" />
      {label && <span className="text-sm">{label}</span>}
    </div>
  )
}

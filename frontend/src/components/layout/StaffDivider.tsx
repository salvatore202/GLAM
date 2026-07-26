interface StaffDividerProps {
  label?: string
  tone?: 'onDark' | 'onLight'
}

/**
 * L'elemento firma del sito: un rigo musicale a 5 linee usato come
 * separatore strutturale tra sezioni, non come decorazione. Il pentagramma
 * e' l'unita' di misura di qualunque spartito: qui segna letteralmente dove
 * finisce un "movimento" della pagina e ne inizia un altro.
 */
export function StaffDivider({ label, tone = 'onLight' }: StaffDividerProps) {
  const lineColor = tone === 'onDark' ? 'bg-parchment/25' : 'bg-ink/15'
  const labelColor = tone === 'onDark' ? 'text-brass-pale' : 'text-ink-soft'
  const bgColor = tone === 'onDark' ? 'bg-stage' : 'bg-paper'

  return (
    <div className="relative my-2 flex flex-col gap-[5px] py-3" role="separator" aria-hidden={!label}>
      {[0, 1, 2, 3, 4].map((i) => (
        <span key={i} className={`h-px w-full ${lineColor}`} />
      ))}
      {label && (
        <span
          className={`absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 ${bgColor} px-4 font-display text-xs uppercase tracking-[0.3em] ${labelColor}`}
        >
          {label}
        </span>
      )}
    </div>
  )
}

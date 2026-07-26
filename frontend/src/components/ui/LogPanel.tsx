import { useState } from 'react'
import { ChevronRight, Terminal } from 'lucide-react'

/**
 * Control.GestioneLezioni (il backend Java originale) comunica l'esito
 * stampando su console: questo pannello mostra esattamente quel testo,
 * catturato lato server ad ogni chiamata. Non e' decorativo: e' l'unico modo
 * in cui il messaggio originale del backend arriva fino a qui.
 */
export function LogPanel({ log }: { log: string }) {
  const [open, setOpen] = useState(false)
  if (!log) return null

  return (
    <div className="rounded-lg border border-ink/10 bg-stage-deep">
      <button
        type="button"
        onClick={() => setOpen((v) => !v)}
        className="flex w-full items-center gap-2 px-3.5 py-2 text-left text-xs font-medium text-parchment/70 hover:text-parchment"
      >
        <ChevronRight className={`h-3.5 w-3.5 transition-transform ${open ? 'rotate-90' : ''}`} />
        <Terminal className="h-3.5 w-3.5" />
        Console del backend originale
      </button>
      {open && (
        <pre className="overflow-x-auto whitespace-pre-wrap break-words border-t border-parchment/10 px-3.5 py-3 font-mono text-[11px] leading-relaxed text-brass-pale/90">
          {log}
        </pre>
      )}
    </div>
  )
}

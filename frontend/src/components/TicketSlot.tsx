import { Check } from 'lucide-react'

const PREZZI: Record<string, number> = { BASE: 15, INTERMEDIO: 30 }

function formatData(iso: string): string {
  const d = new Date(`${iso}T00:00:00`)
  return d.toLocaleDateString('it-IT', { weekday: 'short', day: 'numeric', month: 'short' })
}

interface TicketSlotProps {
  data: string
  ora: string
  livello: string
  selected?: boolean
  disabled?: boolean
  onClick?: () => void
}

/**
 * L'elemento firma dell'interfaccia studente: ogni lezione prenotabile e'
 * presentata come lo stacco di un biglietto da concerto, con tanto di
 * "perforazione" tra il blocco data/livello e il blocco orario/prezzo.
 * Prenotare una lezione diventa, visivamente, staccare un biglietto.
 */
export function TicketSlot({ data, ora, livello, selected, disabled, onClick }: TicketSlotProps) {
  const prezzo = PREZZI[livello.toUpperCase()] ?? 15
  const oraBreve = ora.slice(0, 5)

  return (
    <button
      type="button"
      disabled={disabled}
      onClick={onClick}
      style={{ '--notch-bg': 'var(--color-paper)' } as React.CSSProperties}
      className={`ticket-notch group relative flex w-full items-stretch overflow-hidden rounded-xl border text-left transition-all duration-150 disabled:cursor-not-allowed disabled:opacity-40 ${
        selected
          ? 'border-brass-deep bg-brass-pale shadow-[0_0_0_2px_var(--color-brass)]'
          : 'border-ink/12 bg-parchment hover:border-brass/50 hover:shadow-md'
      }`}
    >
      <div className="flex flex-1 flex-col gap-1 px-4 py-3.5">
        <span className="text-[11px] font-semibold uppercase tracking-wider text-ink-faint">
          {formatData(data)}
        </span>
        <span className="font-display text-base font-medium text-ink">{livello}</span>
      </div>

      <div
        className="relative flex w-[86px] shrink-0 flex-col items-center justify-center gap-0.5 border-l border-dashed border-ink/20 py-3.5 font-mono"
        aria-hidden
      >
        <span className="text-sm font-semibold text-ink">{oraBreve}</span>
        <span className="text-[11px] text-ink-soft">&euro;{prezzo.toFixed(2)}</span>
      </div>

      {selected && (
        <span className="absolute right-2 top-2 flex h-5 w-5 items-center justify-center rounded-full bg-velvet text-parchment">
          <Check className="h-3 w-3" strokeWidth={3} />
        </span>
      )}
    </button>
  )
}

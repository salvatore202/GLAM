import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { GraduationCap, Piano, Search } from 'lucide-react'
import { StaffDivider } from '../components/layout/StaffDivider'
import { Spinner } from '../components/ui/Spinner'
import { api } from '../api/client'
import type { StrumentoConMaestri } from '../api/types'

export function HomePage() {
  const [strumenti, setStrumenti] = useState<StrumentoConMaestri[] | null>(null)

  useEffect(() => {
    api.strumenti().then(setStrumenti).catch(() => setStrumenti([]))
  }, [])

  return (
    <div>
      <section className="relative overflow-hidden bg-stage">
        <div
          className="pointer-events-none absolute inset-0 opacity-60"
          style={{
            background:
              'radial-gradient(60% 50% at 50% 0%, color-mix(in srgb, var(--color-brass) 18%, transparent), transparent)',
          }}
        />
        <div className="relative mx-auto flex max-w-4xl flex-col items-center px-6 py-24 text-center sm:py-32">
          <span className="font-mono text-xs uppercase tracking-[0.35em] text-brass-pale/80">
            Gestione Lezioni A Maestri
          </span>
          <h1 className="mt-6 max-w-2xl font-display text-4xl font-medium leading-[1.1] text-parchment sm:text-6xl">
            Prenota la tua prossima lezione di musica.
          </h1>
          <p className="mt-6 max-w-lg text-balance text-base leading-relaxed text-parchment/70">
            Pianoforte, chitarra, violino, batteria, basso, sassofono. Scegli un Maestro, trova un
            orario libero, stacca il biglietto.
          </p>
          <div className="mt-10 flex flex-wrap items-center justify-center gap-3">
            <Link
              to="/studente"
              className="inline-flex items-center gap-2 rounded-full bg-brass px-6 py-3 text-sm font-semibold text-stage-deep transition-colors hover:bg-brass-deep"
            >
              <GraduationCap className="h-4 w-4" />
              Sono uno Studente
            </Link>
            <Link
              to="/maestro"
              className="inline-flex items-center gap-2 rounded-full border border-parchment/25 px-6 py-3 text-sm font-semibold text-parchment transition-colors hover:bg-parchment/10"
            >
              <Piano className="h-4 w-4" />
              Sono un Maestro
            </Link>
          </div>
          <Link
            to="/catalogo"
            className="mt-6 inline-flex items-center gap-1.5 text-xs font-medium text-parchment/50 transition-colors hover:text-parchment/80"
          >
            <Search className="h-3.5 w-3.5" />
            Oppure sfoglia il catalogo senza accedere
          </Link>
        </div>
        <StaffDivider tone="onDark" />
      </section>

      <section className="mx-auto max-w-5xl px-6 py-16">
        <h2 className="text-center font-display text-2xl text-ink">Strumenti insegnati</h2>
        <p className="mx-auto mt-2 max-w-md text-center text-sm text-ink-soft">
          Ogni strumento e' insegnato da uno o piu' Maestri registrati sulla piattaforma.
        </p>

        {strumenti === null ? (
          <Spinner label="Carico gli strumenti disponibili..." />
        ) : (
          <div className="mt-10 grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-6">
            {strumenti.map((s) => (
              <div
                key={s.nome}
                className="flex flex-col items-center gap-2 rounded-xl border border-ink/10 bg-parchment px-3 py-5 text-center"
              >
                <span className="font-display text-sm font-medium text-ink">{s.nome}</span>
                <span className="text-xs text-ink-faint">
                  {s.maestri.length === 0
                    ? 'Nessun Maestro'
                    : `${s.maestri.length} Maestro${s.maestri.length > 1 ? 'i' : ''}`}
                </span>
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  )
}

import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { ChevronDown, Phone, ArrowRight } from 'lucide-react'
import { Spinner } from '../components/ui/Spinner'
import { Alert } from '../components/ui/Alert'
import { Select } from '../components/ui/Select'
import { TicketSlot } from '../components/TicketSlot'
import { StaffDivider } from '../components/layout/StaffDivider'
import { api, ApiClientError } from '../api/client'
import type { DisponibilitaMaestro, Maestro, StrumentoConMaestri } from '../api/types'

export function CatalogoPage() {
  const [strumenti, setStrumenti] = useState<StrumentoConMaestri[]>([])
  const [maestri, setMaestri] = useState<Maestro[]>([])
  const [filtro, setFiltro] = useState<string>('TUTTI')
  const [loading, setLoading] = useState(true)
  const [errore, setErrore] = useState<string | null>(null)
  const [espanso, setEspanso] = useState<number | null>(null)
  const [disponibilita, setDisponibilita] = useState<Record<number, DisponibilitaMaestro>>({})

  useEffect(() => {
    Promise.all([api.strumenti(), api.maestri()])
      .then(([s, m]) => {
        setStrumenti(s)
        setMaestri(m)
      })
      .catch((e) => setErrore(e instanceof ApiClientError ? e.message : 'Errore di rete'))
      .finally(() => setLoading(false))
  }, [])

  async function toggleEspandi(m: Maestro) {
    if (espanso === m.id) {
      setEspanso(null)
      return
    }
    setEspanso(m.id)
    if (!disponibilita[m.id]) {
      try {
        const d = await api.disponibilitaMaestro(m.id)
        setDisponibilita((prev) => ({ ...prev, [m.id]: d }))
      } catch (e) {
        setErrore(e instanceof ApiClientError ? e.message : 'Errore di rete')
      }
    }
  }

  const maestriFiltrati =
    filtro === 'TUTTI' ? maestri : maestri.filter((m) => m.strumenti?.includes(filtro))

  return (
    <div className="mx-auto max-w-4xl px-6 py-14">
      <header className="mb-10 text-center">
        <span className="font-mono text-xs uppercase tracking-[0.3em] text-brass-deep">Catalogo pubblico</span>
        <h1 className="mt-3 font-display text-3xl text-ink">Maestri e strumenti</h1>
        <p className="mx-auto mt-2 max-w-md text-sm text-ink-soft">
          Consulta le disponibilita' senza accedere. Per prenotare una lezione serve un account Studente.
        </p>
      </header>

      {errore && (
        <div className="mb-6">
          <Alert kind="error">{errore}</Alert>
        </div>
      )}

      {loading ? (
        <Spinner label="Carico il catalogo..." />
      ) : (
        <>
          <div className="mb-8 max-w-xs">
            <Select label="Filtra per strumento" value={filtro} onChange={(e) => setFiltro(e.target.value)}>
              <option value="TUTTI">Tutti gli strumenti</option>
              {strumenti.map((s) => (
                <option key={s.nome} value={s.nome}>
                  {s.nome}
                </option>
              ))}
            </Select>
          </div>

          <div className="flex flex-col gap-4">
            {maestriFiltrati.map((m) => (
              <div key={m.id} className="overflow-hidden rounded-2xl border border-ink/10 bg-parchment">
                <button
                  type="button"
                  onClick={() => toggleEspandi(m)}
                  className="flex w-full items-center justify-between gap-4 px-5 py-4 text-left"
                >
                  <div>
                    <p className="font-display text-lg text-ink">
                      {m.nome} {m.cognome}
                    </p>
                    <p className="mt-1 flex flex-wrap gap-1.5">
                      {(m.strumenti ?? []).map((s) => (
                        <span
                          key={s}
                          className="rounded-full bg-velvet-pale px-2.5 py-0.5 text-[11px] font-medium text-velvet-deep"
                        >
                          {s}
                        </span>
                      ))}
                    </p>
                  </div>
                  <ChevronDown
                    className={`h-4 w-4 shrink-0 text-ink-faint transition-transform ${espanso === m.id ? 'rotate-180' : ''}`}
                  />
                </button>

                {espanso === m.id && (
                  <div className="border-t border-ink/10 px-5 py-5">
                    <p className="mb-3 flex items-center gap-1.5 text-xs text-ink-faint">
                      <Phone className="h-3 w-3" /> {m.numeroDiTelefono}
                    </p>
                    {!disponibilita[m.id] ? (
                      <Spinner label="Carico la disponibilita'..." />
                    ) : disponibilita[m.id].giorni.length === 0 ? (
                      <p className="text-sm text-ink-soft">Nessun giorno di disponibilita' inserito al momento.</p>
                    ) : (
                      <div className="flex flex-col gap-5">
                        {disponibilita[m.id].giorni.map((g) => (
                          <div key={g.idData}>
                            <p className="mb-2 text-xs font-semibold uppercase tracking-wider text-ink-faint">
                              {new Date(`${g.data}T00:00:00`).toLocaleDateString('it-IT', {
                                weekday: 'long',
                                day: 'numeric',
                                month: 'long',
                              })}
                            </p>
                            <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
                              {(g.lezioniDisponibili ?? []).length === 0 ? (
                                <p className="col-span-full text-sm text-ink-faint">Tutti gli slot sono occupati.</p>
                              ) : (
                                g.lezioniDisponibili!.map((l) => (
                                  <TicketSlot key={l.idLezione} data={g.data} ora={l.ora} livello={l.livello} disabled />
                                ))
                              )}
                            </div>
                          </div>
                        ))}
                        <Link
                          to="/studente"
                          className="inline-flex items-center gap-1 self-start text-xs font-semibold text-brass-deep underline underline-offset-2"
                        >
                          Accedi come Studente per prenotare uno di questi slot
                          <ArrowRight className="h-3 w-3" />
                        </Link>
                      </div>
                    )}
                  </div>
                )}
              </div>
            ))}
          </div>
        </>
      )}

      <StaffDivider />
    </div>
  )
}

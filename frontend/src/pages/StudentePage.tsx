import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { GraduationCap, LogOut, ShoppingBag, Trash2, X } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { api, ApiClientError } from '../api/client'
import { Card } from '../components/ui/Card'
import { Button } from '../components/ui/Button'
import { TextField } from '../components/ui/TextField'
import { Select } from '../components/ui/Select'
import { Alert } from '../components/ui/Alert'
import { Spinner } from '../components/ui/Spinner'
import { LogPanel } from '../components/ui/LogPanel'
import { TicketSlot } from '../components/TicketSlot'
import type {
  DisponibilitaMaestro,
  Livello,
  Maestro,
  PrenotazioneStudente,
  RichiestaPrenotazione,
  RisultatoPrenotazione,
  StrumentoConMaestri,
} from '../api/types'

export function StudentePage() {
  const { studente, loginStudente, logoutStudente } = useAuth()
  return (
    <div className="mx-auto max-w-4xl px-6 py-14">
      {studente ? <DashboardStudente /> : <LoginStudente onLogin={loginStudente} />}
      {studente && (
        <div className="mt-10 flex justify-center">
          <Button variant="ghost" size="sm" icon={<LogOut className="h-3.5 w-3.5" />} onClick={logoutStudente}>
            Esci dall'area Studente
          </Button>
        </div>
      )}
    </div>
  )
}

function LoginStudente({ onLogin }: { onLogin: ReturnType<typeof useAuth>['loginStudente'] }) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [errore, setErrore] = useState<string | null>(null)

  async function submit(e: FormEvent) {
    e.preventDefault()
    setLoading(true)
    setErrore(null)
    try {
      const profilo = await api.loginStudente(username, password)
      onLogin({ username, password, profilo })
    } catch (e) {
      setErrore(e instanceof ApiClientError ? e.message : 'Errore di rete')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <header className="mb-8 text-center">
        <GraduationCap className="mx-auto h-6 w-6 text-brass-deep" />
        <h1 className="mt-3 font-display text-3xl text-ink">Area Studente</h1>
        <p className="mt-2 text-sm text-ink-soft">Accedi per prenotare o gestire le tue lezioni.</p>
      </header>
      <Card className="mx-auto max-w-sm p-6">
        <form onSubmit={submit} className="flex flex-col gap-4">
          <TextField
            label="Username"
            required
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="vleinaudi"
          />
          <TextField
            label="Password"
            type="password"
            required
            minLength={8}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
          {errore && <Alert kind="error">{errore}</Alert>}
          <Button type="submit" loading={loading} className="mt-1 w-full">
            Accedi
          </Button>
        </form>
      </Card>
    </div>
  )
}

type Tab = 'prenota' | 'mie'

function DashboardStudente() {
  const { studente } = useAuth()
  const [tab, setTab] = useState<Tab>('prenota')
  if (!studente) return null

  return (
    <div>
      <header className="mb-8 text-center">
        <h1 className="font-display text-3xl text-ink">
          Ciao, {studente.profilo.nome} {studente.profilo.cognome}
        </h1>
      </header>

      <div className="mb-8 flex justify-center gap-2">
        <button
          onClick={() => setTab('prenota')}
          className={`rounded-full px-4 py-2 text-xs font-semibold transition-colors ${
            tab === 'prenota' ? 'bg-stage text-parchment' : 'bg-ink/5 text-ink-soft hover:bg-ink/10'
          }`}
        >
          Prenota una lezione
        </button>
        <button
          onClick={() => setTab('mie')}
          className={`rounded-full px-4 py-2 text-xs font-semibold transition-colors ${
            tab === 'mie' ? 'bg-stage text-parchment' : 'bg-ink/5 text-ink-soft hover:bg-ink/10'
          }`}
        >
          Le mie prenotazioni
        </button>
      </div>

      {tab === 'prenota' ? <PrenotaLezione /> : <MiePrenotazioni />}
    </div>
  )
}

interface CartItem {
  key: string
  strumento: string
  nomeMaestro: string
  cognomeMaestro: string
  maestroLabel: string
  data: string
  ora: string
  livello: Livello
}

function PrenotaLezione() {
  const { studente } = useAuth()
  const [strumenti, setStrumenti] = useState<StrumentoConMaestri[]>([])
  const [strumento, setStrumento] = useState<string>('')
  const [maestri, setMaestri] = useState<Maestro[]>([])
  const [maestroSelezionato, setMaestroSelezionato] = useState<Maestro | null>(null)
  const [disponibilita, setDisponibilita] = useState<DisponibilitaMaestro | null>(null)
  const [cart, setCart] = useState<CartItem[]>([])
  const [caricoMaestri, setCaricoMaestri] = useState(false)
  const [caricoDisponibilita, setCaricoDisponibilita] = useState(false)
  const [errore, setErrore] = useState<string | null>(null)
  const [inviando, setInviando] = useState(false)
  const [risultato, setRisultato] = useState<RisultatoPrenotazione | null>(null)

  useEffect(() => {
    api.strumenti().then(setStrumenti).catch(() => setStrumenti([]))
  }, [])

  async function scegliStrumento(nome: string) {
    setStrumento(nome)
    setMaestroSelezionato(null)
    setDisponibilita(null)
    setMaestri([])
    if (!nome) return
    setCaricoMaestri(true)
    setErrore(null)
    try {
      const risultati = await api.cercaMaestri(nome)
      setMaestri(risultati)
    } catch (e) {
      setErrore(e instanceof ApiClientError ? e.message : 'Errore di rete')
    } finally {
      setCaricoMaestri(false)
    }
  }

  async function scegliMaestro(m: Maestro) {
    setMaestroSelezionato(m)
    setCaricoDisponibilita(true)
    setDisponibilita(null)
    try {
      const d = await api.disponibilitaMaestro(m.id)
      setDisponibilita(d)
    } catch (e) {
      setErrore(e instanceof ApiClientError ? e.message : 'Errore di rete')
    } finally {
      setCaricoDisponibilita(false)
    }
  }

  function toggleSlot(data: string, ora: string, livelloIniziale: Livello) {
    if (!maestroSelezionato) return
    const key = `${maestroSelezionato.id}-${data}-${ora}`
    setCart((prev) => {
      const esiste = prev.find((c) => c.key === key)
      if (esiste) return prev.filter((c) => c.key !== key)
      return [
        ...prev,
        {
          key,
          strumento,
          nomeMaestro: maestroSelezionato.nome,
          cognomeMaestro: maestroSelezionato.cognome,
          maestroLabel: `${maestroSelezionato.nome} ${maestroSelezionato.cognome}`,
          data,
          ora,
          livello: livelloIniziale,
        },
      ]
    })
  }

  function aggiornaLivelloCarrello(key: string, livello: Livello) {
    setCart((prev) => prev.map((c) => (c.key === key ? { ...c, livello } : c)))
  }

  function rimuoviDalCarrello(key: string) {
    setCart((prev) => prev.filter((c) => c.key !== key))
  }

  const totale = useMemo(() => cart.reduce((sum, c) => sum + (c.livello === 'BASE' ? 15 : 30), 0), [cart])

  async function confermaPrenotazione() {
    if (!studente || cart.length === 0) return
    setInviando(true)
    setErrore(null)
    setRisultato(null)
    try {
      const richieste: RichiestaPrenotazione[] = cart.map((c) => ({
        strumento: c.strumento,
        nomeMaestro: c.nomeMaestro,
        cognomeMaestro: c.cognomeMaestro,
        data: c.data,
        ora: c.ora,
        livello: c.livello,
      }))
      const esito = await api.prenota(studente.username, studente.password, richieste)
      setRisultato(esito)
      const chiaviRiuscite = new Set(
        cart.filter((_, i) => esito.risultati[i]?.esito === 'prenotata').map((c) => c.key),
      )
      setCart((prev) => prev.filter((c) => !chiaviRiuscite.has(c.key)))
      if (maestroSelezionato) await scegliMaestro(maestroSelezionato)
    } catch (e) {
      setErrore(e instanceof ApiClientError ? e.message : 'Errore di rete')
    } finally {
      setInviando(false)
    }
  }

  return (
    <div className="grid gap-6 lg:grid-cols-[1fr_320px]">
      <div className="flex flex-col gap-6">
        <Card className="p-6">
          <h2 className="mb-4 font-display text-lg text-ink">1. Scegli uno strumento</h2>
          <div className="flex flex-wrap gap-2">
            {strumenti.map((s) => (
              <button
                key={s.nome}
                onClick={() => scegliStrumento(s.nome)}
                disabled={s.maestri.length === 0}
                className={`rounded-full border px-3.5 py-1.5 text-xs font-semibold transition-colors disabled:cursor-not-allowed disabled:opacity-40 ${
                  strumento === s.nome
                    ? 'border-brass-deep bg-brass text-stage-deep'
                    : 'border-ink/15 bg-white/60 text-ink-soft hover:border-brass/50'
                }`}
              >
                {s.nome}
              </button>
            ))}
          </div>
        </Card>

        {strumento && (
          <Card className="p-6">
            <h2 className="mb-4 font-display text-lg text-ink">2. Scegli un Maestro di {strumento}</h2>
            {caricoMaestri ? (
              <Spinner label="Cerco i Maestri..." />
            ) : maestri.length === 0 ? (
              <p className="text-sm text-ink-soft">Nessun Maestro insegna questo strumento al momento.</p>
            ) : (
              <div className="flex flex-wrap gap-2">
                {maestri.map((m) => (
                  <button
                    key={m.id}
                    onClick={() => scegliMaestro(m)}
                    className={`rounded-full border px-3.5 py-1.5 text-xs font-semibold transition-colors ${
                      maestroSelezionato?.id === m.id
                        ? 'border-velvet-deep bg-velvet text-parchment'
                        : 'border-ink/15 bg-white/60 text-ink-soft hover:border-velvet/50'
                    }`}
                  >
                    {m.nome} {m.cognome}
                  </button>
                ))}
              </div>
            )}
          </Card>
        )}

        {maestroSelezionato && (
          <Card className="p-6">
            <h2 className="mb-4 font-display text-lg text-ink">
              3. Scegli uno o piu' orari con {maestroSelezionato.nome} {maestroSelezionato.cognome}
            </h2>
            {caricoDisponibilita ? (
              <Spinner label="Carico la disponibilita'..." />
            ) : !disponibilita || disponibilita.giorni.length === 0 ? (
              <p className="text-sm text-ink-soft">Nessun giorno di disponibilita' inserito al momento.</p>
            ) : (
              <div className="flex flex-col gap-5">
                {disponibilita.giorni.map((g) => (
                  <div key={g.idData}>
                    <p className="mb-2 text-xs font-semibold uppercase tracking-wider text-ink-faint">
                      {new Date(`${g.data}T00:00:00`).toLocaleDateString('it-IT', {
                        weekday: 'long',
                        day: 'numeric',
                        month: 'long',
                      })}
                    </p>
                    {(g.lezioniDisponibili ?? []).length === 0 ? (
                      <p className="text-sm text-ink-faint">Tutti gli slot sono occupati.</p>
                    ) : (
                      <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
                        {g.lezioniDisponibili!.map((l) => (
                          <TicketSlot
                            key={l.idLezione}
                            data={g.data}
                            ora={l.ora}
                            livello={l.livello}
                            selected={cart.some((c) => c.key === `${maestroSelezionato.id}-${g.data}-${l.ora}`)}
                            onClick={() => toggleSlot(g.data, l.ora, l.livello)}
                          />
                        ))}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </Card>
        )}

        {errore && <Alert kind="error">{errore}</Alert>}
      </div>

      <div className="lg:sticky lg:top-20 lg:self-start">
        <Card className="p-6">
          <h2 className="mb-4 flex items-center gap-2 font-display text-lg text-ink">
            <ShoppingBag className="h-4 w-4" /> Carrello
          </h2>
          {cart.length === 0 ? (
            <p className="text-sm text-ink-soft">Nessuno slot selezionato.</p>
          ) : (
            <div className="flex flex-col gap-3">
              {cart.map((c) => (
                <div key={c.key} className="rounded-lg border border-ink/10 bg-white/50 p-3 text-xs">
                  <div className="mb-2 flex items-start justify-between gap-2">
                    <div>
                      <p className="font-semibold text-ink">{c.strumento}</p>
                      <p className="text-ink-soft">{c.maestroLabel}</p>
                      <p className="font-mono text-ink-faint">
                        {new Date(`${c.data}T00:00:00`).toLocaleDateString('it-IT', { day: 'numeric', month: 'short' })}{' '}
                        &middot; {c.ora.slice(0, 5)}
                      </p>
                    </div>
                    <button onClick={() => rimuoviDalCarrello(c.key)} className="text-ink-faint hover:text-wine">
                      <Trash2 className="h-3.5 w-3.5" />
                    </button>
                  </div>
                  <Select
                    label="Livello"
                    value={c.livello}
                    onChange={(e) => aggiornaLivelloCarrello(c.key, e.target.value as Livello)}
                    className="!py-1.5 !text-xs"
                  >
                    <option value="BASE">BASE &mdash; &euro;15,00</option>
                    <option value="INTERMEDIO">INTERMEDIO &mdash; &euro;30,00</option>
                  </Select>
                </div>
              ))}
              <div className="flex items-center justify-between border-t border-ink/10 pt-3 text-sm font-semibold text-ink">
                <span>Totale</span>
                <span className="font-mono">&euro;{totale.toFixed(2)}</span>
              </div>
              <Button onClick={confermaPrenotazione} loading={inviando} className="w-full">
                Conferma prenotazione
              </Button>
            </div>
          )}
        </Card>

        {risultato && (
          <div className="mt-4 flex flex-col gap-3">
            {risultato.risultati.map((r, i) => (
              <Alert key={i} kind={r.esito === 'prenotata' ? 'success' : 'error'}>
                {r.strumento} con {r.maestro} &middot; {r.data} {r.ora.slice(0, 5)} &mdash;{' '}
                {r.esito === 'prenotata' ? `prenotata (€${r.costo?.toFixed(2)})` : r.motivo}
              </Alert>
            ))}
            <LogPanel log={risultato.log} />
          </div>
        )}
      </div>
    </div>
  )
}

function MiePrenotazioni() {
  const { studente } = useAuth()
  const [stato, setStato] = useState<'carico' | 'pronto' | 'errore'>('carico')
  const [prenotazioni, setPrenotazioni] = useState<PrenotazioneStudente[]>([])
  const [errore, setErrore] = useState<string | null>(null)
  const [disdicendo, setDisdicendo] = useState<number | null>(null)
  const [esitoDisdetta, setEsitoDisdetta] = useState<{ id: number; messaggio: string; log: string } | null>(null)

  function carica() {
    if (!studente) return Promise.resolve()
    return api
      .miePrenotazioni(studente.username, studente.password)
      .then((dati) => {
        setPrenotazioni(dati)
        setStato('pronto')
      })
      .catch((e: unknown) => {
        setErrore(e instanceof ApiClientError ? e.message : 'Errore di rete')
        setStato('errore')
      })
  }

  useEffect(() => {
    if (!studente) return
    api
      .miePrenotazioni(studente.username, studente.password)
      .then((dati) => {
        setPrenotazioni(dati)
        setStato('pronto')
      })
      .catch((e: unknown) => {
        setErrore(e instanceof ApiClientError ? e.message : 'Errore di rete')
        setStato('errore')
      })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  function aggiorna() {
    setStato('carico')
    carica()
  }

  async function disdici(p: PrenotazioneStudente) {
    if (!studente) return
    setDisdicendo(p.idPrenotazione)
    setEsitoDisdetta(null)
    try {
      // La tabella LEZIONE non registra per quale strumento e' stata prenotata
      // una lezione (limite del modello dati originale, non introdotto qui):
      // lo strumento serve solo come verifica "questo Maestro insegna questo
      // strumento?", quindi uno qualsiasi tra quelli insegnati dal Maestro
      // risolve correttamente la stessa lezione.
      const profiloMaestro = await api.disponibilitaMaestro(p.maestro.idMaestro)
      const strumento = profiloMaestro.maestro.strumenti?.[0]
      if (!strumento) throw new ApiClientError(500, 'Impossibile determinare uno strumento per questo Maestro')

      const esito = await api.disdici(studente.username, studente.password, {
        strumento,
        nomeMaestro: p.maestro.nome,
        cognomeMaestro: p.maestro.cognome,
        data: p.data,
        ora: p.ora,
      })
      setEsitoDisdetta({ id: p.idPrenotazione, messaggio: esito.messaggio, log: esito.log })
      await carica()
    } catch (e) {
      setErrore(e instanceof ApiClientError ? e.message : 'Errore di rete')
    } finally {
      setDisdicendo(null)
    }
  }

  const oggi = new Date().toISOString().slice(0, 10)

  return (
    <Card className="mx-auto max-w-2xl p-6">
      <div className="mb-4 flex items-center justify-between">
        <h2 className="font-display text-lg text-ink">Le tue prenotazioni</h2>
        <Button variant="secondary" size="sm" onClick={aggiorna}>Aggiorna</Button>
      </div>

      {stato === 'carico' && <Spinner label="Carico le prenotazioni..." />}
      {stato === 'errore' && errore && <Alert kind="error">{errore}</Alert>}

      {stato === 'pronto' && (
        <>
          {prenotazioni.length === 0 ? (
            <p className="text-sm text-ink-soft">Non hai ancora nessuna prenotazione.</p>
          ) : (
            <ul className="flex flex-col divide-y divide-ink/10">
              {prenotazioni.map((p) => (
                <li key={p.idPrenotazione} className="py-3">
                  <div className="flex items-center justify-between text-sm">
                    <div>
                      <p className="font-medium text-ink">
                        {p.maestro.nome} {p.maestro.cognome} &middot;{' '}
                        {new Date(`${p.data}T00:00:00`).toLocaleDateString('it-IT', { day: 'numeric', month: 'short' })}{' '}
                        <span className="font-mono text-ink-soft">{p.ora.slice(0, 5)}</span>
                      </p>
                      <p className="text-xs text-ink-faint">
                        {p.livello} &middot; &euro;{p.costo.toFixed(2)} &middot; {p.data < oggi ? 'passata' : 'in programma'}
                      </p>
                    </div>
                    {p.data >= oggi && (
                      <Button
                        variant="danger"
                        size="sm"
                        icon={<X className="h-3 w-3" />}
                        loading={disdicendo === p.idPrenotazione}
                        onClick={() => disdici(p)}
                      >
                        Disdici
                      </Button>
                    )}
                  </div>
                  {esitoDisdetta?.id === p.idPrenotazione && (
                    <div className="mt-3 flex flex-col gap-2">
                      <Alert kind="info">{esitoDisdetta.messaggio}</Alert>
                      <LogPanel log={esitoDisdetta.log} />
                    </div>
                  )}
                </li>
              ))}
            </ul>
          )}
        </>
      )}
    </Card>
  )
}

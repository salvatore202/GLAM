import { useState, type FormEvent } from 'react'
import { CalendarPlus, CalendarRange, LogOut, Piano } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { api, ApiClientError } from '../api/client'
import { Card } from '../components/ui/Card'
import { Button } from '../components/ui/Button'
import { TextField } from '../components/ui/TextField'
import { Alert } from '../components/ui/Alert'
import { Spinner } from '../components/ui/Spinner'
import { LogPanel } from '../components/ui/LogPanel'
import { StaffDivider } from '../components/layout/StaffDivider'
import type { EsitoGiorno, LezioneSvoltaMaestro } from '../api/types'

export function MaestroPage() {
  const { maestro, loginMaestro, logoutMaestro } = useAuth()
  return (
    <div className="mx-auto max-w-3xl px-6 py-14">
      {maestro ? <DashboardMaestro /> : <LoginMaestro onLogin={loginMaestro} />}
      {maestro && (
        <div className="mt-10 flex justify-center">
          <Button variant="ghost" size="sm" icon={<LogOut className="h-3.5 w-3.5" />} onClick={logoutMaestro}>
            Esci dall'area Maestro
          </Button>
        </div>
      )}
    </div>
  )
}

function LoginMaestro({ onLogin }: { onLogin: ReturnType<typeof useAuth>['loginMaestro'] }) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [errore, setErrore] = useState<string | null>(null)

  async function submit(e: FormEvent) {
    e.preventDefault()
    setLoading(true)
    setErrore(null)
    try {
      const profilo = await api.loginMaestro(email, password)
      onLogin({ email, password, profilo })
    } catch (e) {
      setErrore(e instanceof ApiClientError ? e.message : 'Errore di rete')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <header className="mb-8 text-center">
        <Piano className="mx-auto h-6 w-6 text-brass-deep" />
        <h1 className="mt-3 font-display text-3xl text-ink">Area Maestro</h1>
        <p className="mt-2 text-sm text-ink-soft">Accedi per gestire i tuoi giorni di disponibilita'.</p>
      </header>
      <Card className="mx-auto max-w-sm p-6">
        <form onSubmit={submit} className="flex flex-col gap-4">
          <TextField
            label="Email"
            type="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="paolo.ventresca@example.com"
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

type Tab = 'inserisci' | 'modifica' | 'svolte'

function DashboardMaestro() {
  const { maestro } = useAuth()
  const [tab, setTab] = useState<Tab>('inserisci')
  if (!maestro) return null

  const tabs: { id: Tab; label: string; icon: React.ReactNode }[] = [
    { id: 'inserisci', label: 'Inserisci giorno', icon: <CalendarPlus className="h-3.5 w-3.5" /> },
    { id: 'modifica', label: 'Modifica giorno', icon: <CalendarRange className="h-3.5 w-3.5" /> },
    { id: 'svolte', label: 'Lezioni prenotate', icon: <Piano className="h-3.5 w-3.5" /> },
  ]

  return (
    <div>
      <header className="mb-8 text-center">
        <span className="font-mono text-xs uppercase tracking-[0.3em] text-brass-deep">
          {maestro.profilo.strumenti?.join(' · ') || 'Maestro'}
        </span>
        <h1 className="mt-3 font-display text-3xl text-ink">
          Bentornato, {maestro.profilo.nome} {maestro.profilo.cognome}
        </h1>
      </header>

      <div className="mb-8 flex justify-center gap-2">
        {tabs.map((t) => (
          <button
            key={t.id}
            onClick={() => setTab(t.id)}
            className={`flex items-center gap-1.5 rounded-full px-4 py-2 text-xs font-semibold transition-colors ${
              tab === t.id ? 'bg-stage text-parchment' : 'bg-ink/5 text-ink-soft hover:bg-ink/10'
            }`}
          >
            {t.icon}
            {t.label}
          </button>
        ))}
      </div>

      {tab === 'inserisci' && <InserisciGiorno />}
      {tab === 'modifica' && <ModificaGiorno />}
      {tab === 'svolte' && <LezioniSvolte />}
    </div>
  )
}

function InserisciGiorno() {
  const { maestro } = useAuth()
  const [data, setData] = useState('')
  const [loading, setLoading] = useState(false)
  const [errore, setErrore] = useState<string | null>(null)
  const [esito, setEsito] = useState<EsitoGiorno | null>(null)

  async function submit(e: FormEvent) {
    e.preventDefault()
    if (!maestro) return
    setLoading(true)
    setErrore(null)
    setEsito(null)
    try {
      const risposta = await api.inserisciGiorno(maestro.email, maestro.password, data)
      setEsito(risposta)
    } catch (e) {
      setErrore(e instanceof ApiClientError ? e.message : 'Errore di rete')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Card className="mx-auto max-w-md p-6">
      <h2 className="mb-1 font-display text-lg text-ink">Apri un nuovo giorno di disponibilità</h2>
      <p className="mb-5 text-xs text-ink-soft">
        Vengono create automaticamente 4 lezioni: 17:00, 18:00, 19:00, 20:00, livello BASE.
      </p>
      <form onSubmit={submit} className="flex flex-col gap-4">
        <TextField label="Data" type="date" required value={data} onChange={(e) => setData(e.target.value)} />
        <Button type="submit" loading={loading}>
          Conferma inserimento
        </Button>
      </form>
      {errore && (
        <div className="mt-4">
          <Alert kind="error">{errore}</Alert>
        </div>
      )}
      {esito && (
        <div className="mt-4 flex flex-col gap-3">
          <Alert kind="success">
            Giorno del {new Date(`${esito.giorno.data}T00:00:00`).toLocaleDateString('it-IT')} inserito con{' '}
            {esito.lezioniCreate?.length ?? 0} lezioni disponibili.
          </Alert>
          <LogPanel log={esito.log} />
        </div>
      )}
    </Card>
  )
}

function ModificaGiorno() {
  const { maestro } = useAuth()
  const [data, setData] = useState('')
  const [nuovaData, setNuovaData] = useState('')
  const [loading, setLoading] = useState(false)
  const [errore, setErrore] = useState<string | null>(null)
  const [esito, setEsito] = useState<EsitoGiorno | null>(null)

  async function submit(e: FormEvent) {
    e.preventDefault()
    if (!maestro) return
    setLoading(true)
    setErrore(null)
    setEsito(null)
    try {
      const risposta = await api.modificaGiorno(maestro.email, maestro.password, data, nuovaData)
      setEsito(risposta)
    } catch (e) {
      setErrore(e instanceof ApiClientError ? e.message : 'Errore di rete')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Card className="mx-auto max-w-md p-6">
      <h2 className="mb-1 font-display text-lg text-ink">Sposta un giorno di disponibilità</h2>
      <p className="mb-5 text-xs text-ink-soft">Entrambe le date devono essere future.</p>
      <form onSubmit={submit} className="flex flex-col gap-4">
        <TextField
          label="Giorno attuale"
          type="date"
          required
          value={data}
          onChange={(e) => setData(e.target.value)}
        />
        <TextField
          label="Nuovo giorno"
          type="date"
          required
          value={nuovaData}
          onChange={(e) => setNuovaData(e.target.value)}
        />
        <Button type="submit" loading={loading}>
          Sposta giorno
        </Button>
      </form>
      {errore && (
        <div className="mt-4">
          <Alert kind="error">{errore}</Alert>
        </div>
      )}
      {esito && (
        <div className="mt-4 flex flex-col gap-3">
          <Alert kind="success">
            Giorno spostato al {new Date(`${esito.giorno.data}T00:00:00`).toLocaleDateString('it-IT')}.
          </Alert>
          <LogPanel log={esito.log} />
        </div>
      )}
    </Card>
  )
}

function LezioniSvolte() {
  const { maestro } = useAuth()
  const [stato, setStato] = useState<'idle' | 'carico' | 'pronto' | 'errore'>('idle')
  const [lezioni, setLezioni] = useState<LezioneSvoltaMaestro[]>([])
  const [errore, setErrore] = useState<string | null>(null)

  async function carica() {
    if (!maestro) return
    setStato('carico')
    try {
      const dati = await api.lezioniSvolteMaestro(maestro.email, maestro.password)
      setLezioni(dati)
      setStato('pronto')
    } catch (e) {
      setErrore(e instanceof ApiClientError ? e.message : 'Errore di rete')
      setStato('errore')
    }
  }

  const oggi = new Date().toISOString().slice(0, 10)

  return (
    <Card className="mx-auto max-w-2xl p-6">
      <div className="mb-4 flex items-center justify-between">
        <h2 className="font-display text-lg text-ink">Lezioni prenotate</h2>
        <Button variant="secondary" size="sm" onClick={carica} loading={stato === 'carico'}>
          {stato === 'idle' ? 'Carica' : 'Aggiorna'}
        </Button>
      </div>

      {stato === 'errore' && errore && <Alert kind="error">{errore}</Alert>}
      {stato === 'carico' && <Spinner label="Carico le lezioni..." />}

      {stato === 'pronto' && (
        <>
          {lezioni.length === 0 ? (
            <p className="text-sm text-ink-soft">Nessuna lezione prenotata al momento.</p>
          ) : (
            <ul className="flex flex-col divide-y divide-ink/10">
              {lezioni.map((l) => (
                <li key={l.idLezione} className="flex items-center justify-between py-3 text-sm">
                  <div>
                    <p className="font-medium text-ink">
                      {new Date(`${l.data}T00:00:00`).toLocaleDateString('it-IT', {
                        day: 'numeric',
                        month: 'short',
                        year: 'numeric',
                      })}{' '}
                      <span className="font-mono text-ink-soft">{l.ora.slice(0, 5)}</span>
                    </p>
                    <p className="text-xs text-ink-faint">
                      {l.livello} &middot; {l.data < oggi ? 'svolta' : 'in programma'}
                    </p>
                  </div>
                  <div className="text-right">
                    {l.prenotazione ? (
                      <>
                        <p className="text-ink">
                          {l.prenotazione.studente.nome} {l.prenotazione.studente.cognome}
                        </p>
                        <p className="font-mono text-xs text-ink-faint">&euro;{l.prenotazione.costo.toFixed(2)}</p>
                      </>
                    ) : (
                      <p className="text-xs text-ink-faint">studente non disponibile</p>
                    )}
                  </div>
                </li>
              ))}
            </ul>
          )}
        </>
      )}
      <StaffDivider />
    </Card>
  )
}

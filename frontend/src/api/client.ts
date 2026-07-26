import type {
  DisponibilitaMaestro,
  EsitoDisdetta,
  EsitoGiorno,
  LezioneSvoltaMaestro,
  Maestro,
  PrenotazioneStudente,
  RichiestaPrenotazione,
  RisultatoPrenotazione,
  Studente,
  StrumentoConMaestri,
} from './types'

const BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api').replace(/\/$/, '')

export class ApiClientError extends Error {
  readonly status: number
  constructor(status: number, message: string) {
    super(message)
    this.name = 'ApiClientError'
    this.status = status
  }
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  let res: Response
  try {
    res = await fetch(`${BASE_URL}${path}`, {
      ...options,
      headers: { 'Content-Type': 'application/json', ...(options.headers ?? {}) },
    })
  } catch {
    throw new ApiClientError(
      0,
      `Impossibile contattare il backend GLAM su ${BASE_URL}. Verifica che Boundary.web.ApiServer sia in esecuzione.`,
    )
  }

  const raw = await res.text()
  const data = raw ? (JSON.parse(raw) as unknown) : null

  if (!res.ok) {
    const message =
      data && typeof data === 'object' && 'errore' in data
        ? String((data as { errore: unknown }).errore)
        : `Errore HTTP ${res.status}`
    throw new ApiClientError(res.status, message)
  }

  return data as T
}

function authHeaders(pairs: Record<string, string>): HeadersInit {
  return pairs
}

export const api = {
  health: () => request<{ status: string }>('/health'),

  strumenti: () => request<StrumentoConMaestri[]>('/strumenti'),
  maestri: () => request<Maestro[]>('/maestri'),
  cercaMaestri: (strumento: string) =>
    request<Maestro[]>(`/maestri/ricerca?strumento=${encodeURIComponent(strumento)}`),
  disponibilitaMaestro: (id: number) => request<DisponibilitaMaestro>(`/maestri/${id}/disponibilita`),

  loginMaestro: (email: string, password: string) =>
    request<Maestro>('/auth/maestro', { method: 'POST', body: JSON.stringify({ email, password }) }),
  loginStudente: (username: string, password: string) =>
    request<Studente>('/auth/studente', { method: 'POST', body: JSON.stringify({ username, password }) }),

  inserisciGiorno: (email: string, password: string, data: string) =>
    request<EsitoGiorno>('/maestro/giorni', {
      method: 'POST',
      body: JSON.stringify({ email, password, data }),
    }),
  modificaGiorno: (email: string, password: string, data: string, nuovaData: string) =>
    request<EsitoGiorno>('/maestro/giorni', {
      method: 'PUT',
      body: JSON.stringify({ email, password, data, nuovaData }),
    }),
  lezioniSvolteMaestro: (email: string, password: string) =>
    request<LezioneSvoltaMaestro[]>('/maestro/lezioni-svolte', {
      headers: authHeaders({ 'X-Email': email, 'X-Password': password }),
    }),

  prenota: (username: string, password: string, richieste: RichiestaPrenotazione[]) =>
    request<RisultatoPrenotazione>('/studente/prenotazioni', {
      method: 'POST',
      body: JSON.stringify({ username, password, richieste }),
    }),
  disdici: (
    username: string,
    password: string,
    dettagli: { strumento: string; nomeMaestro: string; cognomeMaestro: string; data: string; ora: string },
  ) =>
    request<EsitoDisdetta>('/studente/prenotazioni', {
      method: 'DELETE',
      body: JSON.stringify({ username, password, ...dettagli }),
    }),
  miePrenotazioni: (username: string, password: string) =>
    request<PrenotazioneStudente[]>('/studente/prenotazioni', {
      headers: authHeaders({ 'X-Username': username, 'X-Password': password }),
    }),
}

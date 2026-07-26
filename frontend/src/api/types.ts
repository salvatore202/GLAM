// Tipi allineati alle risposte JSON di Boundary.web.* (vedi backend Java).

export interface Maestro {
  id: number
  nome: string
  cognome: string
  email: string
  numeroDiTelefono: number
  strumenti?: string[]
}

export interface Studente {
  id: number
  nome: string
  cognome: string
  username: string
  email: string
  numeroDiTelefono: number
}

export type Livello = 'BASE' | 'INTERMEDIO'

export interface Lezione {
  idLezione: number
  ora: string
  livello: Livello
  disponibile: boolean
  idMaestro: number
  idData: number
}

export interface GiornoDisponibilita {
  idData: number
  data: string
  idMaestro: number
  lezioniDisponibili?: Lezione[]
}

export interface StrumentoConMaestri {
  nome: string
  maestri: Maestro[]
}

export interface DisponibilitaMaestro {
  maestro: Maestro
  giorni: GiornoDisponibilita[]
}

export interface RichiestaPrenotazione {
  strumento: string
  nomeMaestro: string
  cognomeMaestro: string
  data: string
  ora: string
  livello: Livello
}

export interface EsitoPrenotazione {
  strumento: string
  maestro: string
  data: string
  ora: string
  livello: string
  esito: 'prenotata' | 'non_riuscita'
  idPrenotazione?: number
  costo?: number
  motivo?: string
}

export interface RisultatoPrenotazione {
  successo: boolean
  risultati: EsitoPrenotazione[]
  log: string
}

export interface PrenotazioneStudente {
  idPrenotazione: number
  costo: number
  idLezione: number
  ora: string
  livello: string
  data: string
  maestro: { idMaestro: number; nome: string; cognome: string }
}

export interface LezioneSvoltaMaestro {
  idLezione: number
  ora: string
  livello: string
  data: string
  prenotazione: {
    idPrenotazione: number
    costo: number
    studente: { idStudente: number; nome: string; cognome: string }
  } | null
}

export interface EsitoDisdetta {
  successo: true
  slotLiberato: boolean
  rimborsata: boolean
  messaggio: string
  log: string
}

export interface EsitoGiorno {
  successo: true
  giorno: GiornoDisponibilita
  lezioniCreate?: Lezione[]
  log: string
}

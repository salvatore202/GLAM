import { createContext, useContext, useMemo, useState, type ReactNode } from 'react'
import type { Maestro, Studente } from '../api/types'

export interface MaestroSession {
  email: string
  password: string
  profilo: Maestro
}

export interface StudenteSession {
  username: string
  password: string
  profilo: Studente
}

interface AuthContextValue {
  maestro: MaestroSession | null
  studente: StudenteSession | null
  loginMaestro: (session: MaestroSession) => void
  loginStudente: (session: StudenteSession) => void
  logoutMaestro: () => void
  logoutStudente: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

const MAESTRO_KEY = 'glam.sessione.maestro'
const STUDENTE_KEY = 'glam.sessione.studente'

// Le credenziali restano solo per la durata della scheda del browser
// (sessionStorage, non localStorage): coerente con il fatto che il backend
// originale non ha alcun concetto di sessione/token e richiede email+password
// (o username+password) ad ogni singola operazione, cosi' come farebbe la CLI.
function readSession<T>(key: string): T | null {
  try {
    const raw = sessionStorage.getItem(key)
    return raw ? (JSON.parse(raw) as T) : null
  } catch {
    return null
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [maestro, setMaestro] = useState<MaestroSession | null>(() => readSession(MAESTRO_KEY))
  const [studente, setStudente] = useState<StudenteSession | null>(() => readSession(STUDENTE_KEY))

  const value = useMemo<AuthContextValue>(
    () => ({
      maestro,
      studente,
      loginMaestro: (session) => {
        sessionStorage.setItem(MAESTRO_KEY, JSON.stringify(session))
        setMaestro(session)
      },
      loginStudente: (session) => {
        sessionStorage.setItem(STUDENTE_KEY, JSON.stringify(session))
        setStudente(session)
      },
      logoutMaestro: () => {
        sessionStorage.removeItem(MAESTRO_KEY)
        setMaestro(null)
      },
      logoutStudente: () => {
        sessionStorage.removeItem(STUDENTE_KEY)
        setStudente(null)
      },
    }),
    [maestro, studente],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

// eslint-disable-next-line react-refresh/only-export-components -- hook e provider condividono di proposito lo stesso contesto
export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth deve essere usato dentro <AuthProvider>')
  return ctx
}

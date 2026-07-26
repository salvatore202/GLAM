import { NavLink } from 'react-router-dom'
import { Music2 } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'

const navLinkBase =
  'rounded-full px-3.5 py-1.5 text-sm font-medium transition-colors duration-150 whitespace-nowrap'

export function NavBar() {
  const { maestro, studente } = useAuth()

  return (
    <header className="sticky top-0 z-30 border-b border-parchment/10 bg-stage/95 backdrop-blur">
      <div className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-6 py-3.5">
        <NavLink to="/" className="flex items-center gap-2 text-parchment">
          <Music2 className="h-5 w-5 text-brass" strokeWidth={1.75} />
          <span className="font-display text-lg font-semibold tracking-tight">GLAM</span>
        </NavLink>

        <nav className="flex items-center gap-1">
          <NavLink
            to="/catalogo"
            className={({ isActive }) =>
              `${navLinkBase} ${isActive ? 'bg-parchment/10 text-brass-pale' : 'text-parchment/70 hover:text-parchment'}`
            }
          >
            Catalogo
          </NavLink>
          <NavLink
            to="/maestro"
            className={({ isActive }) =>
              `${navLinkBase} ${isActive ? 'bg-parchment/10 text-brass-pale' : 'text-parchment/70 hover:text-parchment'}`
            }
          >
            {maestro ? `Maestro: ${maestro.profilo.cognome}` : 'Area Maestro'}
          </NavLink>
          <NavLink
            to="/studente"
            className={({ isActive }) =>
              `${navLinkBase} ${isActive ? 'bg-parchment/10 text-brass-pale' : 'text-parchment/70 hover:text-parchment'}`
            }
          >
            {studente ? `Studente: ${studente.profilo.cognome}` : 'Area Studente'}
          </NavLink>
        </nav>
      </div>
    </header>
  )
}

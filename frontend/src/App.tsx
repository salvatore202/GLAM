import { Route, Routes } from 'react-router-dom'
import { NavBar } from './components/layout/NavBar'
import { Footer } from './components/layout/Footer'
import { HomePage } from './pages/HomePage'
import { CatalogoPage } from './pages/CatalogoPage'
import { MaestroPage } from './pages/MaestroPage'
import { StudentePage } from './pages/StudentePage'

function App() {
  return (
    <div className="flex min-h-screen flex-col bg-paper">
      <NavBar />
      <main className="flex-1">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/catalogo" element={<CatalogoPage />} />
          <Route path="/maestro" element={<MaestroPage />} />
          <Route path="/studente" element={<StudentePage />} />
        </Routes>
      </main>
      <Footer />
    </div>
  )
}

export default App

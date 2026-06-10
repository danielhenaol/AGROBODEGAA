import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { useAuth0 } from '@auth0/auth0-react'
import Layout from './components/layout/Layout'
import Dashboard from './pages/Dashboard'
import Entries from './pages/Entries'
import Exits from './pages/Exits'
import Lots from './pages/Lots'
import Farmers from './pages/Farmers'
import Traders from './pages/Traders'
import Products from './pages/Products'
import LoginPage from './pages/LoginPage'

function ProtectedRoute({ children }) {
  const { isAuthenticated, isLoading } = useAuth0()
  if (isLoading) return (
    <div className="loading-screen">
      <div className="loading-spinner" />
      <span>Cargando...</span>
    </div>
  )
  return isAuthenticated ? children : <Navigate to="/login" />
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/" element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }>
          <Route index element={<Dashboard />} />
          <Route path="entradas" element={<Entries />} />
          <Route path="salidas" element={<Exits />} />
          <Route path="lotes" element={<Lots />} />
          <Route path="cosecheros" element={<Farmers />} />
          <Route path="negociantes" element={<Traders />} />
          <Route path="productos" element={<Products />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

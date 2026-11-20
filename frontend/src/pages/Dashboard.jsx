import { useState, useEffect } from 'react'
import { useAuth0 } from '@auth0/auth0-react'
import { Link } from 'react-router-dom'
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, Legend } from 'recharts'
import { api, errorMessage } from '../services/api'
import { useToken } from '../services/useToken'
import { useRole } from '../services/roles'
import { money, fmtShort, fmtTime, fmtDate, monthRange, todayLocal } from '../utils/format'

const DIAS = ['domingo', 'lunes', 'martes', 'miércoles', 'jueves', 'viernes', 'sábado']
const MESES = ['enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio', 'julio', 'agosto', 'septiembre', 'octubre', 'noviembre', 'diciembre']

function saludo() {
  const h = new Date().getHours()
  return h < 12 ? 'Buenos días' : h < 18 ? 'Buenas tardes' : 'Buenas noches'
}

/** Primer nombre solo si el perfil trae un nombre real (nunca el correo). */
function firstName(user) {
  const n = user?.given_name || user?.name || ''
  if (!n || n.includes('@')) return ''
  return n.split(' ')[0]
}

function fechaLarga() {
  const d = new Date()
  return `${DIAS[d.getDay()]}, ${d.getDate()} de ${MESES[d.getMonth()]} de ${d.getFullYear()}`
}

function Kpi({ label, value, sub, tone = 'green' }) {
  return (
    <div className={`kpi kpi-${tone}`}>
      <div className="kpi-label">{label}</div>
      <div className="kpi-value">{value}</div>
      {sub && <div className="kpi-sub">{sub}</div>}
    </div>
  )
}

export default function Dashboard() {
  const getToken = useToken()
  const { user } = useAuth0()
  const { isOwner, label } = useRole()
  const [data, setData] = useState(null)
  const [month, setMonth] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let vivo = true
    ;(async () => {
      try {
        const token = await getToken()
        const { from, to } = monthRange()
        const [d, r] = await Promise.all([
          api.getDashboard(token),
          isOwner ? api.getReportSummary(token, from, to) : Promise.resolve(null),
        ])
        if (!vivo) return
        setData(d.data)
        setMonth(r?.data || null)
      } catch (e) {
        console.error(e)
        if (vivo) setError(errorMessage(e))
      } finally {
        if (vivo) setLoading(false)
      }
    })()
    return () => { vivo = false }
  }, [getToken, isOwner])

  const nombre = firstName(user)
  const chart = (data?.last7Days || []).map((d) => ({ dia: fmtShort(d.date), Entradas: d.entries, Salidas: d.exits }))
  const maxStock = Math.max(1, ...(data?.stockByProduct || []).map((s) => s.bultos))

  return (
    <div>
      <div className="hero">
        <div>
          <h1>{saludo()}{nombre ? `, ${nombre}` : ''}</h1>
          <p>{fechaLarga()} · {label}</p>
        </div>
        <span className="badge badge-green"><span className="live-dot" />Sistema activo</span>
      </div>

      {error && <div className="alert alert-error" onClick={() => setError('')}>⚠ {error}</div>}

      {loading ? (
        <div style={{ display: 'flex', justifyContent: 'center', padding: 60 }}>
          <div className="loading-spinner" style={{ borderTopColor: '#38a15c', width: 40, height: 40 }} />
        </div>
      ) : data && (
        <>
          <div className="kpi-grid">
            <Kpi label="En bodega" value={`${data.stockBultos} bultos`} sub={`${data.lotsWithStock} lotes con existencias`} />
            <Kpi label="Entradas de hoy" value={`${data.todayEntries.bultos} bultos`} sub={`${data.todayEntries.movements} registro(s)`} tone="blue" />
            <Kpi label="Salidas de hoy" value={`${data.todayExits.bultos} bultos`} sub={`${data.todayExits.movements} registro(s)`} tone="earth" />
            <Kpi label="Stock bajo" value={data.lowStock.length} sub={`productos con ${data.lowStockThreshold} bultos o menos`} tone={data.lowStock.length ? 'amber' : 'green'} />
          </div>

          {isOwner && month && (
            <div className="kpi-grid" style={{ gridTemplateColumns: 'repeat(3, 1fr)' }}>
              <Kpi label="Ingresos del mes" value={money(month.income)} sub="Comisiones cobradas" />
              <Kpi label="Gastos del mes" value={money(month.expenses)} sub={`${month.expenseCount} gasto(s)`} tone="amber" />
              <Kpi label="Utilidad del mes" value={money(month.net)} sub={<Link to="/reportes">Ver reporte completo →</Link>} tone={Number(month.net) >= 0 ? 'green' : 'red'} />
            </div>
          )}

          <div className="charts-grid">
            <div className="card">
              <div className="card-header"><div className="card-title">Movimiento de los últimos 7 días (bultos)</div></div>
              <div className="card-body" style={{ height: 280 }}>
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={chart}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} />
                    <XAxis dataKey="dia" tick={{ fontSize: 12 }} />
                    <YAxis allowDecimals={false} tick={{ fontSize: 12 }} />
                    <Tooltip />
                    <Legend />
                    <Bar dataKey="Entradas" fill="#38a15c" radius={[4, 4, 0, 0]} />
                    <Bar dataKey="Salidas" fill="#d4a96a" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>

            <div className="card">
              <div className="card-header"><div className="card-title">Existencias por producto</div></div>
              <div className="card-body">
                {data.stockByProduct.length === 0 ? (
                  <p className="muted">Aún no hay productos registrados.</p>
                ) : data.stockByProduct.map((s) => (
                  <div key={s.product} className="bar-row">
                    <div className="bar-head">
                      <span>{s.product}</span>
                      <span><strong>{s.bultos}</strong> bultos {s.low && <span className="tag tag-amber">Bajo</span>}</span>
                    </div>
                    <div className="bar-track"><div className={`bar-fill ${s.low ? 'low' : ''}`} style={{ width: `${(s.bultos / maxStock) * 100}%` }} /></div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          <div className="card">
            <div className="card-header"><div className="card-title">Actividad reciente</div></div>
            <div className="card-body">
              {data.recent.length === 0 ? (
                <p className="muted">Cuando registres entradas o salidas aparecerán aquí.</p>
              ) : (
                <ul className="activity">
                  {data.recent.map((a, i) => (
                    <li key={i}>
                      <span className={`activity-dot ${a.type === 'ENTRY' ? 'in' : 'out'}`}>{a.type === 'ENTRY' ? '↓' : '↑'}</span>
                      <div className="activity-main">
                        <div><strong>{a.type === 'ENTRY' ? 'Entrada' : 'Salida'}</strong> · {a.party}</div>
                        <div className="muted">{a.detail}</div>
                      </div>
                      <div className="activity-side">
                        <strong>{a.bultos} bultos</strong>
                        <div className="muted">{a.date === todayLocal() ? 'Hoy' : fmtDate(a.date)} {fmtTime(a.occurredAt)}</div>
                      </div>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>
        </>
      )}
    </div>
  )
}

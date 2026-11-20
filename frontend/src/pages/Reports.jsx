import { useState, useEffect, useCallback } from 'react'
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, Legend } from 'recharts'
import { api, errorMessage } from '../services/api'
import { useToken } from '../services/useToken'
import { money, fmtShort, fmtDate, monthRange, todayLocal, downloadCsv } from '../utils/format'

const shift = (days) => {
  const d = new Date(`${todayLocal()}T00:00:00`)
  d.setDate(d.getDate() - days)
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${d.getFullYear()}-${m}-${day}`
}

const PRESETS = [
  { label: 'Este mes', get: () => monthRange() },
  { label: 'Últimos 7 días', get: () => ({ from: shift(6), to: todayLocal() }) },
  { label: 'Últimos 30 días', get: () => ({ from: shift(29), to: todayLocal() }) },
  { label: 'Este año', get: () => ({ from: `${todayLocal().slice(0, 4)}-01-01`, to: todayLocal() }) },
]

function Rank({ title, items }) {
  return (
    <div className="card">
      <div className="card-header"><div className="card-title">{title}</div></div>
      <div className="table-container">
        {items.length === 0 ? <div className="card-body muted">Sin datos en el periodo</div> : (
          <table>
            <thead><tr><th>Nombre</th><th style={{ textAlign: 'right' }}>Bultos</th><th style={{ textAlign: 'right' }}>Valor</th></tr></thead>
            <tbody>
              {items.map((x) => (
                <tr key={x.name}><td>{x.name}</td><td style={{ textAlign: 'right' }}>{x.bultos}</td><td style={{ textAlign: 'right' }}><strong>{money(x.value)}</strong></td></tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  )
}

export default function Reports() {
  const getToken = useToken()
  const [range, setRange] = useState(monthRange())
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const token = await getToken()
      const r = await api.getReportSummary(token, range.from, range.to)
      setData(r.data)
    } catch (e) {
      console.error(e); setError(errorMessage(e))
    } finally {
      setLoading(false)
    }
  }, [getToken, range.from, range.to])

  useEffect(() => { load() }, [load])

  const chart = (data?.daily || []).map((d) => ({
    dia: fmtShort(d.date), Ingresos: Number(d.income), Gastos: Number(d.expenses),
  }))

  const exportCsv = () => {
    if (!data) return
    downloadCsv(`reporte_${data.from}_${data.to}.csv`, [
      ['Reporte económico', `${data.from} a ${data.to}`],
      [],
      ['Concepto', 'Valor'],
      ['Comisión de entradas', data.entries.commission],
      ['Comisión de salidas', data.exits.commission],
      ['Ingresos totales', data.income],
      ['Gastos totales', data.expenses],
      ['Utilidad', data.net],
      [],
      ['Fecha', 'Ingresos', 'Gastos', 'Utilidad'],
      ...data.daily.map((d) => [d.date, d.income, d.expenses, d.net]),
      [],
      ['Gastos por categoría', 'Valor'],
      ...data.expensesByCategory.map((c) => [c.name, c.amount]),
    ])
  }

  return (
    <div>
      <div className="page-header">
        <div className="page-header-left">
          <h1>Reportes económicos</h1>
          <p>Ingresos por comisiones, gastos y utilidad de la bodega</p>
        </div>
        <div className="no-print" style={{ display: 'flex', gap: 8 }}>
          <button className="btn btn-secondary" onClick={exportCsv} disabled={!data}>Descargar CSV</button>
          <button className="btn btn-secondary" onClick={() => window.print()}>Imprimir</button>
        </div>
      </div>

      <div className="toolbar no-print">
        {PRESETS.map((p) => (
          <button key={p.label} className="btn btn-secondary btn-sm" onClick={() => setRange(p.get())}>{p.label}</button>
        ))}
        <label>Desde <input type="date" value={range.from} max={range.to} onChange={(e) => setRange({ ...range, from: e.target.value })} /></label>
        <label>Hasta <input type="date" value={range.to} min={range.from} onChange={(e) => setRange({ ...range, to: e.target.value })} /></label>
      </div>

      {error && <div className="alert alert-error" onClick={() => setError('')}>⚠ {error}</div>}

      {loading ? (
        <div style={{ display: 'flex', justifyContent: 'center', padding: 60 }}>
          <div className="loading-spinner" style={{ borderTopColor: '#38a15c', width: 40, height: 40 }} />
        </div>
      ) : data && (
        <>
          <p className="muted" style={{ marginBottom: 12 }}>Periodo: {fmtDate(data.from)} a {fmtDate(data.to)}</p>
          <div className="kpi-grid" style={{ gridTemplateColumns: 'repeat(3, 1fr)' }}>
            <div className="kpi kpi-green"><div className="kpi-label">Ingresos (comisiones)</div><div className="kpi-value">{money(data.income)}</div>
              <div className="kpi-sub">Entradas {money(data.entries.commission)} · Salidas {money(data.exits.commission)}</div></div>
            <div className="kpi kpi-amber"><div className="kpi-label">Gastos</div><div className="kpi-value">{money(data.expenses)}</div>
              <div className="kpi-sub">{data.expenseCount} gasto(s)</div></div>
            <div className={`kpi ${Number(data.net) >= 0 ? 'kpi-green' : 'kpi-red'}`}><div className="kpi-label">Utilidad</div><div className="kpi-value">{money(data.net)}</div>
              <div className="kpi-sub">Ingresos − gastos</div></div>
          </div>

          <div className="kpi-grid" style={{ gridTemplateColumns: 'repeat(2, 1fr)' }}>
            <div className="kpi kpi-blue"><div className="kpi-label">Entradas</div><div className="kpi-value">{data.entries.bultos} bultos</div>
              <div className="kpi-sub">{data.entries.receipts} registro(s) · mercancía recibida {money(data.entries.value)}</div></div>
            <div className="kpi kpi-earth"><div className="kpi-label">Salidas</div><div className="kpi-value">{data.exits.bultos} bultos</div>
              <div className="kpi-sub">{data.exits.receipts} registro(s) · ventas {money(data.exits.value)}</div></div>
          </div>

          <div className="card" style={{ marginBottom: 16 }}>
            <div className="card-header"><div className="card-title">Ingresos vs gastos por día</div></div>
            <div className="card-body" style={{ height: 300 }}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={chart}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} />
                  <XAxis dataKey="dia" tick={{ fontSize: 11 }} interval="preserveStartEnd" />
                  <YAxis tick={{ fontSize: 11 }} tickFormatter={(v) => v >= 1000 ? `${Math.round(v / 1000)}k` : v} />
                  <Tooltip formatter={(v) => money(v)} />
                  <Legend />
                  <Bar dataKey="Ingresos" fill="#38a15c" radius={[4, 4, 0, 0]} />
                  <Bar dataKey="Gastos" fill="#d4a96a" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

          <div className="two-col">
            <Rank title="Productos más vendidos" items={data.topProducts} />
            <Rank title="Mejores cosecheros (mercancía entregada)" items={data.topFarmers} />
            <Rank title="Mejores negociantes (compras)" items={data.topTraders} />
            <div className="card">
              <div className="card-header"><div className="card-title">Gastos por categoría</div></div>
              <div className="table-container">
                {data.expensesByCategory.length === 0 ? <div className="card-body muted">Sin gastos en el periodo</div> : (
                  <table>
                    <thead><tr><th>Categoría</th><th style={{ textAlign: 'right' }}>Valor</th></tr></thead>
                    <tbody>
                      {data.expensesByCategory.map((c) => (
                        <tr key={c.code}><td>{c.name}</td><td style={{ textAlign: 'right' }}><strong>{money(c.amount)}</strong></td></tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  )
}

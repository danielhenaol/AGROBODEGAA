import { useState, useEffect, useCallback } from 'react'
import { useAuth0 } from '@auth0/auth0-react'
import { api, errorMessage } from '../services/api'
import { useToken } from '../services/useToken'
import { useRole } from '../services/roles'
import { money, nowLocal, toInstant, fmtDateTime, monthRange, downloadCsv } from '../utils/format'

const emptyForm = () => ({ categoryCode: '', description: '', amount: '', occurredAt: nowLocal() })

export default function Expenses() {
  const getToken = useToken()
  const { user } = useAuth0()
  const { isOwner } = useRole()
  const [range, setRange] = useState(monthRange())
  const [categories, setCategories] = useState([])
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [showModal, setShowModal] = useState(false)
  const [form, setForm] = useState(emptyForm())

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const token = await getToken()
      const [c, e] = await Promise.all([
        api.getExpenseCategories(token),
        api.getExpenses(token, range.from, range.to),
      ])
      setCategories(c.data || [])
      setItems(e.data || [])
    } catch (err) {
      console.error(err); setError(errorMessage(err))
    } finally {
      setLoading(false)
    }
  }, [getToken, range.from, range.to])

  useEffect(() => { load() }, [load])

  const total = items.reduce((s, x) => s + Number(x.amount), 0)

  const submit = async (e) => {
    e.preventDefault()
    setError('')
    setSaving(true)
    try {
      const token = await getToken()
      await api.createExpense(token, {
        categoryCode: form.categoryCode,
        description: form.description.trim(),
        amount: parseFloat(form.amount),
        occurredAt: toInstant(form.occurredAt),
        registeredBy: user?.email,
      })
      setShowModal(false)
      setForm(emptyForm())
      setSuccess('Gasto registrado')
      setTimeout(() => setSuccess(''), 3000)
      await load()
    } catch (err) {
      console.error(err); setError(errorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  const remove = async (x) => {
    if (!window.confirm(`¿Eliminar el gasto "${x.description}" por ${money(x.amount)}?`)) return
    try {
      const token = await getToken()
      await api.deleteExpense(token, x.id)
      await load()
    } catch (err) {
      console.error(err); setError(errorMessage(err))
    }
  }

  const exportCsv = () =>
    downloadCsv(`gastos_${range.from}_${range.to}.csv`, [
      ['Fecha y hora', 'Categoría', 'Descripción', 'Valor', 'Registrado por'],
      ...items.map((x) => [fmtDateTime(x.occurredAt, x.expenseDate), x.categoryName, x.description, x.amount, x.registeredBy || '']),
    ])

  return (
    <div>
      <div className="page-header">
        <div className="page-header-left">
          <h1>Gastos</h1>
          <p>Gastos de la bodega: nómina, servicios, arriendo y más</p>
        </div>
        <button className="btn btn-primary" onClick={() => { setError(''); setForm(emptyForm()); setShowModal(true) }}>+ Nuevo gasto</button>
      </div>

      {success && <div className="alert alert-success">✓ {success}</div>}
      {error && !showModal && <div className="alert alert-error" onClick={() => setError('')}>⚠ {error}</div>}

      <div className="toolbar">
        <label>Desde <input type="date" value={range.from} max={range.to} onChange={(e) => setRange({ ...range, from: e.target.value })} /></label>
        <label>Hasta <input type="date" value={range.to} min={range.from} onChange={(e) => setRange({ ...range, to: e.target.value })} /></label>
        <button className="btn btn-secondary btn-sm" onClick={() => setRange(monthRange())}>Este mes</button>
        <button className="btn btn-secondary btn-sm" onClick={exportCsv} disabled={!items.length}>Descargar CSV</button>
        <div className="toolbar-total">Total del periodo: <strong>{money(total)}</strong></div>
      </div>

      <div className="card">
        {loading ? (
          <div className="card-body"><div className="empty-state"><div className="loading-spinner" style={{ borderTopColor: '#38a15c', width: 32, height: 32 }} /></div></div>
        ) : items.length === 0 ? (
          <div className="card-body"><div className="empty-state"><h3>Sin gastos en este periodo</h3><p>Registra el primero con «Nuevo gasto»</p></div></div>
        ) : (
          <div className="table-container">
            <table>
              <thead><tr><th>Fecha y hora</th><th>Categoría</th><th>Descripción</th><th>Registró</th><th style={{ textAlign: 'right' }}>Valor</th>{isOwner && <th />}</tr></thead>
              <tbody>
                {items.map((x) => (
                  <tr key={x.id}>
                    <td>{fmtDateTime(x.occurredAt, x.expenseDate)}</td>
                    <td><span className="tag tag-earth">{x.categoryName}</span></td>
                    <td>{x.description}</td>
                    <td className="muted">{x.registeredBy || '—'}</td>
                    <td style={{ textAlign: 'right' }}><strong>{money(x.amount)}</strong></td>
                    {isOwner && <td><button className="btn btn-danger btn-sm" onClick={() => remove(x)}>Eliminar</button></td>}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {showModal && (
        <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && setShowModal(false)}>
          <div className="modal" style={{ maxWidth: 520 }}>
            <div className="modal-header">
              <div className="modal-title">Registrar gasto</div>
              <button className="modal-close" onClick={() => setShowModal(false)}>✕</button>
            </div>
            <form onSubmit={submit}>
              <div className="modal-body">
                {error && <div className="alert alert-error">⚠ {error}</div>}
                <div className="form-grid">
                  <div className="form-group">
                    <label>Categoría *</label>
                    <select value={form.categoryCode} onChange={(e) => setForm({ ...form, categoryCode: e.target.value })} required>
                      <option value="">Seleccionar</option>
                      {categories.map((c) => <option key={c.code} value={c.code}>{c.name}</option>)}
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Valor ($) *</label>
                    <input type="number" min="1" step="0.01" value={form.amount} onChange={(e) => setForm({ ...form, amount: e.target.value })} required />
                  </div>
                  <div className="form-group full">
                    <label>Descripción *</label>
                    <input type="text" maxLength={200} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} placeholder="Ej: Pago de nómina quincena" required />
                  </div>
                  <div className="form-group full">
                    <label>Fecha y hora *</label>
                    <input type="datetime-local" value={form.occurredAt} onChange={(e) => setForm({ ...form, occurredAt: e.target.value })} required />
                  </div>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancelar</button>
                <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Guardando...' : 'Registrar gasto'}</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}

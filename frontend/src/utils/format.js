export const money = (n) =>
  `$${Number(n ?? 0).toLocaleString('es-CO', { minimumFractionDigits: 0, maximumFractionDigits: 2 })}`

/** Valor inicial de un input datetime-local: la hora actual del equipo, sin segundos. */
export const nowLocal = () => {
  const d = new Date()
  d.setMinutes(d.getMinutes() - d.getTimezoneOffset())
  return d.toISOString().slice(0, 16)
}

/** Fecha de hoy (YYYY-MM-DD) en la hora local del equipo. */
export const todayLocal = () => nowLocal().slice(0, 10)

/** Convierte el valor de un datetime-local a un instante ISO (UTC) para el backend. */
export const toInstant = (localValue) => new Date(localValue).toISOString()

const BOGOTA = 'America/Bogota'

/** "20/11/2026 3:45 p. m." — cae a solo fecha en registros antiguos sin hora. */
export const fmtDateTime = (instant, fallbackDate) => {
  if (instant) {
    return new Date(instant).toLocaleString('es-CO', {
      timeZone: BOGOTA, day: '2-digit', month: '2-digit', year: 'numeric',
      hour: 'numeric', minute: '2-digit',
    })
  }
  return fallbackDate ? fmtDate(fallbackDate) : '—'
}

export const fmtTime = (instant) =>
  instant ? new Date(instant).toLocaleTimeString('es-CO', { timeZone: BOGOTA, hour: 'numeric', minute: '2-digit' }) : ''

/** "2026-11-20" -> "20/11/2026" sin pasar por zonas horarias. */
export const fmtDate = (isoDate) => {
  if (!isoDate) return '—'
  const [y, m, d] = String(isoDate).split('-')
  return `${d}/${m}/${y}`
}

/** "2026-11-20" -> "20 nov" */
export const fmtShort = (isoDate) => {
  const [y, m, d] = String(isoDate).split('-').map(Number)
  return new Date(y, m - 1, d).toLocaleDateString('es-CO', { day: 'numeric', month: 'short' }).replace('.', '')
}

export const monthRange = () => {
  const t = todayLocal()
  return { from: `${t.slice(0, 8)}01`, to: t }
}

export const downloadCsv = (filename, rows) => {
  const esc = (v) => `"${String(v ?? '').replace(/"/g, '""')}"`
  const csv = '﻿' + rows.map((r) => r.map(esc).join(';')).join('\r\n')
  const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8' }))
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  URL.revokeObjectURL(url)
}

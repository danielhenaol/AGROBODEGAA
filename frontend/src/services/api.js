import axios from 'axios'

export const API_URL =
    import.meta.env.VITE_API_URL ||
    'https://agrobodega-gateway.onrender.com'

const TRANSITORIO = [502, 503, 504]
const esperar = (ms) => new Promise((r) => setTimeout(r, ms))

/**
 * Cliente HTTP. Los servicios gratuitos de Render se duermen y tardan en despertar:
 * mientras tanto responden 502/503/504 o no responden. Las lecturas (GET) se
 * reintentan solas; las escrituras NO, para no duplicar un registro.
 */
export const createApiClient = (token) => {
  const client = axios.create({
    baseURL: API_URL,
    timeout: 70000,
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })

  client.interceptors.response.use(undefined, async (error) => {
    const cfg = error.config
    const transitorio = !error.response || TRANSITORIO.includes(error.response.status)
    if (cfg && cfg.method === 'get' && transitorio && (cfg.__reintentos || 0) < 5) {
      cfg.__reintentos = (cfg.__reintentos || 0) + 1
      await esperar(8000)
      return client(cfg)
    }
    return Promise.reject(error)
  })

  return client
}

export const api = {
  getFarmers: (token) => createApiClient(token).get('/api/v1/farmers'),
  createFarmer: (token, data) => createApiClient(token).post('/api/v1/farmers', data),
  updateFarmer: (token, id, data) => createApiClient(token).put(`/api/v1/farmers/${id}`, data),
  deleteFarmer: (token, id) => createApiClient(token).delete(`/api/v1/farmers/${id}`),
  getTraders: (token) => createApiClient(token).get('/api/v1/traders'),
  createTrader: (token, data) => createApiClient(token).post('/api/v1/traders', data),
  updateTrader: (token, id, data) => createApiClient(token).put(`/api/v1/traders/${id}`, data),
  deleteTrader: (token, id) => createApiClient(token).delete(`/api/v1/traders/${id}`),
  getProducts: (token) => createApiClient(token).get('/api/v1/products'),
  createProduct: (token, data) => createApiClient(token).post('/api/v1/products', data),
  updateProduct: (token, id, data) => createApiClient(token).put(`/api/v1/products/${id}`, data),
  deleteProduct: (token, id) => createApiClient(token).delete(`/api/v1/products/${id}`),
  createClassification: (token, productId, data) =>
      createApiClient(token).post(`/api/v1/products/${productId}/classifications`, data),
  getClassifications: (token, productId) =>
      createApiClient(token).get(`/api/v1/products/${productId}/classifications`),
  deleteClassification: (token, productId, classId) =>
      createApiClient(token).delete(`/api/v1/products/${productId}/classifications/${classId}`),
  // Lotes = inventario en bodega. Se alimentan solo con entradas; available=true => solo con existencias
  getLots: (token, available = false) =>
      createApiClient(token).get('/api/v1/lots', { params: { available } }),
  getEntries: (token) => createApiClient(token).get('/api/v1/entries'),
  createEntry: (token, data) => createApiClient(token).post('/api/v1/entries', data),
  getExits: (token) => createApiClient(token).get('/api/v1/exits'),
  createExit: (token, data) => createApiClient(token).post('/api/v1/exits', data),
  getDashboard: (token) => createApiClient(token).get('/api/v1/dashboard'),
  getReportSummary: (token, from, to) =>
      createApiClient(token).get('/api/v1/reports/summary', { params: { from, to } }),
  getExpenseCategories: (token) => createApiClient(token).get('/api/v1/expenses/categories'),
  getExpenses: (token, from, to) =>
      createApiClient(token).get('/api/v1/expenses', { params: { from, to } }),
  createExpense: (token, data) => createApiClient(token).post('/api/v1/expenses', data),
  deleteExpense: (token, id) => createApiClient(token).delete(`/api/v1/expenses/${id}`),
}

export const SSE_URL = `${API_URL}/api/v1/inventory/stream`

/** Convierte un error de axios en un texto legible para el usuario. */
export const errorMessage = (e) => {
  const data = e?.response?.data
  if (data?.message) {
    return Array.isArray(data.message) ? data.message.join(' · ') : data.message
  }
  if (e?.response && TRANSITORIO.includes(e.response.status)) {
    return 'El servidor está despertando (plan gratuito). Espera un minuto e intenta de nuevo.'
  }
  if (e?.response) return `Error ${e.response.status} del servidor`
  if (e?.code === 'ERR_NETWORK' || e?.code === 'ECONNABORTED') return 'No se pudo conectar con el servidor (puede estar despertando, intenta de nuevo en un minuto)'
  return e?.message || 'Error inesperado'
}

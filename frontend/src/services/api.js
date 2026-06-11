import axios from 'axios'

const API_URL =
  import.meta.env.VITE_API_URL ||
  'https://90a76c5879.us.serverless.gateways.konggateway.com'

export const createApiClient = (token) => {
  return axios.create({
    baseURL: API_URL,
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })
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

  getLots: (token) => createApiClient(token).get('/api/v1/lots'),
  createLot: (token, data) => createApiClient(token).post('/api/v1/lots', data),

  getEntries: (token) => createApiClient(token).get('/api/v1/entries'),
  createEntry: (token, data) => createApiClient(token).post('/api/v1/entries', data),

  createExit: (token, data) => createApiClient(token).post('/api/v1/exits', data),
}

export const SSE_URL = `${API_URL}/api/v1/inventory/stream`
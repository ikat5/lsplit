import api from './api'

export const register      = (data) => api.post('/api/auth/register', data).then(r => r.data)
export const login         = (data) => api.post('/api/auth/login', data).then(r => r.data)
export const getProfile    = ()     => api.get('/api/users/me').then(r => r.data)
export const updateProfile = (data) => api.put('/api/users/me', data).then(r => r.data)

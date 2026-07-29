import api from './api'

export const createExpense = (eventId, data) => api.post(`/api/events/${eventId}/expenses`, data).then(r => r.data)
export const getExpenses   = (eventId)       => api.get(`/api/events/${eventId}/expenses`).then(r => r.data)
export const deleteExpense = (expenseId)     => api.delete(`/api/expenses/${expenseId}`)

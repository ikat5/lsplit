import api from './api'

export const createExpense = (eventId, data) => api.post(`/events/${eventId}/expenses`, data).then(r => r.data)
export const getExpenses   = (eventId)       => api.get(`/events/${eventId}/expenses`).then(r => r.data)
export const deleteExpense = (expenseId)     => api.delete(`/expenses/${expenseId}`)

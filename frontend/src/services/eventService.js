import api from './api'

export const createEvent  = (groupId, data) => api.post(`/api/groups/${groupId}/events`, data).then(r => r.data)
export const getEvents    = (groupId)       => api.get(`/api/groups/${groupId}/events`).then(r => r.data)
export const getEventById = (eventId)       => api.get(`/api/events/${eventId}`).then(r => r.data)
export const deleteEvent  = (eventId)       => api.delete(`/api/events/${eventId}`)

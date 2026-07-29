import api from './api'

export const createEvent  = (groupId, data) => api.post(`/groups/${groupId}/events`, data).then(r => r.data)
export const getEvents    = (groupId)       => api.get(`/groups/${groupId}/events`).then(r => r.data)
export const getEventById = (eventId)       => api.get(`/events/${eventId}`).then(r => r.data)
export const deleteEvent  = (eventId)       => api.delete(`/events/${eventId}`)

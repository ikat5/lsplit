import api from './api'

export const createGroup      = (data)              => api.post('/api/groups', data).then(r => r.data)
export const getGroups        = ()                  => api.get('/api/groups').then(r => r.data)
export const getGroupById     = (id)                => api.get(`/api/groups/${id}`).then(r => r.data)
export const deleteGroup      = (id)                => api.delete(`/api/groups/${id}`)
export const addMember        = (groupId, data)     => api.post(`/api/groups/${groupId}/members`, data).then(r => r.data)
export const removeMember     = (groupId, userId)   => api.delete(`/api/groups/${groupId}/members/${userId}`)
export const getBalances      = (groupId)           => api.get(`/api/groups/${groupId}/balances`).then(r => r.data)
export const getSettlements   = (groupId)           => api.get(`/api/groups/${groupId}/settlements`).then(r => r.data)
export const createSettlement = (groupId, data)     => api.post(`/api/groups/${groupId}/settlements`, data).then(r => r.data)

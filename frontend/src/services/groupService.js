import api from './api'

export const createGroup      = (data)              => api.post('/groups', data).then(r => r.data)
export const getGroups        = ()                  => api.get('/groups').then(r => r.data)
export const getGroupById     = (id)                => api.get(`/groups/${id}`).then(r => r.data)
export const deleteGroup      = (id)                => api.delete(`/groups/${id}`)
export const addMember        = (groupId, data)     => api.post(`/groups/${groupId}/members`, data).then(r => r.data)
export const removeMember     = (groupId, userId)   => api.delete(`/groups/${groupId}/members/${userId}`)
export const getBalances      = (groupId)           => api.get(`/groups/${groupId}/balances`).then(r => r.data)
export const getSettlements   = (groupId)           => api.get(`/groups/${groupId}/settlements`).then(r => r.data)
export const createSettlement = (groupId, data)     => api.post(`/groups/${groupId}/settlements`, data).then(r => r.data)

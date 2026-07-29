import React, { useState, useEffect, useCallback } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { useAuth } from '../context/AuthContext'
import {
  getGroupById,
  getBalances,
  getSettlements,
  removeMember,
  deleteGroup,
  createSettlement
} from '../services/groupService'
import { getEvents, createEvent } from '../services/eventService'
import MemberList from '../components/MemberList'
import AddMemberModal from '../components/AddMemberModal'
import BalanceSummary from '../components/BalanceSummary'
import SuggestedPayments from '../components/SuggestedPayments'
import { formatCurrency } from '../utils/currencyFormatter'
import { formatDate, formatDateTime } from '../utils/dateFormatter'

export default function GroupView() {
  const { id: groupId } = useParams()
  const navigate = useNavigate()
  const { user: currentUser } = useAuth()

  const [group, setGroup] = useState(null)
  const [balancesData, setBalancesData] = useState(null)
  const [events, setEvents] = useState([])
  const [settlements, setSettlements] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [activeTab, setActiveTab] = useState('members')

  // Add Member modal
  const [showAddMember, setShowAddMember] = useState(false)

  // New Event modal
  const [showNewEvent, setShowNewEvent] = useState(false)
  const [eventCreating, setEventCreating] = useState(false)
  const [eventError, setEventError] = useState('')
  const {
    register: registerEvent,
    handleSubmit: handleEventSubmit,
    reset: resetEvent,
    formState: { errors: eventErrors }
  } = useForm()

  // Settlement modal
  const [showSettlement, setShowSettlement] = useState(false)
  const [settlementPayeeId, setSettlementPayeeId] = useState('')
  const [settlementAmount, setSettlementAmount] = useState('')
  const [settlementError, setSettlementError] = useState('')
  const [settlementLoading, setSettlementLoading] = useState(false)

  const isAdmin = group?.members?.find(m => m.userId === currentUser?.id)?.role === 'ADMIN'

  const loadAll = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const [groupData, balData, evData, settData] = await Promise.all([
        getGroupById(groupId),
        getBalances(groupId),
        getEvents(groupId),
        getSettlements(groupId)
      ])
      setGroup(groupData)
      setBalancesData(balData)
      setEvents(evData)
      setSettlements(
        [...settData].sort((a, b) => new Date(b.settledAt) - new Date(a.settledAt))
      )
      // default payee to first other member
      const others = (groupData.members || []).filter(m => m.userId !== currentUser?.id)
      if (others.length > 0) setSettlementPayeeId(others[0].userId)
    } catch (err) {
      setError(
        err.response?.data?.message ||
        err.response?.data ||
        'Failed to load group data. Please try again.'
      )
    } finally {
      setLoading(false)
    }
  }, [groupId, currentUser?.id])

  useEffect(() => {
    loadAll()
  }, [loadAll])

  // --- Member actions ---
  const handleRemoveMember = async (userId) => {
    try {
      await removeMember(groupId, userId)
      await loadAll()
    } catch (err) {
      setError(
        err.response?.data?.message ||
        err.response?.data ||
        'Failed to remove member.'
      )
    }
  }

  // --- Delete group ---
  const handleDeleteGroup = async () => {
    if (!window.confirm('Delete this group? This cannot be undone.')) return
    try {
      await deleteGroup(groupId)
      navigate('/')
    } catch (err) {
      setError(
        err.response?.data?.message ||
        err.response?.data ||
        'Failed to delete group.'
      )
    }
  }

  // --- New Event ---
  const openNewEvent = () => {
    resetEvent()
    setEventError('')
    setShowNewEvent(true)
  }

  const handleCreateEvent = async (data) => {
    setEventCreating(true)
    setEventError('')
    try {
      await createEvent(groupId, data)
      resetEvent()
      setShowNewEvent(false)
      await loadAll()
    } catch (err) {
      setEventError(
        err.response?.data?.message ||
        err.response?.data ||
        'Failed to create event.'
      )
    } finally {
      setEventCreating(false)
    }
  }

  // --- Settlement ---
  const openSettlement = (prefill) => {
    if (prefill) {
      setSettlementPayeeId(prefill.toUserId)
      setSettlementAmount(String(prefill.amount))
    } else {
      const others = (group?.members || []).filter(m => m.userId !== currentUser?.id)
      setSettlementPayeeId(others[0]?.userId || '')
      setSettlementAmount('')
    }
    setSettlementError('')
    setShowSettlement(true)
  }

  const handleCreateSettlement = async () => {
    setSettlementLoading(true)
    setSettlementError('')
    const amount = parseFloat(settlementAmount)
    if (!settlementPayeeId) {
      setSettlementError('Please select a payee.')
      setSettlementLoading(false)
      return
    }
    if (isNaN(amount) || amount <= 0) {
      setSettlementError('Please enter a valid positive amount.')
      setSettlementLoading(false)
      return
    }
    try {
      await createSettlement(groupId, { payeeId: settlementPayeeId, amount })
      setShowSettlement(false)
      await loadAll()
    } catch (err) {
      setSettlementError(
        err.response?.data?.message ||
        err.response?.data ||
        'Failed to record settlement.'
      )
    } finally {
      setSettlementLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="d-flex justify-content-center py-5">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    )
  }

  if (error && !group) {
    return (
      <div className="container py-4">
        <div className="alert alert-danger">{error}</div>
        <button className="btn btn-secondary" onClick={() => navigate('/')}>
          Back to Dashboard
        </button>
      </div>
    )
  }

  const otherMembers = (group?.members || []).filter(m => m.userId !== currentUser?.id)

  return (
    <div className="container py-4">
      {/* Header */}
      <div className="d-flex flex-wrap justify-content-between align-items-start mb-4 gap-2">
        <div>
          <button
            className="btn btn-link p-0 text-decoration-none text-muted small mb-1"
            onClick={() => navigate('/')}
          >
            &larr; Back to Dashboard
          </button>
          <h2 className="fw-bold mb-0">{group?.name}</h2>
          {group?.description && (
            <p className="text-muted mb-0">{group.description}</p>
          )}
        </div>
        {isAdmin && (
          <button className="btn btn-outline-danger btn-sm" onClick={handleDeleteGroup}>
            Delete Group
          </button>
        )}
      </div>

      {error && (
        <div className="alert alert-danger alert-dismissible" role="alert">
          {error}
          <button type="button" className="btn-close" onClick={() => setError('')} />
        </div>
      )}

      {/* Tabs */}
      <ul className="nav nav-tabs mb-4">
        {[
          { key: 'members', label: 'Members' },
          { key: 'events', label: 'Events' },
          { key: 'balances', label: 'Balances' },
          { key: 'settlements', label: 'Settlements' }
        ].map(tab => (
          <li className="nav-item" key={tab.key}>
            <button
              className={`nav-link${activeTab === tab.key ? ' active' : ''}`}
              onClick={() => setActiveTab(tab.key)}
            >
              {tab.label}
            </button>
          </li>
        ))}
      </ul>

      {/* Members Tab */}
      {activeTab === 'members' && (
        <div>
          <div className="d-flex justify-content-between align-items-center mb-3">
            <h5 className="mb-0">Members ({group?.members?.length || 0})</h5>
            {isAdmin && (
              <button className="btn btn-primary btn-sm" onClick={() => setShowAddMember(true)}>
                + Add Member
              </button>
            )}
          </div>
          <MemberList
            members={group?.members || []}
            isAdmin={isAdmin}
            currentUserId={currentUser?.id}
            onRemove={handleRemoveMember}
          />
        </div>
      )}

      {/* Events Tab */}
      {activeTab === 'events' && (
        <div>
          <div className="d-flex justify-content-between align-items-center mb-3">
            <h5 className="mb-0">Events ({events.length})</h5>
            <button className="btn btn-primary btn-sm" onClick={openNewEvent}>
              + New Event
            </button>
          </div>
          {events.length === 0 ? (
            <div className="text-center py-4 text-muted">
              <p>No events yet.</p>
              <button className="btn btn-outline-primary btn-sm" onClick={openNewEvent}>
                Create the first event
              </button>
            </div>
          ) : (
            <div className="row row-cols-1 row-cols-md-2 g-3">
              {events.map(event => (
                <div className="col" key={event.id}>
                  <div
                    className="card h-100 shadow-sm border-0"
                    style={{ cursor: 'pointer' }}
                    onClick={() => navigate(`/events/${event.id}`, {
                      state: {
                        groupMembers: group?.members || [],
                        groupId
                      }
                    })}
                    onMouseEnter={e => e.currentTarget.style.boxShadow = '0 4px 16px rgba(0,0,0,0.1)'}
                    onMouseLeave={e => e.currentTarget.style.boxShadow = ''}
                  >
                    <div className="card-body">
                      <h6 className="fw-semibold mb-1">{event.title}</h6>
                      <p className="text-muted small mb-2">{formatDate(event.eventDate)}</p>
                      {event.description && (
                        <p className="text-muted small mb-2">{event.description}</p>
                      )}
                      <div className="d-flex justify-content-between align-items-center">
                        <span className="small text-secondary">
                          {event.expenseCount} expense{event.expenseCount !== 1 ? 's' : ''}
                        </span>
                        <span className="fw-semibold">{formatCurrency(event.totalAmount)}</span>
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Balances Tab */}
      {activeTab === 'balances' && (
        <div>
          <h5 className="mb-3">Balance Summary</h5>
          <BalanceSummary balances={balancesData?.userBalances || []} />

          {balancesData?.suggestedPayments?.length > 0 && (
            <>
              <div className="d-flex justify-content-between align-items-center mt-4 mb-2">
                <h6 className="mb-0">Suggested Payments</h6>
                <button
                  className="btn btn-success btn-sm"
                  onClick={() => openSettlement(null)}
                >
                  Record Settlement
                </button>
              </div>
              <SuggestedPayments
                payments={balancesData.suggestedPayments}
                onSettle={openSettlement}
              />
            </>
          )}

          {(!balancesData?.suggestedPayments || balancesData.suggestedPayments.length === 0) && (
            <div className="mt-4 d-flex justify-content-end">
              <button
                className="btn btn-success btn-sm"
                onClick={() => openSettlement(null)}
              >
                Record Settlement
              </button>
            </div>
          )}
        </div>
      )}

      {/* Settlements Tab */}
      {activeTab === 'settlements' && (
        <div>
          <div className="d-flex justify-content-between align-items-center mb-3">
            <h5 className="mb-0">Settlements ({settlements.length})</h5>
            <button
              className="btn btn-success btn-sm"
              onClick={() => openSettlement(null)}
            >
              + Record Settlement
            </button>
          </div>
          {settlements.length === 0 ? (
            <p className="text-muted">No settlements recorded yet.</p>
          ) : (
            <ul className="list-group">
              {settlements.map(s => (
                <li key={s.id} className="list-group-item d-flex justify-content-between align-items-center">
                  <div>
                    <strong>{s.payer?.name}</strong>
                    <span className="mx-2 text-muted">&rarr;</span>
                    <strong>{s.payee?.name}</strong>
                    <span className="ms-2 text-muted small">{formatDateTime(s.settledAt)}</span>
                  </div>
                  <div className="d-flex align-items-center gap-2">
                    <span className="fw-semibold text-success">{formatCurrency(s.amount)}</span>
                    <span className="badge bg-success">{s.status || 'SETTLED'}</span>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}

      {/* Add Member Modal */}
      <AddMemberModal
        groupId={groupId}
        show={showAddMember}
        onClose={() => setShowAddMember(false)}
        onSuccess={() => {
          setShowAddMember(false)
          loadAll()
        }}
      />

      {/* New Event Modal */}
      {showNewEvent && (
        <>
          <div className="modal show d-block" tabIndex="-1" role="dialog">
            <div className="modal-dialog" role="document">
              <div className="modal-content">
                <div className="modal-header">
                  <h5 className="modal-title">New Event</h5>
                  <button
                    type="button"
                    className="btn-close"
                    onClick={() => setShowNewEvent(false)}
                    aria-label="Close"
                  />
                </div>
                <form onSubmit={handleEventSubmit(handleCreateEvent)} noValidate>
                  <div className="modal-body">
                    {eventError && (
                      <div className="alert alert-danger py-2 small">{eventError}</div>
                    )}
                    <div className="mb-3">
                      <label className="form-label">
                        Title <span className="text-danger">*</span>
                      </label>
                      <input
                        type="text"
                        className={`form-control${eventErrors.title ? ' is-invalid' : ''}`}
                        placeholder="e.g. Dinner, Road Trip"
                        {...registerEvent('title', { required: 'Title is required' })}
                      />
                      {eventErrors.title && (
                        <div className="invalid-feedback">{eventErrors.title.message}</div>
                      )}
                    </div>
                    <div className="mb-3">
                      <label className="form-label">
                        Description <span className="text-muted small">(optional)</span>
                      </label>
                      <textarea
                        className="form-control"
                        rows={2}
                        placeholder="Brief description"
                        {...registerEvent('description')}
                      />
                    </div>
                    <div className="mb-3">
                      <label className="form-label">
                        Event date <span className="text-muted small">(optional)</span>
                      </label>
                      <input
                        type="date"
                        className="form-control"
                        {...registerEvent('eventDate')}
                      />
                    </div>
                  </div>
                  <div className="modal-footer">
                    <button
                      type="button"
                      className="btn btn-secondary"
                      onClick={() => setShowNewEvent(false)}
                    >
                      Cancel
                    </button>
                    <button type="submit" className="btn btn-primary" disabled={eventCreating}>
                      {eventCreating ? (
                        <>
                          <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true" />
                          Creating...
                        </>
                      ) : 'Create Event'}
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </div>
          <div className="modal-backdrop show" />
        </>
      )}

      {/* Record Settlement Modal */}
      {showSettlement && (
        <>
          <div className="modal show d-block" tabIndex="-1" role="dialog">
            <div className="modal-dialog" role="document">
              <div className="modal-content">
                <div className="modal-header">
                  <h5 className="modal-title">Record Settlement</h5>
                  <button
                    type="button"
                    className="btn-close"
                    onClick={() => setShowSettlement(false)}
                    aria-label="Close"
                  />
                </div>
                <div className="modal-body">
                  {settlementError && (
                    <div className="alert alert-danger py-2 small">{settlementError}</div>
                  )}
                  <p className="text-muted small mb-3">
                    Record a payment you made to another group member.
                  </p>
                  <div className="mb-3">
                    <label className="form-label">
                      Pay to <span className="text-danger">*</span>
                    </label>
                    <select
                      className="form-select"
                      value={settlementPayeeId}
                      onChange={e => setSettlementPayeeId(e.target.value)}
                    >
                      {otherMembers.map(m => (
                        <option key={m.userId} value={m.userId}>{m.name}</option>
                      ))}
                    </select>
                  </div>
                  <div className="mb-3">
                    <label className="form-label">
                      Amount (USD) <span className="text-danger">*</span>
                    </label>
                    <input
                      type="number"
                      step="0.01"
                      min="0.01"
                      className="form-control"
                      placeholder="0.00"
                      value={settlementAmount}
                      onChange={e => setSettlementAmount(e.target.value)}
                    />
                  </div>
                </div>
                <div className="modal-footer">
                  <button
                    type="button"
                    className="btn btn-secondary"
                    onClick={() => setShowSettlement(false)}
                  >
                    Cancel
                  </button>
                  <button
                    type="button"
                    className="btn btn-success"
                    onClick={handleCreateSettlement}
                    disabled={settlementLoading}
                  >
                    {settlementLoading ? (
                      <>
                        <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true" />
                        Recording...
                      </>
                    ) : 'Record Settlement'}
                  </button>
                </div>
              </div>
            </div>
          </div>
          <div className="modal-backdrop show" />
        </>
      )}
    </div>
  )
}

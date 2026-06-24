import React, { useState, useEffect, useCallback } from 'react'
import { useParams, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { getEventById, deleteEvent } from '../services/eventService'
import { deleteExpense } from '../services/expenseService'
import ExpenseCard from '../components/ExpenseCard'
import AddExpenseModal from '../components/AddExpenseModal'
import { formatCurrency } from '../utils/currencyFormatter'
import { formatDate } from '../utils/dateFormatter'

export default function EventView() {
  const { id: eventId } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const { user: currentUser } = useAuth()

  const [event, setEvent] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [showAddExpense, setShowAddExpense] = useState(false)

  // groupMembers and groupId may be passed as navigation state from GroupView
  const groupMembers = location.state?.groupMembers || []
  const groupId = location.state?.groupId

  const loadEvent = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const data = await getEventById(eventId)
      setEvent(data)
    } catch (err) {
      setError(
        err.response?.data?.message ||
        err.response?.data ||
        'Failed to load event. Please try again.'
      )
    } finally {
      setLoading(false)
    }
  }, [eventId])

  useEffect(() => {
    loadEvent()
  }, [loadEvent])

  const handleDeleteExpense = async (expenseId) => {
    try {
      await deleteExpense(expenseId)
      await loadEvent()
    } catch (err) {
      setError(
        err.response?.data?.message ||
        err.response?.data ||
        'Failed to delete expense.'
      )
    }
  }

  const handleDeleteEvent = async () => {
    if (!window.confirm('Delete this event and all its expenses? This cannot be undone.')) return
    try {
      await deleteEvent(eventId)
      if (groupId) {
        navigate(`/groups/${groupId}`)
      } else {
        navigate('/')
      }
    } catch (err) {
      setError(
        err.response?.data?.message ||
        err.response?.data ||
        'Failed to delete event.'
      )
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

  if (error && !event) {
    return (
      <div className="container py-4">
        <div className="alert alert-danger">{error}</div>
        <button className="btn btn-secondary" onClick={() => navigate(-1)}>
          Go Back
        </button>
      </div>
    )
  }

  return (
    <div className="container py-4">
      {/* Header */}
      <div className="mb-4">
        <button
          className="btn btn-link p-0 text-decoration-none text-muted small mb-1"
          onClick={() => groupId ? navigate(`/groups/${groupId}`) : navigate(-1)}
        >
          &larr; Back to Group
        </button>

        <div className="d-flex flex-wrap justify-content-between align-items-start gap-2 mt-1">
          <div>
            <h2 className="fw-bold mb-0">{event?.title}</h2>
            {event?.description && (
              <p className="text-muted mb-1">{event.description}</p>
            )}
            <div className="d-flex flex-wrap gap-3 small text-muted mt-1">
              {event?.eventDate && (
                <span>&#128197; {formatDate(event.eventDate)}</span>
              )}
              <span>&#128179; {event?.expenseCount || 0} expense{(event?.expenseCount || 0) !== 1 ? 's' : ''}</span>
              <span className="fw-semibold text-dark">
                Total: {formatCurrency(event?.totalAmount || 0)}
              </span>
            </div>
          </div>
          <div className="d-flex gap-2">
            <button
              className="btn btn-primary btn-sm"
              onClick={() => setShowAddExpense(true)}
            >
              + Add Expense
            </button>
            <button
              className="btn btn-outline-danger btn-sm"
              onClick={handleDeleteEvent}
            >
              Delete Event
            </button>
          </div>
        </div>
      </div>

      {error && (
        <div className="alert alert-danger alert-dismissible" role="alert">
          {error}
          <button type="button" className="btn-close" onClick={() => setError('')} />
        </div>
      )}

      {/* Expenses list */}
      {(!event?.expenses || event.expenses.length === 0) ? (
        <div className="text-center py-5 text-muted">
          <p className="fs-5">No expenses yet.</p>
          <p className="small">Add an expense to start tracking who owes what.</p>
          <button
            className="btn btn-outline-primary"
            onClick={() => setShowAddExpense(true)}
          >
            Add first expense
          </button>
        </div>
      ) : (
        <div>
          {event.expenses.map(expense => (
            <ExpenseCard
              key={expense.id}
              expense={expense}
              currentUserId={currentUser?.id}
              onDelete={handleDeleteExpense}
              groupMembers={groupMembers}
            />
          ))}
        </div>
      )}

      {/* Add Expense Modal */}
      {showAddExpense && (
        <AddExpenseModal
          eventId={eventId}
          groupMembers={groupMembers.length > 0 ? groupMembers : (event?.expenses?.[0] ? [] : [])}
          show={showAddExpense}
          onClose={() => setShowAddExpense(false)}
          onSuccess={() => {
            setShowAddExpense(false)
            loadEvent()
          }}
        />
      )}
    </div>
  )
}

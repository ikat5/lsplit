import React, { useState, useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { createExpense } from '../services/expenseService'
import { formatAmount } from '../utils/currencyFormatter'

export default function AddExpenseModal({ eventId, groupMembers, onSuccess, onClose, show }) {
  const {
    register,
    handleSubmit,
    watch,
    reset,
    formState: { errors }
  } = useForm({
    defaultValues: {
      description: '',
      amount: '',
      splitType: 'EQUAL',
      paidByUserId: groupMembers?.[0]?.userId || ''
    }
  })

  const splitType = watch('splitType')

  // EQUAL: set of selected participant userIds (default all)
  const [selectedParticipants, setSelectedParticipants] = useState(
    new Set(groupMembers.map(m => m.userId))
  )

  // EXACT / PERCENTAGE: map of userId -> string value
  const [participantShares, setParticipantShares] = useState(() => {
    const init = {}
    groupMembers.forEach(m => { init[m.userId] = '' })
    return init
  })

  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (show) {
      reset({
        description: '',
        amount: '',
        splitType: 'EQUAL',
        paidByUserId: groupMembers?.[0]?.userId || ''
      })
      setSelectedParticipants(new Set(groupMembers.map(m => m.userId)))
      const init = {}
      groupMembers.forEach(m => { init[m.userId] = '' })
      setParticipantShares(init)
      setError('')
    }
  }, [show, groupMembers, reset])

  const toggleParticipant = (userId) => {
    setSelectedParticipants(prev => {
      const next = new Set(prev)
      if (next.has(userId)) {
        next.delete(userId)
      } else {
        next.add(userId)
      }
      return next
    })
  }

  const setShare = (userId, value) => {
    setParticipantShares(prev => ({ ...prev, [userId]: value }))
  }

  const percentageTotal = groupMembers
    .filter(m => selectedParticipants.has(m.userId))
    .reduce((sum, m) => sum + (parseFloat(participantShares[m.userId]) || 0), 0)

  const onSubmit = async (formData) => {
    setLoading(true)
    setError('')

    const amount = parseFloat(formData.amount)
    const base = {
      description: formData.description,
      amount,
      splitType: formData.splitType,
      paidByUserId: formData.paidByUserId
    }

    let body

    try {
      if (formData.splitType === 'EQUAL') {
        const participantIds = [...selectedParticipants]
        if (participantIds.length === 0) {
          setError('Select at least one participant.')
          setLoading(false)
          return
        }
        body = { ...base, participantIds }
      } else if (formData.splitType === 'EXACT') {
        const shares = groupMembers
          .filter(m => selectedParticipants.has(m.userId))
          .map(m => ({ userId: m.userId, value: parseFloat(participantShares[m.userId]) || 0 }))
        if (shares.length === 0) {
          setError('Select at least one participant.')
          setLoading(false)
          return
        }
        const sharesTotal = shares.reduce((s, x) => s + x.value, 0)
        if (Math.abs(sharesTotal - amount) > 0.01) {
          setError(`Exact amounts must sum to ${formatAmount(amount)} (currently ${formatAmount(sharesTotal)}).`)
          setLoading(false)
          return
        }
        body = { ...base, shares }
      } else {
        // PERCENTAGE
        const shares = groupMembers
          .filter(m => selectedParticipants.has(m.userId))
          .map(m => ({ userId: m.userId, value: parseFloat(participantShares[m.userId]) || 0 }))
        if (shares.length === 0) {
          setError('Select at least one participant.')
          setLoading(false)
          return
        }
        const total = shares.reduce((s, x) => s + x.value, 0)
        if (Math.abs(total - 100) > 0.01) {
          setError(`Percentages must sum to 100% (currently ${formatAmount(total)}%).`)
          setLoading(false)
          return
        }
        body = { ...base, shares }
      }

      await createExpense(eventId, body)
      reset()
      onSuccess()
    } catch (err) {
      setError(
        err.response?.data?.message ||
        err.response?.data ||
        'Failed to add expense. Please try again.'
      )
    } finally {
      setLoading(false)
    }
  }

  if (!show) return null

  return (
    <>
      <div className="modal show d-block" tabIndex="-1" role="dialog">
        <div className="modal-dialog modal-lg" role="document">
          <div className="modal-content">
            <div className="modal-header">
              <h5 className="modal-title">Add Expense</h5>
              <button type="button" className="btn-close" onClick={onClose} aria-label="Close" />
            </div>
            <form onSubmit={handleSubmit(onSubmit)} noValidate>
              <div className="modal-body">
                {error && (
                  <div className="alert alert-danger py-2 small">{error}</div>
                )}

                {/* Description */}
                <div className="mb-3">
                  <label className="form-label">Description <span className="text-danger">*</span></label>
                  <input
                    type="text"
                    className={`form-control${errors.description ? ' is-invalid' : ''}`}
                    placeholder="e.g. Dinner at Restaurant"
                    {...register('description', { required: 'Description is required' })}
                  />
                  {errors.description && (
                    <div className="invalid-feedback">{errors.description.message}</div>
                  )}
                </div>

                {/* Amount */}
                <div className="mb-3">
                  <label className="form-label">Amount (USD) <span className="text-danger">*</span></label>
                  <input
                    type="number"
                    step="0.01"
                    min="0.01"
                    className={`form-control${errors.amount ? ' is-invalid' : ''}`}
                    placeholder="0.00"
                    {...register('amount', {
                      required: 'Amount is required',
                      validate: v => (parseFloat(v) > 0) || 'Amount must be positive'
                    })}
                  />
                  {errors.amount && (
                    <div className="invalid-feedback">{errors.amount.message}</div>
                  )}
                </div>

                <div className="row mb-3">
                  {/* Paid By */}
                  <div className="col-md-6">
                    <label className="form-label">Paid by <span className="text-danger">*</span></label>
                    <select
                      className="form-select"
                      {...register('paidByUserId', { required: true })}
                    >
                      {groupMembers.map(m => (
                        <option key={m.userId} value={m.userId}>{m.name}</option>
                      ))}
                    </select>
                  </div>

                  {/* Split Type */}
                  <div className="col-md-6">
                    <label className="form-label">Split type</label>
                    <select className="form-select" {...register('splitType')}>
                      <option value="EQUAL">Equal</option>
                      <option value="EXACT">Exact amounts</option>
                      <option value="PERCENTAGE">Percentage</option>
                    </select>
                  </div>
                </div>

                {/* EQUAL: participant checkboxes */}
                {splitType === 'EQUAL' && (
                  <div className="mb-3">
                    <label className="form-label">Participants</label>
                    <div className="border rounded p-2">
                      {groupMembers.map(m => (
                        <div className="form-check" key={m.userId}>
                          <input
                            className="form-check-input"
                            type="checkbox"
                            id={`eq-${m.userId}`}
                            checked={selectedParticipants.has(m.userId)}
                            onChange={() => toggleParticipant(m.userId)}
                          />
                          <label className="form-check-label" htmlFor={`eq-${m.userId}`}>
                            {m.name}
                          </label>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {/* EXACT: per-participant amount inputs */}
                {splitType === 'EXACT' && (
                  <div className="mb-3">
                    <label className="form-label">Exact amounts per participant</label>
                    <div className="border rounded p-2">
                      {groupMembers.map(m => (
                        <div className="row align-items-center mb-2" key={m.userId}>
                          <div className="col-auto">
                            <input
                              className="form-check-input"
                              type="checkbox"
                              id={`ex-chk-${m.userId}`}
                              checked={selectedParticipants.has(m.userId)}
                              onChange={() => toggleParticipant(m.userId)}
                            />
                          </div>
                          <label className="col form-check-label" htmlFor={`ex-chk-${m.userId}`}>
                            {m.name}
                          </label>
                          {selectedParticipants.has(m.userId) && (
                            <div className="col-4">
                              <input
                                type="number"
                                step="0.01"
                                min="0"
                                className="form-control form-control-sm"
                                placeholder="0.00"
                                value={participantShares[m.userId]}
                                onChange={e => setShare(m.userId, e.target.value)}
                              />
                            </div>
                          )}
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {/* PERCENTAGE: per-participant percentage inputs */}
                {splitType === 'PERCENTAGE' && (
                  <div className="mb-3">
                    <label className="form-label">
                      Percentages per participant
                      <span className={`ms-2 badge ${Math.abs(percentageTotal - 100) < 0.01 ? 'bg-success' : 'bg-warning text-dark'}`}>
                        Total: {formatAmount(percentageTotal)}%
                      </span>
                    </label>
                    <div className="border rounded p-2">
                      {groupMembers.map(m => (
                        <div className="row align-items-center mb-2" key={m.userId}>
                          <div className="col-auto">
                            <input
                              className="form-check-input"
                              type="checkbox"
                              id={`pct-chk-${m.userId}`}
                              checked={selectedParticipants.has(m.userId)}
                              onChange={() => toggleParticipant(m.userId)}
                            />
                          </div>
                          <label className="col form-check-label" htmlFor={`pct-chk-${m.userId}`}>
                            {m.name}
                          </label>
                          {selectedParticipants.has(m.userId) && (
                            <div className="col-4">
                              <div className="input-group input-group-sm">
                                <input
                                  type="number"
                                  step="0.01"
                                  min="0"
                                  max="100"
                                  className="form-control"
                                  placeholder="0"
                                  value={participantShares[m.userId]}
                                  onChange={e => setShare(m.userId, e.target.value)}
                                />
                                <span className="input-group-text">%</span>
                              </div>
                            </div>
                          )}
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>

              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={onClose}>
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary" disabled={loading}>
                  {loading ? (
                    <>
                      <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true" />
                      Saving...
                    </>
                  ) : 'Add Expense'}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
      <div className="modal-backdrop show" />
    </>
  )
}

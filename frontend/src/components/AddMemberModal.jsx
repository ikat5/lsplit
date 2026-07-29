import React, { useState, useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { addMember } from '../services/groupService'

export default function AddMemberModal({ groupId, onSuccess, onClose, show }) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors }
  } = useForm()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (show) {
      reset()
      setError('')
    }
  }, [show, reset])

  const onSubmit = async ({ email }) => {
    setLoading(true)
    setError('')
    try {
      await addMember(groupId, { email })
      reset()
      onSuccess()
    } catch (err) {
      setError(
        err.response?.data?.message ||
        err.response?.data ||
        'User not found or already a member.'
      )
    } finally {
      setLoading(false)
    }
  }

  if (!show) return null

  return (
    <>
      <div className="modal show d-block" tabIndex="-1" role="dialog">
        <div className="modal-dialog" role="document">
          <div className="modal-content">
            <div className="modal-header">
              <h5 className="modal-title">Add Member</h5>
              <button type="button" className="btn-close" onClick={onClose} aria-label="Close" />
            </div>
            <form onSubmit={handleSubmit(onSubmit)} noValidate>
              <div className="modal-body">
                {error && (
                  <div className="alert alert-danger py-2 small">{error}</div>
                )}
                <div className="mb-3">
                  <label htmlFor="memberEmail" className="form-label">
                    Email address
                  </label>
                  <input
                    id="memberEmail"
                    type="email"
                    className={`form-control${errors.email ? ' is-invalid' : ''}`}
                    placeholder="member@example.com"
                    {...register('email', {
                      required: 'Email is required',
                      pattern: {
                        value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
                        message: 'Enter a valid email address'
                      }
                    })}
                  />
                  {errors.email && (
                    <div className="invalid-feedback">{errors.email.message}</div>
                  )}
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={onClose}>
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary" disabled={loading}>
                  {loading ? (
                    <>
                      <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true" />
                      Adding...
                    </>
                  ) : 'Add Member'}
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

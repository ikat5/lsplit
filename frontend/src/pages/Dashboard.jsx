import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { getGroups, createGroup } from '../services/groupService'
import { formatCurrency } from '../utils/currencyFormatter'

export default function Dashboard() {
  const navigate = useNavigate()
  const [groups, setGroups] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [showModal, setShowModal] = useState(false)
  const [creating, setCreating] = useState(false)
  const [createError, setCreateError] = useState('')

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors }
  } = useForm()

  const fetchGroups = async () => {
    setLoading(true)
    setError('')
    try {
      const data = await getGroups()
      setGroups(data)
    } catch (err) {
      setError(
        err.response?.data?.message ||
        err.response?.data ||
        'Failed to load groups. Please refresh.'
      )
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchGroups()
  }, [])

  const openModal = () => {
    reset()
    setCreateError('')
    setShowModal(true)
  }

  const closeModal = () => {
    setShowModal(false)
    reset()
    setCreateError('')
  }

  const onCreateGroup = async (data) => {
    setCreating(true)
    setCreateError('')
    try {
      await createGroup(data)
      closeModal()
      fetchGroups()
    } catch (err) {
      setCreateError(
        err.response?.data?.message ||
        err.response?.data ||
        'Failed to create group. Please try again.'
      )
    } finally {
      setCreating(false)
    }
  }

  const getBalanceStyle = (balance) => {
    if (balance > 0) return 'text-success'
    if (balance < 0) return 'text-danger'
    return 'text-secondary'
  }

  const getBalanceLabel = (balance) => {
    if (balance > 0) return `You get back ${formatCurrency(balance)}`
    if (balance < 0) return `You owe ${formatCurrency(Math.abs(balance))}`
    return 'All settled'
  }

  return (
    <div className="container py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="fw-bold mb-0">My Groups</h2>
          <p className="text-muted small mb-0">Manage your bill-splitting groups</p>
        </div>
        <button className="btn btn-primary" onClick={openModal}>
          + New Group
        </button>
      </div>

      {error && (
        <div className="alert alert-danger" role="alert">
          {error}
        </div>
      )}

      {loading ? (
        <div className="d-flex justify-content-center py-5">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
        </div>
      ) : groups.length === 0 ? (
        <div className="text-center py-5 text-muted">
          <p className="fs-5">You have no groups yet.</p>
          <p className="small">Create a group to start splitting bills with friends!</p>
          <button className="btn btn-outline-primary" onClick={openModal}>
            Create your first group
          </button>
        </div>
      ) : (
        <div className="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4">
          {groups.map(group => (
            <div className="col" key={group.id}>
              <div
                className="card h-100 shadow-sm border-0 group-card"
                style={{ cursor: 'pointer', transition: 'box-shadow 0.2s' }}
                onClick={() => navigate(`/groups/${group.id}`)}
                onMouseEnter={e => e.currentTarget.style.boxShadow = '0 4px 16px rgba(0,0,0,0.12)'}
                onMouseLeave={e => e.currentTarget.style.boxShadow = ''}
              >
                <div className="card-body">
                  <div className="d-flex align-items-start justify-content-between mb-2">
                    <h5 className="card-title mb-0 fw-semibold">{group.name}</h5>
                    <span className="badge bg-light text-dark border">
                      {group.memberCount} member{group.memberCount !== 1 ? 's' : ''}
                    </span>
                  </div>
                  {group.description && (
                    <p className="card-text text-muted small mb-3">{group.description}</p>
                  )}
                  <div className={`fw-semibold small ${getBalanceStyle(group.myNetBalance)}`}>
                    {getBalanceLabel(group.myNetBalance)}
                  </div>
                </div>
                <div className="card-footer bg-transparent border-top-0 text-end">
                  <span className="text-primary small fw-semibold">View &rarr;</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* New Group Modal */}
      {showModal && (
        <>
          <div className="modal show d-block" tabIndex="-1" role="dialog">
            <div className="modal-dialog" role="document">
              <div className="modal-content">
                <div className="modal-header">
                  <h5 className="modal-title">Create New Group</h5>
                  <button type="button" className="btn-close" onClick={closeModal} aria-label="Close" />
                </div>
                <form onSubmit={handleSubmit(onCreateGroup)} noValidate>
                  <div className="modal-body">
                    {createError && (
                      <div className="alert alert-danger py-2 small">{createError}</div>
                    )}
                    <div className="mb-3">
                      <label htmlFor="groupName" className="form-label">
                        Group name <span className="text-danger">*</span>
                      </label>
                      <input
                        id="groupName"
                        type="text"
                        className={`form-control${errors.name ? ' is-invalid' : ''}`}
                        placeholder="e.g. Weekend Trip, Roommates"
                        {...register('name', { required: 'Group name is required' })}
                      />
                      {errors.name && (
                        <div className="invalid-feedback">{errors.name.message}</div>
                      )}
                    </div>
                    <div className="mb-3">
                      <label htmlFor="groupDescription" className="form-label">
                        Description <span className="text-muted small">(optional)</span>
                      </label>
                      <textarea
                        id="groupDescription"
                        className="form-control"
                        rows={3}
                        placeholder="What is this group for?"
                        {...register('description')}
                      />
                    </div>
                  </div>
                  <div className="modal-footer">
                    <button type="button" className="btn btn-secondary" onClick={closeModal}>
                      Cancel
                    </button>
                    <button type="submit" className="btn btn-primary" disabled={creating}>
                      {creating ? (
                        <>
                          <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true" />
                          Creating...
                        </>
                      ) : 'Create Group'}
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </div>
          <div className="modal-backdrop show" />
        </>
      )}
    </div>
  )
}

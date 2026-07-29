import React, { useState } from 'react'
import { formatCurrency } from '../utils/currencyFormatter'
import { formatDateTime } from '../utils/dateFormatter'

export default function ExpenseCard({ expense, currentUserId, onDelete, groupMembers }) {
  const [showShares, setShowShares] = useState(false)

  const isAdmin = groupMembers?.find(m => m.userId === currentUserId)?.role === 'ADMIN'
  const canDelete = expense.paidBy?.id === currentUserId || isAdmin

  const handleDelete = () => {
    if (window.confirm(`Delete expense "${expense.description}"?`)) {
      onDelete(expense.id)
    }
  }

  return (
    <div className="card mb-3 shadow-sm">
      <div className="card-body">
        <div className="d-flex justify-content-between align-items-start">
          <div>
            <h6 className="card-title mb-1 fw-semibold">{expense.description}</h6>
            <p className="mb-0 text-muted small">
              Paid by <strong>{expense.paidBy?.name || 'Unknown'}</strong>
              {' · '}
              {formatDateTime(expense.createdAt)}
            </p>
            <p className="mb-0 small text-secondary">
              Split: <span className="badge bg-light text-dark border">{expense.splitType}</span>
            </p>
          </div>
          <div className="text-end">
            <span className="fs-5 fw-bold text-dark">{formatCurrency(expense.amount)}</span>
            <div className="mt-1 d-flex gap-2 justify-content-end">
              <button
                className="btn btn-sm btn-outline-secondary"
                onClick={() => setShowShares(s => !s)}
              >
                {showShares ? 'Hide' : 'Show'} shares
              </button>
              {canDelete && (
                <button
                  className="btn btn-sm btn-outline-danger"
                  onClick={handleDelete}
                >
                  Delete
                </button>
              )}
            </div>
          </div>
        </div>

        {showShares && expense.shares && expense.shares.length > 0 && (
          <div className="mt-3">
            <table className="table table-sm table-bordered mb-0">
              <thead className="table-light">
                <tr>
                  <th>Member</th>
                  <th>Share</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {expense.shares.map((share, idx) => (
                  <tr key={share.userId || idx}>
                    <td>{share.name}</td>
                    <td>{formatCurrency(share.shareAmount)}</td>
                    <td>
                      {share.isSettled ? (
                        <span className="badge bg-success">&#10003; Settled</span>
                      ) : (
                        <span className="badge bg-warning text-dark">Pending</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {showShares && (!expense.shares || expense.shares.length === 0) && (
          <p className="text-muted small mt-2 mb-0">No share breakdown available.</p>
        )}
      </div>
    </div>
  )
}

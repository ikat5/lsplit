import React from 'react'
import { formatCurrency } from '../utils/currencyFormatter'

export default function SuggestedPayments({ payments, onSettle }) {
  if (!payments || payments.length === 0) {
    return <p className="text-muted mt-2">No payments needed — everyone is settled up!</p>
  }

  return (
    <ul className="list-group">
      {payments.map((p, idx) => (
        <li
          key={idx}
          className="list-group-item d-flex justify-content-between align-items-center"
        >
          <span>
            <strong>{p.fromName}</strong>
            <span className="mx-2 text-muted">&rarr;</span>
            <strong>{p.toName}</strong>
            <span className="ms-2 text-muted">{formatCurrency(p.amount)}</span>
          </span>
          <button
            className="btn btn-sm btn-outline-primary"
            onClick={() => onSettle(p)}
          >
            Settle
          </button>
        </li>
      ))}
    </ul>
  )
}

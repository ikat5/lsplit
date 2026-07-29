import React from 'react'
import { formatCurrency } from '../utils/currencyFormatter'

export default function BalanceSummary({ balances }) {
  if (!balances || balances.length === 0) {
    return <p className="text-muted">No balance data available.</p>
  }

  return (
    <div className="table-responsive">
      <table className="table table-hover align-middle">
        <thead className="table-light">
          <tr>
            <th>Member</th>
            <th>Net Balance</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          {balances.map(b => (
            <tr key={b.userId}>
              <td>{b.name}</td>
              <td className={b.netBalance > 0 ? 'text-success' : b.netBalance < 0 ? 'text-danger' : 'text-secondary'}>
                {formatCurrency(Math.abs(b.netBalance))}
              </td>
              <td>
                {b.netBalance > 0 ? (
                  <span className="badge bg-success">
                    Gets back {formatCurrency(b.netBalance)}
                  </span>
                ) : b.netBalance < 0 ? (
                  <span className="badge bg-danger">
                    Owes {formatCurrency(Math.abs(b.netBalance))}
                  </span>
                ) : (
                  <span className="badge bg-secondary">Settled</span>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

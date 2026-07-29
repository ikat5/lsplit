import React from 'react'

export default function MemberList({ members, isAdmin, currentUserId, onRemove }) {
  if (!members || members.length === 0) {
    return <p className="text-muted">No members yet.</p>
  }

  return (
    <div className="table-responsive">
      <table className="table table-hover align-middle">
        <thead className="table-light">
          <tr>
            <th style={{ width: 48 }}>Avatar</th>
            <th>Name</th>
            <th>Email</th>
            <th>Role</th>
            {isAdmin && <th>Actions</th>}
          </tr>
        </thead>
        <tbody>
          {members.map(member => (
            <tr key={member.userId}>
              <td>
                <div
                  className="rounded-circle bg-primary text-white d-flex align-items-center justify-content-center fw-bold"
                  style={{ width: 36, height: 36, fontSize: 16 }}
                >
                  {(member.name || '?').charAt(0).toUpperCase()}
                </div>
              </td>
              <td>{member.name}</td>
              <td>{member.email}</td>
              <td>
                {member.role === 'ADMIN' ? (
                  <span className="badge bg-primary">ADMIN</span>
                ) : (
                  <span className="badge bg-secondary">MEMBER</span>
                )}
              </td>
              {isAdmin && (
                <td>
                  {member.userId !== currentUserId && (
                    <button
                      className="btn btn-sm btn-outline-danger"
                      onClick={() => {
                        if (window.confirm(`Remove ${member.name} from the group?`)) {
                          onRemove(member.userId)
                        }
                      }}
                    >
                      Remove
                    </button>
                  )}
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

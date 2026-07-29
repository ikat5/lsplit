import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [navOpen, setNavOpen] = useState(false)

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <nav className="navbar navbar-expand-md navbar-dark bg-primary">
      <div className="container">
        <Link className="navbar-brand fw-bold" to="/">
          L-Split
        </Link>

        <button
          className="navbar-toggler"
          type="button"
          onClick={() => setNavOpen(o => !o)}
          aria-expanded={navOpen}
          aria-label="Toggle navigation"
        >
          <span className="navbar-toggler-icon" />
        </button>

        <div className={`collapse navbar-collapse${navOpen ? ' show' : ''}`}>
          <ul className="navbar-nav ms-auto align-items-md-center">
            {user ? (
              <>
                <li className="nav-item">
                  <span className="nav-link text-white-50">
                    Hello, <strong className="text-white">{user.name}</strong>
                  </span>
                </li>
                <li className="nav-item">
                  <button
                    className="btn btn-outline-light btn-sm ms-md-2 mt-2 mt-md-0"
                    onClick={handleLogout}
                  >
                    Logout
                  </button>
                </li>
              </>
            ) : (
              <>
                <li className="nav-item">
                  <Link className="nav-link" to="/login">
                    Login
                  </Link>
                </li>
                <li className="nav-item">
                  <Link className="nav-link" to="/register">
                    Register
                  </Link>
                </li>
              </>
            )}
          </ul>
        </div>
      </div>
    </nav>
  )
}

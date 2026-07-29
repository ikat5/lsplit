import React, { createContext, useContext, useState, useEffect } from 'react'
import { getProfile } from '../services/authService'

const AuthContext = createContext(null)

function parseJwt(token) {
  try {
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    const json = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    )
    return JSON.parse(json)
  } catch {
    return null
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [token, setToken] = useState(null)
  // True until the stored session (if any) has been restored, so protected
  // routes don't redirect to /login on a hard refresh.
  const [initializing, setInitializing] = useState(true)

  useEffect(() => {
    const storedToken = localStorage.getItem('token')
    if (storedToken) {
      const payload = parseJwt(storedToken)
      if (payload && payload.exp * 1000 > Date.now()) {
        setToken(storedToken)
        setUser({
          id: payload.userId,
          name: payload.name || payload.sub,
          email: payload.sub
        })
        // The JWT doesn't carry the display name — fetch the real profile
        getProfile()
          .then(profile => {
            setUser(prev => prev ? { ...prev, id: profile.id, name: profile.name, email: profile.email } : prev)
          })
          .catch(() => { /* keep the token-derived user; interceptor handles 401 */ })
      } else {
        localStorage.removeItem('token')
      }
    }
    setInitializing(false)
  }, [])

  const login = (authResponse) => {
    localStorage.setItem('token', authResponse.token)
    setToken(authResponse.token)
    setUser({
      id: authResponse.userId,
      name: authResponse.name,
      email: authResponse.email
    })
  }

  const logout = () => {
    localStorage.removeItem('token')
    setToken(null)
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, token, initializing, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}

export default AuthContext

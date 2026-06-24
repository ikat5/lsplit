import React, { createContext, useContext, useState, useEffect } from 'react'

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
      } else {
        localStorage.removeItem('token')
      }
    }
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
    <AuthContext.Provider value={{ user, token, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}

export default AuthContext

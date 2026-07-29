import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { login as loginService } from '../services/authService'
import { useAuth } from '../context/AuthContext'

export default function Login() {
  const navigate = useNavigate()
  const { login } = useAuth()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const {
    register,
    handleSubmit,
    formState: { errors }
  } = useForm()

  const onSubmit = async (data) => {
    setLoading(true)
    setError('')
    try {
      const authResponse = await loginService(data)
      login(authResponse)
      navigate('/')
    } catch (err) {
      setError(
        err.response?.data?.message ||
        err.response?.data ||
        'Invalid email or password. Please try again.'
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="container py-5">
      <div className="row justify-content-center">
        <div className="col-12 col-sm-10 col-md-6 col-lg-5">
          <div className="card shadow-sm">
            <div className="card-body p-4">
              <h2 className="card-title mb-1 text-center fw-bold">Welcome back</h2>
              <p className="text-center text-muted mb-4 small">Sign in to your L-Split account</p>

              {error && (
                <div className="alert alert-danger py-2 small" role="alert">
                  {error}
                </div>
              )}

              <form onSubmit={handleSubmit(onSubmit)} noValidate>
                <div className="mb-3">
                  <label htmlFor="email" className="form-label">Email address</label>
                  <input
                    id="email"
                    type="email"
                    className={`form-control${errors.email ? ' is-invalid' : ''}`}
                    placeholder="you@example.com"
                    autoComplete="email"
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

                <div className="mb-4">
                  <label htmlFor="password" className="form-label">Password</label>
                  <input
                    id="password"
                    type="password"
                    className={`form-control${errors.password ? ' is-invalid' : ''}`}
                    placeholder="Your password"
                    autoComplete="current-password"
                    {...register('password', {
                      required: 'Password is required',
                      minLength: { value: 6, message: 'Password must be at least 6 characters' }
                    })}
                  />
                  {errors.password && (
                    <div className="invalid-feedback">{errors.password.message}</div>
                  )}
                </div>

                <button
                  type="submit"
                  className="btn btn-primary w-100"
                  disabled={loading}
                >
                  {loading ? (
                    <>
                      <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true" />
                      Signing in...
                    </>
                  ) : 'Sign In'}
                </button>
              </form>

              <hr className="my-4" />
              <p className="text-center mb-0 small">
                Don&apos;t have an account?{' '}
                <Link to="/register" className="fw-semibold">
                  Create one
                </Link>
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

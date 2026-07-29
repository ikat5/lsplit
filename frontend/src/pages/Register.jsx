import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { register as registerService } from '../services/authService'
import { useAuth } from '../context/AuthContext'

export default function Register() {
  const navigate = useNavigate()
  const { login } = useAuth()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors }
  } = useForm()

  const password = watch('password')

  const onSubmit = async (data) => {
    setLoading(true)
    setError('')
    try {
      const { confirmPassword, ...payload } = data
      // Only send phone if provided
      if (!payload.phone) delete payload.phone
      const authResponse = await registerService(payload)
      login(authResponse)
      navigate('/')
    } catch (err) {
      setError(
        err.response?.data?.message ||
        err.response?.data ||
        'Registration failed. This email may already be in use.'
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="container py-5">
      <div className="row justify-content-center">
        <div className="col-12 col-sm-10 col-md-7 col-lg-6">
          <div className="card shadow-sm">
            <div className="card-body p-4">
              <h2 className="card-title mb-1 text-center fw-bold">Create your account</h2>
              <p className="text-center text-muted mb-4 small">Start splitting bills with friends</p>

              {error && (
                <div className="alert alert-danger py-2 small" role="alert">
                  {error}
                </div>
              )}

              <form onSubmit={handleSubmit(onSubmit)} noValidate>
                {/* Name */}
                <div className="mb-3">
                  <label htmlFor="name" className="form-label">Full name <span className="text-danger">*</span></label>
                  <input
                    id="name"
                    type="text"
                    className={`form-control${errors.name ? ' is-invalid' : ''}`}
                    placeholder="Jane Doe"
                    autoComplete="name"
                    {...register('name', {
                      required: 'Name is required',
                      minLength: { value: 2, message: 'Name must be at least 2 characters' }
                    })}
                  />
                  {errors.name && (
                    <div className="invalid-feedback">{errors.name.message}</div>
                  )}
                </div>

                {/* Email */}
                <div className="mb-3">
                  <label htmlFor="email" className="form-label">Email address <span className="text-danger">*</span></label>
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

                {/* Phone (optional) */}
                <div className="mb-3">
                  <label htmlFor="phone" className="form-label">
                    Phone number <span className="text-muted small">(optional)</span>
                  </label>
                  <input
                    id="phone"
                    type="tel"
                    className="form-control"
                    placeholder="+1 555 000 0000"
                    autoComplete="tel"
                    {...register('phone')}
                  />
                </div>

                {/* Password */}
                <div className="mb-3">
                  <label htmlFor="password" className="form-label">Password <span className="text-danger">*</span></label>
                  <input
                    id="password"
                    type="password"
                    className={`form-control${errors.password ? ' is-invalid' : ''}`}
                    placeholder="At least 8 characters"
                    autoComplete="new-password"
                    {...register('password', {
                      required: 'Password is required',
                      minLength: { value: 8, message: 'Password must be at least 8 characters' }
                    })}
                  />
                  {errors.password && (
                    <div className="invalid-feedback">{errors.password.message}</div>
                  )}
                </div>

                {/* Confirm Password */}
                <div className="mb-4">
                  <label htmlFor="confirmPassword" className="form-label">
                    Confirm password <span className="text-danger">*</span>
                  </label>
                  <input
                    id="confirmPassword"
                    type="password"
                    className={`form-control${errors.confirmPassword ? ' is-invalid' : ''}`}
                    placeholder="Repeat your password"
                    autoComplete="new-password"
                    {...register('confirmPassword', {
                      required: 'Please confirm your password',
                      validate: value =>
                        value === password || 'Passwords do not match'
                    })}
                  />
                  {errors.confirmPassword && (
                    <div className="invalid-feedback">{errors.confirmPassword.message}</div>
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
                      Creating account...
                    </>
                  ) : 'Create Account'}
                </button>
              </form>

              <hr className="my-4" />
              <p className="text-center mb-0 small">
                Already have an account?{' '}
                <Link to="/login" className="fw-semibold">
                  Sign in
                </Link>
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

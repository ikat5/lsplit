import { describe, it, expect } from 'vitest'
import { formatAmount } from '../utils/currencyFormatter'
import { isValidEmail, isPasswordMatch, isPositiveNumber } from '../utils/validators'
import { formatDate } from '../utils/dateFormatter'

describe('formatAmount', () => {
  it('formats to 2 decimal places', () => {
    expect(formatAmount(10)).toBe('10.00')
  })
  it('handles null', () => {
    expect(formatAmount(null)).toBe('0.00')
  })
})

describe('validators', () => {
  it('validates email', () => {
    expect(isValidEmail('a@b.com')).toBe(true)
    expect(isValidEmail('bad')).toBe(false)
  })
  it('checks password match', () => {
    expect(isPasswordMatch('abc', 'abc')).toBe(true)
    expect(isPasswordMatch('abc', 'xyz')).toBe(false)
  })
  it('checks positive number', () => {
    expect(isPositiveNumber(5)).toBe(true)
    expect(isPositiveNumber(-1)).toBe(false)
    expect(isPositiveNumber(0)).toBe(false)
  })
})

describe('formatDate', () => {
  it('returns dash for null', () => {
    expect(formatDate(null)).toBe('\u2014')
  })
  it('formats a date string', () => {
    expect(formatDate('2026-06-24')).toBeTruthy()
  })
})

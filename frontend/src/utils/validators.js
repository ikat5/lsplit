export const isValidEmail     = (email)             => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
export const isPasswordMatch  = (password, confirm) => password === confirm
export const isPositiveNumber = (val)               => !isNaN(val) && Number(val) > 0

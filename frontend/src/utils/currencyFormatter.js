export const formatCurrency = (amount, currency = 'USD') =>
  new Intl.NumberFormat('en-US', { style: 'currency', currency }).format(amount ?? 0)

export const formatAmount = (amount) =>
  Number(amount ?? 0).toFixed(2)

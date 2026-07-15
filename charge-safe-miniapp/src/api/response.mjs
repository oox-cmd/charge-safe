export class ApiError extends Error {
  constructor(message, { code, statusCode } = {}) {
    super(message)
    this.name = 'ApiError'
    this.code = code
    this.statusCode = statusCode
  }
}

export function parseApiResponse(response) {
  const statusCode = response?.statusCode
  const body = response?.data
  const code = body?.code
  const message = body?.message || '请求失败'

  if (statusCode !== 200 || code !== 200) {
    throw new ApiError(message, { code, statusCode })
  }
  return body
}

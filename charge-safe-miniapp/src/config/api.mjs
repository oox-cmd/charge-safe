const CONFIG_ERROR = 'API 地址未配置，请设置 VITE_API_BASE_URL'

export function normalizeRequestPath(path) {
  const value = String(path || '').trim()
  if (!value) throw new Error('请求路径不能为空')
  return `/${value.replace(/^\/+/, '')}`
}

export function extractRequestPath(url) {
  const value = String(url || '').trim()
  const withoutOrigin = value.replace(/^[a-z][a-z\d+.-]*:\/\/[^/]+/i, '')
  return normalizeRequestPath(withoutOrigin.split(/[?#]/)[0])
}

export function normalizeApiBaseUrl(rawBaseUrl) {
  const value = String(rawBaseUrl || '').trim()
  if (!value) throw new Error(CONFIG_ERROR)
  if (value === '/') return ''
  return value.replace(/\/+$/, '')
}

export function resolveApiUrl(path, rawBaseUrl) {
  const requestPath = normalizeRequestPath(path)
  const baseUrl = normalizeApiBaseUrl(rawBaseUrl)
  if (!baseUrl) return requestPath

  if (baseUrl.endsWith('/api') && (requestPath === '/api' || requestPath.startsWith('/api/'))) {
    return baseUrl + requestPath.slice(4)
  }
  return baseUrl + requestPath
}

export function getConfiguredApiUrl(path) {
  return resolveApiUrl(path, import.meta.env?.VITE_API_BASE_URL)
}

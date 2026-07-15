export function createMockRequest(originalRequest, {
  hasHandler,
  getHandler,
  schedule = setTimeout,
  logger = console.log
}) {
  return function mockAwareRequest(options) {
    const { method = 'GET', url } = options
    if (!hasHandler(method, url)) {
      return originalRequest(options)
    }

    logger(`[Mock] ${method} ${url}`)
    return new Promise((resolve, reject) => {
      schedule(() => {
        try {
          const handler = getHandler(method, url)
          const result = handler(options.data || {})
          const response = {
            statusCode: 200,
            data: result,
            header: { 'X-ChargeSafe-Mock': 'true' }
          }
          options.success?.(response)
          options.complete?.(response)
          resolve(response)
        } catch (error) {
          options.fail?.(error)
          options.complete?.(error)
          reject(error)
        }
      }, 200 + Math.random() * 400)
    })
  }
}

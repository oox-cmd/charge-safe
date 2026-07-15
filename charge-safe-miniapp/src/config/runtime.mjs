export function resolveMockEnabled({ isDev, flag }) {
  return isDev === true && flag === 'true'
}

export const USE_MOCK = resolveMockEnabled({
  isDev: import.meta.env?.DEV,
  flag: import.meta.env?.VITE_USE_MOCK
})

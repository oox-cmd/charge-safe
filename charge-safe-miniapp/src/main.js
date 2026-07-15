import { createSSRApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import { USE_MOCK } from '@/api/request'
import { installMock } from '@/api/mock'

if (USE_MOCK) {
  installMock()
}

export function createApp() {
  const app = createSSRApp(App)
  const pinia = createPinia()
  app.use(pinia)

  return { app, pinia }
}

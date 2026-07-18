import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  build: {
    // Generate a dependency-license inventory with each production build.
    license: true,
    // `three-globe` is a single, route-lazy-loaded module (about 637 KB
    // minified), so it cannot be split further without replacing the required
    // rendering library. Keep the warning threshold above that known module.
    chunkSizeWarningLimit: 700,
    rolldownOptions: {
      output: {
        // Keep large visualisation and UI libraries out of page chunks.  The
        // Grouping preserves route-level lazy loading while keeping visual and
        // UI dependencies out of the page chunks.
        codeSplitting: {
          groups: [
            {
              name: 'three-vendor',
              test: /node_modules[\\/](three|three-globe|@tweenjs)[\\/]/,
              priority: 40,
              maxSize: 320 * 1024
            },
            {
              name: 'charts-vendor',
              test: /node_modules[\\/](echarts|zrender)[\\/]/,
              priority: 30,
              maxSize: 320 * 1024
            },
            {
              name: 'ui-vendor',
              test: /node_modules[\\/](element-plus|@element-plus)[\\/]/,
              priority: 20,
              maxSize: 320 * 1024
            },
            {
              name: 'lottie-vendor',
              test: /node_modules[\\/]lottie-web[\\/]/,
              priority: 20,
              maxSize: 320 * 1024
            }
          ]
        }
      }
    }
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      }
    }
  }
})

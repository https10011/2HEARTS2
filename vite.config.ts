import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { fileURLToPath, URL } from 'node:url';

// https://vitejs.dev/config/
// Vite is the build system: production build (dist/) is consumed by `cap sync`
// and bundled into the Android WebView APK. base path is relative so all
// assets resolve from the APK's bundled web assets (offline-first).
export default defineConfig({
  plugins: [react()],
  base: './',
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
      '@components': fileURLToPath(new URL('./src/components', import.meta.url)),
      '@theme': fileURLToPath(new URL('./src/theme', import.meta.url)),
      '@navigation': fileURLToPath(new URL('./src/navigation', import.meta.url)),
      '@core': fileURLToPath(new URL('./src/core', import.meta.url)),
      '@customization': fileURLToPath(new URL('./src/customization', import.meta.url)),
    },
  },
  server: {
    host: true,
    port: 5173,
  },
  build: {
    outDir: 'dist',
    sourcemap: false,
    target: 'es2020',
  },
});

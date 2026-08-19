import type { CapacitorConfig } from '@capacitor/cli';

// Capacitor configuration for the TwoHearts V1 Android-first application.
// The React production build (dist/) is the WebView content bundled into
// the APK — there is no hosted website; the app runs fully offline.
const config: CapacitorConfig = {
  appId: 'com.twohearts.app',
  appName: 'TwoHearts',
  webDir: 'dist',
  android: {
    // Allow the WebView to be translucent over system bars when needed.
    backgroundColor: '#FDF6F0',
  },
  plugins: {
    // Minimal foundation config. Feature plugins are added in later phases.
    StatusBar: {
      style: 'DARK',
      backgroundColor: '#6A1B2B',
      overlaysWebView: false,
    },
  },
};

export default config;

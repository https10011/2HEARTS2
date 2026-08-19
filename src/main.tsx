import React from 'react';
import ReactDOM from 'react-dom/client';
import { App } from './App';
import './styles/global.css';

// Application root. Mounts the React tree. Capacitor-native behaviors
// (back button, lifecycle, text-size) are wired in AppRootProvider.
ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
);

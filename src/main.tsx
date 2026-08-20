import React, { useState } from 'react';
import ReactDOM from 'react-dom/client';
import { App } from './App';
import './styles/global.css';
import { bootstrapApp } from './services/bootstrap/appBootstrap';

// In browser/dev contexts the persistence layer runs on sql.js (the same SQL
// schema as native Android SQLite). The WASM asset URL is handed to the
// connection resolver before initialization. On Android this import resolves
// but is simply unused (native adapter is chosen).
import sqlWasmUrl from 'sql.js/dist/sql-wasm.wasm?url';

(globalThis as { __TWOHEARTS_SQL_WASM_URL__?: string }).__TWOHEARTS_SQL_WASM_URL__ = sqlWasmUrl;

/**
 * Splash screen shown during bootstrap initialization.
 * Displays the TwoHearts branding while core services start up.
 */
function SplashView() {
  return (
    <main
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '100dvh',
        background: 'var(--th-color-bg)',
        gap: 'var(--th-space-4)',
      }}
    >
      <div
        style={{
          width: '80px',
          height: '80px',
          borderRadius: '50%',
          background: 'var(--th-color-burgundy)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          boxShadow: 'var(--th-shadow-md)',
        }}
      >
        <svg
          width="48"
          height="48"
          viewBox="0 0 80 80"
          fill="none"
          aria-hidden="true"
        >
          <path
            d="M40 58C40 58 18 44 18 30C18 22 24 16 32 16C36 16 39 18 40 20C41 18 44 16 48 16C56 16 62 22 62 30C62 44 40 58 40 58Z"
            fill="#FFF8F3"
            opacity="0.95"
          />
        </svg>
      </div>
      <h1
        style={{
          fontFamily: 'var(--th-font-family-display)',
          fontSize: 'var(--th-font-size-xl)',
          color: 'var(--th-color-burgundy)',
          fontWeight: 'var(--th-font-weight-semibold)',
        }}
      >
        TwoHearts
      </h1>
    </main>
  );
}

/**
 * Application bootstrap: the Phase 3 initialization pipeline (persistence +
 * migrations + core services) completes BEFORE the React tree is allowed to
 * mount, so feature code can always assume the platform is ready. Failure
 * renders a calm, raw-error-free fallback with retry.
 */
function AppGate() {
  const [status, setStatus] = useState<'pending' | 'ready' | 'failed'>('pending');
  const [error, setError] = useState<string | null>(null);

  React.useEffect(() => {
    let cancelled = false;
    bootstrapApp()
      .then((result) => {
        if (cancelled) return;
        if (result.ok) setStatus('ready');
        else {
          setError(result.failureMessage ?? 'Initialization failed.');
          setStatus('failed');
        }
      })
      .catch(() => {
        if (cancelled) return;
        setError('Initialization failed.');
        setStatus('failed');
      });
    return () => {
      cancelled = true;
    };
  }, [status]);

  if (status === 'pending') {
    return <SplashView />;
  }

  if (status === 'failed') {
    return (
      <main
        style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          minHeight: '100dvh',
          padding: 'var(--th-space-6)',
          textAlign: 'center',
        }}
      >
        <h2 style={{ color: 'var(--th-color-burgundy)' }}>TwoHearts</h2>
        <p style={{ color: 'var(--th-color-text-secondary)', maxWidth: '32ch' }}>
          {error}. Please try restarting the app.
        </p>
        <button type="button" className="th-button th-button--primary" onClick={() => setStatus('pending')}>
          Try again
        </button>
      </main>
    );
  }

  return <App />;
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <AppGate />
  </React.StrictMode>,
);

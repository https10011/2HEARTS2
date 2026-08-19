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
    return null;
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

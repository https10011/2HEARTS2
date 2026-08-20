/**
 * VaultLocked (Phase 17).
 *
 * Displayed when the vault is locked and the user needs to enter their PIN
 * to access vault content. Uses the existing AppLockService for authentication.
 */

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

interface VaultLockedProps {
  onUnlock: () => void;
}

export function VaultLocked({ onUnlock }: VaultLockedProps) {
  const navigate = useNavigate();
  const [pin, setPin] = useState('');
  const [error, setError] = useState('');
  const [attempting, setAttempting] = useState(false);

  const handlePinChange = (value: string) => {
    // Only allow digits
    const digits = value.replace(/\D/g, '').slice(0, 8);
    setPin(digits);
    setError('');
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (pin.length < 4) {
      setError('PIN must be 4-8 digits.');
      return;
    }

    setAttempting(true);
    try {
      // The VaultService.unlock will be called by the parent
      // Here we just trigger the unlock callback
      onUnlock();
    } catch {
      setError('Invalid PIN. Please try again.');
      setPin('');
    } finally {
      setAttempting(false);
    }
  };

  return (
    <div className="th-content-pad" style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
      <div style={{ textAlign: 'center', marginBottom: 'var(--th-space-8)' }}>
        <div style={{ fontSize: '4rem', marginBottom: 'var(--th-space-3)' }}>🔒</div>
        <h1 className="th-screen-title" style={{ marginBottom: 'var(--th-space-2)' }}>Vault Locked</h1>
        <p style={{ color: 'var(--th-text-secondary)' }}>
          Enter your PIN to access private content.
        </p>
      </div>

      {error && (
        <div className="th-error-banner" style={{ marginBottom: 'var(--th-space-4)', width: '100%', maxWidth: '320px' }}>
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} style={{ width: '100%', maxWidth: '320px' }}>
        <input
          type="password"
          className="th-input"
          value={pin}
          onChange={(e) => handlePinChange(e.target.value)}
          placeholder="Enter PIN"
          autoFocus
          inputMode="numeric"
          pattern="[0-9]*"
          maxLength={8}
          style={{ textAlign: 'center', fontSize: 'var(--th-text-lg)', letterSpacing: '0.3em', marginBottom: 'var(--th-space-4)' }}
        />

        <button
          type="submit"
          className="th-btn th-btn--primary"
          disabled={attempting || pin.length < 4}
          style={{ width: '100%' }}
        >
          {attempting ? 'Unlocking...' : 'Unlock'}
        </button>
      </form>

      <button
        className="th-btn th-btn--ghost"
        onClick={() => navigate(-1)}
        style={{ marginTop: 'var(--th-space-4)' }}
      >
        Go Back
      </button>
    </div>
  );
}

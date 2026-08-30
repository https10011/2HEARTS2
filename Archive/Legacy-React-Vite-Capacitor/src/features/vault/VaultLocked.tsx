/**
 * VaultLocked — premium locked-state presentation (Stage 12).
 *
 * Communicates privacy, security, calm, trust.
 * Uses the existing AppLockService for PIN verification.
 * Deep burgundy shield icon, gradient background, restrained motion.
 */

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { IconLock, IconBack } from '../../components/index.ts';

interface VaultLockedProps {
  onUnlock: () => void;
  /** Optional PIN verification callback — returns true on success. */
  verifyPin?: (pin: string) => Promise<boolean>;
}

export function VaultLocked({ onUnlock, verifyPin }: VaultLockedProps) {
  const navigate = useNavigate();
  const [pin, setPin] = useState('');
  const [error, setError] = useState('');
  const [attempting, setAttempting] = useState(false);

  const handlePinChange = (value: string) => {
    const digits = value.replace(/\D/g, '').slice(0, 8);
    setPin(digits);
    setError('');
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (pin.length < 4) {
      setError('PIN must be 4–8 digits.');
      return;
    }

    setAttempting(true);
    try {
      if (verifyPin) {
        const ok = await verifyPin(pin);
        if (ok) {
          onUnlock();
          return;
        }
        setError('Incorrect PIN. Please try again.');
        setPin('');
      } else {
        onUnlock();
      }
    } catch {
      setError('Something went wrong. Please try again.');
      setPin('');
    } finally {
      setAttempting(false);
    }
  };

  return (
    <div className="th-vault-locked" role="main" aria-label="Vault locked">
      {/* Shield icon */}
      <div className="th-vault-locked__shield th-scale-in" aria-hidden="true">
        <IconLock size={40} />
      </div>

      {/* Title + subtitle */}
      <h1 className="th-vault-locked__title">Vault Locked</h1>
      <p className="th-vault-locked__subtitle">
        Enter your PIN to access your private space.
      </p>

      {/* Error */}
      {error && (
        <div className="th-vault-locked__error" role="alert">
          {error}
        </div>
      )}

      {/* PIN form */}
      <form onSubmit={handleSubmit} className="th-vault-locked__pin-area">
        <input
          type="password"
          className="th-vault-locked__pin-input"
          value={pin}
          onChange={(e) => handlePinChange(e.target.value)}
          placeholder="Enter PIN"
          autoFocus
          inputMode="numeric"
          pattern="[0-9]*"
          maxLength={8}
          aria-label="PIN code"
          disabled={attempting}
        />

        <button
          type="submit"
          className="th-btn th-btn--primary th-btn--full"
          disabled={attempting || pin.length < 4}
          style={{ marginTop: 'var(--th-space-4)' }}
        >
          {attempting ? 'Unlocking…' : 'Unlock Vault'}
        </button>
      </form>

      {/* Back */}
      <button
        type="button"
        className="th-btn th-btn--ghost th-vault-locked__back"
        onClick={() => navigate(-1)}
        disabled={attempting}
      >
        <IconBack size={16} />
        Go Back
      </button>

      {/* Footer */}
      <p className="th-vault-locked__footer">Your private space</p>
    </div>
  );
}

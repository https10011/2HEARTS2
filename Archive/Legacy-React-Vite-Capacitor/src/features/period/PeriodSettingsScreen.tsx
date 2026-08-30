/**
 * Period Settings (Phase 22, productized in Stage 11).
 *
 * Quiet, trustworthy configuration for the Period Tracker: typical
 * cycle length and typical period length. These drive the on-device
 * predictions only. Everything persists locally via PeriodService —
 * nothing leaves the device, and the privacy tone is front and center.
 */

import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { usePeriodService } from './usePeriodService.ts';
import { OWNER_PROFILE_ID } from './periodPresentation.ts';
import {
  Button,
  IconBack,
  IconButton,
  IconLock,
  LoadingState,
  useToast,
} from '../../components/index.ts';

const CYCLE_MIN = 20;
const CYCLE_MAX = 45;
const PERIOD_MIN = 1;
const PERIOD_MAX = 10;

export function PeriodSettingsScreen() {
  const navigate = useNavigate();
  const service = usePeriodService();
  const toast = useToast();
  const [cycleDays, setCycleDays] = useState(28);
  const [periodDays, setPeriodDays] = useState(5);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!service) return;
    let cancelled = false;
    (async () => {
      try {
        const settings = await service.getSettings(OWNER_PROFILE_ID);
        if (!cancelled) {
          setCycleDays(settings.cycleLengthDays);
          setPeriodDays(settings.periodLengthDays);
        }
      } catch {
        if (!cancelled) setError('Could not load period settings.');
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => { cancelled = true; };
  }, [service]);

  const handleSave = useCallback(async () => {
    if (!service) return;
    setSaving(true);
    setError(null);
    try {
      await service.saveSettings(OWNER_PROFILE_ID, cycleDays, periodDays);
      toast.success('Settings saved');
    } catch {
      setError('Could not save settings. Please try again.');
      toast.error('Could not save settings');
    } finally {
      setSaving(false);
    }
  }, [service, cycleDays, periodDays, toast]);

  if (loading || !service) {
    return <LoadingState label="Loading settings…" />;
  }

  const header = (
    <header className="th-period-header">
      <IconButton label="Go back" onClick={() => navigate(RoutePath.appPeriod)}>
        <IconBack />
      </IconButton>
      <div className="th-period-header__copy">
        <h1 className="th-period-title">Period Settings</h1>
        <p className="th-period-subtitle">Predicted on-device, always private.</p>
      </div>
    </header>
  );

  const stepper = (
    label: string,
    hint: string,
    value: number,
    setValue: (n: number) => void,
    min: number,
    max: number,
    unit: string,
  ) => (
    <div className="th-period-stepper">
      <div className="th-period-stepper__copy">
        <div className="th-period-stepper__label">{label}</div>
        <p className="th-period-stepper__hint">{hint}</p>
        <div className="th-period-stepper__value">{value}<br /><span style={{ fontSize: 'var(--th-font-size-xs)', color: 'var(--th-color-text-secondary)' }}>{unit}</span></div>
      </div>
      <div className="th-period-stepper__controls">
        <button
          className="th-period-stepper-btn"
          onClick={() => { setValue(Math.max(min, value - 1)); }}
          disabled={value <= min}
          aria-label={`Decrease ${label}`}
        >
          −
        </button>
        <button
          className="th-period-stepper-btn"
          onClick={() => { setValue(Math.min(max, value + 1)); }}
          disabled={value >= max}
          aria-label={`Increase ${label}`}
        >
          +
        </button>
      </div>
    </div>
  );

  return (
    <div className="th-content-pad th-screen-warm">
      {header}

      <div className="th-period-privacy">
        <span className="th-period-privacy__icon" aria-hidden="true"><IconLock size={20} /></span>
        <div>
          <div className="th-period-privacy__title">Private by design</div>
          <p className="th-period-privacy__text">
            Your periods and settings never leave this device. No account,
            no cloud, no sharing.
          </p>
        </div>
      </div>

      <div className="th-period-settings-section">
        <h2 className="th-period-settings-title">Your cycle</h2>
        <p className="th-period-settings-desc">
          These are used only to estimate when your next period may begin.
        </p>
        {stepper(
          'Typical cycle length',
          'How many days usually between periods.',
          cycleDays, setCycleDays, CYCLE_MIN, CYCLE_MAX,
          'days',
        )}
        {stepper(
          'Typical period length',
          'How many days your period usually lasts.',
          periodDays, setPeriodDays, PERIOD_MIN, PERIOD_MAX,
          'days',
        )}
      </div>

      {error && <p className="th-form-error" style={{ marginBottom: 'var(--th-space-3)' }} role="alert">{error}</p>}

      <div className="th-period-settings-save">
        <Button variant="primary" full onClick={handleSave} disabled={saving}>
          {saving ? 'Saving…' : 'Save Settings'}
        </Button>
      </div>
    </div>
  );
}

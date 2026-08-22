/**
 * PeriodSettingsScreen (Phase 22).
 *
 * Local configuration for the Period Tracker: typical cycle length and
 * typical period length. These drive the offline predictions. Settings
 * persist locally via PeriodService — nothing leaves the device.
 */

import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { getDatabase } from '../../data/database/connection.ts';
import { IconBack } from '../../components/index.ts';
import { PeriodRepository } from '../../repositories/periodRepository.ts';
import { PeriodService } from '../../services/period/periodService.ts';

let _periodService: PeriodService | null = null;
async function getPeriodService(): Promise<PeriodService> {
  if (!_periodService) {
    const repo = new PeriodRepository(await getDatabase());
    _periodService = new PeriodService(repo);
  }
  return _periodService;
}

const CYCLE_MIN = 20;
const CYCLE_MAX = 45;
const PERIOD_MIN = 1;
const PERIOD_MAX = 10;

export function PeriodSettingsScreen() {
  const navigate = useNavigate();
  const [cycleDays, setCycleDays] = useState(28);
  const [periodDays, setPeriodDays] = useState(5);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const service = await getPeriodService();
        const settings = await service.getSettings('owner');
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
  }, []);

  const handleSave = useCallback(async () => {
    setSaving(true);
    setError(null);
    setSaved(false);
    try {
      const service = await getPeriodService();
      await service.saveSettings('owner', cycleDays, periodDays);
      setSaved(true);
    } catch {
      setError('Could not save settings. Please try again.');
    } finally {
      setSaving(false);
    }
  }, [cycleDays, periodDays]);

  const stepper = (
    label: string,
    value: number,
    setValue: (n: number) => void,
    min: number,
    max: number,
    unit: string,
  ) => (
    <div className="th-card" style={{ padding: 'var(--th-space-4)', marginBottom: 'var(--th-space-3)' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div>
          <div style={{ fontWeight: 'var(--th-font-weight-semibold)', fontSize: 'var(--th-font-size-md)' }}>{label}</div>
          <div style={{ fontSize: 'var(--th-font-size-xs)', color: 'var(--th-color-text-secondary)' }}>
            {value} {unit}
          </div>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--th-space-2)' }}>
          <button
            className="th-btn th-btn--outline"
            style={{ minWidth: '44px', padding: 'var(--th-space-2)' }}
            onClick={() => { setValue(Math.max(min, value - 1)); setSaved(false); }}
            disabled={value <= min}
            aria-label={`Decrease ${label}`}
          >
            −
          </button>
          <button
            className="th-btn th-btn--outline"
            style={{ minWidth: '44px', padding: 'var(--th-space-2)' }}
            onClick={() => { setValue(Math.min(max, value + 1)); setSaved(false); }}
            disabled={value >= max}
            aria-label={`Increase ${label}`}
          >
            +
          </button>
        </div>
      </div>
    </div>
  );

  return (
    <div className="th-content-pad">
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'center', marginBottom: 'var(--th-space-4)' }}>
        <button
          className="th-btn th-btn--ghost"
          onClick={() => navigate(RoutePath.appPeriod)}
          style={{ minWidth: 'auto', padding: 'var(--th-space-2)' }}
        >
          <IconBack size={20} />
        </button>
        <h1 className="th-screen-title" style={{ flex: 1 }}>Period Settings</h1>
      </div>

      {loading ? (
        <div className="th-loading">
          <div className="th-loading__spinner" />
          <p>Loading settings...</p>
        </div>
      ) : (
        <>
          {stepper('Typical cycle length', cycleDays, setCycleDays, CYCLE_MIN, CYCLE_MAX, 'days')}
          {stepper('Typical period length', periodDays, setPeriodDays, PERIOD_MIN, PERIOD_MAX, 'days')}

          <p style={{ fontSize: 'var(--th-font-size-xs)', color: 'var(--th-color-text-secondary)', marginBottom: 'var(--th-space-4)' }}>
            These values are used for on-device predictions only. All period
            tracking data stays on this device.
          </p>

          {error && <p className="th-form-error" style={{ marginBottom: 'var(--th-space-3)' }}>{error}</p>}
          {saved && (
            <p style={{ color: 'var(--th-color-success)', fontSize: 'var(--th-font-size-sm)', marginBottom: 'var(--th-space-3)' }}>
              Settings saved.
            </p>
          )}

          <button
            className="th-btn th-btn--primary th-btn--full"
            onClick={handleSave}
            disabled={saving}
          >
            {saving ? 'Saving...' : 'Save Settings'}
          </button>
        </>
      )}
    </div>
  );
}

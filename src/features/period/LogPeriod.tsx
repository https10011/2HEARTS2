/**
 * LogPeriod (Phase 16).
 *
 * Form for logging a period entry: start date, end date, flow level, notes.
 * Uses real persisted data via PeriodService.
 */

import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { getDatabase } from '../../data/database/connection.ts';
import { PeriodRepository } from '../../repositories/periodRepository.ts';
import { PeriodService } from '../../services/period/periodService.ts';
import { AppError } from '../../services/errors/appError.ts';
import type { FlowLevel } from '../../data/period/periodTypes.ts';

const FLOW_OPTIONS: Array<{ value: FlowLevel; label: string; emoji: string }> = [
  { value: 'light', label: 'Light', emoji: '💧' },
  { value: 'medium', label: 'Medium', emoji: '🩸' },
  { value: 'heavy', label: 'Heavy', emoji: '🔴' },
];

let _periodService: PeriodService | null = null;
async function getPeriodService(): Promise<PeriodService> {
  if (!_periodService) {
    const repo = new PeriodRepository(await getDatabase());
    _periodService = new PeriodService(repo);
  }
  return _periodService;
}

function todayKey(): string {
  const d = new Date();
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

export function LogPeriod() {
  const navigate = useNavigate();
  const { entryId } = useParams<{ entryId: string }>();
  const isEditing = Boolean(entryId);

  const [startDate, setStartDate] = useState(todayKey());
  const [endDate, setEndDate] = useState('');
  const [flowLevel, setFlowLevel] = useState<FlowLevel>('medium');
  const [note, setNote] = useState('');
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);
  const [loadingEntry, setLoadingEntry] = useState(isEditing);

  useEffect(() => {
    if (!entryId) return;
    const load = async () => {
      try {
        const service = await getPeriodService();
        const entry = await service.getEntryById(entryId);
        if (entry) {
          setStartDate(entry.startDate);
          setEndDate(entry.endDate ?? '');
          setFlowLevel(entry.flowLevel);
          setNote(entry.note ?? '');
        }
      } catch {
        setError('Could not load period entry.');
      } finally {
        setLoadingEntry(false);
      }
    };
    load();
  }, [entryId]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSaving(true);

    try {
      const service = await getPeriodService();
      const profileId = 'owner';

      if (isEditing && entryId) {
        await service.updateEntry(entryId, {
          startDate,
          endDate: endDate || null,
          flowLevel,
          note: note || null,
        });
      } else {
        await service.logPeriod({
          startDate,
          endDate: endDate || null,
          flowLevel,
          note: note || null,
          profileId,
        });
      }
      navigate(RoutePath.appPeriod);
    } catch (err) {
      if (err instanceof AppError) {
        setError(err.userMessage);
      } else {
        setError('An unexpected error occurred.');
      }
    } finally {
      setSaving(false);
    }
  };

  if (loadingEntry) {
    return (
      <div className="th-content-pad">
        <div className="th-loading">
          <div className="th-loading__spinner" />
          <p>Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="th-content-pad">
      <h1 className="th-screen-title" style={{ marginBottom: 'var(--th-space-4)' }}>
        {isEditing ? 'Edit Period' : 'Log Period'}
      </h1>

      {error && (
        <div className="th-error-banner" style={{ marginBottom: 'var(--th-space-4)' }}>
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--th-space-4)' }}>
        {/* Start date */}
        <div>
          <label className="th-label">Start Date *</label>
          <input
            type="date"
            className="th-input"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
            required
          />
        </div>

        {/* End date */}
        <div>
          <label className="th-label">End Date (if period ended)</label>
          <input
            type="date"
            className="th-input"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            min={startDate}
          />
          {!endDate && (
            <p style={{ fontSize: 'var(--th-text-xs)', color: 'var(--th-text-tertiary)', marginTop: 'var(--th-space-1)' }}>
              Leave empty if still ongoing
            </p>
          )}
        </div>

        {/* Flow level */}
        <div>
          <label className="th-label">Flow Level *</label>
          <div style={{ display: 'flex', gap: 'var(--th-space-3)' }}>
            {FLOW_OPTIONS.map((opt) => (
              <button
                key={opt.value}
                type="button"
                className={`th-btn th-btn--sm ${flowLevel === opt.value ? 'th-btn--primary' : 'th-btn--outline'}`}
                onClick={() => setFlowLevel(opt.value)}
                style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 'var(--th-space-1)' }}
              >
                <span style={{ fontSize: '1.25rem' }}>{opt.emoji}</span>
                <span>{opt.label}</span>
              </button>
            ))}
          </div>
        </div>

        {/* Note */}
        <div>
          <label className="th-label">Notes (optional)</label>
          <textarea
            className="th-input"
            value={note}
            onChange={(e) => setNote(e.target.value)}
            placeholder="How are you feeling?"
            rows={3}
            maxLength={500}
            style={{ resize: 'vertical' }}
          />
        </div>

        {/* Actions */}
        <div style={{ display: 'flex', gap: 'var(--th-space-3)', marginTop: 'var(--th-space-2)' }}>
          <button
            type="button"
            className="th-btn th-btn--outline"
            onClick={() => navigate(-1)}
            style={{ flex: 1 }}
          >
            Cancel
          </button>
          <button
            type="submit"
            className="th-btn th-btn--primary"
            disabled={saving}
            style={{ flex: 1 }}
          >
            {saving ? 'Saving...' : isEditing ? 'Save Changes' : 'Log Period'}
          </button>
        </div>
      </form>
    </div>
  );
}

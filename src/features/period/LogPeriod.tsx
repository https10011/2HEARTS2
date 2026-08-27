/**
 * Log Period / Edit Period (Phase 16, productized in Stage 11).
 *
 * Calm, private composer for a period entry: start date, optional end
 * date, flow level, and a personal note. Uses the centralized branded
 * DatePicker (no duplicate picker), flows through the existing
 * PeriodService, and maps 1:1 onto the same create/update calls — the
 * underlying save behavior is unchanged.
 */

import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { usePeriodService } from './usePeriodService.ts';
import { OWNER_PROFILE_ID, flowLabel, flowOptions, localDateKey } from './periodPresentation.ts';
import { AppError } from '../../services/errors/appError.ts';
import type { FlowLevel } from '../../data/period/periodTypes.ts';
import {
  Button,
  DatePicker,
  IconBack,
  IconButton,
  IconInfo,
  LoadingState,
  useToast,
} from '../../components/index.ts';

const NOTE_MAX = 500;
/** Allow logging back several years; start date remains a real calendar day. */
const MIN_YEAR = new Date().getFullYear() - 5;

export function LogPeriod() {
  const navigate = useNavigate();
  const service = usePeriodService();
  const toast = useToast();
  const { entryId } = useParams<{ entryId: string }>();
  const isEditing = Boolean(entryId);

  const [startDate, setStartDate] = useState(localDateKey());
  const [endDate, setEndDate] = useState('');
  const [flowLevel, setFlowLevel] = useState<FlowLevel>('medium');
  const [note, setNote] = useState('');
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);
  const [loadingEntry, setLoadingEntry] = useState(isEditing);

  useEffect(() => {
    if (!entryId || !service) return;
    let cancelled = false;
    const load = async () => {
      try {
        const entry = await service.getEntryById(entryId);
        if (entry && !cancelled) {
          setStartDate(entry.startDate);
          setEndDate(entry.endDate ?? '');
          setFlowLevel(entry.flowLevel);
          setNote(entry.note ?? '');
        }
      } catch {
        if (!cancelled) setError('Could not load this entry.');
      } finally {
        if (!cancelled) setLoadingEntry(false);
      }
    };
    load();
    return () => { cancelled = true; };
  }, [entryId, service]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!service) return;
    setError('');
    setSaving(true);

    try {
      if (isEditing && entryId) {
        await service.updateEntry(entryId, {
          startDate,
          endDate: endDate || null,
          flowLevel,
          note: note || null,
        });
        toast.success('Period updated');
      } else {
        await service.logPeriod({
          startDate,
          endDate: endDate || null,
          flowLevel,
          note: note || null,
          profileId: OWNER_PROFILE_ID,
        });
        toast.success('Period logged');
      }
      navigate(RoutePath.appPeriod);
    } catch (err) {
      if (err instanceof AppError) {
        setError(err.userMessage);
      } else {
        setError('An unexpected error occurred. Please try again.');
      }
      toast.error('Could not save period');
    } finally {
      setSaving(false);
    }
  };

  if (loadingEntry || !service) {
    return <LoadingState label="Loading entry…" />;
  }

  const options = flowOptions();

  return (
    <div className="th-content-pad th-screen-warm">
      {/* Header */}
      <div className="th-period-header">
        <IconButton label="Go back" onClick={() => navigate(-1)}>
          <IconBack />
        </IconButton>
        <div className="th-period-header__copy">
          <h1 className="th-period-title">{isEditing ? 'Edit Period' : 'Log Period'}</h1>
          <p className="th-period-subtitle">A few details, kept private.</p>
        </div>
      </div>

      {error && (
        <div
          className="th-form-error"
          style={{ marginBottom: 'var(--th-space-4)' }}
          role="alert"
        >
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--th-space-5)' }}>
          {/* Start date */}
          <div className="th-period-field">
            <label className="th-period-field__label" id="period-start-label">Start date</label>
            <DatePicker
              value={startDate}
              onChange={(val) => setStartDate(val || localDateKey())}
              label="Start date"
              placeholder="Tap to choose a date"
              minYear={MIN_YEAR}
            />
          </div>

          {/* End date */}
          <div className="th-period-field">
            <label className="th-period-field__label" id="period-end-label">End date (optional)</label>
            <DatePicker
              value={endDate || null}
              onChange={(val) => setEndDate(val)}
              label="End date"
              placeholder="Tap to choose a date"
              minYear={MIN_YEAR}
            />
            {!endDate && (
              <p className="th-period-field__hint">
                Leave empty if your period is still ongoing.
              </p>
            )}
          </div>

          {/* Flow level */}
          <div className="th-period-field">
            <label className="th-period-field__label" id="period-flow-label">Flow level</label>
            <div className="th-period-chips" role="group" aria-labelledby="period-flow-label">
              {options.map((opt) => (
                <button
                  key={opt.value}
                  type="button"
                  className={`th-period-chip ${flowLevel === opt.value ? 'th-period-chip--selected' : ''}`}
                  onClick={() => setFlowLevel(opt.value)}
                  aria-pressed={flowLevel === opt.value}
                >
                  <span
                    className="th-period-chip__dot"
                    aria-hidden="true"
                    style={{
                      background: opt.value === 'light'
                        ? 'var(--th-color-blush)'
                        : opt.value === 'medium'
                          ? 'var(--th-color-rose-muted)'
                          : 'var(--th-color-burgundy)',
                    }}
                  />
                  <span className="th-period-chip__label">{opt.label}</span>
                </button>
              ))}
            </div>
            <p className="th-period-field__hint">The selected flow is {flowLabel(flowLevel).toLowerCase()}.</p>
          </div>

          {/* Note */}
          <div className="th-period-field">
            <label className="th-period-field__label" htmlFor="period-note">A note (optional)</label>
            <textarea
              id="period-note"
              className="th-input"
              value={note}
              onChange={(e) => setNote(e.target.value)}
              placeholder="Anything to note for yourself…"
              rows={3}
              maxLength={NOTE_MAX}
            />
            <p className="th-period-note-count">{note.length}/{NOTE_MAX}</p>
          </div>

          {/* Actions */}
          <div className="th-period-actions-row">
            <Button type="button" variant="ghost" onClick={() => navigate(-1)} disabled={saving}>
              Cancel
            </Button>
            <Button variant="primary" type="submit" disabled={saving}>
              {saving ? 'Saving…' : isEditing ? 'Save Changes' : 'Log Period'}
            </Button>
          </div>

          <p className="th-period-composer-note">
            <IconInfo size={15} /> Stored on this device only
          </p>
        </div>
      </form>
    </div>
  );
}

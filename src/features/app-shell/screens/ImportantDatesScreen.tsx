/**
 * ImportantDatesScreen (Phase 10, productized Stage 4).
 *
 * The couple's dates — anniversaries, birthdays, and once-off milestones
 * with upcoming/past semantics reusing RelationshipService.getSummary()
 * (sorted by the next occurrence).
 *
 * Stage 4 productization:
 *   - Branded DatePicker replaces the native browser date input (the same
 *     component Stage 2 introduced for onboarding).
 *   - Rows show "In N days / Today! / Yearly / Once" badges; once-off dates
 *     already past are dimmed under a quiet "Passed" section.
 *   - Warm empty state; save/delete feedback via the shared toast layer.
 */

import { useEffect, useState, useCallback } from 'react';
import { coreServices } from '../../../services/bootstrap/appBootstrap.ts';
import type { RelationshipService, RelationshipSummary } from '../../../services/relationship/relationshipService.ts';
import type { ImportantDate, Recurrence } from '../../../data/relationship/relationshipTypes.ts';
import { IconCalendar, IconPlus, IconTrash, IconEdit, LoadingState, useToast } from '../../../components/index.ts';
import { DatePicker } from '../../../components/index.ts';

function formatDate(dateStr: string): string {
  const [year, month, day] = dateStr.split('-').map(Number);
  const d = new Date(Date.UTC(year, month - 1, day));
  return d.toLocaleDateString(undefined, { year: 'numeric', month: 'long', day: 'numeric' });
}

interface DecoratedDate extends ImportantDate {
  daysUntil: number | null; // null for once-off dates already past
  isPast: boolean;
}

function decorate(all: ImportantDate[], upcoming: RelationshipSummary['upcomingDates']): DecoratedDate[] {
  return all.map((d) => {
    // Yearly entries store the *original* date while the summary holds the
    // *next occurrence*, so match on title+recurrence rather than exact date.
    const hit = d.recurrence === 'yearly'
      ? upcoming.find((u) => u.title === d.title && u.recurrence === 'yearly')
      : upcoming.find((u) => u.title === d.title && u.date === d.date && u.recurrence === d.recurrence);
    const isPast = d.recurrence === 'none' && !hit;
    return { ...d, daysUntil: hit?.daysUntil ?? null, isPast };
  });
}

function sortForDisplay(dates: DecoratedDate[]): DecoratedDate[] {
  return [...dates].sort((a, b) => {
    if (a.isPast !== b.isPast) return a.isPast ? 1 : -1;
    const au = a.daysUntil ?? Number.POSITIVE_INFINITY;
    const bu = b.daysUntil ?? Number.POSITIVE_INFINITY;
    if (au !== bu) return au - bu;
    return a.date.localeCompare(b.date);
  });
}

export function ImportantDatesScreen() {
  const toast = useToast();
  const [dates, setDates] = useState<DecoratedDate[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editId, setEditId] = useState<string | null>(null);
  const [title, setTitle] = useState('');
  const [dateVal, setDateVal] = useState('');
  const [recurrence, setRecurrence] = useState<Recurrence>('none');
  const [errors, setErrors] = useState<string[]>([]);
  const [saving, setSaving] = useState(false);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  const loadDates = useCallback(async () => {
    const svc = coreServices.relationship as RelationshipService | undefined;
    if (!svc) return;
    try {
      const [all, summary] = await Promise.all([svc.listImportantDates(), svc.getSummary()]);
      setDates(sortForDisplay(decorate(all, summary.upcomingDates)));
    } catch {
      // Graceful
    }
  }, []);

  useEffect(() => {
    let cancelled = false;
    const load = async () => {
      await loadDates();
      if (!cancelled) setLoading(false);
    };
    load();
    return () => { cancelled = true; };
  }, [loadDates]);

  const resetForm = () => {
    setTitle('');
    setDateVal('');
    setRecurrence('none');
    setErrors([]);
    setEditId(null);
    setShowForm(false);
  };

  const startEdit = (d: ImportantDate) => {
    setEditId(d.id);
    setTitle(d.title);
    setDateVal(d.date);
    setRecurrence(d.recurrence);
    setErrors([]);
    setShowForm(true);
  };

  const handleSave = useCallback(async () => {
    const newErrors: string[] = [];
    const trimmed = title.trim();
    if (!trimmed) newErrors.push('Title is required.');
    if (!dateVal) newErrors.push('Date is required.');
    if (newErrors.length > 0) { setErrors(newErrors); return; }

    setSaving(true);
    setErrors([]);
    try {
      const svc = coreServices.relationship as RelationshipService | undefined;
      if (!svc) return;
      if (editId) {
        await svc.updateImportantDate(editId, { title: trimmed, date: dateVal, recurrence });
        toast.success('Date updated');
      } else {
        await svc.addImportantDate({ title: trimmed, date: dateVal, recurrence });
        toast.success('Date added');
      }
      resetForm();
      await loadDates();
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'Failed to save.';
      setErrors([msg]);
      toast.error('Could not save the date');
    } finally {
      setSaving(false);
    }
  }, [title, dateVal, recurrence, editId, loadDates, toast]);

  const handleDelete = useCallback(async (id: string) => {
    setDeletingId(id);
    try {
      const svc = coreServices.relationship as RelationshipService | undefined;
      if (!svc) return;
      await svc.removeImportantDate(id);
      toast.success('Date removed');
      await loadDates();
    } catch {
      toast.error('Could not remove the date');
    } finally {
      setDeletingId(null);
    }
  }, [loadDates, toast]);

  if (loading) return <LoadingState label="Loading your dates…" />;

  return (
    <div className="th-content-pad">
      <div className="th-screen-header--enhanced th-us-dates-header">
        <div>
          <h1 className="th-screen-title">Important Dates</h1>
          <p className="th-screen-subtitle" style={{ marginTop: 'var(--th-space-1)' }}>
            Anniversaries, birthdays & moments
          </p>
        </div>
        <button
          className="th-btn th-btn--secondary th-us-dates-add"
          onClick={() => { resetForm(); setShowForm(!showForm); }}
        >
          <IconPlus size={16} /> {showForm ? 'Close' : 'New date'}
        </button>
      </div>

      {showForm && (
        <div className="th-card th-date-form" style={{ marginBottom: 'var(--th-space-4)' }}>
          <h2 className="th-date-form__title">
            {editId ? 'Edit date' : 'Add a date'}
          </h2>
          {errors.length > 0 && (
            <div className="th-form-errors">
              {errors.map((e, i) => <p key={i} className="th-form-error">{e}</p>)}
            </div>
          )}
          <div className="th-form">
            <div className="th-form-group">
              <label className="th-label" htmlFor="date-title">Title *</label>
              <input id="date-title" type="text" className="th-input" value={title}
                onChange={(e) => { setTitle(e.target.value); setErrors([]); }}
                placeholder="e.g. Our anniversary, Sam’s birthday…" autoFocus />
            </div>
            <div className="th-form-group">
              <label className="th-label" htmlFor="date-value">Date *</label>
              <DatePicker
                value={dateVal}
                onChange={(val) => { setDateVal(val); setErrors([]); }}
                label="Important date"
                placeholder="Tap to choose a date"
              />
            </div>
            <div className="th-form-group">
              <label className="th-label">Repeats?</label>
              <div className="th-option-group">
                <button className={`th-option-chip ${recurrence === 'none' ? 'th-option-chip--active' : ''}`}
                  onClick={() => setRecurrence('none')}>Once</button>
                <button className={`th-option-chip ${recurrence === 'yearly' ? 'th-option-chip--active' : ''}`}
                  onClick={() => setRecurrence('yearly')}>Every year</button>
              </div>
            </div>
            <div className="th-timeline-editor-actions">
              <button className="th-btn th-btn--secondary" onClick={resetForm} disabled={saving}>Cancel</button>
              <button className="th-btn th-btn--primary" onClick={handleSave} disabled={saving || !title.trim() || !dateVal}>
                {saving ? 'Saving…' : editId ? 'Save changes' : 'Add date'}
              </button>
            </div>
          </div>
        </div>
      )}

      {dates.length === 0 && !showForm ? (
        <div className="th-empty-state th-empty-state--enhanced">
          <div className="th-empty-state__visual">
            <IconCalendar size={36} />
          </div>
          <h3 className="th-empty-state__title">No dates yet</h3>
          <p className="th-empty-state__desc">
            Add the moments that make your story yours — anniversaries, birthdays, first trips.
          </p>
          <button className="th-btn th-btn--primary" onClick={() => setShowForm(true)}>
            <IconPlus size={18} /> Add your first date
          </button>
        </div>
      ) : (
        <div className="th-hub-grid--enhanced">
          {dates.map((d, i) => (
            <div
              key={d.id}
              className={`th-feature-card th-feature-card--enhanced th-stagger-item ${d.isPast ? 'th-date-row--past' : ''}`}
              style={{ cursor: 'default', animationDelay: `${Math.min(i, 8) * 45}ms` }}
            >
              <div className="th-feature-card__icon th-date-row__icon">
                <IconCalendar size={20} />
              </div>
              <div className="th-feature-card__body">
                <div className="th-feature-card__title">{d.title}</div>
                <div className="th-feature-card__desc">
                  {formatDate(d.date)}
                  <span className="th-date-row__badge">
                    {d.isPast
                      ? 'Passed'
                      : d.daysUntil === 0
                        ? 'Today!'
                        : d.daysUntil !== null
                          ? `In ${d.daysUntil} ${d.daysUntil === 1 ? 'day' : 'days'}`
                          : ''}
                    {d.recurrence === 'yearly' ? ' · Yearly' : ''}
                  </span>
                </div>
              </div>
              <div className="th-date-row__actions">
                <button className="th-icon-btn" onClick={() => startEdit(d)} aria-label="Edit">
                  <IconEdit size={16} />
                </button>
                <button className="th-icon-btn th-icon-btn--danger" onClick={() => handleDelete(d.id)}
                  disabled={deletingId === d.id} aria-label="Delete">
                  <IconTrash size={16} />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

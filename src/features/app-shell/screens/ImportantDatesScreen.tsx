/**
 * ImportantDatesScreen (Phase 10).
 *
 * Lists all important dates, allows adding and managing them.
 * Uses RelationshipService for CRUD operations.
 */

import { useEffect, useState, useCallback } from 'react';
import { coreServices } from '../../../services/bootstrap/appBootstrap.ts';
import type { RelationshipService } from '../../../services/relationship/relationshipService.ts';
import type { ImportantDate, Recurrence } from '../../../data/relationship/relationshipTypes.ts';
import { IconPlus, IconTrash, IconEdit, LoadingState } from '../../../components/index.ts';

function formatDate(dateStr: string): string {
  const [year, month, day] = dateStr.split('-').map(Number);
  const d = new Date(Date.UTC(year, month - 1, day));
  return d.toLocaleDateString(undefined, { year: 'numeric', month: 'long', day: 'numeric' });
}

export function ImportantDatesScreen() {
  const [dates, setDates] = useState<ImportantDate[]>([]);
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
      const all = await svc.listImportantDates();
      setDates(all);
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
      } else {
        await svc.addImportantDate({ title: trimmed, date: dateVal, recurrence });
      }
      resetForm();
      await loadDates();
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'Failed to save.';
      setErrors([msg]);
    } finally {
      setSaving(false);
    }
  }, [title, dateVal, recurrence, editId, loadDates]);

  const handleDelete = useCallback(async (id: string) => {
    setDeletingId(id);
    try {
      const svc = coreServices.relationship as RelationshipService | undefined;
      if (!svc) return;
      await svc.removeImportantDate(id);
      await loadDates();
    } catch {
      // Graceful
    } finally {
      setDeletingId(null);
    }
  }, [loadDates]);

  if (loading) return <LoadingState label="Loading dates…" />;

  return (
    <div className="th-content-pad">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--th-space-4)' }}>
        <h1 className="th-screen-title">Important Dates</h1>
        <button
          className="th-btn th-btn--ghost"
          onClick={() => { resetForm(); setShowForm(!showForm); }}
          style={{ minWidth: 'auto', padding: 'var(--th-space-2)' }}
        >
          <IconPlus size={20} />
        </button>
      </div>

      {showForm && (
        <div className="th-card" style={{ marginBottom: 'var(--th-space-4)' }}>
          <h2 style={{ fontSize: 'var(--th-font-size-md)', fontWeight: 'var(--th-font-weight-semibold)', marginBottom: 'var(--th-space-3)' }}>
            {editId ? 'Edit Date' : 'Add Date'}
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
                placeholder="e.g. Anniversary, Birthday..." autoFocus />
            </div>
            <div className="th-form-group">
              <label className="th-label" htmlFor="date-value">Date *</label>
              <input id="date-value" type="date" className="th-input" value={dateVal}
                onChange={(e) => { setDateVal(e.target.value); setErrors([]); }} />
            </div>
            <div className="th-form-group">
              <label className="th-label">Repeat yearly?</label>
              <div className="th-option-group">
                <button className={`th-option-chip ${recurrence === 'none' ? 'th-option-chip--active' : ''}`}
                  onClick={() => setRecurrence('none')}>Once</button>
                <button className={`th-option-chip ${recurrence === 'yearly' ? 'th-option-chip--active' : ''}`}
                  onClick={() => setRecurrence('yearly')}>Yearly</button>
              </div>
            </div>
            <div className="th-timeline-editor-actions">
              <button className="th-btn th-btn--secondary" onClick={resetForm} disabled={saving}>Cancel</button>
              <button className="th-btn th-btn--primary" onClick={handleSave} disabled={saving || !title.trim() || !dateVal}>
                {saving ? 'Saving...' : editId ? 'Save Changes' : 'Add Date'}
              </button>
            </div>
          </div>
        </div>
      )}

      {dates.length === 0 && !showForm ? (
        <div style={{ textAlign: 'center', padding: 'var(--th-space-12) 0', color: 'var(--th-color-text-secondary)' }}>
          <p style={{ marginBottom: 'var(--th-space-3)' }}>No important dates yet.</p>
          <button className="th-btn th-btn--primary" onClick={() => setShowForm(true)}>
            <IconPlus size={18} /> Add your first date
          </button>
        </div>
      ) : (
        <div className="th-hub-grid">
          {dates.map((d) => (
            <div key={d.id} className="th-feature-card" style={{ cursor: 'default' }}>
              <div className="th-feature-card__body">
                <div className="th-feature-card__title">{d.title}</div>
                <div className="th-feature-card__desc">
                  {formatDate(d.date)}{d.recurrence === 'yearly' ? ' · yearly' : ''}
                </div>
              </div>
              <div style={{ display: 'flex', gap: 'var(--th-space-1)' }}>
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

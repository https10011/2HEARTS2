/**
 * AddEvent (Phase 9).
 *
 * Create/edit timeline event screen with title, date, and description.
 * Validates input and persists through TimelineService.
 * Reused for both add and edit flows via the :eventId URL parameter.
 */

import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { useTimelineService } from './useTimelineService.ts';

export function AddEvent() {
  const navigate = useNavigate();
  const { eventId } = useParams<{ eventId: string }>();
  const { createEvent, updateEvent, getEvent } = useTimelineService();

  const isEditing = Boolean(eventId);

  const [title, setTitle] = useState('');
  const [eventDate, setEventDate] = useState('');
  const [description, setDescription] = useState('');
  const [errors, setErrors] = useState<string[]>([]);
  const [saving, setSaving] = useState(false);
  const [loading, setLoading] = useState(isEditing);

  useEffect(() => {
    if (!eventId) return;
    let cancelled = false;
    (async () => {
      try {
        const event = await getEvent(eventId);
        if (cancelled) return;
        setTitle(event.title);
        setEventDate(event.eventDate);
        setDescription(event.description);
      } catch {
        if (!cancelled) {
          navigate(RoutePath.appTimelineRoot, { replace: true });
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => { cancelled = true; };
  }, [eventId, getEvent, navigate]);

  const handleSave = useCallback(async () => {
    // Client-side validation
    const newErrors: string[] = [];
    const trimmedTitle = title.trim();
    if (!trimmedTitle) {
      newErrors.push('Title is required.');
    } else if (trimmedTitle.length > 200) {
      newErrors.push('Title must be at most 200 characters.');
    }

    if (!eventDate) {
      newErrors.push('Date is required.');
    } else {
      // Validate yyyy-mm-dd format
      const datePattern = /^\d{4}-\d{2}-\d{2}$/;
      if (!datePattern.test(eventDate)) {
        newErrors.push('Date must use YYYY-MM-DD format.');
      } else {
        const parsed = new Date(`${eventDate}T00:00:00Z`);
        if (Number.isNaN(parsed.getTime()) || parsed.toISOString().slice(0, 10) !== eventDate) {
          newErrors.push('Date is not a valid calendar date.');
        }
      }
    }

    if (description.trim().length > 5000) {
      newErrors.push('Description must be at most 5000 characters.');
    }

    if (newErrors.length > 0) {
      setErrors(newErrors);
      return;
    }

    setSaving(true);
    setErrors([]);

    try {
      const input = {
        title: trimmedTitle,
        eventDate,
        description: description.trim() || null,
      };

      if (isEditing && eventId) {
        await updateEvent(eventId, input);
      } else {
        await createEvent(input);
      }
      navigate(RoutePath.appTimelineRoot, { replace: true });
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to save event.';
      setErrors([message]);
    } finally {
      setSaving(false);
    }
  }, [title, eventDate, description, isEditing, eventId, createEvent, updateEvent, navigate]);

  if (loading) {
    return (
      <div className="th-content-pad">
        <div className="th-loading-state">
          <div className="th-spinner" />
          <p>Loading event...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="th-content-pad">
      <h1 className="th-screen-title" style={{ marginBottom: 'var(--th-space-4)' }}>
        {isEditing ? 'Edit Event' : 'Add Event'}
      </h1>

      {errors.length > 0 && (
        <div className="th-form-errors">
          {errors.map((err, i) => (
            <p key={i} className="th-form-error">{err}</p>
          ))}
        </div>
      )}

      <div className="th-form">
        <div className="th-form-group">
          <label className="th-label" htmlFor="event-title">Title *</label>
          <input
            id="event-title"
            type="text"
            className="th-input"
            placeholder="e.g. First Date, Got Engaged..."
            value={title}
            onChange={(e) => { setTitle(e.target.value); setErrors([]); }}
            maxLength={200}
            autoFocus
          />
          <span className="th-char-count">{title.length}/200</span>
        </div>

        <div className="th-form-group">
          <label className="th-label" htmlFor="event-date">Date *</label>
          <input
            id="event-date"
            type="date"
            className="th-input"
            value={eventDate}
            onChange={(e) => { setEventDate(e.target.value); setErrors([]); }}
            max={new Date().toISOString().slice(0, 10)}
          />
        </div>

        <div className="th-form-group">
          <label className="th-label" htmlFor="event-description">Description</label>
          <textarea
            id="event-description"
            className="th-textarea"
            placeholder="Tell the story behind this moment..."
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            rows={8}
          />
          <span className="th-char-count">{description.length}/5000</span>
        </div>
      </div>

      <div className="th-timeline-editor-actions">
        <button
          className="th-btn th-btn--secondary"
          onClick={() => navigate(-1)}
          disabled={saving}
        >
          Cancel
        </button>
        <button
          className="th-btn th-btn--primary"
          onClick={handleSave}
          disabled={saving || !title.trim() || !eventDate}
        >
          {saving ? 'Saving...' : isEditing ? 'Save Changes' : 'Add Event'}
        </button>
      </div>
    </div>
  );
}

/**
 * AddEvent (Stage 7).
 *
 * "Add a moment to your story" — branded editor for timeline moments.
 * Title, centralized DatePicker (Stage 2/4 pattern, no native browser
 * date input), and a story textarea. Validation, limits, and the
 * TimelineService data flow are unchanged.
 */

import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import {
  Button,
  DatePicker,
  Header,
  IconBack,
  IconButton,
  LoadingState,
  RoseLilyDecoration,
  useToast,
} from '../../components/index.ts';
import { useTimelineService } from './useTimelineService.ts';

export function AddEvent() {
  const navigate = useNavigate();
  const { eventId } = useParams<{ eventId: string }>();
  const { createEvent, updateEvent, getEvent } = useTimelineService();
  const toast = useToast();

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
        toast.success('Moment updated');
      } else {
        await createEvent(input);
        toast.success('Moment added to your story');
      }
      navigate(RoutePath.appTimelineRoot, { replace: true });
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to save event.';
      setErrors([message]);
      toast.error('Could not save moment');
    } finally {
      setSaving(false);
    }
  }, [title, eventDate, description, isEditing, eventId, createEvent, updateEvent, navigate, toast]);

  if (loading) {
    return (
      <div className="th-screen">
        <Header
          title="Edit Moment"
          left={
            <IconButton label="Go back" onClick={() => navigate(-1)}>
              <IconBack />
            </IconButton>
          }
        />
        <LoadingState label="Loading moment…" />
      </div>
    );
  }

  return (
    <div className="th-screen th-screen-warm">
      <RoseLilyDecoration variant={11} size={110} position="bottom-right" opacity={0.08} />
      <Header
        title={isEditing ? 'Edit Moment' : 'Add to Timeline'}
        left={
          <IconButton label="Go back" onClick={() => navigate(-1)}>
            <IconBack />
          </IconButton>
        }
      />

      <div className="th-scroll th-content-pad">
        {/* Emotional intro */}
        <h2 className="th-tl-intro">
          {isEditing ? 'Edit this moment.' : 'Add a moment to your story.'}
        </h2>
        <p className="th-tl-intro__sub">
          {isEditing
            ? 'Update the details so your story stays true to how it felt.'
            : 'Save something meaningful so you can look back on it later.'}
        </p>

        {errors.length > 0 && (
          <div className="th-form-errors" role="alert">
            {errors.map((err, i) => (
              <p key={i} className="th-form-error">{err}</p>
            ))}
          </div>
        )}

        <div className="th-form">
          <div className="th-form-group">
            <label className="th-label" htmlFor="event-title">Event title</label>
            <input
              id="event-title"
              type="text"
              className="th-input"
              placeholder="Give this moment a name"
              value={title}
              onChange={(e) => { setTitle(e.target.value); setErrors([]); }}
              maxLength={200}
              autoFocus
            />
            <span className="th-char-count">{title.length}/200</span>
          </div>

          <div className="th-form-group">
            <span className="th-label">Date</span>
            <DatePicker
              value={eventDate}
              onChange={(iso) => { setEventDate(iso); setErrors([]); }}
              label="Event date"
              placeholder="Add a date"
            />
          </div>

          <div className="th-form-group">
            <label className="th-label" htmlFor="event-description">Tell the story</label>
            <textarea
              id="event-description"
              className="th-textarea"
              placeholder="What happened?"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={6}
            />
            <span className="th-char-count">{description.length}/5000</span>
          </div>
        </div>

        <div className="th-tl-editor-actions">
          <Button
            variant="primary"
            full
            onClick={handleSave}
            disabled={saving || !title.trim() || !eventDate}
          >
            {saving ? 'Saving…' : isEditing ? 'Save Changes' : 'Add to Timeline'}
          </Button>
          <Button
            variant="ghost"
            full
            onClick={() => navigate(-1)}
            disabled={saving}
          >
            Cancel
          </Button>
        </div>
      </div>
    </div>
  );
}

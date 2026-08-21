/**
 * EventDetail (Phase 9).
 *
 * Individual timeline event view with full details, edit, and delete actions.
 */

import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { useTimelineService } from './useTimelineService.ts';
import type { TimelineEventView } from '../../services/timeline/timelineService.ts';
import { IconTrash, IconEdit, IconCalendar } from '../../components/index.ts';

export function EventDetail() {
  const navigate = useNavigate();
  const { eventId } = useParams<{ eventId: string }>();
  const { getEvent, deleteEvent } = useTimelineService();

  const [event, setEvent] = useState<TimelineEventView | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    if (!eventId) {
      navigate(RoutePath.appTimelineRoot, { replace: true });
      return;
    }
    let cancelled = false;
    (async () => {
      try {
        const e = await getEvent(eventId);
        if (!cancelled) setEvent(e);
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : 'Event not found.');
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => { cancelled = true; };
  }, [eventId, getEvent, navigate]);

  const handleDelete = useCallback(async () => {
    if (!eventId) return;
    setDeleting(true);
    try {
      await deleteEvent(eventId);
      navigate(RoutePath.appTimelineRoot, { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete event.');
      setDeleting(false);
      setShowDeleteConfirm(false);
    }
  }, [eventId, deleteEvent, navigate]);

  const formatDate = (dateStr: string): string => {
    const [year, month, day] = dateStr.split('-').map(Number);
    const date = new Date(Date.UTC(year, month - 1, day));
    return date.toLocaleDateString(undefined, {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  const formatTimestamp = (iso: string): string => {
    const d = new Date(iso);
    return d.toLocaleDateString(undefined, {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

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

  if (error || !event) {
    return (
      <div className="th-content-pad">
        <div className="th-empty-state th-empty-state--enhanced">
          <div className="th-empty-state__visual">
            <IconCalendar size={36} />
          </div>
          <h3 className="th-empty-state__title">Event not found</h3>
          <p className="th-empty-state__desc">{error ?? 'This event may have been deleted.'}</p>
          <button
            className="th-btn th-btn--primary"
            onClick={() => navigate(RoutePath.appTimelineRoot)}
          >
            Back to Timeline
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="th-content-pad">
      {/* Header with actions */}
      <div className="th-timeline-detail-header">
        <button
          className="th-btn th-btn--secondary th-btn--sm"
          onClick={() => navigate(RoutePath.appTimelineRoot)}
        >
          ← Back
        </button>
        <div className="th-timeline-detail-actions">
          <button
            className="th-icon-btn"
            onClick={() => navigate(`${RoutePath.appTimelineRoot}/${event.id}/edit`)}
            aria-label="Edit event"
          >
            <IconEdit size={18} />
          </button>
          <button
            className="th-icon-btn th-icon-btn--danger"
            onClick={() => setShowDeleteConfirm(true)}
            aria-label="Delete event"
          >
            <IconTrash size={18} />
          </button>
        </div>
      </div>

      {/* Event content */}
      <div className="th-timeline-detail">
        <div className="th-timeline-detail__date">{formatDate(event.eventDate)}</div>
        <h1 className="th-timeline-detail__title">{event.title}</h1>
        <div className="th-timeline-detail__meta">
          <span>Created: {formatTimestamp(event.createdAt)}</span>
          {event.updatedAt !== event.createdAt && (
            <span>Updated: {formatTimestamp(event.updatedAt)}</span>
          )}
        </div>
        <div className="th-timeline-detail__content">
          {event.description ? (
            event.description.split('\n').map((line, i) => (
              <p key={i}>{line || '\u00A0'}</p>
            ))
          ) : (
            <p className="th-timeline-detail__empty">No description</p>
          )}
        </div>
      </div>

      {/* Delete confirmation modal */}
      {showDeleteConfirm && (
        <div className="th-modal-overlay" onClick={() => setShowDeleteConfirm(false)}>
          <div className="th-modal" onClick={(e) => e.stopPropagation()}>
            <h2 className="th-modal__title">Delete Event</h2>
            <p className="th-modal__text">
              Are you sure you want to delete &ldquo;{event.title}&rdquo;? This cannot be undone.
            </p>
            <div className="th-modal__actions">
              <button
                className="th-btn th-btn--secondary"
                onClick={() => setShowDeleteConfirm(false)}
                disabled={deleting}
              >
                Cancel
              </button>
              <button
                className="th-btn th-btn--danger"
                onClick={handleDelete}
                disabled={deleting}
              >
                {deleting ? 'Deleting...' : 'Delete'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

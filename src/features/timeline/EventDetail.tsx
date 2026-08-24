/**
 * EventDetail (Stage 7).
 *
 * One page from the couple's story: date-forward header, serif title,
 * "The story" reading section, a quiet "Your story · Chapter N" band,
 * and deliberate Edit/Delete actions. Delete confirmation uses the
 * centralized Modal bottom-sheet (no second modal framework).
 */

import { useState, useEffect, useCallback, useMemo } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { useTimelineService } from './useTimelineService.ts';
import { chapterOf, formatEventDate } from './timelineStory.ts';
import type { TimelineEventView } from '../../services/timeline/timelineService.ts';
import {
  Button,
  Header,
  IconBack,
  IconButton,
  IconCalendar,
  IconEdit,
  IconTrash,
  LoadingState,
  Modal,
  RoseLilyDecoration,
  useToast,
} from '../../components/index.ts';

export function EventDetail() {
  const navigate = useNavigate();
  const { eventId } = useParams<{ eventId: string }>();
  const { events, getEvent, deleteEvent } = useTimelineService();
  const toast = useToast();

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
      toast.success('Moment deleted');
      navigate(RoutePath.appTimelineRoot, { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete event.');
      setDeleting(false);
      setShowDeleteConfirm(false);
      toast.error('Could not delete moment');
    }
  }, [eventId, deleteEvent, navigate, toast]);

  const chapter = useMemo(
    () => (eventId ? chapterOf(events, eventId) : null),
    [events, eventId],
  );

  const backButton = (
    <IconButton label="Go back" onClick={() => navigate(-1)}>
      <IconBack />
    </IconButton>
  );

  if (loading) {
    return (
      <div className="th-screen">
        <Header title="Moment" left={backButton} />
        <LoadingState label="Opening this moment…" />
      </div>
    );
  }

  if (error || !event) {
    return (
      <div className="th-screen">
        <Header title="Moment" left={backButton} />
        <div className="th-scroll th-content-pad">
          <div className="th-empty-state th-empty-state--enhanced">
            <div className="th-empty-state__visual">
              <IconCalendar size={36} />
            </div>
            <h3 className="th-empty-state__title">Moment not found</h3>
            <p className="th-empty-state__desc">{error ?? 'This moment may have been deleted.'}</p>
            <button
              className="th-btn th-btn--primary"
              onClick={() => navigate(RoutePath.appTimelineRoot)}
            >
              Back to Timeline
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="th-screen th-screen-warm">
      <RoseLilyDecoration variant={11} size={110} position="bottom-right" opacity={0.08} />
      <Header
        title="Moment"
        left={backButton}
        right={
          <IconButton
            label="Delete event"
            onClick={() => setShowDeleteConfirm(true)}
          >
            <IconTrash />
          </IconButton>
        }
      />

      <div className="th-scroll th-content-pad">
        {/* Date-forward identity */}
        <p className="th-tl-detail__date">
          <IconCalendar size={14} />
          {formatEventDate(event.eventDate)}
        </p>
        <h1 className="th-tl-detail__title">{event.title}</h1>

        {/* The story */}
        <h2 className="th-tl-detail__story-label">The story</h2>
        <div className="th-tl-detail__content">
          {event.description ? (
            event.description.split('\n').map((line, i) => (
              <p key={i}>{line || ' '}</p>
            ))
          ) : (
            <p className="th-tl-detail__empty">
              No story written yet — edit this moment to add one.
            </p>
          )}
        </div>

        {/* Chapter band */}
        {chapter !== null && (
          <p className="th-tl-detail__chapter">Your story · Chapter {chapter}</p>
        )}

        {/* Actions */}
        <div className="th-tl-detail__actions">
          <Button
            variant="primary"
            full
            onClick={() => navigate(`${RoutePath.appTimelineRoot}/${event.id}/edit`)}
          >
            <IconEdit size={18} /> Edit event
          </Button>
          <button
            className="th-tl-detail__delete"
            onClick={() => setShowDeleteConfirm(true)}
          >
            <IconTrash size={16} /> Delete this moment
          </button>
        </div>

        {/* Quiet metadata */}
        <p className="th-tl-detail__meta">
          Added {new Date(event.createdAt).toLocaleDateString()}
          {event.updatedAt !== event.createdAt &&
            ` · Updated ${new Date(event.updatedAt).toLocaleDateString()}`}
        </p>
      </div>

      {/* Delete confirmation (centralized bottom-sheet) */}
      <Modal
        open={showDeleteConfirm}
        onClose={() => setShowDeleteConfirm(false)}
        label="Delete event"
      >
        <div style={{ padding: 'var(--th-space-2) 0' }}>
          <h3 className="th-note-confirm-title">Delete this moment?</h3>
          <p className="th-note-confirm-copy">
            “{event.title}” will be removed from your story permanently.
            This action cannot be undone.
          </p>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--th-space-3)' }}>
            <Button variant="primary" full onClick={handleDelete} disabled={deleting}>
              {deleting ? 'Deleting…' : 'Delete moment'}
            </Button>
            <Button variant="ghost" full onClick={() => setShowDeleteConfirm(false)} disabled={deleting}>
              Cancel
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}

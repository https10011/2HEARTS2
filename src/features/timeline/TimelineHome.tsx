/**
 * TimelineHome (Phase 9).
 *
 * Main timeline screen — chronological list of relationship events.
 * Shows events ordered by date (earliest first), with add-event action,
 * proper empty state, and navigation to event details.
 */

import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import { IconPlus, IconChevronRight, IconCalendar } from '../../components/index.ts';
import { useTimelineService } from './useTimelineService.ts';
import type { TimelineEventView } from '../../services/timeline/timelineService.ts';

export function TimelineHome() {
  const navigate = useNavigate();
  const { events, loading, error } = useTimelineService();

  const formatDate = (dateStr: string): string => {
    const [year, month, day] = dateStr.split('-').map(Number);
    const date = new Date(Date.UTC(year, month - 1, day));
    return date.toLocaleDateString(undefined, {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  if (loading) {
    return (
      <div className="th-content-pad">
        <div className="th-loading-state">
          <div className="th-spinner" />
          <p>Loading timeline...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="th-content-pad" style={{ textAlign: 'center', paddingTop: 'var(--th-space-12)' }}>
        <p style={{ color: 'var(--th-color-error)', marginBottom: 'var(--th-space-4)' }}>{error}</p>
        <button className="th-btn th-btn--secondary" onClick={() => window.location.reload()}>
          Retry
        </button>
      </div>
    );
  }

  // Empty state
  if (events.length === 0) {
    return (
      <div className="th-content-pad">
        <div className="th-empty-state th-empty-state--enhanced">
          <div className="th-empty-state__visual">
            <IconCalendar size={36} />
          </div>
          <h3 className="th-empty-state__title">No events yet</h3>
          <p className="th-empty-state__desc">
            Start building your relationship timeline
          </p>
          <button className="th-btn th-btn--primary" onClick={() => navigate(RoutePath.appTimelineAdd)}>
            <IconPlus size={18} /> Add your first event
          </button>
        </div>
      </div>
    );
  }

  // Timeline list
  return (
    <div className="th-content-pad">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--th-space-4)' }}>
        <h1 className="th-screen-title">Timeline</h1>
        <button
          className="th-btn th-btn--ghost"
          onClick={() => navigate(RoutePath.appTimelineAdd)}
          style={{ minWidth: 'auto', padding: 'var(--th-space-2)' }}
        >
          <IconPlus size={20} />
        </button>
      </div>

      <div className="th-timeline-list">
        {events.map((event, index) => (
          <TimelineEventCard
            key={event.id}
            event={event}
            formatDate={formatDate}
            isFirst={index === 0}
            isLast={index === events.length - 1}
          />
        ))}
      </div>

      {/* FAB */}
      <button
        className="th-fab"
        onClick={() => navigate(RoutePath.appTimelineAdd)}
        aria-label="Add event"
      >
        <IconPlus size={24} />
      </button>
    </div>
  );
}

function TimelineEventCard({
  event,
  formatDate,
  isFirst,
  isLast,
}: {
  event: TimelineEventView;
  formatDate: (dateStr: string) => string;
  isFirst: boolean;
  isLast: boolean;
}) {
  const navigate = useNavigate();

  return (
    <div className="th-timeline-event" data-first={isFirst} data-last={isLast}>
      {/* Timeline connector line */}
      <div className="th-timeline-event__connector">
        <div className="th-timeline-event__dot" />
        {!isLast && <div className="th-timeline-event__line" />}
      </div>

      {/* Event content */}
      <div
        className="th-timeline-event__card"
        onClick={() => navigate(`${RoutePath.appTimelineRoot}/${event.id}`)}
        role="button"
        tabIndex={0}
        onKeyDown={(e) => {
          if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            navigate(`${RoutePath.appTimelineRoot}/${event.id}`);
          }
        }}
      >
        <div className="th-timeline-event__date">{formatDate(event.eventDate)}</div>
        <h3 className="th-timeline-event__title">{event.title}</h3>
        {event.excerpt && (
          <p className="th-timeline-event__excerpt">{event.excerpt}</p>
        )}
        <IconChevronRight size={16} className="th-timeline-event__chevron" />
      </div>
    </div>
  );
}

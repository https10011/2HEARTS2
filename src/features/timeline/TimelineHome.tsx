/**
 * TimelineHome (Stage 7).
 *
 * "Our story" — the couple's relationship history rendered as a warm
 * chronological narrative: branded header, story banner, a continuous
 * spine with ring markers, year anchors when the story spans years,
 * and the newest moment emphasized as "Latest". Events still flow
 * unchanged through TimelineService → TimelineRepository → SQLite.
 */

import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import {
  IconPlus,
  IconBack,
  IconChevronRight,
  IconCalendar,
  IconHeart,
  IconButton,
  LoadingState,
  RoseLilyDecoration,
} from '../../components/index.ts';
import { useTimelineService } from './useTimelineService.ts';
import { buildStoryRows, formatEventDate } from './timelineStory.ts';
import type { TimelineEventView } from '../../services/timeline/timelineService.ts';

export function TimelineHome() {
  const navigate = useNavigate();
  const { events, loading, error, loadEvents } = useTimelineService();
  const rows = buildStoryRows(events);

  if (loading) {
    return <LoadingState label="Opening your story…" />;
  }

  if (error) {
    return (
      <div className="th-content-pad" style={{ textAlign: 'center', paddingTop: 'var(--th-space-12)' }}>
        <p style={{ color: 'var(--th-color-error)', marginBottom: 'var(--th-space-4)' }}>{error}</p>
        <button className="th-btn th-btn--secondary" onClick={loadEvents}>
          Try again
        </button>
      </div>
    );
  }

  return (
    <div className="th-content-pad th-screen-warm">
      <RoseLilyDecoration variant={14} size={120} position="top-right" opacity={0.12} />

      {/* Branded header */}
      <header className="th-tl-header">
        <IconButton label="Go back" onClick={() => navigate(-1)}>
          <IconBack />
        </IconButton>
        <div className="th-tl-header__copy">
          <h1 className="th-tl-title">Timeline</h1>
          <p className="th-tl-subtitle">Your story, one moment at a time.</p>
        </div>
        <IconButton label="Add event" onClick={() => navigate(RoutePath.appTimelineAdd)}>
          <IconPlus />
        </IconButton>
      </header>

      {events.length === 0 ? (
        <div className="th-empty-emotional">
          <div className="th-empty-emotional__visual th-scale-in">
            <IconHeart size={42} />
          </div>
          <h3 className="th-empty-emotional__title">Your story starts here</h3>
          <p className="th-empty-emotional__message">
            Your story is waiting for its first chapter — add the moment where it all began.
          </p>
          <div className="th-empty-emotional__action">
            <button className="th-btn th-btn--primary" onClick={() => navigate(RoutePath.appTimelineAdd)}>
              <IconPlus size={18} /> Add your first moment
            </button>
          </div>
        </div>
      ) : (
        <>
          {/* Story banner */}
          <section className="th-tl-banner th-stagger-item">
            <div className="th-tl-banner__copy">
              <h2 className="th-tl-banner__title">Our story</h2>
              <p className="th-tl-banner__text">
                Every little moment becomes part of something bigger.
              </p>
            </div>
            <span className="th-tl-banner__icon" aria-hidden="true">
              <IconCalendar size={26} />
            </span>
          </section>

          {/* Chronological spine */}
          <div className="th-timeline-list">
            {rows.map((row, rowIndex) =>
              row.type === 'year' ? (
                <div className="th-timeline-year" key={row.key}>
                  <div className="th-timeline-event__connector th-timeline-event__connector--bare">
                    <div className="th-timeline-event__line" />
                  </div>
                  <span className="th-timeline-year__label">{row.year}</span>
                </div>
              ) : (
                <TimelineEventCard
                  key={row.key}
                  event={row.event}
                  chapter={row.chapter}
                  isLatest={row.isLatest}
                  isLastRow={rowIndex === rows.length - 1}
                />
              ),
            )}
          </div>

          <p className="th-tl-footer">More memories are waiting to be added.</p>

          {/* FAB */}
          <button
            className="th-fab"
            onClick={() => navigate(RoutePath.appTimelineAdd)}
            aria-label="Add event"
          >
            <IconPlus size={24} />
          </button>
        </>
      )}
    </div>
  );
}

function TimelineEventCard({
  event,
  chapter,
  isLatest,
  isLastRow,
}: {
  event: TimelineEventView;
  chapter: number;
  isLatest: boolean;
  isLastRow: boolean;
}) {
  const navigate = useNavigate();
  const open = () => navigate(`${RoutePath.appTimelineRoot}/${event.id}`);

  return (
    <div
      className={`th-timeline-event th-stagger-item${isLatest ? ' th-timeline-event--latest' : ''}`}
    >
      {/* Spine: ring marker + connecting line */}
      <div className="th-timeline-event__connector">
        <div className="th-timeline-event__dot" />
        {!isLastRow && <div className="th-timeline-event__line" />}
      </div>

      {/* Moment card */}
      <div
        className="th-timeline-event__card"
        onClick={open}
        role="button"
        tabIndex={0}
        aria-label={`Chapter ${chapter}: ${event.title}`}
        onKeyDown={(e) => {
          if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            open();
          }
        }}
      >
        <span className="th-timeline-event__badge" aria-hidden="true">
          {isLatest ? <IconHeart size={18} /> : <IconCalendar size={18} />}
        </span>
        <div className="th-timeline-event__body">
          {isLatest && <span className="th-tl-pill">Latest</span>}
          <div className="th-timeline-event__date">{formatEventDate(event.eventDate)}</div>
          <h3 className="th-timeline-event__title">{event.title}</h3>
          {event.excerpt && (
            <p className="th-timeline-event__excerpt">{event.excerpt}</p>
          )}
        </div>
        <IconChevronRight size={16} className="th-timeline-event__chevron" />
      </div>
    </div>
  );
}

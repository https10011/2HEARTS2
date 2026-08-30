/**
 * RemindersHome (Stage 8).
 *
 * "The little things we remember for each other" — the reminders list as a
 * warm, date-forward product screen: branded header, filter chips, a "next
 * up" hero for the soonest reminder, today/upcoming sections with icon
 * badges, and a quiet history. Data still flows unchanged through
 * ReminderService → ReminderRepository → SQLite; all grouping/labeling is
 * pure client-side derivation in reminderSchedule.ts.
 */

import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { RoutePath } from '../../navigation/routes.ts';
import {
  IconBack,
  IconPlus,
  IconBell,
  IconClock,
  IconRepeat,
  IconChevronRight,
  IconButton,
  LoadingState,
  RoseLilyDecoration,
} from '../../components/index.ts';
import type { Reminder, ReminderStatus } from '../../data/reminder/reminderTypes.ts';
import { useReminderService } from './useReminderService.ts';
import {
  buildReminderGroups,
  filterReminderGroups,
  formatReminderDate,
  formatReminderTime,
  historyStatusLabel,
  relativeDayLabel,
  RECURRENCE_LABELS,
  type ReminderFilter,
} from './reminderSchedule.ts';

const FILTERS: { key: ReminderFilter; label: string }[] = [
  { key: 'all', label: 'All' },
  { key: 'today', label: 'Today' },
  { key: 'upcoming', label: 'Upcoming' },
  { key: 'done', label: 'Done' },
];

export function RemindersHome() {
  const navigate = useNavigate();
  const service = useReminderService();
  const [reminders, setReminders] = useState<Reminder[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<ReminderFilter>('all');

  const loadReminders = useCallback(async () => {
    if (!service) return;
    try {
      const list = await service.list();
      setReminders(list);
    } catch {
      // Graceful degradation — show empty state
    } finally {
      setLoading(false);
    }
  }, [service]);

  useEffect(() => {
    loadReminders();
  }, [loadReminders]);

  if (loading) {
    return <LoadingState label="Gathering your reminders…" />;
  }

  const groups = filterReminderGroups(buildReminderGroups(reminders), filter);
  const isEmpty = reminders.length === 0;

  return (
    <div className="th-content-pad th-screen-warm">
      <RoseLilyDecoration variant={14} size={120} position="top-right" opacity={0.12} />

      {/* Branded header */}
      <header className="th-rem-header">
        <IconButton label="Go back" onClick={() => navigate(-1)}>
          <IconBack />
        </IconButton>
        <div className="th-rem-header__copy">
          <h1 className="th-rem-title">Reminders</h1>
          <p className="th-rem-subtitle">The little things we remember for each other.</p>
        </div>
        <IconButton label="Add reminder" onClick={() => navigate(RoutePath.appRemindersAdd)}>
          <IconPlus />
        </IconButton>
      </header>

      {isEmpty ? (
        <div className="th-empty-emotional">
          <div className="th-empty-emotional__visual th-scale-in">
            <IconBell size={42} />
          </div>
          <h3 className="th-empty-emotional__title">Nothing to remember yet</h3>
          <p className="th-empty-emotional__message">
            Save the little things — a call, a date, a small promise — and we&apos;ll
            keep them safe until it&apos;s time.
          </p>
          <div className="th-empty-emotional__action">
            <button className="th-btn th-btn--primary" onClick={() => navigate(RoutePath.appRemindersAdd)}>
              <IconPlus size={18} /> Add your first reminder
            </button>
          </div>
        </div>
      ) : (
        <>
          {/* Filter chips */}
          <div className="th-rem-filters th-stagger-item" role="group" aria-label="Filter reminders">
            {FILTERS.map((f) => (
              <button
                key={f.key}
                type="button"
                className={`th-option-chip ${filter === f.key ? 'th-option-chip--active' : ''}`}
                aria-pressed={filter === f.key}
                onClick={() => setFilter(f.key)}
              >
                {f.label}
              </button>
            ))}
          </div>

          {groups.next && filter !== 'done' && <NextUpHero reminder={groups.next} />}

          {groups.today.length > 0 && (
            <ReminderSection
              title="Today"
              reminders={groups.today}
              heroId={groups.next?.id ?? null}
            />
          )}

          {groups.upcoming.length > 0 && (
            <ReminderSection
              title="Upcoming"
              reminders={groups.upcoming}
              heroId={groups.next?.id ?? null}
            />
          )}

          {groups.history.length > 0 && (
            <section className="th-rem-section th-stagger-item">
              <h2 className="th-rem-section__title">
                <span className="th-rem-section__badge" aria-hidden="true">
                  <IconBell size={14} />
                </span>
                Earlier
              </h2>
              <div className="th-rem-list">
                {groups.history.map((reminder) => (
                  <HistoryRow key={reminder.id} reminder={reminder} />
                ))}
              </div>
            </section>
          )}

          {!groups.next && groups.today.length === 0 && groups.upcoming.length === 0 && groups.history.length === 0 && (
            <p className="th-rem-none">Nothing matches this filter right now.</p>
          )}

          <p className="th-rem-footer">A gentle nudge, right when it matters.</p>

          {/* FAB */}
          <button
            className="th-fab"
            onClick={() => navigate(RoutePath.appRemindersAdd)}
            aria-label="Add reminder"
          >
            <IconPlus size={24} />
          </button>
        </>
      )}
    </div>
  );
}

/* ------------------------------------------------------------------ */
/*  "Next up" hero — the soonest reminder, emphasized                  */
/* ------------------------------------------------------------------ */

function NextUpHero({ reminder }: { reminder: Reminder }) {
  const navigate = useNavigate();
  const open = () => navigate(`${RoutePath.appReminders}/${reminder.id}`);

  return (
    <section
      className="th-rem-hero th-stagger-item"
      onClick={open}
      role="button"
      tabIndex={0}
      aria-label={`Next up: ${reminder.title}, ${relativeDayLabel(reminder.scheduledDate)} at ${formatReminderTime(reminder.scheduledTime)}`}
      onKeyDown={(e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          open();
        }
      }}
    >
      <span className="th-rem-hero__badge" aria-hidden="true">
        <IconBell size={22} />
      </span>
      <div className="th-rem-hero__body">
        <div className="th-rem-hero__when">{relativeDayLabel(reminder.scheduledDate)}</div>
        <h2 className="th-rem-hero__title">{reminder.title}</h2>
        <div className="th-rem-hero__meta">
          <IconClock size={14} />
          <span>{formatReminderTime(reminder.scheduledTime)}</span>
          {reminder.recurrence !== 'none' && (
            <span className="th-rem-hero__repeat">
              <IconRepeat size={12} /> {RECURRENCE_LABELS[reminder.recurrence]}
            </span>
          )}
        </div>
      </div>
      <IconChevronRight size={18} className="th-rem-hero__chevron" />
    </section>
  );
}

/* ------------------------------------------------------------------ */
/*  Sections                                                           */
/* ------------------------------------------------------------------ */

function ReminderSection({
  title,
  reminders,
  heroId,
}: {
  title: string;
  reminders: Reminder[];
  heroId: string | null;
}) {
  const visible = reminders.filter((r) => r.id !== heroId);
  if (visible.length === 0) return null;

  return (
    <section className="th-rem-section th-stagger-item">
      <h2 className="th-rem-section__title">
        <span className="th-rem-section__badge" aria-hidden="true">
          <IconBell size={14} />
        </span>
        {title}
      </h2>
      <div className="th-rem-list">
        {visible.map((reminder) => (
          <ReminderRow key={reminder.id} reminder={reminder} />
        ))}
      </div>
    </section>
  );
}

function ReminderRow({ reminder }: { reminder: Reminder }) {
  const navigate = useNavigate();
  const open = () => navigate(`${RoutePath.appReminders}/${reminder.id}`);

  return (
    <div
      className="th-rem-row"
      onClick={open}
      role="button"
      tabIndex={0}
      aria-label={`${reminder.title}, ${relativeDayLabel(reminder.scheduledDate)} at ${formatReminderTime(reminder.scheduledTime)}`}
      onKeyDown={(e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          open();
        }
      }}
    >
      <span className="th-rem-row__badge" aria-hidden="true">
        {reminder.recurrence !== 'none' ? <IconRepeat size={16} /> : <IconBell size={16} />}
      </span>
      <div className="th-rem-row__body">
        <div className="th-rem-row__date">{relativeDayLabel(reminder.scheduledDate)}</div>
        <h3 className="th-rem-row__title">{reminder.title}</h3>
        {reminder.description && <p className="th-rem-row__excerpt">{reminder.description}</p>}
      </div>
      <span className="th-rem-row__time">
        <IconClock size={13} />
        {formatReminderTime(reminder.scheduledTime)}
      </span>
    </div>
  );
}

function HistoryRow({ reminder }: { reminder: Reminder }) {
  const navigate = useNavigate();
  const open = () => navigate(`${RoutePath.appReminders}/${reminder.id}`);
  const status: ReminderStatus = reminder.status;

  return (
    <div
      className="th-rem-row th-rem-row--history"
      onClick={open}
      role="button"
      tabIndex={0}
      aria-label={`${reminder.title}, ${historyStatusLabel(status)}`}
      onKeyDown={(e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          open();
        }
      }}
    >
      <div className="th-rem-row__body">
        <h3 className="th-rem-row__title">{reminder.title}</h3>
        <div className="th-rem-row__history-meta">
          {historyStatusLabel(status)} · {formatReminderDate(reminder.scheduledDate)}
        </div>
      </div>
    </div>
  );
}

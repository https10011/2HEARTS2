# Stage 8 — Visual Productization: Reminders + Important Dates

## 1. Stage objective

Productize the Reminders experience (Home, Create/Edit, Detail, dialogs) and
verify the Important Dates integration so both feel like a natural part of the
TwoHearts relationship experience — "the little things we remember for each
other" rather than a generic task/reminder manager.

## 2. Starting commit

`f3cb3fd` — "Stage 7 visual productization: Timeline + Story experience"
(master == origin/master at stage start, working tree clean).

## 3. Ending commit

Recorded at stage completion (see `git log -1`; commit message:
"Stage 8 visual productization: Reminders + Important Dates").

## 4. Stage 7 audit result

Audited and preserved, no defects found:

- `git log` confirms Stage 7 committed at `f3cb3fd`, pushed, and
  `master == origin/master`.
- `docs/STAGE-7-VISUAL-PRODUCTIZATION.md` present.
- Stage 7 Timeline sources intact: `features/timeline/TimelineHome.tsx`,
  `AddEvent.tsx`, `EventDetail.tsx`, `timelineStory.ts`, the centralized
  `components/DatePicker.tsx`, and the Stage 7 `.th-tl-*` CSS vocabulary.
- Full test suite (including `tests/stage7-timeline.test.ts` and the
  DatePicker coverage) passes after Stage 8 changes — Timeline navigation,
  creation, editing, deletion, DatePicker behavior, modal/toast systems,
  and the global shell are unbroken.
- Stage 8 reused the Stage 2/7 centralized DatePicker rather than creating
  a second date system; no Timeline code was touched.

## 5. Files changed

New:

- `src/components/TimePicker.tsx` — centralized branded time-picker wheel
  (hour / minute / AM-PM columns), mirroring the centralized DatePicker
  interaction model; replaces the native `<input type="time">`.
- `src/features/reminders/reminderSchedule.ts` — pure, Node-testable
  reminder logic: `buildReminderGroups` (next / today / upcoming / history),
  `isAhead`, `relativeDayLabel`, `formatReminderTime`,
  `historyStatusLabel`, `RECURRENCE_LABELS`, `ReminderFilter` type,
  recurrence advancement for repeating reminders.
- `tests/stage8-reminders.test.ts` — focused Stage 8 suite.

Modified:

- `src/features/reminders/RemindersHome.tsx` — full productization: branded
  header with rose accent and warm micro-copy ("The little things we
  remember for each other"), filter chips (All / Today / Upcoming / Done),
  blush "next up" hero card, Today/Upcoming/History sections with
  date-forward cards (status eyebrow, serif title, time chip, repeat badge,
  note preview), emotional empty state, footer line
  ("A gentle nudge, right when it matters"), staggered entrances.
- `src/features/reminders/CreateReminder.tsx` — branded composer: serif
  header, labeled fields, centralized DatePicker for date, new centralized
  TimePicker for time, repeat-selection bottom sheet, "Notify me" Switch
  row with warm copy, note textarea, warm validation banner, edit mode
  (prefill + "Save Changes"), toasts on save.
- `src/features/reminders/ReminderDetail.tsx` — product screen instead of
  record viewer: branded header with delete affordance, bell emblem, serif
  title, date-forward moment card (relative day eyebrow, large serif time,
  full date), Repeat and Notification info rows, note card, status line,
  primary Edit / secondary Mark-as-done / text Delete actions, branded
  bottom-sheet delete confirmation ("…will be removed for good."),
  toast feedback.
- `src/components/Icon.tsx` — added `IconBell`, `IconRepeat`,
  `IconChevronRight` to the centralized icon set (no emoji, no ad-hoc SVG).
- `src/components/index.ts` — export `TimePicker` + new icons.
- `src/components/primitives.css` — Stage 8 `.th-reminders-*` vocabulary
  (+588 lines): home layout, filter chips, next-up hero, reminder cards,
  section labels, composer fields, repeat sheet, detail moment card,
  delete sheet, TimePicker wheel columns; all token-driven with dark-theme
  overrides and reduced-motion guards.

## 6. Screens changed

- Reminders Home (`/app/reminders`)
- Create Reminder (`/app/reminders/add`)
- Edit Reminder (`/app/reminders/:id/edit`)
- Reminder Detail (`/app/reminders/:id`)
- Delete confirmation bottom sheet
- Repeat-selection bottom sheet
- Important Dates (`/app/us/reminders`) — audit only; Stage 4
  implementation preserved, verified consistent.

## 7. Visual improvements

- Date-forward hierarchy: every card leads with the moment (relative day
  eyebrow + time), not the record fields.
- "Next up" blush hero gives the nearest moment a warm focal point;
  sections (Today / Upcoming / History) group the rest.
- Warm, relationship-oriented language throughout: header micro-copy,
  empty state ("Save the little things — a call, a date, a small
  promise…"), notification row ("Your phone will gently nudge you when
  it's time."), footer line, gentle validation ("Give this reminder a
  name."), delete sheet ("…will be removed for good.").
- Cards are deliberately composed (status eyebrow, icon medallion —
  bell for one-time, repeat glyph for recurring — serif titles, time
  chips) rather than identical white boxes.
- Branded wheel pickers (date + time) replace native inputs; repeat uses a
  bottom sheet; confirmations use the shared sheet vocabulary.

## 8. Functional preservation

- ReminderService, ReminderRepository, notification scheduling,
  persistence, and routes untouched — presentation-layer changes only.
- Create / edit / delete / mark-done flows verified end-to-end in the
  browser; recurrence advancement verified (weekly reminder rolled from
  Aug 24 to Aug 31 after "Mark as done"; detail shows the next moment).
- One behavioral fix: `buildReminderGroups` previously surfaced a
  passed-but-still-today one-time moment as the "next up" hero; `next`
  now requires `isAhead(r, now)` so the hero is always a genuinely
  upcoming moment while passed-today moments stay visible in Today.

## 9. Notification architecture preservation

Local-first architecture fully preserved. No FCM, no remote scheduling,
no server APIs, no cloud storage. Reminder scheduling still flows through
the existing ReminderService → NotificationService (local native
notifications) path; the "Notify me" switch only toggles the existing
local scheduling flag. Native delivery itself is not reproducible in
Vite (browser has no Android notification driver) — the UI states and
the scheduling code path were verified, native delivery was not faked.

## 10. Important Dates integration

Stage 4 Important Dates screen audited, not rebuilt:

- Verified empty state, add form (title + centralized DatePicker +
  Once/Every-year toggle), populated card (date, "In 12 days" countdown,
  Yearly badge, edit/delete affordances), and Us-hub "Coming up" entry.
- Verified it shares the same centralized DatePicker, warm language, and
  card vocabulary as the Stage 8 reminders UI; no duplicated logic added.
- Yearly-recurrence semantics remain owned by the relationship service.

## 11. DatePicker usage

The single centralized `components/DatePicker.tsx` (Stage 2/7) is reused
unchanged in the reminder composer and Important Dates. A matching
centralized `TimePicker.tsx` was added once, in `src/components/`, so
time entry is also branded; no second date or modal system was created.

## 12. Animation changes

- Reminder cards and sections use the existing stagger/entrance
  primitives; sheets reuse the shared sheet entrance; toasts use the
  centralized toast system.
- No new animation framework; every new keyframe/transition is behind the
  existing reduced-motion guards in primitives.css.

## 13. Accessibility

- All actions are semantic buttons with accessible names; icon-only
  controls carry aria-labels; the Notify-me control is a real switch.
- Touch targets ≥ 44px (chips, sheet options, card hit areas, pickers).
- Focus-visible states inherited from primitives; contrast checked in
  both themes; chips and time/status pills do not clip at XL text.

## 14. Dark mode

Verified in the rendered app (Settings → Appearance → Dark): home,
populated cards, composer, and detail all recolor correctly via tokens —
deep warm charcoal surfaces, rose/burgundy accents, no hardcoded colors.
Theme flip is immediate (existing useSyncExternalStore + CSS tokens).

## 15. Text scaling

Verified at Extra Large: header title wraps cleanly, subtitle wraps,
empty-state copy wraps, composer fields and switch row stay full-width
without clipping, and the layout scrolls correctly. Reset to Default
after verification.

## 16. Responsive behavior

The app renders in its mobile portrait column; all Stage 8 layouts are
single-column, flex-wrapped (chips wrap, cards stack, sheets pin to the
bottom), and safe-area aware via existing global rules.

## 17. Vite/browser verification

Real rendered walkthrough on the Vite dev server
(`npx vite --host 0.0.0.0 --port 12000`; served at
http://localhost:12000 — the external proxy host is rejected by Vite's
host check, so localhost was used):

1. Completed onboarding (Alex + Sam, start date June 14 2023 via the
   centralized DatePicker, app lock skipped) → Home.
2. Reminders empty state (branded bell emblem, warm copy, CTA).
3. Create composer: title, centralized DatePicker wheel, centralized
   TimePicker wheel (9:30 AM), repeat bottom sheet (Weekly), Notify-me
   switch, note → saved "Call Mom".
4. Populated home: hero "next up" card + Today section + filter chips.
5. Second reminder "Anniversary dinner plans" (tomorrow 9:00 AM) → hero
   switched to the tomorrow moment.
6. Detail screen: moment card, Repeat/Notification rows, note card.
7. Edit flow: prefilled composer, changed repeat to Yearly, saved —
   hero shows the Yearly badge.
8. Delete sheet: opened, cancelled ("Keep it"), reminder preserved.
9. "Mark as done" on the weekly reminder → advanced to Aug 31 with the
   next occurrence shown.
10. Done filter → correct "Nothing matches this filter right now." state.
11. Validation: saving with an empty title shows the warm error banner
    ("Give this reminder a name.") and does not save.
12. Important Dates: empty → add form (DatePicker + Every year) →
    populated card with countdown and badges.
13. Dark mode: empty state, populated home, composer verified.
14. Extra Large text: home + composer verified without clipping.
15. Back navigation between home/detail/composer verified.

Browser-data note: the browser dev adapter (SqlJsAdapter) is in-memory,
so full page reloads clear seeded data — an expected dev limitation, not
a persistence defect; all flows were exercised via SPA navigation without
intermediate reloads. Android uses the persistent native adapter.

## 18. Screenshots / visual verification performed

Screenshots captured during the walkthrough: populated home with hero,
detail screen, delete sheet, dark-mode empty state, dark-mode composer,
dark-mode populated home, XL-text home, XL-text composer, validation
error, Important Dates empty + populated, Us hub. Stored in the session
observations folder (not committed).

## 19. Reference comparison

Relevant approved references inspected: `Reminders.png`,
`Create Reminder.png`, `Reminder Details.png`, plus the Stage 4
Important Dates references. The implementation follows the references'
intent — serif burgundy headers, blush hero, date-forward cards,
bottom-sheet delete — with deliberate improvements where references were
weaker: filter chips for focus, a distinct next-up hero, icon medallions
that differentiate one-time vs recurring moments, and warm micro-copy
throughout instead of generic labels.

## 20. Tests

`npm test` — 727/727 passing across 100 suites, including the new
`tests/stage8-reminders.test.ts` (pure-helper coverage: grouping into
next/today/upcoming/history, ahead-of-now hero selection, passed-today
visibility, recurrence advancement, relative-day labels, time
formatting, history status labels, filter vocabulary). All previous
stage suites remain green.

## 21. TypeScript

`npx tsc -b` — clean (exit 0). Fixed one TS6133 (unused `ReminderGroups`
import) during the stage.

## 22. Production build

`npm run build` — passes (tsc -b && vite build, Vite 5.4.21).

## 23. Capacitor sync

`npx cap sync android` — passes against the fresh `dist/`.

## 24. Android build status

APK BUILD BLOCKED — JDK/Android SDK unavailable in this environment.
Vite/browser was the designated visual verification environment;
no APK rendering is claimed.

## 25. Known limitations

- Native notification delivery cannot be exercised in Vite; only the UI
  states and the local scheduling code path are verifiable in-browser.
- Browser dev database is in-memory (data resets on full reload);
  Android production persistence is unaffected.
- Reduced motion was code-verified (guards on every new animation) rather
  than visually toggled in the OS.

## 26. Deferred work

- None blocking. Stage 9+ per the roadmap.

## 27. Exact next-stage starting point

Branch `master`, working tree clean, `master == origin/master` at the
Stage 8 commit ("Stage 8 visual productization: Reminders + Important
Dates"). All 727 tests passing, TypeScript clean, production build and
Capacitor sync passing. Next: Stage 9 per the roadmap — do not begin it
from this stage.

# STAGE 7 — Timeline + Story Experience

**Status:** COMPLETE
**Date:** 2026-08-24
**Base:** Stage 6 (Notes + Private Thoughts) @ `2818bb6`

---

## 1. Stage objective

Transform Timeline from a generic chronological CRUD list into the couple's
"our story" narrative: photo-free but date-forward, chapter-numbered, with a
continuous spine, year anchors, and a "Latest" moment — warm, romantic,
elegant, recognizably TwoHearts, and fully local-first.

## 2. Starting commit

`2818bb6` — Stage 6 completion (verified via `git log` + `git ls-remote`:
HEAD == origin/master, working tree clean).

## 3. Ending commit

See `git log -1` (Stage 7 commit).

## 4. Files changed

- `src/features/timeline/TimelineHome.tsx` — rewritten (story composition)
- `src/features/timeline/AddEvent.tsx` — rewritten (branded composer)
- `src/features/timeline/EventDetail.tsx` — rewritten (story page)
- `src/features/timeline/timelineStory.ts` — NEW: pure storytelling helpers
- `src/components/primitives.css` — Timeline (Phase 9) block replaced by the
  Timeline (Stage 7) vocabulary; unused Phase 26
  `.th-timeline-event__card--enhanced` block removed
- `tests/stage7-timeline.test.ts` — NEW: 9 tests
- `AGENTS.md` — Stage 7 note

## 5. Timeline Home

- **Branded header** (`.th-tl-header`): back IconButton (Timeline is reached
  from the Us hub, not the bottom nav), serif burgundy "Timeline", one-line
  subtitle "Your story, one moment at a time.", trailing + IconButton.
- **"Our story" banner** (`.th-tl-banner`): blush band with serif title and
  "Every little moment becomes part of something bigger." + calendar icon in
  a cream circle — mirrors reference 20.
- **Chronological spine** (`.th-timeline-event__connector`): hollow ring
  markers with a warm blush halo (filled burgundy on the latest moment) and a
  rose-tinted connecting line.
- **Year anchors** (`.th-timeline-year`): serif year labels inserted between
  year groups only when the story spans multiple years (pure logic in
  `timelineStory.ts`).
- **Moment cards** (`.th-timeline-event__card`): blush circular badge
  (calendar icon; heart for the latest), uppercase burgundy date, serif
  title, 2-line excerpt, chevron; `role="button"`, keyboard Enter/Space,
  focus-visible ring, pressed feedback. `aria-label` includes the chapter
  number.
- **Latest emphasis** (`.th-timeline-event--latest`): burgundy border + blush
  fill + "Latest" pill + heart badge — mirrors reference 20's emphasized
  newest card.
- **Footer** (`.th-tl-footer`): "More memories are waiting to be added."
- **Staggered entrance** via the shared `.th-stagger-item` vocabulary; FAB
  retained.
- Empty state rewritten as an invitation: "Your story starts here — your
  story is waiting for its first chapter", heart icon, primary CTA "Add your
  first moment".

## 6. Add Event (composer)

- Screen + `Header` (back + "Add to Timeline" / "Edit Moment") with a subtle
  RoseLily corner decoration.
- Serif intro "Add a moment to your story." + "Save something meaningful so
  you can look back on it later." (edit mode has its own copy).
- Title field ("Give this moment a name") with char count (200 limit kept).
- **Native `<input type="date">` replaced by the centralized Stage 2/4
  `DatePicker`** (wheel sheet, "Add a date" trigger) — no generic browser
  control remains.
- "Tell the story" textarea (6 rows) with char count (5000 limit kept).
- Actions: full-width burgundy "Add to Timeline" / "Save Changes" + ghost
  "Cancel" below (`.th-tl-editor-actions`).
- Toasts: "Moment added to your story" / "Moment updated" / "Could not save
  moment" via the centralized `useToast`.
- Client-side validation, service validation, limits, and the
  TimelineService data flow are unchanged.

## 7. Event Detail (story page)

- Screen + `Header` (back + "Moment" + delete IconButton).
- Date-forward identity: uppercase date chip with calendar icon, serif title.
- "The story" labeled reading section (divided from the header by a warm
  rule), with an inviting italic prompt when no description exists.
- **Chapter band** (`.th-tl-detail__chapter`): "Your story · Chapter N" —
  N is the event's 1-based chronological position (pure `chapterOf` helper;
  no data-model change), echoing reference 22.
- Actions: full-width burgundy "Edit event" + quiet danger "Delete this
  moment".
- Quiet metadata footer: "Added … · Updated …".
- **Hand-rolled `.th-modal` delete dialog replaced by the centralized
  `Modal` bottom-sheet** (danger primary "Delete moment" + ghost Cancel).
- Toasts: "Moment deleted" / "Could not delete moment".

## 8. Local data architecture preservation

- `TimelineEvent` entity, serializer, repository, service, and hook are
  untouched. No schema change, no new persistence, no network, no cloud.
- All new presentation logic (dates, chapters, year anchors, latest flag) is
  pure client-side derivation in `timelineStory.ts`.

## 9. Branding

Burgundy/rose/cream tokens only; serif display type for narrative titles;
no new colors, no logo changes, no floral system duplication (existing
`RoseLilyDecoration` variants 14/11 reused at low opacity).

## 10. Animation

Reuses the shared vocabulary only: `.th-stagger-item` fade-rise entrance
(newest-first list), `.th-scale-in` empty state, tokenized pressed states,
toast system. New transitions are token-based and disabled under
`[data-th-motion='reduced']` (`.th-timeline-event__card`,
`.th-tl-detail__delete`).

## 11. Accessibility

- Moment cards: `role="button"`, `tabIndex`, Enter/Space, focus-visible
  ring, chapter-aware `aria-label`.
- All header/sheet buttons have aria-labels; DatePicker is the accessible
  centralized component (listbox wheels).
- Contrast verified in light and dark screenshots; text scales cleanly to
  1.28×; touch targets ≥ 44px (IconButton, pills, buttons).

## 12. Dark mode / text scaling / responsive findings

- Dark: banner, cards, latest emphasis, chapter band, delete sheet all flip
  via tokens — no hardcoded colors. Verified by screenshot.
- XL text (1.28×): titles wrap without clipping, cards stay coherent.
- Narrow (320px): spine + cards + banner remain readable and tappable.

## 13. Browser/Vite verification

**VERIFIED** against the real application (`npx vite`, port 12000) via a
Playwright walkthrough (onboarding → Us hub → Timeline): empty state,
add flow through the centralized DatePicker, populated story with year
anchors + Latest card, chapter band on detail, edit + save, delete modal
(cancel + confirm), back navigation, dark mode (home, detail, composer,
DatePicker), XL text, narrow viewport. 30+ steps, all passing, screenshots
reviewed.

## 14. Reference comparison

- **20-Timeline.png:** header with back + serif title + subtitle ✓; "Our
  story" blush banner ✓; ring-marker spine ✓; emphasized latest card with
  pill ✓; footer line ✓; add affordance ✓. (Filter chips intentionally
  omitted — the TimelineEvent model has no type field; adding one is a
  schema change deferred to a later stage.)
- **21-Add-Timeline.png:** back + "Add to Timeline" ✓; serif intro + sub ✓;
  "Give this moment a name" / "What happened?" placeholders ✓; branded date
  control ✓ (centralized DatePicker); primary + Cancel actions ✓. (Type
  picker / photo / location / time fields omitted — no schema support.)
- **22-Set-Edit-Timeline.png:** date-forward header ✓; serif title ✓; "The
  story" section ✓; "Your story · Chapter N" band ✓; "Edit event" primary ✓.

## 15. Tests

`npm test` — **709/709 pass** (100 suites). New `tests/stage7-timeline.test.ts`
(9 tests): date formatting (incl. UTC safety + malformed keys), chapter
ordering, latest flagging, year-anchor insertion rules (single-year none,
multi-year anchored), empty list, `chapterOf` lookup.

## 16. TypeScript

`npx tsc -b` — clean.

## 17. Production build

`npm run build` — PASS.

## 18. Capacitor sync

`npx cap sync android` — PASS.

## 19. Android build status

**Android APK verification unavailable due to environment limitation**
(no JDK/SDK). Vite/browser verification is the designated fallback.

## 20. Known limitations

- DatePicker `maxYear` is the current year (moments are historical; same
  semantics as the onboarding relationship date).
- Event type chips/photos/location from references 21/22 require a schema
  change and are deferred.

## 21. Deferred items

- Timeline event `type` (Memory/Milestone/Date/Place) — needs schema v5.
- Photo support on timeline events — schema + media coordination, deferred.

## 22. Exact next-stage starting point

Stage 7 commit on `master`, working tree clean, 709/709 tests passing.
**Next: Stage 8 — Reminders experience** (per the Master Stage Plan).

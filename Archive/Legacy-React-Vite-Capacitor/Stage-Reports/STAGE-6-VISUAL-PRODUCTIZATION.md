# Stage 6 — Notes + Private Thoughts Visual Productization

## 1. Stage objective
Make Notes feel like a genuine part of the TwoHearts product — a warm,
private journal for two people, not a generic CRUD list. Cozy, elegant,
text-first, unmistakably TwoHearts. Local-first architecture untouched.

## 2. Starting commit
`365686a` — Stage 5 visual productization: Memories experience

## 3. Ending commit
_See `git log -1` (Stage 6 commit on master)._

## 4. Files / components changed
- `src/features/notes/NotesHome.tsx` — rewritten (branded header,
  paper note cards with category icon badges, keepsake variant)
- `src/features/notes/NoteEditor.tsx` — rewritten (app header with
  back + Save, paper writing surface, "last edited" line)
- `src/features/notes/NoteDetail.tsx` — rewritten (app header, serif
  title, quiet action row, centralized Modal for delete)
- `src/features/notes/categoryMeta.ts` — restored to labels/colors
  only (icon map moved out; see below)
- `src/features/notes/categoryIcons.tsx` — NEW: category → icon
  mapping, typed `Record<NoteCategory, …>` so TypeScript enforces
  completeness (Node tests cannot load .tsx, so it lives apart from
  categoryMeta.ts)
- `src/features/notes/noteTime.ts` — NEW: pure relative-time helpers
  (`formatRelativeTime`, `formatLastEdited`)
- `src/components/primitives.css` — obsolete Phase 8/22/26 notes CSS
  removed (~200 lines: `.th-note-search`, `.th-note-categories`,
  `.th-chip`, `.th-note-list`, old `.th-note-card*`,
  `.th-note-detail-header/actions`, `.th-note-card--enhanced`); Stage 6
  `.th-notes-*` / `.th-note-*` vocabulary appended (~420 lines)
- `tests/stage6-notes.test.ts` — NEW: 10 focused tests
- `tests/games.test.ts` — one-line-class fix for a pre-existing flaky
  Memory Match test (see §20)

## 5. Notes Home changes
- Branded serif burgundy "Notes" title + subtitle "Keep the little
  thoughts that matter." (reference 16-Notes.png) + plus IconButton +
  subtle RoseLilyDecoration (top-right, 0.14 opacity)
- Warm pill search with focus ring (existing search preserved)
- Serif category chips with counts (existing filter preserved)
- "All Notes" serif section title
- Paper-inspired note cards: circular blush category icon badge
  (one icon per category), serif title (ellipsis-safe), 2-line excerpt,
  relative date ("Just now", "5m ago"…), pressed feedback, stagger
  entrance, focus-visible ring
- Love-letter notes get a blush "keepsake" card treatment (rose border)
- FAB preserved; error retry now reloads data in-place instead of
  `window.location.reload()` (avoids losing the in-memory dev DB)
- Routes, repository ordering (updated_at DESC), and data flow unchanged

## 6. Note Editor changes
- App Header: back IconButton + "New Note"/"Edit Note" title +
  burgundy "Save" pill in header (reference 75-Add-Note.png)
- Serif title input with "Give your note a title" placeholder
- Rose "Last edited …" line in edit mode; "A fresh page, just for the
  two of you." for new notes
- Paper writing surface: large rounded warm card containing the
  textarea ("Start writing..." placeholder), focus ring
- Category chips preserved (all 7 categories)
- Header Save disabled until title is present; validation errors still
  surface inline; save toasts preserved ("Note saved"/"Note updated")
- Cancel kept as a quiet ghost action at the bottom
- Faint RoseLilyDecoration bottom-right

## 7. Media picker changes
N/A — Notes has no media. No media architecture was touched.

## 8. Note Detail changes
- App Header: back + "Note" + delete IconButton (reference 76-Edit-Note.png)
- Category label with icon + serif title + "Last edited …" line with
  warm divider
- Comfortable reading typography (relaxed line-height, pre-wrap)
- Action row: primary "Edit Note" button + quiet danger "Delete this note"
- Quiet metadata footer ("Written … · Updated …")
- Delete confirmation moved from hand-rolled `.th-modal` markup to the
  centralized `Modal` bottom-sheet component (consistent with Stage 5
  Memory Detail); delete toast preserved

## 9. Empty / loading / error / success states
- Empty: `th-empty-emotional` with floral, "No notes yet" + "A private
  place for the thoughts you keep for each other." + "Write your first
  note" CTA (verified navigates to editor)
- Search-empty: "No notes found" variant preserved
- Loading: LoadingState spinner + caption on home/editor/detail
- Error: inline error + in-place "Try again" reload; save/delete
  failures surface inline error + error toast (unchanged behavior)
- Success: centralized toasts on save/update/delete (verified)

## 10. Local media architecture preservation
N/A to Notes. NoteService → NoteRepository → SQLite flow is unchanged;
no schema migration; no new storage.

## 11. Branding changes
Burgundy serif headers per the design system; no logo changes;
BrandLogo untouched.

## 12. Floral changes
Two restrained placements only: Notes Home header (variant 14,
top-right, 0.14) and editor/detail (variant 11, bottom-right, 0.08).
Reuses the centralized RoseLilyDecoration system.

## 13. Animation changes
Card stagger entrance (existing `.th-stagger-item`), pressed-scale
feedback on cards/chips/Save/delete, focus rings. All transitions use
existing motion tokens; reduced-motion override block added for all
new interactive classes.

## 14. Accessibility findings
- Chips are `role="listbox"`/`role="option"` with `aria-selected`
- Note cards keep `role="button"`, `tabIndex`, Enter/Space activation,
  and gain `aria-label="Note: {title}"`
- 44px minimum touch targets on chips, Save pill, delete action
- Focus-visible rings on search, chips, cards, Save, delete
- Contrast verified in light + dark (token-driven)

## 15. Dark-mode findings
Verified on home, detail, and editor: warm charcoal surfaces, blush
keepsake card adapts, rose/burgundy accents recolor via tokens,
florals auto-recolor. No hardcoded colors introduced.

## 16. Text-scaling findings
Extra Large (1.28×) verified: header subtitle wraps, chips wrap, card
titles ellipsis without layout break, editor paper + chips remain
usable (category row scrolls with the page).

## 17. Responsive findings
320×700 narrow viewport verified: chips wrap to two rows, cards keep
badge + text proportions, FAB reachable. Android-first preserved.

## 18. Browser/Vite verification
VERIFIED — Playwright walkthrough against the real Vite dev server
(port 12000), 28 steps all passing: onboarding → Notes empty →
empty-state CTA → editor (Save disabled without title) → create 3
notes (Love Letter/Shared/Reminder) → category filter → search →
detail → edit → delete modal (cancel + confirm) → back navigation →
dark mode (home/detail/editor) → XL text → narrow viewport.
18 screenshots reviewed against the approved references.

## 19. Reference comparison findings
- 16-Notes.png: header, search, section title, icon-badge rows,
  per-note dates — matched. The "Pinned" section was NOT built: the
  Note entity has no pin field and adding one would require a schema
  migration (explicitly out of scope). Deferred.
- 75-Add-Note.png: header Save, title field, "last edited" line,
  paper card, category — matched. The rich-text formatting toolbar
  (B/I/U/•) was NOT built: notes are plain text in the V1 data model.
  Deferred.
- 76-Edit-Note.png: header + overflow, serif title, "Edited" line,
  body, Edit/Delete actions — matched (overflow menu → header delete
  icon, consistent with Stage 5 Memory Detail).

## 20. Tests
700/700 passing (690 Stage 5 baseline + 10 new Stage 6 tests).
Note: a pre-existing flaky test (`games.test.ts` → "resets unmatched
cards") failed intermittently (~1 in 5 runs) because the random board
shuffle can place a matching pair at indices 0/1. Fixed minimally by
selecting two cards from different pairs before flipping — the same
deterministic pattern used by the neighboring "game over" test.
Verified stable across 6 consecutive runs. No games code changed.

## 21. TypeScript
PASS — `npx tsc -b` clean.

## 22. Production build
PASS — `npm run build` (existing chunk-size warning only).

## 23. Capacitor sync
PASS — `npx cap sync android`.

## 24. Android build status
Android APK verification unavailable due to environment limitation
(no JDK/Android SDK in this environment). Vite/browser used as the
designated visual verification fallback.

## 25. Known limitations
- Browser sql.js dev database is in-memory; full reloads lose seeded
  data (expected dev-adapter behavior, not a production defect).
- The reference formatting toolbar and pinning require data-model
  changes and are intentionally deferred (see §26).

## 26. Deferred items
- Note pinning ("Pinned" section) — requires a schema migration;
  out of scope for a visual stage.
- Rich-text formatting toolbar — notes are plain text in V1.

## 27. Exact next-stage starting point
Stage 6 commit on `master` (pushed to origin). Next stage per the
master plan: STAGE 7 — TIMELINE EXPERIENCE. Do not begin Stage 7
from this document; read the Stage 7 directive first.

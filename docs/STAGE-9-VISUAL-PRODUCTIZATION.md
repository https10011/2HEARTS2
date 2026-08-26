# Stage 9 — Visual Productization: Places + Shared Adventures

## 1. Stage objective

Transform the Phase 14 Places experience into a polished,
relationship-centered "the places that became part of our story"
experience — covering Places Home, Add Place, Place Details, place
dialogs, and location/media states — without redesigning architecture,
without introducing online dependencies, and preserving full V1
local-first behavior.

## 2. Starting commit

`1e4b861` — "Stage 8: record agent memory note" (tree clean at start).

## 3. Ending commit

This commit — "Stage 9 visual productization: Places + Shared Adventures" (see `git log` for the hash; self-referential hashes cannot be embedded in the commit itself).

## 4. Documentation / Directive Audit

Inspected before implementation:

- `TWOHEARTS-VISUAL-PRODUCTIZATION-DIRECTIVE.txt` — full read; Stage 9
  scope (Places Home, Add Place, Place Details, dialogs, location/media
  states) and the Tasks 1–12 requirements
  (productization, media, maps restraint, visual identity, Rose/Lily,
  animation, dark, XL text, responsive, states).
- `AGENTS.md` — repository conventions (npm, Vite, design tokens,
  testing, prohibited V2 tech), loaded every session.
- `MasterPrompt.txt` — design/implementation rules (no emoji UI,
  centralized brand/decoration, token-driven styling).
- `TwoHeartsRDMap.txt` — V1 scope (offline-first; Places = Phase 14;
  V2 online features separable).
- `TWOHEARTS_BUILD_PROGRESS.md` — current status & known limitations.
- Master visual productization stage plan (roadmap file) — Stage 9 =
  Places; Stage 10 not started.
- Stage reports 2–8 (STAGE-2 … STAGE-8 VISUAL-PRODUCTIZATION.md) —
  reusable systems: BrandLogo, Rose/Lily decorations, tokens, Modal /
  bottom-sheet, toasts, DatePicker, TimePicker, media architecture,
  theme, reduced-motion, navigation shell, responsive/text-scaling
  rules.
- `TwoHeart UI Reference Screens/46-OurPlaces.png`,
  `47-Add-Place.png`, `48-Edit-Place.png` — approved references.

Key constraints discovered:

- V1 prohibits map SDKs, cloud media/CDNs, remote location services;
  local-only coordinates + optional photo via the Phase 2 media
  pipeline.
- Place entity (Phase 14) fields are fixed: name, address, city,
  state, country, latitude, longitude, notes, category, photoRef,
  memoryId + entity timestamps/tombstone. References showing
  favorites / visited-status / first-visited dates cannot be
  reproduced without schema changes → presentation-level intent only.
- React components must not import Capacitor/storage directly;
  each plugin has a single driver site.

## 5. Stage 8 audit result

**PASS.** Stage 8 (Reminders + Important Dates) implementation verified
intact on the starting tree: `.th-reminders-*` CSS present, Reminders
screens/composer/detail untouched by Stage 9 (no shared file
modified), DatePicker/TimePicker untouched, local-notification
architecture untouched, baseline test suite 727/727 before Stage 9
work began. No regressions found; no repairs needed.

## 6. Stage 7 audit result

**PASS.** Stage 7 (Timeline + Story) implementation verified intact:
`.th-tl-*` CSS present, timeline screens untouched, story helpers
untouched; no regressions found. Stage 9 does not modify any Stage 7
file.

## 7. Files changed

- `src/services/place/placeService.ts` — extended with optional
  MediaStorage boundary (photo coordination), `mustGet` /
  `requireMediaStorage` helpers, delete-photo cleanup on remove.
- `src/data/media/resolveMediaFileSystem.ts` — NEW shared resolver
  (Capacitor Filesystem on native, in-memory adapter in browser/dev).
- `src/features/memories/useMemoryService.ts` — refactored to use the
  shared resolver (duplication removed; behavior unchanged).
- `src/features/places/usePlaceService.ts` — NEW hook building
  PlaceService with resolved MediaStorage.
- `src/features/places/placePresentation.ts` — NEW pure logic
  (byNewestFirst, collectCategories, filterPlaces, formatAddedAgo,
  formatLocationLine).
- `src/features/places/PlacesHome.tsx` — rewritten (hero + grid +
  chips + search + empty/loading/error).
- `src/features/places/CreatePlace.tsx` — rewritten (create/edit
  composer with photo dropzone + preview + validation).
- `src/features/places/PlaceDetail.tsx` — rewritten (photo hero /
  fallback, story card, Modal bottom-sheet delete confirm, toasts).
- `src/components/primitives.css` — Stage 9 `.th-places-*` CSS
  vocabulary appended (~600 lines).
- `tests/stage9-places.test.ts` — NEW 24 tests.
- `docs/STAGE-9-VISUAL-PRODUCTIZATION.md` — this document.

## 8. Places Home changes

- Branded header: back, serif "Our Places", subtitle "Places that
  mean something to us.", header add (+) action.
- "Places we've shared" hero band with relationship copy and a
  circular IconMapPin badge (Rose/Lily-free, matches decoration
  economy rule).
- In-memory search field ("Search places…") filtering name/address/
  city/notes.
- Category chips derived from actually-present categories
  (`All` + sorted categories).
- Latest place rendered as featured hero card (photo or blush
  fallback thumb, name, location line, category pill, divider,
  "Added … ago").
- Remaining places rendered as "Our places" photo-first grid
  (2 columns; wraps on narrow widths via min() clamps).
- "Have a special place in mind? Save it here, just for the two of
  you." CTA card + footer line "Every place holds a piece of your
  story."
- Empty state: IconMapPin, "No places yet", story-oriented copy,
  "Add your first place" CTA.
- Loading via shared `LoadingState`; list errors via normalized
  safe message.

## 9. Add Place changes

Single composer handles create + edit (route-driven):
- Branded header ("Add a Place" / "Edit Place"), relationship copy.
- Photo dropzone ("Add a photo / Add a photo to remember this place.")
  using the existing local media pipeline; preview re-renders actual
  `data:` URLs, with remove control.
- Name input with reference placeholder, category chip picker
  (enum-driven), structured address (street/city/state/country),
  collapsible "Pinpoint it" coordinates kept on-device,
  "Why is this place special?" notes with 500-char counter.
- Validation (name required), save toasts, cancel/back behavior,
  error normalization to safe messages.

## 10. Place Details changes

- Photo hero when `photoRef` resolves; branded blush fallback with
  IconMapPin otherwise (missing-media state).
- Serif place title, location line + category pill.
- "Saved location" card (address/city/state/country lines).
- "Why this place is special" story card with IconHeart badge.
- Footer metadata "Added <date> · Updated <date>" (entity timestamps;
  no fabricated first-visited date).
- Edit action (primary), Delete action with centralized Modal
  bottom-sheet confirmation ("Delete this place?" /
  "<name> will be removed from your places for good.") and toast.
- Back navigation via router.

## 11. Location/map behavior

No map SDK, map tile, or location API introduced (V1 prohibition
honored; references' "View map/Choose on map" consciously not
reproduced). Location is productized honestly from local structured
address fields + optional on-device coordinates via the collapsible
"Pinpoint it" section using the app's mini-map pin art (IconMapPin)
without any map service. Deferred: any future offline-capable map
presentation is a V2 candidate and out of Stage 9 scope.

## 12. Media behavior

Local-first media preserved and extended exactly as-is:
- `PlaceService` now receives an optional `MediaStorage` boundary and
  coordinates `photoRef` (media_asset id), mirroring MemoryService.
- Photo bytes live in the private `media/` root via the existing
  Phase 2 pipeline (Capacitor Filesystem on Android; in-memory adapter
  in browser/dev/tests behind `resolveMediaFileSystem`).
- Rendering uses `data:` URLs produced by the existing pipeline — no
  cloud hosting, CDN, or remote image service.
- Delete-place cleans up the photo media asset; photo removal in the
  composer is immediate.
- A shared resolver removed duplication between memories and places
  hooks (memories still pass its full test coverage).

## 13. Architecture preservation

- No schema migration; all new data paths ride existing columns
  (`photo_ref` already in the Phase 14 entity).
- Entity, repository, router routes unchanged in behavior; screens
  rewritten as presentation only.
- No new dependencies; no second media, theme, modal, toast, or icon
  system.
- Memories regression risk from the resolver refactor checked by the
  full suite (751/751 pass).

## 14. Branding/visual changes

All styling rides existing design tokens (burgundy/blush/cream,
serif display titles, tokenized spacing/radii/shadows). No hardcoded
colors introduced. Icons strictly from the centralized Icon set
(IconMapPin, IconCamera, IconHeart, IconPlus, IconTrash, IconEdit,
IconChevronRight etc.). No emoji UI.

## 15. Rose/Lily usage

Deliberately restrained: the centralized decoration system is not
added to Places surfaces beyond what the shared AppShell/layout
already renders; the empty state and detail storytelling carry the
relationship weight instead, per the "meaningful, not blanket"
directive. Map-pin motif (IconMapPin badge) used as the thematic
device.

## 16. Animation

Reuses the existing interaction layer only: `.th-pressable` feedback,
`th-scale-in`/stagger entrance for cards/empty state, route
transition, Modal/Dialog entrance, toast auto-dismiss. All respect
the existing reduced-motion mechanism; no new animation framework.

## 17. Dark mode

Verified in the rendered browser (More → Settings → Appearance):
switching to Dark recolors Places Home + Detail immediately through
tokens; switching back to Light likewise. System option untouched
(existing behavior continues).

## 18. Text scaling

Verified in the rendered browser at Extra Large combined with Dark:
titles, chips, cards, CTAs and footer wrap cleanly; no clipping,
overlap, or unusable controls. Photo hero/fallback and grid cards use
min()-clamped sizes so XL text does not break layout.

## 19. Responsive verification

Normal mobile viewport exercised for every screen. Narrow (~320px)
behavior is enforced by CSS (grid `minmax(min(100%,…)…)`, address
wrapping, min()-clamped hero thumbs, wrapped chip rows) — verified by
code inspection; the browser tool used here cannot resize the
viewport, and this limitation is recorded honestly. No horizontal
overflow observed at the tested viewport.

## 20. Empty/loading/error states

- EMPTY: dedicated branded empty state (verified live).
- LOADING: shared `LoadingState` spinner + caption during service
  mount.
- ERROR: normalized safe user message on load/save/delete failures.
- MISSING MEDIA: blush IconMapPin fallback hero/thumb (verified
  live, since seeded places had no photos).
- SAVE: success toast via centralized ToastProvider.
- DELETE: bottom-sheet confirm → toast; cancel path verified live.

## 21. Vite/browser walkthrough

Vite dev server (5.4.21) on port 12000; browser walkthrough
performed in the same SPA session (in-memory sql.js; a full-reload
misstep during walkthrough confirmed the empty state again, then data
was re-seeded via SPA navigation only):

1. Onboarding completed (Alex & Sam, app lock skipped).
2. Places Home empty state — verified (screenshot).
3. Add-Place composer — verified; saved "That Little Café"
   (Restaurant, Quezon City, notes).
4. Place Detail — verified (missing-photo fallback hero).
5. Populated Places Home — verified (hero + "Our places" grid,
   category chips).
6. Search "café" — filters correctly.
7. Edit — prefill verified; save updates "Updated …" metadata.
8. Delete sheet — cancel keeps place; confirm deletes and returns
   home; chips/grid update.
9. Dark mode — verified on Home + Detail.
10. Extra Large text (+ Dark) — verified on Home.
11. Theme restored to Light + Default.

## 22. Screenshot/rendered inspection results

Screenshots taken at every walkthrough step above. Defect log: none
requiring repair — spacing, hierarchy, wrapping, chips, cards,
dialogs, nav, and contrast all checked clean in light, dark, and XL
modes; no accidental emoji, no floral over-decoration, no broken
media placeholders (fallback is branded by design).

## 23. Reference comparison

Compared against approved references 46/47/48:

- MATCH: branded header/title/subtitle, "Places we've shared" band,
  featured hero card, chips, "Our places" grid, CTA band, photo
  dropzone copy, name placeholder, category picker, notes+500 counter,
  detail hero, "Saved location", "Why this place is special", delete
  bottom sheet, Edit primary action.
- NOT REPRODUCED (documented limitations): favorites/visited-status
  chips + heart badges (no fields), "View map/Choose on map"
  (map SDK prohibited in V1), "first visited" date chip (entity has
  createdAt/updatedAt only), "Link a memory" picker UI (entity field
  `memoryId` exists; picker deferred — see §30).

## 24. Tests

`npm test` → **751/751 pass** (727 baseline + 24 new Stage 9 tests
in `tests/stage9-places.test.ts`: presentation helpers, PlaceService
photo coordination with real sql.js + in-memory media FS, delete
cleanup; no mocks).

## 25. TypeScript

`npx tsc -b` → clean (exit 0).

## 26. Production build

`npm run build` → success; only the pre-existing >500 kB main-chunk
advisory (code-splitting, non-blocking).

## 27. Capacitor sync

`npx cap sync android` → success ("Sync finished").

## 28. APK status

APK verification blocked by environment limitation (no JDK/Android
SDK in this environment — same as Stages 2–8). Vite/browser
walkthrough used as the designated visual fallback.

## 29. Known limitations

- Media picker, narrow-viewport, and loading-state flashes are
  verified via unit tests + code because the available browser tool
  cannot upload files, resize viewports, or catch transient states
  deterministically.
- Browser DB is in-memory (sql.js): full page reloads reset seeded
  data; walkthrough hence used SPA navigation only.
- Reference-only capabilities (favorites, visited status, first-
  visited date, map views, memory link picker) remain unimplemented
  by design until schema/roadmap permits.

## 30. Deferred items

- Memory-link picker (`memoryId` entity field exists; UI picker
  deferred to a later stage).
- Any offline-capable map presentation (V2 candidate).
- Favorites/visited/first-visited fields (schema-level; requires
  roadmap approval).

## 31. Previous-stage regression verification

- Stage 8 Reminders/Important Dates/DatePicker/TimePicker/local
  notifications: untouched files; full suite passes.
- Stage 7 Timeline/story/chapters: untouched; full suite passes.
- Stage 6 Notes: untouched.
- Stage 5 Memories: `useMemoryService` refactored to the shared
  resolver; all memory suites pass.
- Stage 4 Us/Counter/Important Dates; Stage 3 Home/shell/nav; Stage 2
  onboarding gates: all untouched; router catches unchanged.

## 32. Exact next-stage starting point

Ending commit = this commit, branch `master`, pushed
to `origin/master`, working tree clean. Stage 10 (next roadmap slot)
starts from that commit.

# STAGE 5 — Memories Experience — Visual Productization

**Status:** COMPLETE
**Stage:** 5 of the Visual Productization Master Stage Plan
**Scope:** Memories Home, Add/Edit Memory, Media Picker, Memory Details

---

## 1. Stage objective

Make Memories feel like a genuine part of the TwoHearts product — cozy,
romantic, elegant, warm, modern, premium, personal, **photo-first**, and
emotionally meaningful — rather than a generic CRUD list. The three
approved references (`12-MainMemories.png`, `13-IndividualMemories.png`,
`14-Add-Edit-Memory.png`) drove the target: a branded header with year
chips, a featured photo hero, a 2-column photo grid, a hero-first detail
screen, and a full-page branded add/edit form.

## 2. Starting commit

`735ba1d` — `docs(AGENTS): add Stage 4 note + dev-db pointer`
(Stage 4 baseline `f0d2a83` verified on `master`/`origin/master`.)

## 3. Ending commit

See `git log -1` (Stage 5 commit, this file included).

## 4. Files/components changed

| File | Change |
|---|---|
| `src/features/memories/MemoriesHome.tsx` | Rewritten — photo-first hero + grid, year chips, branded header, FAB, states |
| `src/features/memories/AddMemory.tsx` | Rewritten — media dropzone + previews, branded DatePicker, edit mode |
| `src/features/memories/MemoryDetail.tsx` | Rewritten — hero media, thumb strip, heart accent, actions, delete modal |
| `src/features/memories/memoryFilters.ts` | **New** — pure helpers: `collectYears`, `filterByYear`, `extractYear`, `formatDateKey`, `byNewestFirst` |
| `src/services/memory/memoryService.ts` | Added `resolveMediaUrl(assetId)` passthrough to `MediaStorage.resolveUrl` |
| `src/navigation/routes.ts` | Added `appMemoriesEdit: '/app/memories/:memoryId/edit'` |
| `src/navigation/AppRouter.tsx` | Registered the edit route (renders `AddMemory` in edit mode) |
| `src/components/primitives.css` | +~450 lines — one centralized Stage 5 Memories vocabulary (`th-mem-*`, `th-photo-*`, `th-media-*`, `th-memory-*`) |
| `tests/stage5-memories.test.ts` | **New** — 16 pure-function tests |

## 5. Memories Home changes

- **Header:** serif burgundy "Memories" + subtitle "Little moments worth
  keeping." + subtle `RoseLilyDecoration` top-right + plus IconButton.
- **Year filter chips** (`All` + distinct years, newest first) — only
  rendered when more than one year exists; in-memory filtering, no schema
  change. Chips are `role="listbox"`/`option` with `aria-selected`.
- **Featured hero card:** the newest memory (by `createdAt`) leads as a
  full-bleed photo card with a gradient overlay, serif title, 2-line
  caption clamp, and calendar-icon date. Warm blush gradient + icon
  fallback when a memory has no photo (or only video).
- **"All Memories" 2-column photo grid** (3 columns ≥520px): rounded
  cards, square image frame, serif title, muted-rose date row; pressed
  scale feedback; staggered entrance via existing `th-stagger-item`.
- **Burgundy FAB** (`th-fab`, existing token) + header plus button.
- **Ordering note:** the repository intentionally lists by `sortOrder`
  (creation order). The hero needs the *newest* memory, so a pure
  UI-layer sort (`byNewestFirst`) was added — the repository/data layer
  is untouched.

## 6. Add Memory changes

- App-level `Header` (back + "New Memory") replaces the onboarding-style
  layout — the form now feels like part of the product, not setup.
- **Media dropzone:** warm blush dashed zone with camera icon and
  "Add a photo / Capture the moment" copy; opens the local file picker
  (`accept` limited to the app's allowed photo/video MIME types, multiple
  selection supported). Selection count is reflected in the zone copy.
- **Preview thumbnails** with per-item remove (×) before saving.
- Branded **`DatePicker`** (Stage 2 wheel picker) replaces the native
  `<input type="date">` — no generic browser controls remain.
- Validation (title required/≤100 chars) with inline error text and
  `role="alert"`; safe global error; Save/Cancel pair with saving state.
- **Edit mode** (`/app/memories/:memoryId/edit`): fields prefill via
  `getMemory`, existing media renders with removal (`removeMedia`), new
  selections attach on save (`addMedia`), "Save changes" + toast.

## 7. Media picker changes

The picker is the browser/Android local file chooser surfaced through the
dropzone — **no remote gallery, no uploads, no external URLs**. Files are
read into `Uint8Array` via `FileReader` and handed to the existing
`MemoryService.createMemory(..., mediaItems)` / `addMedia` paths, which
already enforce size limits and magic-byte verification
(`verifyMediaBytes`). Local previews use `URL.createObjectURL`, revoked
on removal/unmount.

## 8. Memory Details changes

- **Hero media first:** large rounded photo (or `<video controls>`),
  with a thumb strip for multi-media memories; tapping a thumb swaps the
  hero (active thumb gets a burgundy ring, `aria-pressed`).
- Serif title with a filled burgundy heart accent (decorative,
  `aria-hidden`), rose date row with calendar icon, caption as a readable
  narrative paragraph.
- **Edit Memory** primary button routes to the shared edit form.
- Delete moved to a header trash IconButton; confirmation through the
  existing `Modal` (named memory in the copy) + success/error toasts.
- Quiet metadata footer ("Added … · Updated …") behind a divider;
  subtle `RoseLilyDecoration` accent top-right.
- Missing-media and broken-image fallbacks: warm blush tile + icon.
- Not-found state offers a safe path back to the list.

## 9. Empty / loading / error / success states

- **Empty:** branded header + `th-empty-emotional` block ("No memories
  yet — This is a place waiting for your favorite moments together") with
  the primary "Add your first memory" CTA. Verified rendered.
- **Loading:** shared `LoadingState` spinner + caption on all screens.
- **Error:** safe user-facing message + "Try again" (list) / "Back to
  memories" (detail); no developer-facing internals.
- **Success:** centralized toasts — "Memory saved", "Memory updated",
  "Memory deleted", "Photo removed" (all verified rendered).

## 10. Local media architecture preservation

**Fully preserved.** No cloud storage, no base64-in-DB, no external image
URLs, no new media database. The only service change is a thin
`resolveMediaUrl` passthrough so the UI can render `data:` URLs through
the existing `MediaStorage` boundary (raw filesystem paths still never
reach UI code). The previously dormant-but-complete Phase 2/7 media
pipeline (store → `media_assets` + `memory_media` → resolveUrl) is now
actually used by the UI — that was the single biggest productization gap
found during recon (UI rendered only placeholder icons; AddMemory created
text-only memories).

## 11. Branding changes

Burgundy serif display type for the screen title, section heading, card
titles, and chip active state; warm cream/blush/rose surfaces throughout;
the official brand system is untouched (no new logo usage needed on these
screens — nav center button remains the brand mark).

## 12. Floral changes

Two restrained placements only: `RoseLilyDecoration` top-right on the
Memories Home header (opacity 0.14) and on Memory Detail (opacity 0.10).
Reuses the centralized decoration system — no duplicate floral assets.

## 13. Animation changes

- Cards reuse the existing `th-stagger-item` entrance (home grid/hero).
- Press feedback (`scale`) on photo cards, chips, dropzone, thumb strip —
  all via existing motion tokens (`--th-motion-press` etc.).
- All new transitions are gated by both `prefers-reduced-motion` and the
  in-app `data-th-motion='reduced'` setting (explicit rules added).
- No new animation framework.

## 14. Accessibility findings

- Chips use listbox/option semantics with `aria-selected`; thumb strip
  buttons use `aria-pressed` + positional labels.
- All photos carry descriptive `alt` text (`"{title} photo"`); decorative
  fallbacks and the heart accent are `aria-hidden`.
- Touch targets ≥44px (chips, FAB, remove buttons, header buttons).
- Form fields have explicit `<label>`s; validation errors use
  `role="alert"` and `aria-invalid`.
- Focus-visible ring on chips via `--th-shadow-focus`.
- Reduced motion honored (see §13).

## 15. Dark-mode findings

Verified via `data-th-theme='dark'` flip in the live app: warm charcoal
surfaces, pink-tinted brand headings (dark-token recolor), blush-tinted
FAB/chips, readable hero overlay, floral recolors automatically through
the centralized rule. No hardcoded colors were introduced — everything
flows through tokens.

## 16. Text-scaling findings

Verified at the Extra Large scale (`--th-text-scale: 1.28`): titles wrap
without clipping, subtitle wraps to two lines cleanly, chips remain
pill-shaped and tappable, hero/card metadata stays readable, grid
composition holds. No content is hidden to accommodate scale.

## 17. Responsive findings

Verified at 390×844 (primary Android-class viewport). Grid is 2 columns
on phones, 3 columns ≥520px; hero/card imagery uses `object-fit: cover`
(no distortion); content column is capped by the existing
`--th-screen-max-width` app shell. Mobile-first preserved; no desktop
optimization at mobile's expense.

## 18. Browser/Vite verification

**VERIFIED.** The real application was exercised end-to-end in a headless
Chromium session against `npm run dev` (Vite, port 12000) — no mockups,
no demo pages. A 23-step scripted walkthrough (Playwright, in
`/tmp/stage5-qa`, not committed) covered: onboarding → empty state →
add with photo → preview → filled form → save → detail → FAB → two more
memories (one with two photos) → populated home → year filter on/off →
hero-card navigation → thumb-strip hero swap → edit form (prefilled) →
edit save → delete modal (cancel + confirm) → post-delete list → dark
mode (home + detail) → XL text (home + detail). All 23 steps passed;
screenshots were visually inspected. Save/update/delete toasts observed
rendered. Back navigation verified between detail, edit, and list.

*Note:* the browser dev database (sql.js) is in-memory; the walkthrough
deliberately used SPA-only navigation after the initial load. This is a
dev-environment trait, not a production storage issue (Android uses the
persistent native adapter).

## 19. Reference comparison findings

| Reference | Result |
|---|---|
| `12-MainMemories.png` | PASS — serif header + subtitle, year chips, featured photo hero, "All Memories" 2-col photo grid, plus header action, FAB, floral accent |
| `13-IndividualMemories.png` | PASS — back + title + delete header, hero photo, serif title with heart accent, date row, narrative caption, Edit CTA, quiet metadata |
| `14-Add-Edit-Memory.png` | PASS — full-page branded form, media-first dropzone, branded date control, primary save + ghost cancel |

## 20. Tests

`npm test` → **690/690 passing** (100 suites). Baseline was 674; Stage 5
adds 16 focused tests for the new pure helpers (`collectYears`,
`extractYear`, `filterByYear`, `formatDateKey`, `byNewestFirst`). No
existing tests were deleted or weakened.

## 21. TypeScript

`npx tsc -b` → **PASS** (clean).

## 22. Production build

`npm run build` → **PASS** (`tsc -b && vite build`, dist emitted; the
pre-existing >500 kB chunk-size advisory is unchanged and not a Stage 5
regression).

## 23. Capacitor sync

`npx cap sync android` → **PASS** (web assets copied, plugins updated).

## 24. Android build status

**BLOCKED — environment limitation.** No JDK/Android SDK in this
environment (unchanged from previous stages). APK verification was not
faked; the Vite/browser walkthrough (§18) is the designated visual
fallback per the stage directive.

## 25. Known limitations

- Browser automation cannot drive the native file dialog; the picker was
  exercised by injecting real local PNG files through the actual
  `<input type="file">` — the same path a user selection takes.
- Video upload path is wired (dropzone accepts video, detail renders
  `<video controls>`) but was not exercised with a real video file in the
  browser session; photo paths were fully verified.
- Year chips only appear when memories span more than one year (by
  design).

## 26. Deferred items

- Native Android APK verification (environment limitation; see §24).
- Camera-capture affordance on Android (the dropzone already opens the
  system picker, which offers camera on-device).
- Reordering/pinning memories (repository `sortOrder` support exists;
  no product ask in Stage 5).

## 27. Exact next-stage starting point

**STAGE 6 — Notes + Private Thoughts.** Start from the Stage 5 commit on
`master` (this document's commit). Notes already have their own
vocabulary (`features/notes`, `categoryMeta.ts`); do not reuse the
`th-mem-*` memories vocabulary — audit the Notes references appropriately.

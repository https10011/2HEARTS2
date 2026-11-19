# Stage 9 — Home

## Starting checkpoint

Stage 8 is verified at `4b5161a2704ce105b6ed101e08bcc4e43a76183a`. Onboarding and Settings provide both locally saved profile photos. Home displays them in a 60 dp avatar row. The unweighted name columns can push the second portrait out of view at 360 dp and 200% font scale. The greeting repeats the name, and the day counter is isolated in a separate panel. [Stage 6 native evidence](../Stage-6/evidence/navigation-selected-structure.jpg) · [Stage 8 report](../Stage-8/REPORT.md)

Reviewed Home's Compose entry point and derivation, profile avatar and thumbnail handling, the existing route contracts, calendar calculation, the native Home harness, and the Room-backed Home tests. The approved scope is the portrait centerpiece, useful action order, and visible representation of real records.

## Acceptance targets

- Inspect both saved portraits, a single photo, missing sources, initials and missing records. Keep two balanced slots and both names at standard and narrow widths, including 200% text.
- Keep notifications and search in reach. Group relationship duration with the pair. Keep the next real date or a first-use action immediately below, then memories, notes, reminders, mood and Yuki. Keep feature destinations reachable through their existing routes.
- Verify valid and invalid legacy dates, live record changes, and navigation with native composition and local fixtures. Run the full suites and local APK checks, then verify the published stage and workflow.

## Implementation

- Home puts the portraits and both names above the exact duration and start date. It uses two equally weighted slots with 72–112 dp portraits with the shared cached avatar renderer, a thin frame, and the established serif display type. Large text on narrower layouts uses two full-width portrait rows. The existing alerts and search actions stay in the header.
- The next saved occasion uses a compact calendar banner. Missing or invalid relationship dates expose a route to set the start date when an owner profile is available. The calendar invitation stays available on an empty profile. Invalid and future relationship dates contribute neither a duration nor an anniversary.
- The latest memory opens its saved record. The gallery remains available, and the empty-memory action opens Add Memory. Notes and Reminders share a two-row section with their real previews. Mood and Yuki follow with their existing records and destinations.
- Home reuses Room flows for the lifetime of each composition and collects them only while the screen is active. The change uses existing tables, media references, settings, and routes.

## Verification

- The focused Android API 34 native tests saved and drew two private image files, drew one image and a fallback initial, and confirmed the initials and icon fallbacks when the media source and relationship were absent. Portrait colors were checked on the rendered bitmap. The narrow test checked both name bounds and the header actions at 360 × 640 dp and 200% text. A first render exposed names broken midword. The revised composition uses two stacked portrait rows at that size and shows both fixture names in full. [Portrait renders](evidence/portrait-compositions.jpg)
- Inspected the day and night palettes, including long content, missing records, and saved previews. The sampled token pairs for names, duration, date labels, initials, and record copy had contrast ratios from 4.97:1 to 12.77:1. The fixture for the whole section order uses a tall native viewport to include the Mood and Yuki rows. [Saved previews and actions](evidence/saved-previews-dark.jpg)
- Native semantics tests verified the Us portrait action, notifications, search, the shared-date and calendar routes, memory creation and detail, Notes, Reminders, Mood, and Yuki. Existing repository tests continue to exercise the persisted data that feeds Home. The new date regression covers blank, impossible, and future dates.
- Each full unit suite passed 175 tests with zero failures, errors, or skips. The debug APK passed v2 signing, alignment, and ZIP integrity checks. Package metadata remains `com.twohearts.app` 1.0.0 (1), min API 26 and target API 35. No broad photo-library or storage permission is declared. Local APK SHA-256 `e83f80650a43ab4dd93a34bd9fdf98939b2861dce2b465306995afde5ae022b4`.
- The test photos were generated illustrations written to private files by the native harness. The handset walkthrough and installed-candidate verification remain release work.

- Git verified `779e69d0c9d764f96dc45f347b8630f3bf9776ac` on `origin/master`. The Android workflow passed and published the Home stage APK. [Commit](https://github.com/https10011/2HEARTS2/commit/779e69d0c9d764f96dc45f347b8630f3bf9776ac) · [CI](https://github.com/https10011/2HEARTS2/actions/runs/44570070046) · [APK](https://github.com/https10011/2HEARTS2/actions/runs/44570070046/artifacts/14554072238)

## Completion checklist

- [x] Record the starting commit, source reviewed, initial behavior, decisions and stage dependencies.
- [x] Verify locally stored and missing photos, invalid dates, persisted records, and navigation.
- [x] Inspect native renders in both themes, narrow and standard layouts, and at 200% text.
- [x] Pass both full test variants, verify the APK, and review the final diff.
- [x] Commit and push the stage. Verify the exact remote SHA, Android workflow, and progress tracker.

## Next stage

Stage 10 brings the couple portraits into Us and gives relationship milestones, shared memories, other saved previews, and private destinations a clear hierarchy.

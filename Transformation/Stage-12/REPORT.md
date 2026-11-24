# Stage 12 — Notes and Timeline

## Starting checkpoint

Stage 11 passed on `e30b345bc09f8ada7d2797614063fa47aa1410a3`. Notes has local title and body filtering, and Timeline has a newest-first list. The Notes baseline shows a saved `love-letter` category as General. Timeline has no month grouping and its connector height is fixed. Both editors repeat the Save action and lose draft fields during recreation. [Stage 11 report](../Stage-11/REPORT.md)

Reviewed the six native Notes and Timeline screens, category metadata, Room repository and route lifecycles, the previous implementation reports, and the native empty, saved, edit and reading renders.

## Acceptance targets

- Keep local search and all categories discoverable. Accept installed and legacy category spellings, preserve saved records, and verify combinations with title and content search.
- Group Timeline by real calendar months in newest-first order, with readable dates and recoverable legacy date errors.
- Render the lists, editors, detail, delete confirmation and missing states in both palettes and at narrow widths and large text sizes. Verify CRUD and reopen, then pass local and exact remote tests and APK checks.

## Implementation

- Notes exposes local title and body search, a category sheet with all seven categories, a result count and a clear-filters action. Both Love Letter spellings resolve to the same category. Saving an edit keeps the stored category text when the selection stays the same.
- Timeline groups true dates by calendar month and sorts newest first. Same-day moments use stable creation-time and ID ordering. Unreadable legacy dates stay accessible through their own group.
- Notes and Timeline share a reader layout with full text, strong metadata contrast, edit and delete actions. Their shared editor layout keeps one Save action above the navigation and keyboard insets. Draft fields use saved state, and failed saves leave an inline error with the form available for retry.
- The routes retain their saved records, IDs, lookup states and existing navigation. Both lists use lifecycle-aware Room streams.

## Verification

- Native tests selected the Love Letter category and searched the note body, including spaces around the query. They opened the matching record and verified that unrelated notes leave the result list. A file-backed Room test created both record types, edited the content and date, reopened with the legacy category and intact paragraph text, and checked deletion after another reopen.
- Native tests covered chronological month groups, same-day order, the invalid-date group, and the original invalid value in its detail. They exercised note and moment validation, long note content, cancellation and confirmation of deletion, and Back from both missing states.
- Inspected light and dark Compose captures. At 360 × 640 dp and 200% text, titles wrap and Save stays visible while the body scrolls. The refreshed 274 × 488 dp empty Notes capture retains its first-note action and bottom bar. Sampled metadata, body, placeholder and heading-detail colors ranged from 5.72:1 to 9.99:1 against their surfaces. [Notes and writing](evidence/notes-and-editor.jpg) · [Timeline and record states](evidence/timeline-and-record-states.jpg)
- Debug and release each passed 193 tests with zero failures, errors or skips. The debug APK passed v2 signing, alignment, archive integrity and metadata checks. It reports `com.twohearts.app` 1.0.0 (1), min API 26 and target API 35. Local APK SHA-256 `62758060a55650688fea04d0a82782ce752d08b35afeabad5df2e8487b390943`.
- Android API 34 native Compose and Room tests cover the recorded interactions. Installed keyboard behavior and the transformed app walkthrough on the TECNO handset remain part of release validation.

## Completion checklist

- [x] Record the starting checkpoint, reviewed source, implementation, and stage dependencies.
- [x] Verify filtering, older categories, date group ordering, existing data and routes, CRUD and reopen.
- [x] Inspect native screens and dialogs in both palettes, narrow and standard widths, and at 200% text.
- [x] Pass both full unit variants, verify the debug APK, and review the staged diff.
- [x] Commit, push, verify exact remote CI and artifact, and update the progress tracker.

## Remote verification

GitHub confirmed `2bd93c06959e66e0440e9f2151c5c44e5c428198` on `master`. Build Android APK passed both unit suites and published `twohearts-debug-apk` for that SHA in run `45432768555`. Artifact ID `14939427600` was current at verification. [Commit](https://github.com/https10011/2HEARTS2/commit/2bd93c06959e66e0440e9f2151c5c44e5c428198) · [CI](https://github.com/https10011/2HEARTS2/actions/runs/45432768555) · [APK](https://github.com/https10011/2HEARTS2/actions/runs/45432768555/artifacts/14939427600)

## Next stage

Stage 13 revisits Reminders, Important Dates and Notifications. It verifies recurrence, time-zone changes, permission handling and deep-link delivery.

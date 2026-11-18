# Phase 8 — Timeline experience

Timeline now communicates continuity instead of presenting unrelated cards. A visible spine connects each chapter, the newest moment receives restrained gold emphasis, and every event combines a human date, title, and bounded story excerpt. The screen enforces newest-first chronology even when a caller supplies unsorted data.

The empty state invites the couple to begin their story. Creation and editing explain the purpose of the entry, validate both title and ISO date, provide room for an optional description, and save away from the main thread. Detail uses a calmer reading treatment with complete text, human timestamps, explicit editing, and confirmed deletion. Editing can intentionally clear an existing description.

## Verification

- `Phase8TimelineTest` verifies Room ordering and intentional description clearing.
- Native Compose renders cover empty, populated, detail, and editor states. The visual review confirmed the spine, hierarchy, form fit, and destructive-action placement. [Compose review](evidence/compose-review.png)
- `./gradlew :app:testDebugUnitTest :app:testReleaseUnitTest :app:assembleDebug` passed. All 92 tests passed in each variant.
- Physical-device verification of this phase remains pending.

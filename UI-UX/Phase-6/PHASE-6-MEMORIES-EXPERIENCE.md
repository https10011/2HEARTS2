# Phase 6 — Memories experience

Memories now opens as a private visual story instead of a database-style grid. The newest moment receives the strongest visual treatment, dates and captions establish context, and the empty state gives one clear first action. Creation and editing use the system photo picker, validate the required title and ISO date, and can attach up to ten photos or videos.

Selected media is copied into TwoHearts' private app storage and recorded through the existing `media_assets` and `memory_media` tables. Browse and detail load those associations from Room. Images render from the private copy, video items receive an explicit play treatment, and edit can add further keepsakes. Deletion soft-deletes the memory and removes its associations. The underlying private asset is retained for the established orphan-cleanup path because the current schema permits other features to reference the same asset. No database schema change was required.

## Reliability and privacy

- Memory creation, media copying, edits, and deletion run away from the main thread.
- Unsupported MIME types and files above the existing 25 MB photo or 500 MB video limits are rejected before storage. Streams are closed and over-limit copies are removed.
- Editing can now intentionally clear an existing caption. Previously a null caption silently restored the old text.
- Existing memories without media remain readable and receive a restrained visual placeholder. No synthetic relationship content is introduced.

## Design and accessibility

The browse surface uses a single narrative column with generous imagery, burgundy actions, warm surfaces, and human date formatting. Long titles and captions are bounded in browsing and remain complete on detail. The empty, populated, detail, and editor states were rendered through the native Robolectric Compose harness at the established phone size. The review removed the cramped top-bar save label and kept the full-width save action in the form. [Compose review](evidence/compose-review.png)

## Verification

- `Phase6MemoriesTest` covers the media acceptance policy and clearing a persisted caption through the real Room repository.
- Existing Phase 0 renders exercise empty, populated, detail, and creation states after the redesign.
- `./gradlew :app:testDebugUnitTest :app:testReleaseUnitTest :app:assembleDebug` passed. All 89 tests passed in each variant.
- Owner-device verification applies to the pre-Phase-5 startup build. Phase 6 physical-device validation remains pending.

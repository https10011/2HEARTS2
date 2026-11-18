# Phase 7 — Notes experience

Notes now feels like opening a private collection of letters and thoughts. The home screen uses warm paper-like surfaces, more generous excerpts, meaningful category cues, and one clear writing action. Empty copy invites a first personal note instead of describing a record-management task.

Search works across titles and full note text. Category chips can narrow the collection to love letters, shared notes, private thoughts, gratitude, ideas, reminders, or general notes, and search composes with that selection. Filtering is local and immediate.

The editor introduces the purpose of the space, gives the writing field room to breathe, preserves category selection, and provides inline feedback for missing title or content. Detail uses a serif reading treatment, human-readable timestamps, a restrained category label, and explicit edit and confirmed-delete actions. Creation, edits, and deletion now run off the main thread.

## Visual and accessibility review

The native Compose harness rendered empty, populated, reading, writing, and extra-large text states. The result keeps labels readable, lets category controls scroll horizontally, bounds long previews, and preserves complete note text in the reading view. Decorative category icons are excluded from accessibility while edit, clear-search, and destructive actions retain labels. [Compose review](evidence/compose-review.png)

## Verification

- `Phase7NotesTest` verifies title/content search and combined category filtering.
- Existing Phase 0 renders exercise the redesigned empty, populated, detail, editor, and large-text states.
- `./gradlew :app:testDebugUnitTest :app:testReleaseUnitTest :app:assembleDebug` passed. All 91 tests passed in each variant.
- Physical-device verification of this phase remains pending.

# Stage 11 — Memories

## Starting checkpoint

Stage 10 passed on `161fc6137ba06d099ce19679e2aa4c358f634989`. The Gallery renders saved images and offers video placeholders. Detail can open videos. The editor adds media but cannot preview, remove, or replace saved attachments. A canceled picker can clear pending additions. Metadata and attachments commit separately, so a failed import can leave a partial edit. [Stage 10 report](../Stage-10/REPORT.md)

Reviewed the three Memories screens, AppRouter, Room DAOs and sort order, local media storage and thumbnail code, the Photo Picker, shared video dialog, native render harnesses, and existing Memories tests.

## Acceptance targets

- Render a distinct featured memory, a compact gallery, readable detail, accessible editor, and clear deletion feedback.
- Verify ordered private attachments through add, replace, remove, picker cancellation, failure with retry, and database reopen. Preserve existing references and supported media formats.
- Verify image preview, video opening and error states, navigation, both themes, narrow and large-text layouts, local build, and exact remote CI.

## Implementation

- The gallery leads with the latest record. Earlier memories use a compact list with optional thumbnails. The detail page separates the date and title, the selected attachment and its thumbnail selector, the complete caption, and the delete action.
- The shared editor preserves the form and ordered draft during activity recreation. The Android Photo Picker adds or replaces local photos and videos. A canceled result leaves the draft alone. Removing an attachment updates the draft until Save. The editor shows validation and a persistent error when saving fails.
- Both record and links commit in one Room transaction after the selected files can be read. The writer compares the saved snapshot before editing to avoid overwriting newer changes. Failed or removed files are cleaned only if no profile, place, or memory references them. A dismiss or a failed selection leaves the prior record and attachments usable.
- The detail can open a fitted photo preview or a video by private URI. Video pauses when the activity backgrounds and releases playback on dismissal. Missing or inaccessible media shows explicit feedback inside the preview. The existing shared media frame presentation remains available to Places and Vault.

## Verification

- Verified JPEG and H.264 copies, ordered links after a file-backed Room reopen, replacement and removal, shared-file retention, stale edits, rollback for a denied attachment, and successful retry. The first failed import leaves no record or file behind. The failed edit preserves the prior record and bytes. Picker cancellation keeps the existing selections.
- The native Android API 34 Compose tests exercised gallery links, thumbnail selection, fitted photo opening and dismissal, deletion confirmation, removal before submission, and validation at 200%. Inspected the gallery and detail at 411 dp, the edited draft at 150%, and the new editor at 360 × 640 dp and 200%. In the short viewport, attachment controls and Save are below the title and date and remain in the scroll area. [Gallery, detail and editor](evidence/gallery-detail-and-editor.jpg) · [Preview, confirmation and validation details](evidence/previews-confirmation-and-validation.jpg)
- The test MP4 is a generated 96 × 64 H.264 baseline clip with 12 frames. `ffprobe` identified all frames and FFmpeg decoded the full file without error. The native Android test verified the local URI reaches VideoView, the prepared callback starts playback, the activity pause pauses playback, and a missing file produces an error. Hardware decoder output and Photo Picker grant behavior on the TECNO handset remain part of release validation.
- Each full unit suite passed 185 tests with zero failures, errors or skips. The assembled debug APK passed v2 signature, alignment, archive and package checks. It reports `com.twohearts.app` 1.0.0 (1), min API 26 and target API 35. Local APK SHA-256 `40602f1476d3b54f80c973e4c08048a4df705148649da0bff00f28008598837b`.
- Stage 11 uses the existing Room schema and Photo Picker access. Saved media survives database reopen. A pending picker URI whose grant expires can be replaced in the editor before retrying.

- Git verified `e30b345bc09f8ada7d2797614063fa47aa1410a3` on `origin/master`. Its Android workflow succeeded and published the Memories checkpoint. [Commit](https://github.com/https10011/2HEARTS2/commit/e30b345bc09f8ada7d2797614063fa47aa1410a3) · [CI](https://github.com/https10011/2HEARTS2/actions/runs/45428206120) · [APK](https://github.com/https10011/2HEARTS2/actions/runs/45428206120/artifacts/14939205361)

## Completion checklist

- [x] Record the starting checkpoint, source audit, implementation, persistence contract, and stage dependencies.
- [x] Verify ordered local files, references, cancellation, replacement, removal, rollback, retry, and reopen.
- [x] Inspect native gallery, detail, editor, preview, error, and confirmation states at representative sizes and in both themes.
- [x] Pass the full debug and release unit suites, verify the APK, and review the final diff.
- [x] Commit, push, verify exact remote CI and APK, and update the progress tracker.

## Next stage

Stage 12 improves Notes filtering, writing and reading, and gives Timeline clear chronological date groups. It preserves local records and covers both features’ editors, detail and confirmation flows.

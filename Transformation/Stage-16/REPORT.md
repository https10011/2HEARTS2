# Stage 16 — App lock and Vault

## Starting checkpoint

Stage 15 passed on `d5ddbe54426247b0ab93b0fe44d0a4bc5b718296`. Reviewed the native lock and Vault renders, AppRouter’s cold and foreground branches, BootstrapService, lifecycle timing, the lazy Keystore-backed PIN store, PIN setup and change actions, the Vault DAO and writer, the media-reference query and cleanup, the viewer, system document actions, and the startup and data-retention tests. [Stage 15 report](../Stage-15/REPORT.md)

## Initial findings

- Startup restores the app lock before composing content. The lock and PIN dialogs use basic layouts and need visual checks with large text and the keyboard.
- A Vault attachment is copied before the database transaction starts. Cache cleanup can run between the copy and registration, and a failed write can leave a file behind.
- Shared-media retention recognizes profile, place, and memory links. It also needs to honor a live Vault reference before deleting an asset.
- Vault’s note, photo, video and document flows need distinct presentation, reliable unavailable-file feedback, and verified export and deletion.

## Implementation

- The cold lock gate uses the official mark, the shared masked PIN field, and a scrollable layout that accounts for system insets and the keyboard. PIN settings use the same control for setup, change and disable. The fields use composition-local state, and rejection clears the entered current PIN. The existing Keystore verifier format, initialization order and foreground timeout are preserved.
- Vault groups records by type and keeps note contents inside the reader. The editor has one pinned Save action and uses Android Photo Picker for images and video and Open Document for other files. It keeps drafts in memory. The viewer uses the existing local image and video components and confirms when an exported copy leaves Vault.
- Vault now registers copied attachments in the same Room transaction that excludes cache cleanup. Live Vault links protect shared assets. Failed writes clean up unreferenced copies, and deletion discards the local attachment when it has no remaining live link. Cleanup now recognizes the existing document extension. The viewer and export both check the saved file is available under the private media directory. The Room schema and transfer format are unchanged.
- The implementation modifies the media DAO and writer, media storage, PIN material interface and lock service, shared utility and editor layouts, AppRouter, and the lock and Vault screens. Existing startup and product regression tests remain in place.

## Verification

- The production router tests start with an enabled lock and verify that Home stays hidden after a rejected PIN. A successful PIN restores Home. A simulated background timeout locks it again. The native PIN dialogs cover mismatched confirmation, creation, cancellation, change and disable. They exercise the existing verifier with replaceable PIN storage. The separate startup regression checks lazy initialization.
- Room tests reopen a database with a private note and document, compare the exported bytes, reject a path outside the private media directory, handle missing and corrupt attachments, and check file removal and reference protection. The Notes and Reminders transfer and the existing live search tests keep Vault content private.
- Reviewed the native Compose captures at 411 × 891, 360 × 740 and 320 × 618 dp in both themes, including 200% text. The review corrected the lock heading's dark contrast and the confirmation sheets' font scaling. The lock heading now measures 13.39:1 in dark and 14.03:1 in light. The Vault sheets retain their actions at 200%, and the long note scrolls to its delete action. [Lock and PIN](evidence/lock-and-pin.jpg) · [Vault list and editor](evidence/vault-list-and-editor.jpg) · [Reader and confirmations](evidence/vault-reader-and-decisions.jpg) · [Media previews](evidence/vault-media.jpg)
- The Android API 34 Robolectric runs exercise Android selection and export contracts and render the video loading state. Physical Keystore, software keyboard, device playback, and the TECNO Android 13 walkthrough remain release checks. ADB found no attached device.
- Debug and release each passed 236 tests with zero failures, errors or skips. The debug APK passed archive integrity, alignment and v2 signature checks. The package is `com.twohearts.app` 1.0.0 (1), min API 26 and target API 35. Local APK SHA-256 `6c5d97b5e2fe6b5e06f471d3f7d7882572e31351a56f9e0a4d9f591eb1570fbe`.

## Acceptance and checkpoint

- [x] Preserve lazy Keystore initialization, stored PIN compatibility and private data boundaries. Verify cold lock, authentication, timeout relock, and content gating.
- [x] Improve the lock gate and PIN settings and cover creation, change, rejection, disable and cancel through native controls.
- [x] Refine Vault list, creation, note and media viewing, document export and delete. Verify local selection, commit, cancellation, shared references and reopen.
- [x] Verify Vault stays outside ordinary search and Notes and Reminders transfer.
- [x] Inspect both themes and large text on native screens, then pass both full suites and the debug APK checks.
- [x] Commit, push, verify exact CI and artifact, and continue to the Yuki art pipeline.

## Remote verification

GitHub confirmed `d144a9dfb2188e4c52cd3e1f2761b58eec77a5ae` on `master`. Build Android APK run `45526347818` passed and published `twohearts-debug-apk` artifact `14982262989` for that commit. [Commit](https://github.com/https10011/2HEARTS2/commit/d144a9dfb2188e4c52cd3e1f2761b58eec77a5ae) · [CI](https://github.com/https10011/2HEARTS2/actions/runs/45526347818) · [APK](https://github.com/https10011/2HEARTS2/actions/runs/45526347818/artifacts/14982262989)

# Stage 7 — Couple photo persistence

## Starting checkpoint

Navigation was published and verified at `82997ddb77b3af17577087a939faa720777b2865`. Both profiles already had `photo_ref`. Only the owner settings exposed the Android picker. That path held the picker URI until the settings form was saved, persisted the original image, and offered no way to crop or clear it. The partner screen could only display a saved reference. [Stage 6 report](../Stage-6/REPORT.md)

Reviewed the owner and relationship settings, Home and Us avatar consumers, shared `ProfileAvatar`, `AppRouter`, Room profile and media entities, `RelationshipService`, media size and preview limits, and orphan cleanup.

## Implementation

- Owner and partner settings share AndroidX Photo Picker, including its platform-supported document fallback, and use the same circular crop preview. Both offer choose, change, remove, and keep actions. The crop offers zoom and position controls with accessible labels. No broad photo-library permission is required.
- Selection copies the URI to private cache within the existing 25 MB photo limit. The app previews the local copy, uses bounded bitmap decoding, and corrects EXIF orientation. A saved crop is a square JPEG up to 1,024 pixels per side. JPEG encoding excludes the original EXIF metadata. The source is discarded after crop completion or cancellation. The accepted draft remains available for retry if the settings save fails.
- `ProfilePhotoWriter` copies the cropped file to app media and commits its `MediaAsset` row alongside the profile update in Room. The partner's name, photo and relationship update share the same transaction. “Keep” preserves a reference, “Remove” clears it, and “Replace” requires a readable staged crop. No schema migration was needed.
- The reopen test exposed a placeholder in the one-shot relationship lookup. It always returned `null`, so the existing settings save could insert another relationship row. The lookup now queries Room. Settings updates the existing relationship and preserves its identity. Previously stored rows are retained.
- Cleanup checks both profiles, places, and memory attachments before deleting detached files. Storage cleanup and avatar writes serialize through Room, so a cache sweep cannot race the avatar's commit. A force-terminated process can leave an unreferenced file. Storage cleanup can remove that file while preserving every attached reference.
- The shared `ProfileAvatar` uses the same local reference for Home, Us, and settings. It reuses the bounded thumbnail cache with orientation correction and falls back to a name initial or icon if the file is unavailable. Onboarding integration is the next stage.

## Verification

- The six focused native Android tests passed for provider grant revocation after staging, a rotated and repositioned crop, large and unreadable sources, both saved photo references and the relationship across a file-backed Room reopen, rollback with retry, and shared reference cleanup.
- Inspected the saved owner photo in light mode, the partner fallback at 200% in dark mode, and the actual crop sheet at 360 × 640 dp and 200% in dark mode. The snapshot checks the existing avatar cache's render using pixels from the test-owned image. The crop and Cancel controls stay reachable. A physical Photo Picker and settings walkthrough remains part of release validation. [Native settings and crop renders](evidence/profile-photos-and-crop.jpg)
- The full debug and release suites each completed 163 tests with zero failures, errors, or skips. The assembled debug APK passed v2 signature, alignment, archive, and package checks. It targets API 35, supports API 26 and newer, and declares no broad media or storage permissions. Local APK SHA-256 `8981fa84f139197ebc09a7ad948a5235c4df26ee1c5f2837e858bb4b2c67a952`.
- Git verified `a8529f8468c3ff4afd9be4d46d3da8872fc5f739` on `origin/master`. Its Android workflow passed and published the stage APK. [Commit](https://github.com/https10011/2HEARTS2/commit/a8529f8468c3ff4afd9be4d46d3da8872fc5f739) · [Actions](https://github.com/https10011/2HEARTS2/actions/runs/44526243440) · [APK](https://github.com/https10011/2HEARTS2/actions/runs/44526243440/artifacts/14532355211)

## Completion checklist

- [x] Record starting commit, inspected source, existing photo limitations, implementation, and next-stage dependency.
- [x] Verify native picker-source fixtures, retained local data, and crop and removal behavior.
- [x] Inspect native Compose renders and record visual evidence and device limits.
- [x] Pass both full unit suites, assemble and inspect the APK, and review the final diff.
- [x] Commit the stage, push to `master`, and verify the exact SHA and Android workflow.

## Next stage

Stage 8 integrates the shared local photo flow with onboarding and adds Android capability explanations and optional notification permission handling.

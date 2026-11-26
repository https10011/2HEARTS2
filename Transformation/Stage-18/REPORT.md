# Stage 18 — Yuki engine and persistence

## Starting checkpoint

Stage 17 passed on `d74c35dd0bf629db86f6c8a5a4c6e9a23e67c02d`. Reviewed the Yuki product directive, registered atlas, current Compose room, ViewModel, persisted JSON and domain time calculations, Home adapter and existing game and native rendering tests. [Stage 17 report](../Stage-17/REPORT.md)

## Initial findings

- The room swapped whole poses on independent delays. Pose choice, travel and playback state could reset while a care action was active.
- The ViewModel suppressed concurrent clicks while saving. A second service instance could still accept another action against the same save if it carried an outdated copy.
- Hour-by-hour refreshes could skip mood and grooming decay. An expired nap could count hours outside the actual sleep interval.
- The route polled every minute and scheduled a full action duration again after resume.

## Implementation

- The motion controller reads the registered clip catalog and uses seeded, mood-sensitive idle cues. These include blinking, ears, tail, curiosity, walking and a brief low-energy doze. The doze is a visual idle behavior and leaves durable care and needs intact. Explicit care and wake animations take priority.
- The Canvas uses a common ground anchor, eased movement and premultiplied pose blending. A draw-only state carries animation samples. The renderer reuses decoded images for an active clip and samples at most about 30 times per second. It switches to the current activity's still pose when paused or reduced motion is enabled.
- The route refreshes on resume and at needs or activity deadlines. An action in progress resumes from elapsed wall time. Timing inside the renderer uses monotonic time, and presentation does not write a save.
- Locked read-modify-write operations reread the on-device save before accepting care or changing equipment. An in-progress action rejects duplicates. The original preference key and versioned JSON fields retain levels, toys, accessories and existing saves. Clock processing bounds long absence, counts only overlapping sleep hours, and preserves partial decay intervals across refreshes.

## Verification

- The focused debug run passed 19 tests. It covered every care clip, a 15-minute seeded simulation, sleep and wake, resumed elapsed offsets, and behavior priority. Concurrent calls from two repository instances saved one care action. The time tests compared long and hourly advancement, nap boundaries, clock reversal, old saves and invalid JSON recovery.
- The Android API 34 native renderer captured the actual 260 dp room. The dark frame sequence shows Yuki standing, approaching the bowl, and lowering into the meal with the paws aligned to the rug. Captures taken after additional frame time are byte-identical when inactive or reduced motion is enabled. Standard-width light and compact 200% dark room renders were inspected. [Meal and pause](evidence/meal-and-pause.jpg) · [Room themes](evidence/room-themes.jpg)
- The lifecycle and idle results use automated time and visibility in Robolectric. The handset frame-time, playback and battery checks remain part of the room and release stages.
- Debug and release each passed 249 tests with zero failures, errors or skips. The 27,868,170-byte debug APK passed archive integrity, alignment, v2 signature and atlas checks. It reports `com.twohearts.app` 1.0.0 (1), min API 26 and target API 35. APK SHA-256 `a74000a1dd348b26c734d378fcf9a15a780e986a07116fb45b81c165c95ce852`.

## Acceptance and checkpoint

- [x] Verify all authored interaction clips, autonomous idle, breathing, and action priority.
- [x] Verify suspend and resume, reduced motion, stable anchors, and actual Compose frame sequences in automated tests.
- [x] Prove consistent elapsed-time updates, old-save recovery, duplicate suppression and persistence.
- [x] Pass both full suites and verify the debug APK.
- [ ] Commit, push, verify exact CI and artifact, and continue to the room and Home integration stage.

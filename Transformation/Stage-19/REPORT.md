# Stage 19 — Yuki’s room and Home integration

## Starting checkpoint

Stage 18 passed on `03d68fd182f23f26cd1a3f31a8a8446dffdf6990`. Reviewed the room themes and meal sequence, Companion state and motion, foreground route, Home's saved-state observation, care controls and the existing toy and accessory sheet. [Stage 18 report](../Stage-18/REPORT.md)

## Initial findings

- The cat shared a simple room with a static window and rug. Care, sleep, and toys needed clearer visual cues at the locations where Yuki interacts with them.
- The room used six matching outline buttons. Large text needed more room and a clear path to the secondary controls. The keepsake dialog used platform text sizing and placed progress, toy selection, and accessories inside one body.
- Home used a generic paw symbol. The adapter also returned freshly initialized status when a save did not exist.

## Implementation

- The native room uses an illustrated day or night window, layered floor and rug, a bowl suited to eating or drinking, and the selected toy. Yuki remains the foreground subject. Small bite and lap beats animate around the existing paw anchor.
- Recorded playback review exposed a double silhouette during a turn and a large step between loafing and stretching. Turns now show a single changing silhouette, other blends follow the registered head height within a small range, and waking has an additional crouched pose.
- The room centers status and feedback around Yuki, groups care actions, and adapts the grid to phone width and font scale. The pet scene and Pet button provide independent accessible actions. Wake remains available during a nap.
- A scrolling keepsakes sheet keeps Done visible and shows the actual companion level, saved toy and accessory choices, unlock requirements, and care counts. Animation pauses when the sheet is open.
- Original apparel drawings mount on the registered head or chest and follow the equipped choice in the room and Home. Their paths are reused while drawing.
- Home shares the room's resting portrait mapping and displays the saved activity after a route resume. The portrait loads one local sprite and uses no animation timer. Home shows an invitation until a usable save exists. The compact card frees space for the status and level at 200% text.

## Verification

- An earlier native run passed 23 focused tests. The revised debug suite passed 255 tests, including both pet actions, every care control, toy locks, saved accessories and milestones, application activity stop and restart, waking, and a Home portrait linked to the room.
- Inspected native screenshots at 411, 360, and 320 dp in both themes and at 200% type. The home card's compact treatment fits the sleeping status and level. The keepsakes sheet has a visible Done action while the choices scroll. [Room and Home](evidence/room-and-home.jpg) · [Keepsakes and return](evidence/keepsakes-and-return.jpg)
- Reviewed 196 sampled Compose frames covering idle, feed, drink, play, pet, groom, nap and wake. The turn now keeps a single silhouette, a crouched pose breaks up the wake transition, and the paws remain on the same floor line between poses. Brief overlap during the airborne-to-paw blend is visible when paused on an intermediate frame. [Motion recording](evidence/native-motion.mp4) · [Idle, feeding and play](evidence/poses-1.jpg) · [Care, nap and wake](evidence/poses-2.jpg)
- The 15-minute simulation still varies idle cues, and the native tests verify frozen frames in reduced motion. Repeatedly cycling the entire catalog left the shared bitmap cache at the same size within its 5 MiB budget. The Home preview has one pose and no clock. ADB found no attached hardware. The 33 ms frame-time target and extended memory checks on the TECNO handset remain device verification.
- Debug and release each passed 255 tests with zero failures, errors or skips. The 28,125,523-byte APK passed archive integrity, alignment, v2 signature and atlas checks. It reports `com.twohearts.app` 1.0.0 (1), min API 26 and target API 35. Local APK SHA-256 `bcfa01d431e3f7ce27e2daa339e1bf3eff3abe046c7231f94156f5f0345573ac`.

## Acceptance and checkpoint

- [x] Inspect light and dark rooms and Home at narrow and normal sizes, including 200% text.
- [x] Verify both pet actions, care and feedback, toy locks, accessory selection, actual milestones, and return from background.
- [x] Inspect recorded idle and interaction motion, reduced motion, and stable sprite memory in automated tests.
- [x] Pass both full suites and verify the debug APK.
- [ ] Commit, push, verify exact CI and artifact, and continue to the cross-app audit.

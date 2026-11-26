# Stage 17 — Yuki art pipeline

## Starting checkpoint

Stage 16 passed on `d144a9dfb2188e4c52cd3e1f2761b58eec77a5ae`. Reviewed the 21 originals in registration sheets and inspected their source dimensions and channels. Read the product directive, Gradle asset task, Kotlin bitmap decoder, room composable, Home adapter, Yuki state and storage boundaries, and the Phase 13 render and regression tests. [Yuki product directive](../../Yuki%20Assets/Yuki_Game_Engine.md) · [Stage 16 report](../Stage-16/REPORT.md)

## Initial findings

- The 21 originals have opaque pixels, including the PNG stored with an alpha channel. A gray-and-white checker lies behind the cat. The file numbering skips 016.
- Android currently packages about 22 MB of source PNGs, removes the matte on first decode for each pose, and tightly crops each result. Independent cropping changes the body scale and apparent floor position as poses change.
- The originals provide distinct silhouettes for locomotion, care, rest, and mood. They share the orange coat, cream muzzle and chest, and dark features, with differences in apparent scale and placement.

## Registration and provenance

- Kept the original PNGs and their filenames. The pipeline matches each to exactly one semantic pose and records its SHA-256 in the runtime atlas. The asset upload is recorded in Git at `ea2b961`.
- The generator measures each pose with an authored body position and eye span, extracts the cat's outline and cream fur, and aligns its footprint to the shared ground point. The 416 × 324 tiles use ground point (208, 299). The engine can move the registered flying and chase poses independently of that ground point.
- The five reference frames for blinking, ear movement and tail motion derive from the original seated pose. Motion is local to each feature. The blink repaint follows the cream muzzle boundary and preserves the face and chest. The atlas maps the registered sprites to the care, locomotion, rest and mood clips used by the next stage.
- Android packages the lossless registered PNGs and atlas through the existing Gradle task. The packaged art shrinks from 22,125,542 to 1,614,337 bytes. Android decodes the small tiles directly and retains the existing 5 MiB LRU budget. The room sizes the fixed tile to bring the shared ground point onto the rug. Its current gameplay state, save format, and timing are preserved.
- Regeneration uses Python 3.12 with the versions pinned in [yuki-art-requirements.txt](../../tools/yuki-art-requirements.txt). Install them in a Python virtual environment and run `python3 tools/generate-yuki-art.py` at the repository root. Run it with `--check` to compare the generated PNG and atlas bytes with the committed copies. Gradle uses the committed copies and requires no Python during the APK build. [Registration inputs](../../Yuki%20Assets/sprite-layout.json) · [Generator](../../tools/generate-yuki-art.py) · [Runtime atlas](../../Yuki%20Assets/Runtime/atlas.json)

## Verification

- Regeneration and the byte comparison passed for 26 sprites and the atlas. The current source originals have no Git diff. The light and dark sheets show the registered silhouettes along a common ground line. Magnified review caught an edge around the first blink. The final reconstruction follows the cream muzzle contour. [Light registration](evidence/registered-light.jpg) · [Dark registration](evidence/registered-dark.jpg) · [Blink and ear detail](evidence/face-detail.jpg)
- Ten focused Android API 34 tests passed. The tests cover the packaged catalog, all frames, contour transparency, floor registration, and the unchanged areas of the motion frames. Actual Compose renders show sitting, feeding, drinking, grooming, sleeping, and play at 411, 360, and 320 dp, in both themes and up to 200% text. [Native room](evidence/compose-room.jpg) · [Native care poses](evidence/compose-care.jpg)
- Debug and release each passed 240 tests with zero failures, errors or skips. A fresh local package reduced the APK from 48,883,559 to 27,780,144 bytes. The first incremental APK had retained a large empty section in the ZIP after replacing the originals. The fresh package contains exactly the atlas and 26 sprites byte for byte. It passed archive integrity, alignment and v2 signature checks. It reports `com.twohearts.app` 1.0.0 (1), min API 26 and target API 35. APK SHA-256 `3f888aacbf0c26a9cd43a9813494bc16ea936d00c31feb5116b4d02b730a8b74`.
- Installed-device verification remains part of the Android release rehearsal. Stage 18 will exercise the motion sequences and the app lifecycle.

## Acceptance and checkpoint

- [x] Inspect and preserve 21 original poses, their provenance, and the character's recognizable coat and features.
- [x] Map every pose to one or more clips and generate reproducible, transparent and floor-aligned sprites.
- [x] Author supplemental eye, ear and tail motion frames with stable body anchors.
- [x] Inspect the current Compose room and Android packaged artwork, and verify both full Gradle suites and the APK.
- [x] Commit, push, verify exact remote CI and artifact, and continue to the Yuki engine and persistence stage.

## Remote verification

GitHub confirmed `d74c35dd0bf629db86f6c8a5a4c6e9a23e67c02d` on `master`. The first attempt built the APK and then failed while Robolectric fetched its Android runtime. Attempt 2 tested the same source and published `twohearts-debug-apk` artifact `15139834523` without a workflow change. [Commit](https://github.com/https10011/2HEARTS2/commit/d74c35dd0bf629db86f6c8a5a4c6e9a23e67c02d) · [CI](https://github.com/https10011/2HEARTS2/actions/runs/45529359216) · [APK](https://github.com/https10011/2HEARTS2/actions/runs/45529359216/artifacts/15139834523)

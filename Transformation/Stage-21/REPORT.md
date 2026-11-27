# Stage 21 — Repository cleanup

## Starting checkpoint

Started from `ad00145944a3ef4ed6b7129bebe4db2ade08f6f0` after its workflow and APK verified. Reviewed the root and archive documentation, the native build inputs and CI workflow, the migration and UI/UX reports, historical crash evidence, source artwork, art tools, licenses, and tracked paths for generated packages. Searched source, build, and tool files for the two former planning paths before moving them. [Stage 20 checkpoint](../Stage-20/REPORT.md)

## Changes

- Moved the completed native migration roadmap and post-migration UI/UX directive into `Archive/Planning/`. Added a historical notice, updated affected references, and linked each plan to its original reports. The transformation roadmap retains its full 23-stage plan and shared acceptance checks. [Archived plans](../../Archive/Planning/) · [Current roadmap](../TWOHEARTS-MASTER-TRANSFORMATION-ROADMAP.md)
- Consolidated the active project guide in the root README. It identifies the native source, Room migrations, test and build commands, APK workflow, signing constraints, and current product documentation. It also explains private record handling and the scope of JSON transfer. [Project guide](../../README.md)
- Marked the prior UI/UX progress document as historical and kept its implementation and crash evidence in the original location. Left the archived app source, all implementation reports, test fixtures, licensed assets, art pipeline, Gradle files, and packaging workflow in place. No tracked APKs, AABs, or temporary build files were found. The ignore rules now include generated Kotlin state and use the existing general dependency-directory rule for tool installs. [UI/UX records](../../UI-UX/) · [Migration records](../../Migration/)

## Verification and next stage

- All added local Markdown links resolve. Git classified both moves as renames. The staged diff contains documentation and ignore rules, with no changes to build inputs, the packaging workflow, or native app code.
- A fresh detached worktree assembled the debug APK. The debug suite executed again from that checkout using `:app:testDebugUnitTest --rerun` and passed all 258 tests with zero failures, errors, or skips. Git reported a clean checkout after the run.
- The clean APK passed ZIP integrity, alignment, and signature verification. Each of its 229 archive entries matches the contents of the previous Stage 20 local build byte for byte. The wrapper archive itself has a different entry order. Clean APK SHA-256 `37e1e3a99e52d586e26e12d63ba987e0fe8886d55b8d8e2a20c69a846f6e5615`. This change leaves Stage 20’s visual and runtime limitations applicable. [Cross-app evidence](../Stage-20/REPORT.md)
- Stage 22 will rehearse the signed candidate in available Android runtimes and record the handset checks. The exact Stage 21 remote commit and CI receipt belong in the next verified progress update.

## Completion checklist

- [x] Check references and archive superseded plans while preserving implementation reports, crash records, licenses, assets, tests, and build files.
- [x] Verify the documentation, review the staged diff, and run the suite and APK build from a clean checkout.
- [x] Verify that packaged runtime resources and app code match the previous review. Record why fresh screen capture is unnecessary for this checkpoint.
- [x] Verify the exact pushed SHA and CI artifact before beginning release rehearsal.

## Remote verification

GitHub confirmed `7ddcad4cf6ee74787370c2a5e1f6909aeef68ea5` on `master`. Build Android APK run `45918312614` passed on its first attempt and published `twohearts-debug-apk` artifact `15163390569`. [Commit](https://github.com/https10011/2HEARTS2/commit/7ddcad4cf6ee74787370c2a5e1f6909aeef68ea5) · [CI](https://github.com/https10011/2HEARTS2/actions/runs/45918312614) · [APK](https://github.com/https10011/2HEARTS2/actions/runs/45918312614/artifacts/15163390569)

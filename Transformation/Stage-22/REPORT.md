# Stage 22 — Android release rehearsal

## Starting checkpoint

Stage 21 published `7ddcad4cf6ee74787370c2a5e1f6909aeef68ea5`. Reviewed the APK workflow, manifest and launcher activity, bootstrap, onboarding, PIN gate, Notes persistence, Us navigation, Yuki lifecycle, and prior Android 13 upgrade evidence. This workspace has no attached handset or KVM device. The earlier software Android guest had System UI ANRs, so the supported hosted emulator is the primary release runtime. [Stage 21 report](../Stage-21/REPORT.md) · [Earlier Android 13 evidence](../Stage-2/REPORT.md)

## Rehearsal setup

- The existing **Build Android APK** workflow still builds, tests, and uploads the candidate. The downstream Android matrix downloads those same signed bytes and records their SHA-256 and signer before installation.
- The rehearsal drives the actual launcher and accessibility hierarchy through ADB. API 26 and 35 check a fresh APK and a warm return to Welcome. API 33 adds the first-run picker, denied notifications, completed setup, a persisted note, real PIN storage, cold unlock, Yuki care, and background/foreground return.
- A separate API 33 upgrade fixture builds the known schema-13 source, signs it and a copy of the candidate with a temporary matching fixture key, and checks that the profiles and note remain visible after installing the update. The distribution APK is independently installed with its original signature. The fixture key stays on the temporary runner.
- Runtime artifacts record source and APK identity, screenshots, launch timing, crash and activity exit information, and emulator frame statistics. Kept the APK artifact name and retention in the original packaging job.

## Status

The Python, YAML, and embedded shell syntax checks passed. Gradle verified the up-to-date debug and release unit suites and debug APK for the unchanged app source. The hosted checks are the next gate. The TECNO Spark 10 Pro is unavailable here. Completing a photo selection, notification delivery, and device-codec playback require runtime evidence. The owner confirmed the pre-transformation APK on Android 13, which identifies the working baseline only.

Run `45918989486` packaged the APK for `93264de47907b84722481d5a4e43c33a0901c013`. All three runtime jobs stopped at the KVM accessibility check before starting Android. The next revision waits for udev permission updates and uses software emulation on runners without a KVM device. [First rehearsal run](https://github.com/https10011/2HEARTS2/actions/runs/45918989486) · [APK](https://github.com/https10011/2HEARTS2/actions/runs/45918989486/artifacts/15163725838)

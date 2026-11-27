# Stage 22 — Android release rehearsal

## Starting checkpoint

Stage 21 published `7ddcad4cf6ee74787370c2a5e1f6909aeef68ea5`. Reviewed the APK workflow, manifest and launcher activity, bootstrap, onboarding, PIN gate, Notes persistence, Us navigation, Yuki lifecycle, and prior Android 13 upgrade evidence. This workspace has no attached handset or KVM device. The earlier software Android guest had System UI ANRs, so the supported hosted emulator is the primary release runtime. [Stage 21 report](../Stage-21/REPORT.md) · [Earlier Android 13 evidence](../Stage-2/REPORT.md)

## Rehearsal setup

- The existing **Build Android APK** workflow still builds, tests, and uploads the candidate. The downstream Android matrix downloads those same signed bytes and records their SHA-256 and signer before installation.
- The rehearsal drives the actual launcher and accessibility hierarchy through ADB. API 26 and 35 check a fresh APK and a warm return to Welcome. API 33 adds the first-run picker, denied notifications, completed setup, a persisted note, real PIN storage, cold unlock, Yuki care, and background/foreground return.
- A separate API 33 upgrade fixture builds the known schema-13 source, signs it and a copy of the candidate with a temporary matching fixture key, and checks that the profiles and note remain visible after installing the update. The distribution APK is independently installed with its original signature. The fixture key stays on the temporary runner.
- Runtime artifacts record source and APK identity, screenshots, launch timing, crash and activity exit information, and emulator frame statistics. Kept the APK artifact name and retention in the original packaging job.
- API 33 also runs two instrumentation checks against the exact published APK. The test APK is built with the same signer and published separately. The media check waits for actual H.264 decoder pixels in the app’s video preview. The notification check invokes the real WorkManager schedule, verifies the system notification and its open action, and checks the corresponding single Room receipt. The fixture video stays in the test APK.

## Status

The Python, YAML, and embedded shell syntax checks passed. Gradle verified the debug and release unit suites and debug APK for the unchanged app source. The instrumentation APK built locally, contains the H.264 fixture, and matches the local candidate’s signer. Hosted execution of the decoder and delivery checks is pending. The TECNO Spark 10 Pro is unavailable here. The owner confirmed the pre-transformation APK on Android 13, which identifies the working baseline only.

Run `45918989486` packaged the APK for `93264de47907b84722481d5a4e43c33a0901c013`. All three runtime jobs stopped at the KVM accessibility check before starting Android. The next revision waits for udev permission updates and uses software emulation on runners without a KVM device. [First rehearsal run](https://github.com/https10011/2HEARTS2/actions/runs/45918989486) · [APK](https://github.com/https10011/2HEARTS2/actions/runs/45918989486/artifacts/15163725838)

Run `45919248043` reached the emulators. The action executes each script line separately, which split the shell conditional. The current workflow gives the action a single command and selects the walkthrough using Android’s reported API level. [Runner setup retry](https://github.com/https10011/2HEARTS2/actions/runs/45919248043)

Run `45919452340` on `292f84c38c78417395c8cd730a320f848bd89e5b` packaged and installed the same APK on API 26, 33, and 35. API 26 and 35 displayed Welcome on cold launch and after returning from the Android launcher. On API 33, the published candidate opened the system photo picker, continued after system alert denial, finished setup, saved and reloaded a note, enforced a cold PIN gate, and returned to Yuki after backgrounding. No TwoHearts fatal exception appeared in any crash buffer. The APK SHA-256 was `8c5c8d29392077edf2fe6bcfcb014fb789ff3845b15842d17c32066977797a9f`. [Workflow](https://github.com/https10011/2HEARTS2/actions/runs/45919452340) · [API 26 and 35 screenshots](evidence/startup-api26-and-api35.jpg) · [API 33 walkthrough](evidence/first-api33-journey.jpg)

The API 33 job failed while populating the older schema-13 fixture. Its date picker opens inline, and the visible wheel clipped the confirmation control below the fold. The API 33 hierarchy and screenshots showed the emulator and both installed apps running. The retry scrolls the fixture to reveal its confirmation before exercising the update. [API 33 diagnostics](https://github.com/https10011/2HEARTS2/actions/runs/45919452340/artifacts/15163947904)

Run `45920503579` reached the older editor after scrolling the picker. Its read-only category control is exposed as a text field, so the rehearsal was entering the note body in the category menu. The Android hierarchy identified the mismatch. The revised fixture uses the final editable field for the note body and the legacy toolbar’s Save action. [Retry](https://github.com/https10011/2HEARTS2/actions/runs/45920503579) · [API 33 diagnostics](https://github.com/https10011/2HEARTS2/actions/runs/45920503579/artifacts/15164134434)

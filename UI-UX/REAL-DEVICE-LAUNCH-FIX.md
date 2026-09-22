# TwoHearts — Real-Device Launch Crash: Root Cause and Fix

Status: **FIXED and verified on a real Android runtime.**
Scope: runtime launch crash only. No UI/UX phase work, no migration, no
redesign. Phase 4 remains as committed.

- Base commit: `961c4e7` (`feat(ui-ux): refine home experience`)
- Affected code: `app/src/main/java/com/twohearts/app/data/settings/SettingsStorage.kt`
  (plus its two construction sites)
- Regression test: `app/src/test/java/com/twohearts/app/SettingsStorageSingletonTest.kt`
- Fix commit: `fix(android): resolve real-device launch crash` (see final section)

---

## 1. Observed symptom

The GitHub build succeeds and the debug APK installs, but opening the app on a
physical phone immediately reports that the app has stopped. No error screen is
shown — the process dies.

## 2. APK / build variant tested

- Variant: **debug** — the same variant the GitHub Actions workflow produces
  (`.github/workflows/build-android.yml` runs `assembleDebug` and uploads
  `app/build/outputs/apk/debug/*.apk`).
- Release/R8 is **not** involved: `isMinifyEnabled = false`. There was no
  release-only hypothesis to chase, so none was invented.

## 3. Environment

- Repo environment had **no JDK and no Android SDK** at the start of this task
  (the previous session's `/opt/android-sdk` and `/usr/lib/jvm/java-21-openjdk-amd64`
  were gone). Installed these to be able to test at all:
  - OpenJDK 21 (`openjdk-21-jdk-headless`) — Debian trixie has no JDK 17 package;
    JDK 21 runs this AGP 8.7.3 / Gradle 8.11 build.
  - Android SDK: `platform-tools`, `platforms;android-35`, `build-tools;35.0.0`.
  - Emulator + system image.
- **No physical phone and no KVM droplet** were available, so the target was the
  strongest substitute: a **real Android runtime** (goldfish `emulator-5554`),
  not Robolectric. Guest image: `system-images;android-30;default;x86_64`
  (an AOSP image was chosen because the first attempt, android-34 google_apis,
  repeatedly crashed its *own* system processes under software emulation —
  `DeadSystemException` in `com.android.systemui`, `com.android.phone`, etc.
  That instability was the emulator's, not the app's, and is why the lighter
  image was used).

## 4. Reproduction

```
adb install -r -t app/build/outputs/apk/debug/app-debug.apk
adb logcat -c
adb shell am start -n com.twohearts.app/.MainActivity
adb logcat -d
```

## 5. Actual crash exception

```
FATAL EXCEPTION: main
Process: com.twohearts.app, PID: 3095
java.lang.IllegalStateException: There are multiple DataStores active for the
same file: /data/user/0/com.twohearts.app/files/datastore/twohearts_settings.preferences_pb.
    at androidx.datastore.core.okio.OkioStorage.createConnection(OkioStorage.kt:65)
    at androidx.datastore.core.DataStoreImpl$storageConnectionDelegate$1.invoke(DataStoreImpl.kt:189)
    at androidx.datastore.core.DataStoreImpl.getCoordinator(DataStoreImpl.kt:192)
    at androidx.datastore.core.DataStoreImpl.readAndInitOrPropagateAndThrowFailure(DataStoreImpl.kt:272)
    at androidx.datastore.core.DataStoreImpl$readState$2.invokeSuspend(DataStoreImpl.kt:226)
    at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.run(CoroutineScheduler.kt:684)
```

Full stack trace and surrounding logs: `UI-UX/REAL-DEVICE-FIX-evidence/00-logcat-crash-and-fix.txt`.

## 6. Root cause

`SettingsStorage` declared its DataStore as a **member** property:

```kotlin
class SettingsStorage(private val context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "twohearts_settings"
    )
```

A `preferencesDataStore(...)` delegate is only safe as a **top-level** (or
Activity-scoped) property, because it must be created once per process. As a
member property it is created once per *instance*. DataStore allows exactly one
active instance per file per process, and there were **two instances during a
single startup**:

| Order | Site | What happens |
|---|---|---|
| 1 | `MainActivity.onCreate` → `SettingsStorage(this)` | Instance A created. |
| 2 | MainActivity collects `settings` for theme/text-size (before `setContent` returns) | **DataStore A activated.** |
| 3 | `BootstrapService.bootstrap()` stage 6 → `SettingsStorage(context)` | Instance B created. |
| 4 | `AppLockService.initialize()` → `settingsStorage.settings.first()` | **DataStore B activated on the same file.** |
| 5 | bootstrap returns `true`; `MainActivity` line 65 calls `appStateService.initialize()` | Uses instance A → DataStore A collides with B → **throws.** |

Step 6 (bootstrap's app-lock stage) happens *after* the main-content lambda has
already begun, which is why the Activity's instance is active first. The
exception is thrown from a **coroutine worker** (`CoroutineScheduler`), i.e.
inside `LaunchedEffect`, and that call sits **outside** `BootstrapService`'s
`try { … } catch (e: Exception) { return false }`. So it is not swallowed — it
reaches the uncaught-exception handler and kills the process. That is the
"installs but immediately stops" report.

### Why this was not a Phase 4 regression

The two-instance wiring and the member delegate both predate Phase 4. Phase 4
changed `HomeScreen`/`AppRouter`/`HomePresentation` and did not touch settings
acquisition, so it neither caused nor could have caught this. The bug needed
(a) two call sites in one process and (b) the DataStore actually being read —
conditions no existing unit test created.

### Why the 80-test suite was green

Robolectric gives every test method a **fresh data directory**, and no existing
test constructed two storages in one process. A green suite was therefore never
evidence that the app could start. This is exactly the "build success ≠ runtime
success" trap the directive warned about.

## 7. Fix

Smallest correct change, in one file plus the two call sites:

1. Hoist the delegate to a **top-level (process-scoped) property** — this alone
   removes the crash, because both instances then share one DataStore.
2. Add a `getInstance(context)` singleton and make the constructor **private**,
   so a second instance cannot be created again by construction. This is the
   same double-checked-singleton shape already used by
   `TwoHeartsDatabase.getDatabase`, so it matches existing conventions.
3. Point both call sites (`MainActivity`, `BootstrapService`) at
   `SettingsStorage.getInstance(...)`.
4. Update `Phase0RenderHarness` (two call sites) to the shared accessor — the
   private constructor caught these at compile time.

Nothing else was changed. Phase 4 Home behaviour, navigation, the design system,
Room, onboarding, and Yuki are all untouched.

## 8. Regression test

`SettingsStorageSingletonTest` (4 tests) reproduces the real startup shape, not a
happy path:

- `repeatedAcquisitionReturnsTheSameInstance` — both call sites get one object.
- `twoCallSitesCanReadConcurrently` — the Activity read and the bootstrap read
  in one process (the exact sequence that threw).
- `writeThroughOneReferenceIsVisibleThroughTheOther` — sharing one instance did
  not cost correctness.
- `appLockInitializationDoesNotPoisonTheActivitySettingsAccess` — the full
  crash path including the post-bootstrap `AppStateService.initialize()`.

**The test was verified to fail on the original code.** Reverting
`SettingsStorage` to its pre-fix shape (member delegate + non-singleton accessor)
makes 3 of the 4 tests fail with the production exception itself:

```
twoCallSitesCanReadConcurrently -> java.lang.IllegalStateException: There are
  multiple DataStores active for the same file: .../twohearts_settings.preferences_pb
writeThroughOneReferenceIsVisibleThroughTheOther -> (same exception)
repeatedAcquisitionReturnsTheSameInstance -> AssertionError: expected same ... was not
```

With the fix, all 4 pass. The test protects the exact failure.

## 9. Verification

### Build
- `./gradlew :app:testDebugUnitTest` → **83 tests, 0 failures, 0 errors**
  (80 baseline from Phase 4 + 3 net new; the singleton test file has 4 tests).
- `./gradlew :app:assembleDebug` → **BUILD SUCCESSFUL**,
  `app/build/outputs/apk/debug/app-debug.apk` (~25 MB).

### Real Android runtime (A/B on the same emulator, same commit)

| Build | Result |
|---|---|
| **Pre-fix APK** | `FATAL EXCEPTION: main`, `IllegalStateException: There are multiple DataStores active for the same file`, FATAL count = 1, app gone (launcher resumed). |
| **Post-fix APK** | Bootstrap completes all 7 stages; `TH:AppState: App state initialized` logged twice (line-65 call **and** Compose gate); `mResumedActivity = com.twohearts.app/.MainActivity`; process alive; **FATAL count = 0**. |

The A/B is the causal proof: identical source except this wiring, and the
crash appears and disappears with it.

### First launch
Fresh install (`adb uninstall` → `adb install`) → launched → no crash →
`OnboardingFlow` composed → screenshot
`UI-UX/REAL-DEVICE-FIX-evidence/01-fresh-launch-onboarding.png` (1080×1920). Onboarding opens.

### Existing state / relaunch
`adb shell am force-stop com.twohearts.app` → `am start` → no crash,
`MainActivity` resumed, FATAL count = 0, bootstrap re-completes against the
already-populated DataStore and Room DB.

### Honest limitation
This is a **real Android runtime**, but not the user's physical
**Tecno Spark 10 Pro**. Everything about the failure — an
`IllegalStateException` from DataStore file locking — is device-independent, and
the A/B demonstrates causality, so a physical retest is expected to pass. I am
not claiming physical-device verification, because I did not perform it.

## 10. Known deferred

- `Build.VERSION_CODES`-era behaviour on Android 13+ (POST_NOTIFICATIONS runtime
  grant) was not exercised because the test image was Android 10 (`sdk 30`).
  This is unrelated to the crash but worth a pass on the real device.
- Bootstrap's Keystore-backed secure storage (`SecureStorage`) cannot initialize
  under Robolectric (`KeyStoreException: AndroidKeyStore not found`). It works on
  a real runtime — confirmed by Stage 6 completing on the emulator. Not a defect;
  it does mean unit tests cannot cover app-lock initialisation directly.

## 11. Final commit

`fix(android): resolve real-device launch crash` — see the commit and remote
verification recorded in the task report. The Phase 4 report
(`UI-UX/Phase-4/PHASE-4-HOME-EXPERIENCE.md`) was **not** modified.

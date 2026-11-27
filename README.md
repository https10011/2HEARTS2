# TwoHearts

TwoHearts is a private, offline Android app for two people. It keeps couple profiles, memories, notes, timelines, reminders, important dates, and personal records on the device. Yuki is an interactive companion with saved care and progress.

## Develop

Use JDK 17 and Android SDK 35 with the checked-in Gradle wrapper. TwoHearts supports Android 8.0 (API 26) and later.

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug
./gradlew :app:testReleaseUnitTest
```

Gradle writes the debug APK to `app/build/outputs/apk/debug/app-debug.apk`. The **Build Android APK** workflow runs on pushes to `master` and `main` and supports manual dispatch. Successful runs publish `twohearts-debug-apk` for 30 days. Android requires matching signing certificates for an in-place upgrade. [Android builds](https://github.com/https10011/2HEARTS2/actions/workflows/build-android.yml)

| Path | Contents |
|---|---|
| [app/src/main](app/src/main/) | Kotlin, Jetpack Compose, Android resources, and the application manifest |
| [app/src/test](app/src/test/) | JVM and Robolectric tests covering Room, persistence, navigation, lifecycle, and native Compose renders |
| [Room database](app/src/main/java/com/twohearts/app/data/database/) | Schema, migration history, and database recovery |
| [Yuki Assets](Yuki%20Assets/) | Source poses, engine notes, registered runtime sprites, and atlas |
| [tools](tools/) | Reproducible art and branding tools |

## Architecture

Compose screens use ViewModels and repositories backed by Room, DataStore, and app-private files. WorkManager schedules local reminders. Profile photos use Android’s system picker and stay in app-private storage. The PIN verifier uses Android Keystore-backed preferences. Search excludes Vault and private health records. JSON transfer covers Notes and Reminders.

Yuki uses a Kotlin behavior controller and Compose Canvas. Its motion clock pauses with the screen lifecycle. The runtime art can be checked against the supplied poses with `python3 tools/generate-yuki-art.py --check`. [Yuki engine](Yuki%20Assets/Yuki_Game_Engine.md)

## Project records

- The 23-stage master transformation roadmap defines the current work and verification gates. The tracker links each stage to its implementation, evidence, and GitHub checks. [Master roadmap](Transformation/TWOHEARTS-MASTER-TRANSFORMATION-ROADMAP.md) · [Progress](Transformation/TRANSFORMATION-PROGRESS.md)
- The migration and UI/UX reports document prior implementation, technical decisions, and Android crash evidence. The completed master plans and the legacy React/Vite/Capacitor source are archived. [Migration reports](Migration/) · [UI/UX reports](UI-UX/) · [Archived plans](Archive/Planning/) · [Legacy archive](Archive/)

## License

Private application. Not open source.

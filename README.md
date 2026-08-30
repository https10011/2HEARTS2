# TwoHearts

A private, offline-first, local-first couples app for Android, built with
**Kotlin + Android SDK + Jetpack**.

TwoHearts provides a shared digital space for two people in a relationship
to record memories, write notes, build a timeline, set reminders, track
mood, and more — all stored locally on-device with no cloud dependency.

## Technology Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| Platform | Android SDK (minSdk 26, targetSdk 35) |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM / Clean Architecture |
| Database | Room (SQLite) |
| Navigation | Navigation Compose |
| Background | WorkManager |
| Security | EncryptedSharedPreferences |
| CI/CD | GitHub Actions → APK |

## Project Structure

```
TwoHearts/
├── app/                          # Android application module
│   ├── build.gradle.kts          # App build configuration
│   └── src/main/
│       ├── AndroidManifest.xml   # App manifest
│       ├── java/com/twohearts/app/  # Kotlin source
│       │   ├── MainActivity.kt   # App entry point
│       │   └── ui/theme/         # Material theme (brand colors, typography)
│       └── res/                  # Android resources
│           └── values/           # Strings, themes
├── gradle/wrapper/               # Gradle wrapper
├── build.gradle.kts              # Root build configuration
├── settings.gradle.kts           # Gradle settings
├── gradlew                       # Gradle wrapper (Unix)
├── gradlew.bat                   # Gradle wrapper (Windows)
├── Migration/                    # Migration roadmap & documentation
│   └── TWOHEARTS-MASTER-AUDIT-AND-MIGRATION-ROADMAP.md
└── Archive/                      # Legacy React/Vite/Capacitor implementation
    ├── README.md                 # Archive documentation
    └── Legacy-React-Vite-Capacitor/  # Complete legacy codebase
```

## Development

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Install on connected device
./gradlew installDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

## CI/CD

GitHub Actions workflow (`.github/workflows/build-android.yml`) builds
the Android APK automatically on push to `master`/`main` or manual
trigger. The workflow produces a debug APK artifact with 30-day retention.

## Migration Status

This repository contains the **native Android rewrite** of TwoHearts.
The legacy React/Vite/Capacitor implementation is preserved in `Archive/`
for reference.

See `Migration/TWOHEARTS-MASTER-AUDIT-AND-MIGRATION-ROADMAP.md` for the
comprehensive migration roadmap.

## Brand Identity

- **App name:** TwoHearts
- **Primary color:** Burgundy (#6A1B2B)
- **Theme:** Warm, intimate, elegant, personal
- **Logo:** Preserved in `Archive/Legacy-React-Vite-Capacitor/Screen-References/`
  and brand assets

## License

Private application. Not open source.

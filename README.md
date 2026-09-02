# TwoHearts

A private, offline-first, local-first couples app for Android, built with
**Kotlin + Android SDK + Jetpack Compose**.

TwoHearts provides a shared digital space for two people in a relationship
to record memories, write notes, build a timeline, set reminders, track
mood, and more — all stored locally on-device with no cloud dependency.

## Technology Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Kotlin | 2.1.0 |
| Platform | Android SDK | minSdk 26, targetSdk/compileSdk 35 |
| UI | Jetpack Compose + Material 3 | BOM 2024.12.01 |
| Architecture | MVVM / Clean Architecture | — |
| Database | Room (SQLite) | 2.6.1 |
| Navigation | Navigation Compose | 2.8.5 |
| Background | WorkManager | 2.10.0 |
| Security | EncryptedSharedPreferences | 1.1.0-alpha06 |
| Settings | DataStore Preferences | 1.1.1 |
| Build | Gradle (Kotlin DSL) | 8.11, AGP 8.7.3 |
| CI/CD | GitHub Actions | JDK 17 (Temurin) |

## Project Structure

```
TwoHearts/
├── app/                          # Android application module
│   ├── build.gradle.kts          # App build configuration
│   ├── proguard-rules.pro        # ProGuard rules
│   └── src/main/
│       ├── AndroidManifest.xml   # App manifest
│       ├── assets/               # SVG brand/decoration assets
│       │   ├── branding/         # Logo, mark, app icon
│       │   ├── decorations/      # Rose-lily SVGs (20)
│       │   ├── images/           # Onboarding assets
│       │   └── yuki-cat.svg      # Companion cat
│       ├── java/com/twohearts/app/
│       │   ├── MainActivity.kt   # Entry point + bootstrap
│       │   ├── config/           # AppInfo metadata
│       │   ├── data/
│       │   │   ├── dao/          # 16 Room DAOs
│       │   │   ├── database/     # Room DB + 13 migrations
│       │   │   ├── entity/       # 17 Room entities
│       │   │   ├── game/         # Yuki state types
│       │   │   ├── repository/   # 14 repositories
│       │   │   └── settings/     # DataStore + SecureStorage
│       │   ├── services/         # 15+ service modules
│       │   │   ├── appstate/     # App state management
│       │   │   ├── bootstrap/    # Initialization pipeline
│       │   │   ├── datamanagement/ # Storage report + reset
│       │   │   ├── datetime/     # Date/time utilities
│       │   │   ├── device/       # Device capabilities
│       │   │   ├── error/        # Error taxonomy
│       │   │   ├── game/         # Yuki service
│       │   │   ├── lifecycle/    # Foreground/background
│       │   │   ├── logger/       # Leveled logger
│       │   │   ├── media/        # File + media storage
│       │   │   ├── notification/ # Local notifications
│       │   │   ├── permission/   # Permission management
│       │   │   ├── relationship/ # Couple management
│       │   │   ├── search/       # Global search engine
│       │   │   ├── security/     # App lock + PIN hashing
│       │   │   └── validation/   # Input validators
│       │   └── ui/
│       │       ├── components/   # 17 shared UI components
│       │       ├── navigation/   # AppRouter + BottomNav
│       │       ├── onboarding/   # 8 onboarding screens
│       │       ├── screens/      # All feature screens
│       │       │   ├── about/
│       │       │   ├── home/
│       │       │   ├── importantdates/
│       │       │   ├── memories/
│       │       │   ├── mood/
│       │       │   ├── more/
│       │       │   ├── notes/
│       │       │   ├── notifications/
│       │       │   ├── period/
│       │       │   ├── places/
│       │       │   ├── reminders/
│       │       │   ├── search/
│       │       │   ├── security/
│       │       │   ├── settings/
│       │       │   ├── shared/
│       │       │   ├── timeline/
│       │       │   ├── us/
│       │       │   ├── vault/
│       │       │   └── yuki/
│       │       └── theme/        # Material theme + design tokens
│       └── res/                  # Android resources
│           ├── drawable/         # Launcher icons
│           ├── mipmap-*/         # Launcher icons (all densities)
│           └── values/           # Strings, themes
├── gradle/wrapper/               # Gradle wrapper
├── build.gradle.kts              # Root build configuration
├── settings.gradle.kts           # Gradle settings
├── gradlew                       # Gradle wrapper (Unix)
├── gradlew.bat                   # Gradle wrapper (Windows)
├── Migration/                    # Migration roadmap & stage docs
│   ├── TWOHEARTS-MASTER-AUDIT-AND-MIGRATION-ROADMAP.md
│   ├── Stage-0/ through Stage-15/
├── Archive/                      # Legacy React/Vite/Capacitor
│   ├── README.md
│   └── Legacy-React-Vite-Capacitor/
└── .github/workflows/            # CI/CD
    └── build-android.yml         # APK build pipeline
```

## Development

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Install on connected device
./gradlew installDebug

# Clean build
./gradlew clean assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

## CI/CD

GitHub Actions workflow (`.github/workflows/build-android.yml`) builds
the Android APK automatically on push to `master`/`main` or manual
trigger. The workflow:

1. Checks out the repository
2. Sets up JDK 17 (Temurin)
3. Configures Gradle with caching
4. Builds the debug APK (`assembleDebug`)
5. Runs unit tests
6. Uploads the APK artifact (30-day retention)

## Migration Status

This repository contains the **native Android rewrite** of TwoHearts.
The legacy React/Vite/Capacitor implementation is preserved in `Archive/`
for reference.

See `Migration/TWOHEARTS-MASTER-AUDIT-AND-MIGRATION-ROADMAP.md` for the
comprehensive migration roadmap.

### Migration Stages

| Stage | Name | Status |
|-------|------|--------|
| 0 | Reconnaissance & Setup | ✅ Complete |
| 1 | Design System & Token Migration | ✅ Complete |
| 2 | Component Library Migration | ✅ Complete |
| 3 | Data Layer Migration | ✅ Complete |
| 4 | Core Services Migration | ✅ Complete |
| 5 | Onboarding Flow Migration | ✅ Complete |
| 6 | App Shell & Navigation Migration | ✅ Complete |
| 7 | Home, Us & Core Hub Screens | ✅ Complete |
| 8 | Content Features (Notes, Memories, Timeline) | ✅ Complete |
| 9 | Relationship Features | ✅ Complete |
| 10 | Vault & Security | ✅ Complete |
| 11 | Yuki Companion | ✅ Complete |
| 12 | Games Archival | ✅ Complete |
| 13 | Settings & Utilities | ✅ Complete |
| 14 | Integration Testing & Polish | ✅ Complete |
| 15 | Final Build & Release Preparation | ✅ Complete |

## Architecture

### Data Flow

```
UI (Compose) → ViewModel/Composable → Repository → DAO → Room → SQLite
```

### Database

- **17 Room entities** mapping to 13 legacy SQL migrations
- **16 DAOs** with Flow-based reactive queries
- **14 repositories** providing domain-level CRUD operations
- **13 schema migrations** preserved from legacy implementation

### Security

- PIN hashing: PBKDF2-HMAC-SHA-256 (120k iterations, 128-bit salt)
- PIN storage: Android Keystore via EncryptedSharedPreferences
- App lock: MEMORY-ONLY state (re-locks on cold start)
- Vault: Separate access control with same PIN
- No analytics, telemetry, or remote calls

### Key Features

- **77 screen references** from legacy implementation
- **55+ screens** with real implementations
- **Full CRUD** for notes, memories, timeline, reminders, places, mood, period
- **Yuki companion cat** with needs, actions, leveling, and accessories
- **App lock** with PIN protection and automatic re-lock
- **Vault** with PIN-protected private content
- **Global search** across all features
- **Dark mode** and text scaling support
- **Reduced motion** accessibility support

## Brand Identity

- **App name:** TwoHearts
- **App ID:** com.twohearts.app
- **Primary color:** Burgundy (#6A1B2B)
- **Theme:** Warm, intimate, elegant, personal
- **Logo:** Preserved in `app/src/main/assets/branding/`

## License

Private application. Not open source.

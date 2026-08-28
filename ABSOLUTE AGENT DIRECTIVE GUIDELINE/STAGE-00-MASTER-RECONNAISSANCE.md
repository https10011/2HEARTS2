# STAGE 00 — MASTER RECONNAISSANCE

## Date
August 28, 2026

## Repository State

| Item | Value |
|------|-------|
| Branch | `master` |
| Remote | `origin/master` |
| Latest commit | `b2ba916` — "New Directive for future Agents" |
| Working tree | Clean — no untracked, modified, or staged files |
| TypeScript | Compiles cleanly (`npx tsc -b --noEmit`) |
| Total files | ~328 indexed files, 24 asset files |

## Tech Stack (Actual)

| Layer | Technology |
|-------|-----------|
| Framework | React 18.3 |
| Build | Vite 5.4 + `@vitejs/plugin-react` |
| Routing | React Router DOM 6.26 (BrowserRouter) |
| Mobile wrapper | Capacitor 6.2 (Android-first) |
| Database | SQLite via `@capacitor-community/sqlite` (native) / `sql.js` (web/dev) |
| Schema | 12 migrations, schemaVersion=12 |
| Secure storage | `@aparajita/capacitor-secure-storage` (native) / MemorySecureStore (web) |
| Local notifications | `@capacitor/local-notifications` |
| File system | `@capacitor/filesystem` (native) / MemoryFileSystem (web) |
| State management | Custom `appSettingsStore` (localStorage-backed, `useSyncExternalStore`) |
| CSS | Plain CSS with CSS custom properties (tokens.css), no Tailwind, no CSS-in-JS |

**NOT present:** No React Query, no Zustand/Redux, no Tailwind, no Framer Motion, no Convex.

## Application Entry Flow

```
index.html
  → src/main.tsx (AppGate component)
    → bootstrapApp() — 7-stage pipeline:
        1. persistence (SQLite init + migrations) [CRITICAL]
        2. schema-verify [CRITICAL]
        3. device-capabilities
        4. lifecycle (Capacitor back-button, foreground/background)
        5. notifications (NotificationService)
        6. app-lock (PIN via SecureStore)
        7. application-state (AppStateService, RelationshipService, MediaStorage, DataManagement)
    → <App /> (if bootstrap succeeds)
      → <ErrorBoundary>
        → <AppRootProvider>
          → <AppLockGate>
            → <AppRouter> (React Router)
```

**Splash:** BrandLogo centered on cream background during bootstrap.

## Navigation Architecture

**Router:** `createBrowserRouter` — all routes in a single tree.

**Bottom navigation (5-position):**
```
Home | Notifications | ♥ TWOHEARTS (center) | Notes | More
```
- Center: elevated circular BrandLogo mark → `/app/us`
- Centralized config in `navConfig.ts`

**Key routes under `/app`:** home, us, games, notes, more, timeline, memories, reminders, places, mood, period, vault, notifications, plus settings sub-routes.

## Onboarding Architecture

**Flow:**
```
/ → OnboardingGate
  → if onboardingStage === 'complete' → /app/home
  → else: appState.reconcileOnboardingStage() → derives from domain truth
    - 'fresh' → /onboarding/welcome
    - 'owner' → /onboarding/profile
    - 'relationship' → /onboarding/relationship
    - 'personalization' → /onboarding/personalization
```

**Onboarding screens:** Welcome → ProfileSetup → RelationshipSetup → PersonalizationSetup → AppLockSetup → SetupComplete

**KEY FINDING — potential Welcome skip bug:**
- `reconcileOnboardingStage()` derives stage from **domain truth** (profile exists? couple row complete?)
- If persisted stage is not 'complete', it checks: owner profile → couple data → returns derived stage
- If owner profile exists but partner doesn't → returns 'relationship' → **skips Welcome**
- This is the most likely root cause of the Welcome screen being skipped

## Profile/Avatar Architecture

**Current state: TEXT INITIALS ONLY.**
- No image upload capability exists.
- `Profile` type: `displayName`, `birthDate`, `createdAt`, `updatedAt` — no photo field.
- Avatars render as `name.charAt(0).toUpperCase()` in a circle.

## Permission Architecture

**PermissionService** — pluggable, only `notifications` registered. Camera/photos intentionally unregistered.

## Import/Export Architecture

Export format envelope exists (`twohearts-export` v1). No import UI/flow.

## Game Architecture

10 games (6 couple + 4 casual). Pure state engine (`gameEngine.ts`). Level-based progression. **Will be replaced by YUKI in Stage 8.**

## Asset Structure

```
src/assets/
  branding/  — twohearts-logo.svg, twohearts-logo-mark.svg, twohearts-app-icon.svg
  decorations/ — rose-lily-01 through rose-lily-20 (20 floral SVGs)
  images/ — onboarding-welcome-photo.svg
```

All SVGs, locally bundled, offline-ready.

## Branding Systems

**BrandLogo** component — single source of truth. Variants: 'brand' (full), 'mark' (hearts only). Tones: 'brand', 'light'.

## Floral/RoseLily Systems

**RoseLilyDecoration** — 14 approved SVG variants, positioned absolutely, pointer-events:none.
**OnboardingArt** — 7 inline SVG illustration variants.

## Theme/Token Systems

Full CSS custom property system in `tokens.css`:
- Colors: burgundy family + warm neutrals
- Dark theme: warm dark surfaces, burgundy accent preserved
- Typography: Segoe UI base, Georgia display, 7-step scale
- Spacing: 4pt base
- Motion: standard/decelerate/accelerate/emphasized easing
- JS mirror in `tokens.ts`

## Local Storage Systems

Two-tier: localStorage (settings) + SQLite (domain data). Schema v12, 12 migrations.

## Security Systems

AppLockService: PIN 4-8 digits, PBKDF2-HMAC-SHA-256, SecureStore, re-locks on foreground. Vault: separate PIN-protected content.

## Media Systems

MediaStorage with photo/video support, MIME validation, magic-byte sniffing. Not connected to profiles.

## Notification Systems

Three layers: NotificationService (OS), NotificationCenterService (in-app history), PermissionService.

## Architectural Strengths

- Clean repository pattern
- Centralized navigation config
- Well-structured token system
- Proper migration framework
- Production-quality error handling
- Security properly implemented
- Offline-first architecture solid
- Single BrandLogo/RoseLily source of truth

## Architectural Weaknesses

- No profile photo capability
- Game system will be replaced entirely
- UI/UX feels functional but not premium ("HTML panels")
- No import system despite export format
- Bottom nav not yet pill-shaped
- No motion library (CSS only)

## Critical Risks

1. **Onboarding Welcome skip** — deriveStage() skips Welcome when owner profile exists
2. **No profile photos** — needs schema migration + picker + storage
3. **UI/UX gap** — functional but not emotionally premium
4. **Game replacement** — massive scope for Stage 8
5. **No import system** — needs full feature build

## Systems Reusable As-Is

BrandLogo, RoseLilyDecoration, OnboardingArt, Design tokens, Dark theme, MediaStorage, PermissionService, AppLockService, Database + migrations, Toast system, Error handling, Validation, Navigation config, Settings store, Repository pattern

## Systems Requiring Major Work

Profile photos (Stage 4), Permission experience (Stage 6), Import system (Stage 7), Game → YUKI (Stage 8), UI/UX overhaul (Stage 9), Brand/visual language (Stage 10), Asset archive (Stage 11), Component system (Stage 12), Responsive/a11y (Stage 13)

## Conclusion

The **backend architecture is solid** — clean repositories, proper migrations, security systems, permission framework, media pipeline. The **frontend architecture is adequate** — centralized routing, single navigation vocabulary, reusable components. The **primary gap is visual product quality** — the app has correct colors and components but feels like "HTML with CSS panels" rather than a designed product. The rebuild should extend and improve existing systems rather than replacing them wholesale.

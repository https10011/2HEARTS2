# Phase 02 · Home and the five-tab dock

Starting revision `e03bc2a2262674b672e1b7bc8b0b5105b1e6ccaf`. Locate the published implementation with `git log --diff-filter=A --format=%H -- VISUAL-REINVENTION/Phase-02/REPORT.md`. The GitHub workflow on that revision carries its own install results and artifact metadata.

## Implementation and behavior

The dock now follows the five-destination reading order Home, Notes, Us, Yuki and More. Notes and companion care have direct entry points, Us holds the circular official seal, and More leads to tools. The body reports its measured height to the shell. Saved portraits and fallback initials sit in paired photo mounts with the official optical mark between them. Narrow screens with large type stack names beside their photographs. Dates lead the content below the portrait spread and a saved memory has a captioned reading area. Existing repository, media decoding and notification callbacks still drive the screens.

## Native review

Reviewed two private fixture photos, a missing file, a missing partner, a very long name, dark Home and the dock across destinations. The first run exposed a partially obscured date action and a 68 px label in a 60 px tab. The revision brings the date action above the memory section and uses the condensed Android family for enlarged labels. Both checks then passed. Dark More also exposed inherited black text in its settings section; Phase 03 must correct the underlying inherited foreground.

- [destinations](evidence/destinations.jpg)
- [home-and-dock](evidence/home-and-dock.jpg)

## Verification

`./gradlew :app:testDebugUnitTest :app:assembleDebug` succeeded. The JUnit XML records 259 tests, 0 failures, 0 errors and 0 skipped. The incremental local debug APK is 28,201,551 bytes. Five distinct selectable tab nodes, their positions and widths at 320 dp and 200% type, tab restoration, Back, IME, photo pixels and real Home actions are covered in the existing suites.

## Changed files

- M · app/src/main/java/com/twohearts/app/ui/components/CouplePortrait.kt
- M · app/src/main/java/com/twohearts/app/ui/components/ProfileAvatar.kt
- M · app/src/main/java/com/twohearts/app/ui/navigation/AppShell.kt
- M · app/src/main/java/com/twohearts/app/ui/navigation/BottomNav.kt
- M · app/src/main/java/com/twohearts/app/ui/navigation/NavConfig.kt
- M · app/src/main/java/com/twohearts/app/ui/navigation/ShellLayout.kt
- M · app/src/main/java/com/twohearts/app/ui/screens/home/HomeScreen.kt
- M · app/src/test/java/com/twohearts/app/Phase2ShellNavigationTest.kt
- M · app/src/test/java/com/twohearts/app/TransformationNavigationTest.kt
- M · app/src/test/java/com/twohearts/app/TransformationNavigationUiTest.kt

## Continuation

Next implement Phase 03, the theme, interaction primitives, onboarding, Us and More. Handset, TalkBack and frame pacing require separate device evidence.

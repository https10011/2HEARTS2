# Stage 3 — Visual identity and design tokens

## Starting state

Stage 2 closed with verified native Room upgrades, passing Android packaging, and an Android 13 same-signer update that retained the test note. The latest documentation checkpoint at entry was `4e3d27c5365905e396eb845c0be5b4488fb79074`. Stage 3 establishes the palette, typography, and rendered reference used by the subsequent component and screen work. [Stage 2 report](../Stage-2/REPORT.md)

## Design decisions

- Warm ivory and cream define the light background and raised surfaces. Cocoa and plum supply distinct steps in dark mode. The core wine action color pairs with a white label in either theme. Dark rose remains available for text and focus where it has contrast against plum surfaces.
- Gold is reserved for relationship milestones. Rose and sage have separate decorative and text-safe tones. Existing success, warning, error, and information colors stay paired with dedicated surfaces. Visible field borders and focus indicators have a stronger role than hairline dividers.
- DM Serif Display anchors display headings and relationship numerals. Inter supplies regular, medium, semibold, and bold resources for interface and reading text. The fonts are direct upstream distributions bundled with their SIL Open Font License notices. Android supplies fallback glyphs through its font system. [DM Serif Display source](https://github.com/google/fonts/tree/5bd87dfc525c8a0d1d8529d3684716e00c3fc59a/ofl/dmserifdisplay) · [Inter 4.1 source](https://github.com/rsms/inter/releases/tag/v4.1)
- The native palette APIs keep their existing roles. The shared primary button reads the action fill and foreground tokens. The Material primary role remains suitable for interactive text, focus, and its existing caller patterns. The design does not redraw the official logo. Stage 5 owns the compiled brand and launcher resource presentation.

## Changed areas

`Tokens.kt` and `Theme.kt` provide the palette and semantic mappings. `Type.kt` binds the bundled fonts. The shared `Button.kt` consumes the new action roles. The existing Phase 0 Compose harness adds a palette and typography reference. It checks semantic text/background pairs, interactive contrast, and display/body overflow in light and dark at default and an effective 200% Compose font scale.

## Compose reference

The focused Robolectric/Compose tests passed. The reference rendered at 411 dp in both themes and at 360 dp with an effective font scale of 200%. The narrow renders show the headline, date, body text, action, and success state without clipped text. The tests checked 4.5:1 text contrast for the tested surfaces and semantic pairs, 3:1 for focus and interactive outlines, and display and body overflow. Real Android system font scaling across the full product stays in the Stage 20 and release matrix. [Light, dark, and enlarged renders](evidence/palette-and-type.jpg)

| Role | Light | Dark |
|---|---|---|
| Main background | `#F8F1E8` | `#201720` |
| Raised surface | `#FFFDFA` | `#352833` |
| Primary action fill | `#74213B` | `#AC5268` |
| Primary action text | `#FFFFFF` | `#FFFFFF` |
| Rose-tinted surface | `#F5E2E2` | `#3E2C39` |
| Milestone surface and ink | `#F6EBCF` and `#835D2A` | `#433323` and `#E2BE80` |

## Verification and checkpoint

- The full debug and release test variants each passed 139 tests with zero failures, errors, or skips. The production Home and Notes screens were also spot-checked after rendering with the bundled fonts.
- The debug APK assembled and passed APK v2 signature, ZIP alignment, and archive integrity checks. Its SHA-256 is `b0218adb534b7b4fa854c99431a56d2e0579d0b0e0612734e10aae08ee125ef3`. The package contains all five font files and both license notices.
- Android 13 startup and upgrade evidence is recorded in Stage 2. A handset has yet to run the new visual build. [Stage 2 runtime](../Stage-2/evidence/android-13-upgrade.txt)
- The stage source checkpoint `9fea116bd2f72e3033221189ac0b4192011f8feb` was verified on `master`. GitHub Actions passed and published the APK. [Actions](https://github.com/https10011/2HEARTS2/actions/runs/44479597056) · [APK](https://github.com/https10011/2HEARTS2/actions/runs/44479597056/artifacts/14508027272)

## Completion checklist

- [x] Record starting commit, objectives, design decisions, changed files, and dependencies.
- [x] Run debug and release tests, assemble the APK, and verify its packaged fonts and licenses.
- [x] Inspect the Compose reference in both themes, exercise 200% effective font scaling, and check contrast and overflow.
- [x] Exercise the production theme and existing navigation and persistence suites.
- [x] Document evidence and the remaining product-wide system font and physical-device checks.
- [x] Verify the exact stage commit and its successful CI run.

## Next stage

Stage 4 applies these roles to shared controls, semantic surfaces, and motion. It verifies input, focus, selection, loading, error, and feedback states across the existing UI.

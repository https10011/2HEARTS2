# Stage 4 — Shared components and motion

## Starting state and objectives

Stage 3 published the app palette and bundled typography at `9fea116bd2f72e3033221189ac0b4192011f8feb`. Both local suites passed 139 tests. GitHub's APK workflow passed for that exact SHA. Stage 4 applies the new roles to shared controls while preserving routes, inputs, and data behavior. [Stage 3 report](../Stage-3/REPORT.md) · [Actions](https://github.com/https10011/2HEARTS2/actions/runs/44479597056) · [APK](https://github.com/https10011/2HEARTS2/actions/runs/44479597056/artifacts/14508027272)

## Implementation

- Buttons distinguish the primary, disabled, and saving states. Keyboard focus uses an explicit outline on primary and icon controls. Fields use the same focus color and preserve their error labels and keyboard behavior.
- Notes, Period, and Vault use the same choice-chip layout. Places, Mood history, and Reminders share the selected tab treatment. The existing filter and tab callbacks still own selection and persistence.
- Shared cards clip interaction feedback to the visible surface and distinguish raised and accent tones. The shell headers preserve their centered layout with a clearer root-title hierarchy. Empty states use more of the available width for copy on small screens.
- Date and setup sheets use a native dialog window with a named pane and outside-tap and Back dismissal. The sheet body does not expose a fake click action. Confirmation dialogs use the raised surface. The shared loading state exposes indeterminate status, and inline status banners announce text changes.
- A single motion policy combines the in-app setting with Android's animator-duration setting. Existing loading, press, toast, and navigation effects consume that policy.

## Verification

- The first Compose renders showed that the new dialog window inherited Android's default density. They also showed a tab label splitting mid-word at 200% scale. The sheet now explicitly inherits the parent density and foreground color, and the selected tab gives its label more room. The final 360 × 640 dp render shows the full sheet and action at 200% scale. [Components and sheet](evidence/components-and-sheet.jpg)
- Native interaction tests verify selected-tab semantics, touch activation, disabled and saving states, field focus, and sheet dismissal by the scrim. The scrim's measured spacer keeps its touch area outside the sheet. The release variant uses an explicit Robolectric activity fixture so it can exercise the same tests without the debug test manifest.
- The light and dark Compose renders cover normal, disabled, saving, validation, loading and success states. The dark run turns Android animator duration off and verifies the static indicator and shared reduced-motion policy. Physical-device behavior remains part of release validation.
- Both debug and release ran 143 tests with zero failures, errors, or skips. The debug APK built, verified its v2 signature, and passed archive and alignment checks. SHA-256 `c263493026f98a7cd870d0ab3b13d3a5a03ee89ca2dc394d637eb874f42ad33f`.
- The stage source checkpoint is the commit adding this report. Verify its exact remote SHA and successful CI before the branding stage.

## Completion checklist

- [x] Record the starting commit, component scope, implementation decisions, known limits, and dependencies.
- [x] Pass full debug and release tests and assemble the checked APK.
- [x] Render and inspect representative light, dark, reduced-motion, and enlarged component states.
- [x] Verify selection, focus, touch, disabled and loading behavior, and the modal tap boundary through native Compose tests.
- [x] Preserve existing route callbacks and data behavior and record the remaining device checks.
- [ ] Verify the exact stage commit and CI.

## Next stage

Stage 5 resolves the Android launcher foreground, in-app logo and splash resources, verifies the compiled package, and checks launcher masks.

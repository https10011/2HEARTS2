# Stage 5 — Branding and Android resources

## Starting state and objectives

Stage 4 published shared controls and motion at `6830edb71912566a1130d77754a8b9ba97499b39`. Stage 5 improves the official interlocking hearts across the launcher, launch window, onboarding, and in-app branding. The existing adaptive icon referred to a resource name shared by an obsolete stock single-heart vector and the generated interlocking-heart PNG. The legacy square and round icons put the wine-colored mark on a similar burgundy background. The full lockup also lacked a light treatment for dark Compose surfaces. [Stage 4 report](../Stage-4/REPORT.md)

## Implementation

- The two supplied SVG files in `app/src/main/assets/branding` remain the source for every generated resource. They contain embedded image data and SVG masks and filters. Faithful Android conversion uses high-resolution transparent rasters. The Node and Chromium generator measures the visible alpha bounds, aligns the visual center, and resamples the artwork for each output size. [Generator](../../tools/generate-brand-assets.mjs)
- Standard and round adaptive icons share one padded foreground and an ivory field. Android 13 and later also use the alpha silhouette as the monochrome layer. Legacy densities use the corresponding round or rounded-square field with transparent edges. The old single-heart drawable was removed after auditing resource references.
- Android 12 and later use a platform splash resource with the same mark. The earlier launch-window theme uses a centered layer-list. Both use wine ink on ivory by day and ivory on plum by night with matching system-bar appearance. Compose `BrandLogo` follows the app's active scheme and keeps the explicit light-ink mode for dark accent surfaces. Android's starting window uses the system nighttime configuration.
- Package identity, launch activity, routing, and local storage paths retain their existing contracts. The changes introduce no new runtime libraries.

## Verification

- An isolated replay with Chromium 156 and Node 24 regenerated all 13 PNGs byte for byte. The compiled foreground is 576 px square with visible bounds centered at `(288, 288)`. Its visible radius is 30.4 dp in a 108 dp layer. This fits inside the 33 dp safe radius. The Robolectric resource check inflates both adaptive icons with their monochrome layers and draws each resolved splash variant. The contact sheet shows the resource renders alongside the legacy square and round fallbacks. [Android resources](evidence/android-icon-and-launch-resources.png)
- The foreground extracted from the assembled APK retains its visible pixels under simulated circle, squircle, rounded-square, and teardrop masks. The day and night splash ink and background have nominal contrast ratios of 9.26:1 and 16.58:1. [Packaged icon mask previews](evidence/packaged-foreground-mask-previews.png)
- Inspected native Compose renders at 411 dp in both themes and at 360 dp in dark mode at 200% font scale. The logo preserves its proportions and the ivory lockup stays legible on plum. At 200%, the welcome copy scrolls above the pinned Begin action. Stage 8 will revisit the welcome composition and scroll boundary as part of onboarding. [Welcome in both themes](evidence/welcome-light-and-dark.jpg) · [Welcome at 200%](evidence/welcome-dark-200-percent.jpg)
- Debug and release each passed 146 tests with zero failures, errors, or skips. The assembled debug APK passed v2 signature, ZIP integrity, and alignment checks. The APK identifies `com.twohearts.app` version `1.0.0` (1), min API 26, and target API 35. Its resource table contains one `ic_launcher_foreground` PNG, both launcher resources with five legacy densities and the API 26 and API 33 adaptive versions, and the API 31 splash theme. Local APK SHA-256 `977bd93f74ce6329bf1ee6140c1e4a00bd59733f419ea088b9318bd7c26db7fb`.
- The Android 13 software guest accepted the local same-signer update and resumed `MainActivity`. TwoHearts had no crash or process-exit entry during the check. A guest System UI “isn't responding” dialog and black captures from Launcher3, Settings, and TwoHearts prevented a live visual check. The compiled resource and Compose renders provide the visual evidence for this stage. Verify installed launcher masking and splash on a healthy guest or the TECNO handset during release validation. [Android 13 smoke record](evidence/android-13-branding.txt)
- Git verified `3acb3c54fc296424cc042b8916e8b917de98cbd6` on `origin/master`. Its exact Android workflow passed and uploaded the debug APK. [Commit](https://github.com/https10011/2HEARTS2/commit/3acb3c54fc296424cc042b8916e8b917de98cbd6) · [Actions](https://github.com/https10011/2HEARTS2/actions/runs/44515105097) · [APK](https://github.com/https10011/2HEARTS2/actions/runs/44515105097/artifacts/14525491594)

## Completion checklist

- [x] Audit the source artwork, production Compose consumers, conflicting foregrounds, adaptive and density resources, and platform themes.
- [x] Verify deterministic asset output, build resources, APK checks, and full debug and release tests.
- [x] Inspect the native branding renders and simulated launcher masks and record the runtime limitation and handset status.
- [x] Verify the exact remote commit, CI conclusion, and APK artifact.

## Next stage

Stage 6 renders the existing five destinations and prototypes Home, Us, Notes, and More. The decision depends on label legibility, discoverability, and preservation of navigation contracts.

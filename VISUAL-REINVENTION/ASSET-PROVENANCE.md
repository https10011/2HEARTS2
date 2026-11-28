# Visual assets

| Asset | Origin and rights | Local source | Usage and modifications |
|---|---|---|---|
| Official TwoHearts lockup and intertwined mark | Owner-supplied project artwork, retained for TwoHearts. No separate third-party redistribution grant assumed. | app/src/main/assets/branding/twohearts-logo.svg and twohearts-logo-mark.svg | Existing PNG output comes from tools/generate-brand-assets.mjs. Use the mark contour to prepare a density-independent startup plate. |
| Display and interface fonts | DM Serif Display and Inter contributors, SIL Open Font License. Full notices ship in assets/licenses. | app/src/main/res/font | Locally bundled headings and controls. |
| Yuki sprites and wardrobe | Project-supplied orange-tabby materials, usage tied to this product. | Yuki Assets/ and tools/generate-yuki-art.py | Preserve aligned poses, atlas, animation anchors and source evidence. |
| Baseline imagery | Native test scenes, synthetic fixture records and locally bundled demo imagery | app/src/test and Phase-00/evidence | Comparison only, outside the APK. |

Record each new visual source, attribution, redistribution terms, transformations and final location before bundling. Use programmatic native geometry for page folds and rules.

## Phase 01 artwork

twohearts_mark.xml is a monochrome vector derivative of the owner's official mark. Thresholding the canonical brand_logo_mark.png alpha mask at 128 produced the 429 × 334 optical frame. Potrace 1.16 (Peter Selinger, development tool on Debian, unbundled) traced the three filled contours with unit 1 and tolerance 0.55. The native path group preserves the source orientation. TransformationBrandingTest compares the compiled vector against that mask, including the holes between strokes. The existing full lockup remains available. twohearts_launch_mark.xml places the vector inside Android's splash plate.

The welcome cover's spine, foil lines and two corner stitches are original Compose geometry in WelcomeScreen.kt. The obsolete welcome image source remains byte-identical in Archive/Legacy-React-Vite-Capacitor/src/assets/images/onboarding-welcome-photo.svg, SHA-256 3b742d3f429f97fa919ae4e564b02e2a7394a974617b47a2e20ff85d4e5d5b6f. Its duplicate production SVG, generated PNG and generator target were removed.

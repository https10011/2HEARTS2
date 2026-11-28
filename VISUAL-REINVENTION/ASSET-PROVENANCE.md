# Visual assets

| Asset | Origin and rights | Local source | Usage and modifications |
|---|---|---|---|
| Official TwoHearts lockup and intertwined mark | Owner-supplied project artwork, retained for TwoHearts. No separate third-party redistribution grant assumed. | app/src/main/assets/branding/twohearts-logo.svg and twohearts-logo-mark.svg | Existing PNG output comes from tools/generate-brand-assets.mjs. Use the mark contour to prepare a density-independent startup plate. |
| Display and interface fonts | DM Serif Display and Inter contributors, SIL Open Font License. Full notices ship in assets/licenses. | app/src/main/res/font | Locally bundled headings and controls. |
| Yuki sprites and wardrobe | Project-supplied orange-tabby materials, usage tied to this product. | Yuki Assets/ and tools/generate-yuki-art.py | Preserve aligned poses, atlas, animation anchors and source evidence. |
| Baseline imagery | Native test scenes, synthetic fixture records and locally bundled demo imagery | app/src/test and Phase-00/evidence | Comparison only, outside the APK. |

Record each new visual source, attribution, redistribution terms, transformations and final location before bundling. Use programmatic native geometry for page folds and rules.

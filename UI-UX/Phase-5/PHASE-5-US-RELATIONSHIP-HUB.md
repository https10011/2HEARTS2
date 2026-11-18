# Phase 5 — Us relationship hub

The Us tab opens with the two profiles and, when available, their start date and days together. The nearest anniversary or saved important date appears in a gold panel. Our Story highlights the latest memory and timeline event. Our World shows the latest saved place, number of places, and today's mood when logged. The period tracker and Vault remain accessible under Private, with their contents kept inside their own screens.

The hub uses the same date recurrence logic and local repositories as Home and the feature screens. Android lifecycle-aware collection refreshes the hub when saved content changes. The featured memory opens directly, and its library and empty-state creation action use the existing routes. Phase 5 adds no storage schema.

## Design and accessibility

The portrait panel, upcoming date, and latest memory carry the visual emphasis. Divided rows organize the remaining content without a grid. Long names wrap within their portrait columns. Decorative icons are silent for screen readers, section titles are headings, and action rows have at least 48 dp of touch height. The hub scrolls within the shell's existing bottom inset.

The visual pass checked the empty and populated states with standard text, lower scroll content, and a 360 dp dark layout with extra-large text and long names. The first pass revealed uneven avatar alignment and repeated empty-state heart icons. The final layout aligns portraits from the top and uses profile silhouettes for missing names. [Compose captures](evidence/compose-review.jpg)

## Verification

- `./gradlew :app:testDebugUnitTest :app:assembleDebug` passed. All 87 tests passed.
- Phase 5 Compose tests opened the hub against Room, validated all seven existing feature destinations and the memory creation/detail routes, and verified that deleting the latest memory restored the first-memory action.
- The owner confirmed the existing app launches on the TECNO Spark 10 Pro. This confirmation preceded the Phase 5 build.
- Installed the Phase 5 APK over the existing Android 13 emulator data. An input timeout occurred during the first cold start under software emulation. The retry reached Home, opened Us with the persisted profiles and anniversary, entered Important Dates, and returned to Us with Android Back. The successful navigation produced no TwoHearts fatal entry in the crash buffer. [Android 13 screenshot](evidence/us-api33.png)

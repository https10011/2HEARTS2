# Phase 13 — Yuki’s room

Yuki’s room makes the supplied orange tabby the focus. The room frames everyday care with a warm window, rug and bowls. Eating, drinking, playing, grooming, petting and naps each use their own pose or sequence. The visible room alternates idle poses with pauses. Reduced motion uses still poses and retains the same activity, need and save feedback.

Food affects fullness, play spends energy, brushing improves cleanliness, petting improves mood, and a nap restores energy over time. The companion can be awakened at any time. The toy basket starts with a feather wand and unlocks a rolling ball at level two. The two toys use different energy and mood values. Previously saved levels, interaction totals and owned accessories are retained. New keepsakes use cumulative care and levels, so returning after time away keeps earned progress.

The original SharedPreferences key remains in use. Versioned fields record activity start and toy selection. Elapsed needs are bounded across clock changes and long absences. Changes persist after meaningful actions. The app observes them through a single ViewModel state and runs room animation while the route is visible. Yuki has no background simulation job or online dependency.

The repository contains 21 PNG poses, including numbering gaps. Gradle packages those originals from `Yuki Assets/`. The renderer removes the baked pale checker matte from edge-connected pixels at decode time, keeps interior fur, and retains a small cache of processed poses. The supplied PNGs are preserved byte for byte. The Yuki state remains outside the Notes and Reminders export.

## Verification

- Regression coverage includes the 21 packaged PNGs, matte removal with white fur retained, old save handling, invalid values, nap and wake across service instances, feeding limits, toy unlocks, and elapsed-time recovery.
- The full debug and release unit suites passed with 111 tests each. The debug APK assembled, and archive inspection confirmed all 21 original PNGs were present.
- Actual Compose scenes reviewed include resting, eating, napping, and playing. The two dark scenes use the 360 dp viewport with extra-large text. [Compose review](evidence/compose-review.jpg)
- Emulator lifecycle and animation smoothness on the owner’s handset remain pending.

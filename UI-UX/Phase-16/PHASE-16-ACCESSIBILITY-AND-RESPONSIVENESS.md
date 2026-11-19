# Phase 16 — Accessibility and responsiveness

Shared headers expose heading semantics. Text fields announce their label and validation message on the editable control. The full field surface is focusable, including its padding. Shared buttons, icon controls, and navigation tabs provide a minimum 48 dp target. The five navigation destinations expose tab roles and selection, with descriptive names independent of their compact visual labels.

The shell measures its navigation surface and gives content and feedback room above it. On narrow screens with enlarged text, the Us tab stays centered while neighboring labels gain space. The bar uses short labels where the full names do not fit. Touch feedback and state changes remain available when reduced motion is on.

Mood check-ins now use adaptive choices inside the screen’s own scroll view. The selected mood is announced, mood names stay readable, and the longer check-in has both the header action and a labeled save button beside its note field. Draft mood text survives configuration changes.

Floating actions use matched foreground and background colors. The review also corrected selected states, category labels, avatar initials, the center navigation mark, and gold date text.

## Verification

- Native Compose semantics checks cover headings, the editable field and its associated validation, selected tab role and state, and measured field and tab touch targets. Contrast tests measure text and field-outline pairs in both themes.
- Reviewed native Compose captures of the selected mood, optional note and save control, validation, and navigation at 320 dp. The navigation capture combines extra-large in-app text with an additional 1.3× font scale. [Compose review](evidence/compose-review.jpg)
- The debug and release unit suites and debug APK build passed. TalkBack, gesture routing, and system font behavior on the handset remain pending.

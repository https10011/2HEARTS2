TWOHEARTS — MASTER UI/UX REDESIGN DIRECTIVE

Post-Migration Product Experience, Visual Design, UX, Interaction, and Productization Specification

⸻

0. DOCUMENT PURPOSE

This document is the authoritative directive for the post-migration UI/UX transformation of TwoHearts.

The native migration is complete.

The application has already been migrated from the legacy React/Vite/Capacitor implementation into the native Android stack.

The purpose of this document is NOT to perform another migration.

The purpose is to transform the resulting native application into the intended finished TwoHearts product experience.

The current native application should be treated as the functional foundation upon which this UI/UX work will be performed.

The migration established the native architecture.

This directive establishes what the product should now look like, feel like, behave like, and communicate like.

The existing native implementation is not automatically considered the final UI/UX.

The current screens, layouts, spacing, hierarchy, navigation presentation, visual components, empty states, forms, cards, buttons, typography, Yuki presentation, and overall product composition may all be redesigned where necessary.

The goal is not to make the migrated application merely “prettier.”

The goal is to make TwoHearts feel like a deliberately designed, cohesive, emotionally intelligent, polished Android application rather than a technically functional collection of migrated screens.

⸻

1. AUTHORITATIVE REPOSITORY DOCUMENTATION

Before beginning ANY UI/UX phase, the agent MUST read and understand the repository documentation relevant to the work.

The entire:

Migration/

directory is authoritative historical and migration documentation.

The agent MUST inspect the Migration directory before beginning the first UI/UX phase.

The Migration directory contains the recorded history of:

* repository reconnaissance
* design system migration
* component migration
* data architecture
* core services
* onboarding
* navigation
* home
* relationship features
* content features
* settings
* Yuki
* games archival
* integration
* final build
* release preparation

Relevant documentation includes, but is not limited to:

Migration/TWOHEARTS-MASTER-AUDIT-AND-MIGRATION-ROADMAP.md

and the Stage documentation under:

Migration/Stage-0/

through:

Migration/Stage-15/

The agent should use those documents to understand:

* what was migrated
* why certain decisions were made
* what architecture exists
* what services exist
* what routes exist
* what screens exist
* what data models exist
* what limitations were recorded
* what was intentionally archived
* what was intentionally removed
* what the current native architecture expects

The migration documentation is not the visual design specification.

This document is.

The Migration documentation tells the agent:

“What exists and how we arrived here.”

This document tells the agent:

“What the finished product experience should become.”

⸻

2. YUKI REFERENCE

Yuki has its own dedicated product directive.

The agent MUST read:

Yuki Assets/Yuki Game Engine.md

before performing any phase that touches Yuki.

The agent MUST also inspect:

Yuki Assets/

and all of the supplied Yuki visual assets.

Yuki Assets/Yuki Game Engine.md is the authoritative specification for:

* Yuki’s personality
* Yuki’s gameplay
* Yuki’s behavior
* Yuki’s animation philosophy
* Yuki’s state
* Yuki’s progression
* Yuki’s assets
* Yuki’s virtual-pet experience
* Yuki’s interaction model
* Yuki’s technical/product boundaries

This UI/UX directive does NOT replace the Yuki directive.

The relationship is:

UI-UX.md
→ defines how Yuki should fit into the broader TwoHearts product experience.

Yuki Assets/Yuki Game Engine.md
→ defines what Yuki actually is and how Yuki should behave.

When working on Yuki, both documents must be understood.

Do not invent a new Yuki design that contradicts Yuki Game Engine.md.

⸻

3. POST-MIGRATION STATUS

The 15-stage native migration is complete.

Do not:

* create Stage 16 of the migration
* reopen the migration
* migrate legacy React screens again
* restore the legacy web stack
* recreate the old Capacitor architecture
* move the application backward toward webview rendering
* treat archived React/Vite files as the active application
* redesign the data architecture solely because the UI is changing

The active application is native Kotlin/Android/Jetpack Compose.

UI/UX work should be performed on this native foundation.

If a genuine implementation defect prevents UI/UX work, fix the defect as necessary.

Do not turn every visual task into an architecture rewrite.

⸻

4. THE CENTRAL PROBLEM THIS DIRECTIVE SOLVES

The current native application may be functional while still feeling visually and experientially incomplete.

The problem is not necessarily:

“the app is missing code.”

The problem is:

“the app does not yet feel like the product it is supposed to be.”

TwoHearts should not feel like:

* generic Android forms
* generic Material cards
* HTML/CSS panels translated into Compose
* database screens
* developer-created dashboards
* a collection of CRUD interfaces
* unrelated screens sharing a color
* a technical prototype
* a migration artifact

TwoHearts should feel like:

* a private relationship space
* intimate
* warm
* emotionally expressive
* polished
* calm
* personal
* cohesive
* intentional
* modern
* premium without pretending to be a luxury banking application
* playful when appropriate
* romantic without becoming visually cliché
* easy to use
* immediately recognizable as TwoHearts

⸻

5. CORE PRODUCT EXPERIENCE

TwoHearts is a private couples application.

The product exists around the relationship between two people.

The UI should therefore communicate:

“This is our space.”

It should not communicate:

“This is a generic productivity application with relationship-themed colors.”

The emotional tone should be:

* intimate
* warm
* affectionate
* comfortable
* cozy
* personal
* optimistic
* slightly playful
* mature enough to remain attractive over time

Avoid excessive:

* hearts everywhere
* floating hearts on every screen
* cliché romance graphics
* childish Valentine’s Day aesthetics
* excessive pink
* excessive gradients
* excessive decorative clutter
* fake luxury styling
* generic pastel app design

The product should feel like a real private application made specifically for a couple.

⸻

6. DESIGN PRINCIPLE: PRODUCT FIRST

Do not begin UI redesign by asking:

“What component should I use?”

Begin by asking:

“What is the user trying to accomplish?”

Then:

“What information matters most?”

Then:

“What should the user notice first?”

Then:

“What should the user be able to do immediately?”

Then:

“What emotional response should this screen create?”

Only after answering those questions should the implementation choose:

* cards
* buttons
* lists
* tabs
* sheets
* dialogs
* navigation
* forms
* animations

⸻

7. DESIGN PRINCIPLE: NO GENERIC DASHBOARDS

A major goal of this directive is to eliminate the feeling of generic dashboard UI.

Do not automatically represent every feature as:

* rounded rectangle
* icon
* title
* subtitle
* arrow

Repeated dozens of times.

This creates visual monotony.

Instead, use appropriate compositions.

Examples:

* counters can be visual
* memories can be image-led
* timeline can be chronological
* places can be map/photo-led
* mood can use expressive visualization
* reminders can prioritize urgency
* important dates can use calendar/event compositions
* Yuki should be character-led
* Notes should feel like personal writing
* Home should feel like a relationship dashboard, not an admin panel

Each feature should have its own visual language while remaining within the TwoHearts design system.

⸻

8. DESIGN PRINCIPLE: VISUAL HIERARCHY

Every screen must have a clear hierarchy.

The user should know:

1. Where they are
2. What this screen is about
3. What matters most
4. What they can do
5. What can wait

Do not make every element equally prominent.

Primary actions should be obvious.

Secondary actions should remain accessible without competing with the primary experience.

Destructive actions should be visually and interactionally separated.

⸻

9. DESIGN PRINCIPLE: CONTENT BEFORE CONTAINERS

Do not put content inside cards merely because a card exists in the component library.

A card should have a reason to exist.

If content looks better:

* directly on the background
* as a list
* as a visual block
* as a timeline
* as a floating control
* as an image composition

then use that approach.

Containers should support hierarchy, not create hierarchy artificially.

⸻

10. DESIGN PRINCIPLE: EMOTIONAL INFORMATION

TwoHearts should prioritize emotional information over technical metadata.

For example, on Home:

Instead of primarily showing:

Last updated: 2 hours ago

prioritize:

Together for 1 year, 8 months

or:

You have 3 memories from this month

or:

You have something waiting in Notes

The exact content should reflect real available data.

Do not invent data.

⸻

11. DESIGN PRINCIPLE: REAL CONTENT

UI should be designed around actual application state.

Avoid:

* fake statistics
* fake memories
* fake relationship dates
* fake names
* fake notifications
* fake Yuki state
* decorative placeholder content presented as real

Empty states should be intentionally designed.

When the user has no content, the screen should explain:

* what the feature is
* why it is useful
* how to start

An empty screen should not simply say:

“No data.”

⸻

12. DESIGN PRINCIPLE: EMPTY STATES ARE PART OF THE PRODUCT

Every major feature needs a designed empty state.

Examples:

Memories:

* explain that memories can be saved here
* provide a meaningful action to add one
* use appropriate visual atmosphere

Notes:

* communicate the personal nature of notes
* make creating the first note easy

Timeline:

* explain what the timeline represents
* provide an obvious first-event action

Places:

* explain that meaningful places can be saved
* provide an easy creation path

Reminders:

* communicate why reminders matter
* provide a direct creation action

Yuki:

* should never feel empty simply because the pet is in a default state

⸻

13. DESIGN PRINCIPLE: PERSONALIZATION

TwoHearts should feel like it belongs to the user and their partner.

Use personalization where data exists:

* names
* avatars
* relationship date
* anniversary
* memories
* places
* notes
* moods
* Yuki state

But do not force personalization into every screen.

Personal information should feel natural.

⸻

14. BRAND IDENTITY

The official TwoHearts logo is authoritative.

Use the centralized official branding implementation where available.

Do NOT:

* redraw the logo
* replace it with text
* create a duplicate logo
* create a similar-looking logo
* distort the official logo
* use inconsistent logo variants

The official logo should remain the primary brand identity.

⸻

15. COLOR DIRECTION

Burgundy is the primary emotional color direction.

The palette should be balanced.

Use burgundy as a meaningful accent and identity color rather than covering the entire application in dark red.

Supporting tones may include:

* cream
* ivory
* blush
* dusty rose
* muted pink
* warm beige
* soft plum
* muted mauve
* warm gray
* deep burgundy
* restrained gold accents

The palette should feel warm and romantic without becoming overwhelming.

Avoid:

* neon pink
* saturated red everywhere
* excessive purple
* excessive gradients
* random colors per screen
* inconsistent accent colors

⸻

16. COLOR SEMANTICS

Colors should have consistent meaning.

For example:

* primary action
* secondary action
* success
* warning
* destructive
* informational
* relationship accent
* background
* surface
* elevated surface

Do not use burgundy for every semantic purpose.

Destructive actions should remain distinguishable.

Success should not become indistinguishable from the brand color.

⸻

17. TYPOGRAPHY

Typography should feel:

* warm
* readable
* modern
* polished

Hierarchy should be deliberate.

Use:

* strong screen titles
* readable section headings
* comfortable body text
* restrained captions
* clear button labels

Do not make text unnecessarily tiny.

A previous product feedback concern was that some text felt too small.

Therefore:

Readability takes priority over fitting more information onto the screen.

Do not reduce typography simply to avoid wrapping.

⸻

18. TEXT SCALE

The UI must work with increased system font size.

Do not hardcode layouts that depend on one-line text.

Text should be allowed to:

* wrap
* expand
* reflow

where appropriate.

Buttons should accommodate larger text.

Important information should remain readable.

⸻

19. SPACING

Spacing should be consistent but not mechanical.

Use a coherent spacing system.

Avoid:

* elements touching each other
* giant unexplained gaps
* inconsistent padding
* every component having different margins

At the same time, do not make everything uniformly spaced to the point that the UI feels sterile.

Spacing should communicate grouping.

⸻

20. SHAPE LANGUAGE

TwoHearts may use rounded shapes, but avoid making every element a pill.

Use different shapes appropriately:

* rounded surfaces
* subtle cards
* pills for compact controls
* circular avatars
* floating controls
* image frames
* soft containers

A screen full of identical rounded rectangles is not a finished design.

⸻

21. ELEVATION AND SHADOW

Use elevation intentionally.

Avoid:

* every component having a shadow
* exaggerated shadows
* heavy floating cards
* plastic-looking surfaces

Depth should be subtle.

Some sections may be completely flat.

Others may use gentle elevation.

⸻

22. ICONOGRAPHY

Icons should be consistent.

Use the existing native icon system where appropriate.

Avoid mixing:

* Material icons
* random emoji
* unrelated icon packs
* inconsistent stroke weights

Icons should support comprehension.

Do not use an icon where a simple label is clearer.

⸻

23. IMAGERY

Images should feel integrated into the product.

For Memories:

images can be dominant.

For Places:

images can establish emotional context.

For Home:

imagery should remain selective.

Do not decorate every screen with random stock imagery.

All bundled visual assets must remain local.

No remote visual dependency should be introduced for core V1 functionality.

⸻

24. MOTION

Motion should communicate:

* state
* hierarchy
* continuity
* feedback
* personality

Motion should not exist merely because animation is possible.

Examples:

* screen transitions
* content appearance
* button feedback
* modal presentation
* Yuki movement
* progress feedback
* successful save
* deletion confirmation

Animations should generally be:

* smooth
* restrained
* purposeful

⸻

25. REDUCED MOTION

TwoHearts must respect reduced-motion preferences.

When reduced motion is enabled:

* reduce screen transitions
* reduce scaling
* reduce bouncing
* reduce decorative particles
* reduce unnecessary motion

Do not remove essential feedback.

⸻

26. TOUCH INTERACTION

Touch targets must be comfortably usable.

Avoid tiny controls.

Avoid placing destructive controls too close to primary controls.

Tap feedback should be immediate.

Where appropriate, support:

* tap
* long press
* swipe

But do not require gestures when a visible action is clearer.

⸻

27. FEEDBACK

Every meaningful action should provide feedback.

Examples:

* save
* create
* delete
* edit
* favorite
* complete reminder
* add memory
* log mood
* feed Yuki
* interact with Yuki

Feedback may be:

* animation
* state change
* toast/snackbar
* visual confirmation
* navigation
* subtle haptic feedback where appropriate

Avoid noisy feedback for trivial interactions.

⸻

28. FORMS

Forms should feel approachable.

Do not present every input as a technical database form.

Group related information.

Use appropriate:

* labels
* placeholders
* supporting text
* validation
* defaults
* previews

Validation should be clear and immediate enough to be useful.

⸻

29. CREATE VS EDIT

Creation flows should feel different from editing only where useful.

The user should always know:

* what they are creating/editing
* what fields matter
* how to save
* how to cancel
* whether changes are saved automatically

Do not create unnecessarily complicated multi-step forms.

⸻

30. DELETE ACTIONS

Deletion must be deliberate.

Important content such as:

* memories
* notes
* timeline events
* places

should have appropriate confirmation or undo behavior.

Do not make destructive actions visually identical to ordinary actions.

⸻

31. HOME — PRODUCT ROLE

Home should be the emotional entry point into TwoHearts.

Home should answer:

“What is happening in our little world right now?”

It should not be a generic list of every feature.

Potential content includes:

* relationship counter
* relationship greeting
* upcoming important date
* recent memory
* recent note
* reminder
* mood
* Yuki presence
* quick actions

The exact composition should be determined through design iteration.

⸻

32. HOME — PRIORITY

The Home screen should prioritize emotionally meaningful information.

The user should not have to scroll through administrative content before seeing what matters.

Do not overload Home.

It is acceptable for Home to have fewer elements if those elements are better presented.

⸻

33. HOME — GIRLFRIEND FEEDBACK

Previous feedback indicated that:

* Notes
* Reminders

should appear closer to the top rather than being buried around the recent-memory area.

This should be considered during Home redesign.

Do not blindly preserve the old ordering.

The Home hierarchy should reflect actual usefulness and emotional relevance.

⸻

34. RELATIONSHIP COUNTER

The relationship counter should feel special.

It should not look like a generic numeric statistic.

Consider:

* typography
* visual emphasis
* subtle animation
* relationship date context
* milestone awareness

The counter should communicate time together as something meaningful.

⸻

35. US / RELATIONSHIP HUB

The Us experience should feel like the heart of the relationship features.

It can provide access to:

* relationship information
* important dates
* shared memories
* places
* moods
* other relationship-oriented features

Do not make it simply another grid of feature buttons.

It should feel like a relationship hub.

⸻

36. MEMORIES

Memories are emotionally visual.

Prioritize:

* photos
* dates
* titles
* short descriptions
* emotional context

Avoid making Memories look like database records.

A memory should feel like a memory.

⸻

37. MEMORY DETAIL

Memory detail should allow the user to focus on the memory.

Reduce unnecessary chrome.

If a photo is present, let it breathe.

Metadata should be secondary to the actual memory.

Actions such as edit/delete should remain accessible but not dominate.

⸻

38. NOTES

Notes are personal content.

The experience should feel closer to personal writing than document management.

Avoid making Notes look like an enterprise notes application.

The user should be able to quickly:

* see notes
* create a note
* open a note
* edit a note
* organize notes where applicable

⸻

39. TIMELINE

Timeline should visually communicate chronology.

It should not simply be a vertical list of cards.

Use:

* dates
* visual continuity
* meaningful markers
* event emphasis

Important events should be distinguishable.

⸻

40. REMINDERS

Reminders should communicate:

* what needs attention
* when
* status
* importance

Previous product feedback indicates reminders deserve strong visibility.

Do not bury reminders beneath decorative content.

⸻

41. IMPORTANT DATES

Important dates should feel celebratory without becoming cluttered.

Possible categories:

* anniversary
* birthday
* personal milestone
* custom date

Upcoming events should be visually understandable.

Avoid overwhelming calendars if a simpler date presentation communicates the information better.

⸻

42. PLACES

Places should feel like shared memories rather than an address database.

Potential content:

* location
* name
* image
* date
* note
* emotional context

Visual representation should support the idea of “places that matter to us.”

⸻

43. MOOD

Mood should feel expressive.

Avoid reducing the experience to a clinical questionnaire.

The user should be able to quickly communicate:

* how they feel
* optionally why
* when

History should be visually understandable.

⸻

44. PERIOD TRACKING

The period feature should prioritize clarity, privacy, and usability.

Do not overdecorate.

Important dates and cycle information must remain readable.

Sensitive information should be handled privately.

Avoid confusing visualizations.

⸻

45. SETTINGS

Settings should be organized.

Do not present a giant unstructured list.

Group related options:

* appearance
* notifications
* privacy/security
* data
* profile
* relationship
* app information

Settings should be functional rather than visually over-designed.

⸻

46. PROFILE

Profile should feel personal.

Potential elements:

* avatar
* name
* personalization
* relationship role without forced gendered language

Use the product’s relationship-neutral terminology:

You

Your Special Someone

Do not hardcode “boyfriend” or “girlfriend” into the product UI.

⸻

47. ONBOARDING

Onboarding should feel like entering a private space.

Avoid:

* corporate welcome screens
* excessive forms
* long tutorials
* technical explanations

The onboarding should establish:

* what TwoHearts is
* who it is for
* personalization
* relationship setup
* privacy
* optional security

The user should reach the meaningful application experience quickly.

⸻

48. APP LOCK / PRIVACY

Privacy is important because TwoHearts may contain:

* private memories
* personal notes
* relationship information
* potentially private media

Privacy/security screens should feel trustworthy.

Do not use playful styling where seriousness is required.

⸻

49. NOTIFICATION CENTER

The notification center should not feel like a generic system log.

Notifications should be:

* understandable
* actionable
* visually organized
* prioritized

Avoid unnecessary notification noise.

⸻

50. SEARCH

Search should feel integrated.

Search results should clearly identify:

* what type of content was found
* title/content
* where it belongs

Avoid a generic list with no context.

⸻

51. MORE / UTILITY AREAS

Utility screens should not dominate the product.

The emotional core of TwoHearts should remain stronger than utility screens.

⸻

52. NAVIGATION

Navigation should be:

* predictable
* visually coherent
* easy to reach
* thumb-friendly
* consistent

The existing native navigation implementation can be redesigned where necessary.

The current floating-pill navigation should not be considered permanently final simply because it was migrated.

However, do not change navigation merely for novelty.

Any redesign should improve:

* discoverability
* hierarchy
* reachability
* consistency

⸻

53. NAVIGATION DEPTH

Avoid excessive navigation depth.

The user should not need:

Home → Hub → Category → List → Item → Menu → Action

for simple tasks.

Important actions should be reasonably close.

⸻

54. BACK NAVIGATION

Android back behavior must remain intuitive.

Do not override back behavior unpredictably.

Forms, dialogs, sheets, and nested screens should behave naturally.

⸻

55. MODALS AND SHEETS

Use modals/sheets only when appropriate.

Do not put entire screens into dialogs merely because it looks modern.

Use full screens for meaningful tasks.

Use sheets for contextual actions.

Use dialogs for focused decisions.

⸻

56. RESPONSIVE DESIGN

The UI should adapt to different Android screen sizes.

Do not design exclusively around one exact resolution.

Consider:

* narrow screens
* larger screens
* different densities
* system font scaling
* accessibility settings

The Tecno Spark 10 Pro is an important practical target, but the design should remain generally Android-compatible.

⸻

57. PERFORMANCE

UI polish must not destroy performance.

Avoid:

* huge image allocations
* excessive recomposition
* unnecessary animations
* expensive blur effects everywhere
* excessive shadows
* continuous background processing

A beautiful screen that stutters is not finished.

⸻

58. IMAGES AND MEDIA

Media-heavy screens should be optimized.

Use appropriate image loading and sizing.

Do not load full-resolution images into memory when a smaller representation is sufficient.

Preserve image quality where it matters.

⸻

59. OFFLINE-FIRST UI

The UI must communicate the offline-first nature naturally.

Do not introduce:

* cloud sync indicators
* online-only loading states
* network-dependent content
* fake synchronization

V1 functionality should work locally.

⸻

60. NO REMOTE VISUAL DEPENDENCIES

Core visual assets must be bundled with the application.

Do not depend on:

* remote image URLs
* CDN assets
* external icon servers
* online font downloads

The application should remain visually intact offline.

⸻

61. LEGACY REFERENCES

The archived legacy UI assets and 77 historical screen references may be used as historical context.

They are NOT binding.

The agent may inspect:

Archive/

to understand the previous product.

Do not blindly reproduce the old screens.

The native application should represent a deliberate evolution of TwoHearts.

⸻

62. HISTORICAL SCREEN REFERENCES

The old UI reference screens may help answer:

* what features existed
* what information was expected
* what visual concepts existed historically

They should not dictate:

* exact layout
* exact component structure
* exact spacing
* exact navigation
* exact colors

The new native UI should be better where possible.

⸻

63. COMPONENT LIBRARY

The existing Compose component library should be reused where it makes sense.

However:

Reusable components should not become visual constraints.

If a component is wrong for a particular experience:

* compose a better variant
* create a specialized component
* refactor the component

Do not force every screen into the same component template.

⸻

64. DESIGN SYSTEM

The existing token system is a foundation.

It should evolve as UI/UX work exposes genuine needs.

Tokens should cover:

* color
* typography
* spacing
* shapes
* elevation
* motion
* dimensions

Do not create random hardcoded values throughout screens.

⸻

65. DESIGN SYSTEM VS CREATIVITY

A design system provides consistency.

It should not eliminate creativity.

TwoHearts can have:

* visually expressive Home
* photo-led Memories
* timeline-oriented Timeline
* character-led Yuki
* calendar-oriented dates

while still sharing:

* typography
* color semantics
* interaction conventions
* spacing principles
* visual tone

⸻

66. SCREEN-SPECIFIC DESIGN

Every screen should answer:

* What is this?
* Why am I here?
* What is most important?
* What should I do?
* What happens next?

If those questions are not obvious, the screen needs UX work.

⸻

67. INFORMATION DENSITY

Do not maximize information density.

TwoHearts should have breathing room.

However, avoid enormous empty areas that force excessive scrolling.

Balance:

* content
* whitespace
* emotional atmosphere
* efficiency

⸻

68. SCROLLING

Scrolling should be intentional.

If a screen has only a few meaningful items, do not force the user to scroll.

If content is long, provide strong visual anchors.

Important content should not be buried unnecessarily.

⸻

69. MICROINTERACTIONS

Small interactions can make the application feel polished.

Examples:

* button press
* favorite toggle
* memory saved
* reminder completed
* counter milestone
* Yuki reaction
* successful import
* deletion confirmation

Microinteractions should remain subtle.

⸻

70. HAPTICS

Haptics may be used sparingly.

Potential uses:

* important confirmation
* successful action
* milestone
* meaningful interaction

Do not vibrate for every tap.

Respect system settings.

⸻

71. LOADING STATES

Loading should be rare because TwoHearts is local-first.

When loading is necessary:

* use meaningful skeletons or progress indicators
* avoid generic endless spinners
* avoid blocking the entire screen unnecessarily

⸻

72. ERROR STATES

Errors should explain:

* what happened
* whether data was saved
* what the user can do next

Avoid technical stack traces in user-facing UI.

Do not say:

“SQLite exception.”

Say something meaningful to the user.

⸻

73. SUCCESS STATES

Success should feel satisfying but not excessive.

Examples:

* memory saved
* note created
* reminder created
* Yuki fed
* milestone reached

Use subtle confirmation.

⸻

74. FIRST-LAUNCH EXPERIENCE

The first launch should feel intentional.

Do not expose unfinished-looking screens.

The first interaction should establish trust.

⸻

75. RETURNING USER EXPERIENCE

Returning users should be able to resume quickly.

Avoid forcing unnecessary onboarding or explanation.

The app should remember state.

Yuki should remember its state.

⸻

76. EMPTY DATABASE EXPERIENCE

A fresh installation must still look designed.

The absence of data should not expose the underlying database structure.

Empty states should feel like invitations.

⸻

77. DATA DENSITY IN LISTS

Lists should prioritize:

* title
* meaningful preview
* date
* important status

Do not show every database field.

⸻

78. DETAIL SCREENS

Detail screens should focus on the actual content.

Do not fill them with redundant metadata.

⸻

79. EDITING EXPERIENCE

Editing should preserve context.

Where appropriate:

* use familiar fields
* keep save action accessible
* prevent accidental loss
* validate input
* preserve existing values

⸻

80. MEDIA SELECTION

Media selection should feel integrated into TwoHearts.

After selecting media, show:

* preview
* crop/adjustment where appropriate
* clear confirmation
* ability to replace/remove

Do not force users through unnecessary steps.

⸻

81. RELATIONSHIP EMOTION WITHOUT CLICHÉS

TwoHearts can be romantic without making every screen look like Valentine’s Day.

Use:

* warmth
* typography
* photography
* subtle decorative elements
* meaningful copy
* burgundy accents
* soft surfaces

rather than constant hearts and roses.

⸻

82. DECORATIVE ASSETS

Decorative elements should support hierarchy.

Flowers, abstract shapes, subtle illustrations, and other decoration may be used.

Do not let decoration:

* cover content
* reduce readability
* create clutter
* make the app feel like a greeting card

⸻

83. FLOWERS

Rose/lily imagery may be used where appropriate because it is part of the emotional design history of TwoHearts.

However, flower decoration is not mandatory on every screen.

Use it selectively.

⸻

84. PERSONAL PHOTOGRAPHY

When users provide their own photos, the product should let those photos become the emotional focus.

Do not overwhelm personal photos with UI overlays.

⸻

85. AVATARS

Avatar presentation should feel personal.

Avatar selection should be clear.

The interface should support the relationship-oriented identity without forcing gender assumptions.

⸻

86. RELATIONSHIP LANGUAGE

Use relationship-neutral wording.

Preferred:

* You
* Your Special Someone
* Our
* Together
* Us

Avoid hardcoded:

* boyfriend
* girlfriend
* husband
* wife

unless the user explicitly enters such terminology as personal content.

⸻

87. COPYWRITING STYLE

UI text should be:

* concise
* warm
* human
* clear

Avoid:

* corporate language
* technical language
* excessive exclamation marks
* childish phrasing
* forced romance

⸻

88. BUTTON LABELS

Prefer meaningful actions.

Examples:

* Add Memory
* Save Note
* Add Reminder
* Log Mood
* Add Place
* Feed Yuki
* Play
* View Timeline

Avoid vague:

* Continue
* Submit
* Process
* Execute

unless context makes the meaning obvious.

⸻

89. ACCESSIBILITY AS DESIGN QUALITY

Accessibility is not a separate afterthought.

A polished TwoHearts experience should naturally support:

* readable typography
* clear contrast
* touch targets
* reduced motion
* screen readers
* large text
* clear focus
* non-color-dependent status

⸻

90. DARK MODE

Dark mode should be intentionally designed.

Do not simply invert the light palette.

Burgundy should remain attractive.

Cream/ivory surfaces should have appropriate dark equivalents.

Images and Yuki should remain visually coherent.

⸻

91. PERFORMANCE VS VISUAL EFFECTS

Avoid visual effects that look impressive in screenshots but perform poorly on actual devices.

Examples requiring caution:

* large blur layers
* continuous gradients
* excessive shadows
* animated background particles
* multiple simultaneous alpha animations

Use effects when they materially improve the experience.

⸻

92. PRODUCT COHESION

Every feature should feel like it belongs to TwoHearts.

Even if the visual composition differs, the user should recognize:

* typography
* color
* tone
* interaction patterns
* animation quality
* spacing philosophy

⸻

93. YUKI UI/UX INTEGRATION

Yuki is special.

Yuki should not simply be another item in a feature grid.

Yuki should have a dedicated experience.

Before changing Yuki UI, read:

Yuki Assets/Yuki Game Engine.md

Then inspect all Yuki assets.

Yuki should be treated as a character-led experience.

The Hub should prioritize:

1. Yuki
2. Yuki’s current state
3. primary interactions
4. activities
5. progression
6. inventory/environment
7. settings

Do not turn Yuki into a statistics dashboard.

⸻

94. YUKI ASSET USAGE

The assets inside:

Yuki Assets/

are supplied specifically for Yuki.

The agent should use them as the visual foundation.

Do not replace them with:

* generic icons
* placeholder cats
* unrelated illustrations
* remote assets

The agent may create animation transitions and procedural movement around these assets where appropriate.

⸻

95. YUKI HUB

The Yuki Hub should eventually feel like Yuki’s home.

The exact layout is intentionally open.

The design should emerge from:

* Yuki’s gameplay
* supplied assets
* behavior
* needs
* activities
* environment

Do not force the Yuki Hub into the same layout as Home.

⸻

96. YUKI SHOULD FEEL ALIVE

The Yuki UI should make the character the visual hero.

Avoid placing Yuki inside a tiny card.

Avoid reducing Yuki to an icon.

Avoid putting the character below a large statistics header.

The user should see Yuki immediately.

⸻

97. YUKI PHASE

The Yuki UI/UX work must explicitly reference:

Yuki Assets/Yuki Game Engine.md

and should be treated as a dedicated UI/UX phase rather than being mixed into unrelated screen redesign.

The Yuki engine directive remains authoritative for gameplay.

This document controls the UX presentation and integration into TwoHearts.

⸻

98. PHASED UI/UX ROADMAP

The UI/UX transformation should be performed incrementally.

Each phase must be completed and documented before proceeding to the next.

Do not combine all phases into one uncontrolled redesign.

Each phase should leave the repository in a stable state.

⸻

PHASE 0 — UI/UX RECONNAISSANCE AND BASELINE

Objective

Understand the current native application’s complete visual and interaction state before changing it.

This phase is not a redesign.

It is a complete UX/UI baseline.

Inspect:

* current navigation
* every major route
* every screen
* shared components
* design tokens
* typography
* colors
* spacing
* forms
* dialogs
* bottom navigation
* app shell
* onboarding
* Home
* Us
* content features
* relationship features
* settings
* utilities
* Yuki
* empty states
* loading states
* error states

Read all relevant files in:

Migration/

Read:

Yuki Assets/Yuki Game Engine.md

Inspect:

Yuki Assets/

Inspect the active native source.

Inspect archived UI references only where useful.

Create a baseline understanding of:

* what looks good
* what looks generic
* what looks inconsistent
* what feels unfinished
* what feels too dense
* what feels too empty
* what is hard to understand
* what is visually repetitive
* what is technically constrained

Do not perform major redesign work in Phase 0.

The purpose is to establish the starting point.

⸻

PHASE 1 — GLOBAL VISUAL LANGUAGE

Objective

Establish the visual foundation for the redesigned TwoHearts product.

Refine:

* colors
* typography
* spacing
* shapes
* elevation
* iconography
* motion
* surface treatment
* background treatment
* buttons
* common controls

The design system should support the emotional identity described throughout this document.

The result should no longer feel like generic Material UI with a burgundy color applied.

⸻

PHASE 2 — APP SHELL AND NAVIGATION UX

Objective

Redesign the global shell and navigation experience.

Evaluate:

* bottom navigation
* app header
* brand placement
* navigation hierarchy
* transitions
* back behavior
* floating actions
* global search
* notification access
* profile access

The existing floating-pill navigation may be retained, redesigned, or replaced based on actual UX evaluation.

Do not preserve it simply because it exists.

Do not replace it simply because it is familiar.

Choose based on usability and product identity.

⸻

PHASE 3 — ONBOARDING AND FIRST IMPRESSION

Objective

Redesign the first-launch experience.

Focus on:

* welcome
* identity
* relationship setup
* personalization
* privacy
* app lock
* completion

The experience should feel emotionally intentional.

Avoid unnecessary friction.

The user should understand the purpose of TwoHearts quickly.

⸻

PHASE 4 — HOME EXPERIENCE

Objective

Redesign Home as the emotional dashboard of TwoHearts.

Prioritize:

* relationship counter
* meaningful updates
* Notes
* Reminders
* important dates
* recent memories
* mood
* Yuki presence where appropriate
* quick actions

Do not overload Home.

Use hierarchy.

Remember the existing feedback that Notes and Reminders should be positioned more prominently.

Home should feel alive even with little data.

⸻

PHASE 5 — US / RELATIONSHIP HUB

Objective

Redesign the relationship-centered experience.

The Us area should feel like the heart of the couple’s shared space.

Focus on:

* relationship information
* important dates
* shared content
* places
* moods
* meaningful statistics
* navigation to deeper features

Avoid making it a feature directory.

⸻

PHASE 6 — MEMORIES EXPERIENCE

Objective

Make Memories feel emotional and visual.

Redesign:

* Memories home
* memory creation
* memory detail
* media presentation
* empty state
* editing
* deletion
* browsing

Prioritize personal media.

Avoid database-like layouts.

⸻

PHASE 7 — NOTES EXPERIENCE

Objective

Make Notes feel personal and intimate.

Redesign:

* Notes home
* note creation
* note editing
* note detail
* categories
* empty state
* search/filtering where appropriate

The user should feel like they are opening personal notes, not managing records.

⸻

PHASE 8 — TIMELINE EXPERIENCE

Objective

Make Timeline communicate the story of the relationship.

Focus on:

* chronological flow
* important moments
* dates
* event hierarchy
* event creation
* detail
* editing
* empty state

The timeline should visually communicate continuity.

⸻

PHASE 9 — REMINDERS AND IMPORTANT DATES

Objective

Redesign reminders and important dates.

Focus on:

* upcoming information
* urgency
* dates
* completion
* creation
* editing
* notification relationship
* empty state

Reminders should be useful without feeling like a productivity app.

Important dates should feel meaningful.

⸻

PHASE 10 — PLACES AND MOOD

Objective

Redesign Places and Mood.

Places should feel like shared memories.

Mood should feel expressive and human.

Avoid generic forms.

Use visual storytelling where appropriate.

⸻

PHASE 11 — PERIOD EXPERIENCE

Objective

Redesign the period feature for privacy, clarity, and usability.

Focus on:

* current state
* calendar
* history
* settings
* logging
* privacy

Do not over-decorate sensitive information.

⸻

PHASE 12 — SETTINGS, PROFILE, AND UTILITIES

Objective

Redesign:

* Settings
* Profile
* Appearance
* Notifications
* Storage
* Search
* Notification Center
* About
* Import/Export
* App Information

Utility areas should be clean and structured.

Do not over-design them.

⸻

PHASE 13 — YUKI EXPERIENCE

Objective

Transform Yuki into a complete character-led experience.

Before beginning:

READ:

Yuki Assets/Yuki Game Engine.md

INSPECT:

Yuki Assets/

including all supplied PNG assets.

Then inspect the existing Yuki implementation.

The Yuki experience should be redesigned around the game-engine specification.

Focus on:

* Yuki Hub
* character presentation
* animation
* primary interactions
* needs
* status
* activities
* environment
* inventory
* progression
* feedback
* navigation
* persistence presentation

Yuki must feel like a living companion rather than another TwoHearts screen.

The Yuki Game Engine directive defines the gameplay.

This UI/UX directive defines how that gameplay becomes a polished user experience.

⸻

PHASE 14 — CROSS-APP UX CONSISTENCY

Objective

After individual screens have been redesigned, review the entire application as one product.

Check:

* typography
* spacing
* buttons
* icons
* navigation
* transitions
* dialogs
* forms
* empty states
* errors
* success feedback
* loading
* accessibility
* dark mode
* reduced motion

Remove inconsistencies.

The application should feel like one product rather than independently designed screens.

⸻

PHASE 15 — EMOTIONAL POLISH AND MICROINTERACTIONS

Objective

Add the final layer of product polish.

Review:

* tap feedback
* transitions
* save feedback
* milestone feedback
* animations
* subtle decorative motion
* haptics where appropriate
* visual confirmations
* emotional moments

Do not add motion simply to make screenshots impressive.

Every interaction should have a reason.

⸻

PHASE 16 — ACCESSIBILITY AND RESPONSIVENESS

Objective

Perform a dedicated accessibility pass.

Test:

* large text
* reduced motion
* screen readers
* contrast
* touch targets
* dynamic content
* different screen sizes

Fix:

* clipped text
* overlapping controls
* unreadable labels
* inaccessible actions
* animation dependence

⸻

PHASE 17 — PERFORMANCE AND VISUAL OPTIMIZATION

Objective

Ensure visual improvements remain performant.

Review:

* image loading
* recomposition
* animation loops
* bitmap memory
* navigation
* scrolling
* startup
* long sessions
* Yuki animation
* media-heavy screens

Optimize only where justified.

Do not destroy visual quality through premature optimization.

⸻

PHASE 18 — FULL PRODUCT VISUAL QA

Objective

Perform a complete visual and UX walkthrough of the application.

Treat TwoHearts as a finished product.

Inspect every screen.

Look for:

* unfinished areas
* inconsistent spacing
* placeholder-like elements
* weak hierarchy
* generic cards
* unnecessary text
* awkward navigation
* visual bugs
* clipping
* poor empty states
* poor error states
* broken transitions
* weak animations
* inconsistent assets
* incorrect typography
* accessibility problems

This is a product-quality phase.

⸻

PHASE 19 — REAL-DEVICE VALIDATION

Objective

Validate the redesigned application on actual Android hardware.

Pay special attention to:

* Tecno Spark 10 Pro
* touch interaction
* scrolling
* animation
* image rendering
* keyboard behavior
* system font scaling
* app lifecycle
* notifications
* memory
* battery
* startup

Do not assume desktop/live preview is sufficient.

⸻

PHASE 20 — FINAL UI/UX CONSOLIDATION

Objective

Perform the final consolidation pass.

The application should now be treated as a complete product.

Check:

* all major screens
* all navigation
* all shared components
* all design tokens
* all Yuki integration
* accessibility
* performance
* persistence
* visual consistency
* emotional identity

Remove accidental inconsistencies introduced during earlier phases.

Do not introduce large new features here.

This phase is consolidation.

⸻

130. PHASE EXECUTION RULES

Every phase must be isolated.

A phase prompt should instruct the agent to:

* read this directive
* read the relevant Migration documentation
* inspect the current implementation
* perform only the specified phase
* test the phase
* document the phase
* commit
* push
* verify the remote state
* stop

Do not proceed into later phases automatically.

If Phase 3 reveals an issue belonging to Phase 8, document it for Phase 8 rather than silently performing Phase 8.

If a critical defect blocks the current phase, fix only what is necessary to unblock the current phase.

⸻

131. NO SCOPE CREEP

Do not turn a UI phase into:

* database migration
* architecture rewrite
* feature migration
* legacy restoration
* backend creation
* V2 development
* online synchronization
* unrelated feature development

UI/UX work may modify architecture where genuinely required for correct presentation.

It should not use UI as an excuse for unrelated refactoring.

⸻

132. AGENT CREATIVE AUTHORITY

The agent has permission to make creative UI/UX decisions within this directive.

The agent should not merely reproduce the old UI.

The agent should:

* interpret the product goals
* improve hierarchy
* improve visual composition
* improve interaction patterns
* create better layouts
* create specialized components
* improve empty states
* improve transitions
* improve visual storytelling

However, creative authority exists within the boundaries of this document.

Do not change:

* TwoHearts identity
* official logo
* offline-first V1 boundary
* relationship-neutral language
* Yuki’s core identity
* privacy expectations

without explicit product-level direction.

⸻

133. AGENT SHOULD NOT ASK FOR A SCREEN-BY-SCREEN DESIGN

The purpose of this directive is to provide enough design intent that the agent can make informed decisions.

The agent should not stop simply because there is no exact screenshot specifying:

“put this button at x=200, y=400.”

That is not the intended workflow.

The agent should infer layout from:

* information hierarchy
* product purpose
* emotional tone
* usability
* Android conventions
* supplied assets
* existing functionality
* this directive

⸻

134. HOWEVER — DO NOT INVENT PRODUCT FEATURES

Creative UI authority does not mean unlimited feature authority.

Do not invent major functionality simply because it would look good.

For example:

Do not add:

* social feeds
* public profiles
* online communities
* cloud synchronization
* advertisements
* monetization
* multiplayer games

unless explicitly requested in a future product directive.

Design the existing product better.

⸻

135. DESIGN REVIEW QUESTIONS

At the end of every meaningful UI/UX phase, ask:

Identity

Does this feel like TwoHearts?

Hierarchy

Can the user immediately see what matters?

Emotion

Does the screen communicate the intended emotional tone?

Usability

Can the user accomplish the main task easily?

Readability

Is the text large and clear enough?

Consistency

Does the screen belong to the same application?

Originality

Does it feel intentionally designed rather than copied from generic templates?

Performance

Does the design remain practical on the target device?

Accessibility

Does it remain usable with accessibility settings?

⸻

136. PRODUCT-WIDE ANTI-PATTERNS

Avoid these throughout the application:

* card grid overload
* tiny text
* excessive pills
* excessive gradients
* excessive shadows
* random decorative elements
* generic Material templates
* inconsistent icon styles
* unclear primary actions
* too many floating buttons
* redundant labels
* overly deep navigation
* giant headers that consume the screen
* cramped forms
* decorative clutter
* fake statistics
* meaningless animations
* excessive notifications
* overly cute romance styling
* clinical utility styling where emotion matters

⸻

137. THE “SCREENSHOT TEST”

Every redesigned screen should pass this conceptual test:

If someone saw only a screenshot of the screen without seeing the source code, would it look like a deliberately designed application?

Or would it look like:

* a developer prototype
* a CRUD application
* a generic Compose sample
* an HTML dashboard
* a collection of cards

If it looks like the latter, the screen needs more product design work.

⸻

138. THE “ONE-MINUTE TEST”

A new user should be able to open TwoHearts and within roughly one minute understand:

* what TwoHearts is
* where they are
* what the important areas are
* how to access their relationship content
* where Yuki lives
* how to perform a meaningful action

No manual should be required.

⸻

139. THE “RETURN USER TEST”

A returning user should be able to open TwoHearts and immediately see:

* something relevant
* relationship context
* actionable information
* recent activity
* Yuki where appropriate

The app should not feel static.

⸻

140. THE “EMPTY USER TEST”

A completely fresh installation should still look beautiful.

No memories.

No notes.

No reminders.

No places.

No timeline events.

No history.

Yuki at default state.

The app should still communicate what it can become.

⸻

141. THE “REAL USER TEST”

Do not optimize exclusively for development convenience.

A developer may tolerate:

* tiny buttons
* confusing menus
* redundant screens
* placeholder text
* technical terminology

A real user should not have to.

Always evaluate from the user’s perspective.

⸻

142. TWOHEARTS SHOULD FEEL LIKE A PLACE

This is one of the most important design principles.

TwoHearts should not feel like a toolbox.

It should feel like a private place the couple returns to.

The experience should gradually communicate:

Our relationship.

Our memories.

Our notes.

Our timeline.

Our places.

Our moments.

Our Yuki.

The word “Our” should be reflected in the product experience without becoming repetitive copy.

⸻

143. EMOTIONAL BALANCE

TwoHearts should balance:

Romantic + modern

Cute + mature

Personal + usable

Decorative + readable

Warm + clean

Playful + calm

Feature-rich + simple

The product should not become overwhelmingly cute.

It should not become sterile.

The correct balance is intentional warmth.

⸻

144. DESIGN MATURITY

TwoHearts should look like a product that someone intentionally designed over time.

That means:

* visual consistency
* meaningful hierarchy
* intentional spacing
* thoughtful empty states
* polished transitions
* appropriate typography
* coherent iconography
* appropriate imagery
* clear interaction
* responsive behavior
* accessibility
* performance

A color palette alone does not create a design system.

⸻

145. VISUAL QUALITY BAR

Before declaring a phase complete, compare the result mentally against:

* modern Android applications
* polished relationship/lifestyle applications
* high-quality companion applications
* professionally designed mobile products

Do not settle for:

“it works.”

The standard is:

“it works and feels intentionally designed.”

⸻

146. FUNCTIONALITY MUST BE PRESERVED

UI redesign must not accidentally break:

* CRUD
* persistence
* notifications
* search
* import/export
* app lock
* local storage
* media
* relationship state
* Yuki state
* navigation

After UI changes, verify underlying functionality.

⸻

147. UI SHOULD NOT CONTROL DATA LOGIC

Avoid putting data manipulation directly into visual components when existing architecture already provides services/repositories/ViewModels.

UI should communicate intent.

The underlying application layer should perform the operation.

⸻

148. NAVIGATION SHOULD NOT CONTROL GAMEPLAY

For Yuki especially, navigation should not contain gameplay logic.

Yuki gameplay belongs to the Yuki engine.

Navigation should only determine where the user is.

⸻

149. DOCUMENTATION AFTER EACH PHASE

Every completed phase should have documentation.

Documentation should include:

* phase completed
* files changed
* screens affected
* components affected
* design decisions
* UX decisions
* known limitations
* testing performed
* visual QA performed
* future considerations

Documentation should make the next phase easier.

⸻

150. GIT WORKFLOW

Each phase must finish with:

* working tree clean
* changes committed
* commit pushed
* local HEAD matching remote
* successful relevant verification

Do not leave half-finished work.

Do not leave unrelated changes.

Do not proceed automatically.

⸻

151. STOP CONDITION

After completing a phase:

STOP.

Do not begin the next phase.

Do not “helpfully” continue.

Do not redesign another section because it happens to be visible.

The next phase will be explicitly initiated by the user.

⸻

152. IF A PHASE DISCOVERS FUTURE WORK

Record it.

Do not automatically perform it.

For example:

Phase 4 Home redesign discovers that Yuki’s Hub needs improvement.

Record:

“Yuki Hub requires work in Phase 13.”

Then continue only with Phase 4.

⸻

153. IF A BUG BLOCKS THE CURRENT PHASE

Fix the minimum required defect.

Document it.

Do not expand the scope unnecessarily.

⸻

154. IF A CURRENT COMPONENT IS INADEQUATE

The agent may improve or replace it.

Do not preserve a poor component simply because it was created in Stage 2.

The component library exists to serve the product.

The product does not exist to serve the component library.

⸻

155. IF A LEGACY SCREEN WAS BETTER

It is acceptable to borrow conceptual ideas from the archived implementation.

However, do not blindly restore the legacy UI.

Use the old implementation as historical evidence, not as an automatic source of truth.

⸻

156. IF THE AGENT CANNOT VISUALLY PREVIEW THE APK

The agent should use whatever reliable visual-development capability is available, such as the native/live preview or development server workflow supported by the environment.

The inability to install an APK directly should not result in pretending visual QA was performed.

If visual validation is unavailable, document that limitation honestly.

⸻

157. VISUAL QA HONESTY

Do not claim:

“tested visually on device”

unless it was actually tested on device.

Do not claim:

“verified”

when only compilation was performed.

Distinguish:

* compiled
* unit tested
* previewed
* live-preview inspected
* emulator tested
* physical-device tested

⸻

158. FINAL PRODUCT GOAL

The ultimate goal of this directive is to transform TwoHearts from:

a completed native migration

into:

a completed native product experience.

The migration solved the technical foundation.

This directive solves the product presentation and user experience.

⸻

159. FINAL DESIGN PHILOSOPHY

TwoHearts should not try to impress the user by having the most features.

It should impress the user by making the features it already has feel meaningful.

A memory should feel like a memory.

A note should feel like a personal note.

A reminder should feel useful.

A place should feel meaningful.

A timeline should feel like their story.

A relationship counter should feel special.

A mood should feel expressive.

Yuki should feel alive.

Home should feel like their shared space.

Settings should disappear into the background when they are not needed.

The application should feel coherent.

⸻

160. FINAL YUKI PHILOSOPHY

Yuki deserves special treatment.

Yuki is not simply:

“the cat screen.”

Yuki is a character.

Before any Yuki UI/UX work:

READ:

Yuki Assets/Yuki Game Engine.md

INSPECT:

Yuki Assets/

USE:

the supplied Yuki assets as the visual foundation.

The Yuki experience should communicate:

“This is our little companion.”

The Yuki Hub should eventually become one of the most emotionally memorable parts of TwoHearts.

⸻

161. FINAL AUTHORITY HIERARCHY

When working on the UI/UX redesign, use this conceptual hierarchy:

Product identity

TwoHearts remains TwoHearts.

This document

Defines the overall UI/UX direction.

Yuki Game Engine

Defines Yuki-specific gameplay and behavior.

Migration documentation

Defines what was built and why.

Existing implementation

Provides the current technical foundation.

Archived legacy implementation

Provides historical context only.

This hierarchy prevents old implementation details from overriding the intended product direction.

⸻

162. FINAL PRINCIPLE

The application is now technically migrated.

That chapter is finished.

The next chapter is productization.

Do not treat this work as:

“make the Compose screens prettier.”

Treat it as:

“Design the final TwoHearts experience.”

The objective is for the finished application to feel:

private, warm, romantic, modern, polished, emotionally meaningful, intuitive, cohesive, and alive.

Every screen should have a reason to exist.

Every interaction should have a purpose.

Every visual element should support hierarchy.

Every animation should communicate something.

Every feature should feel like it belongs.

And the entire application should ultimately feel like a private little world created for two people.

That is the standard for all UI/UX work performed under this directive.

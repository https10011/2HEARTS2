
YUKI GAME ENGINE

Authoritative Yuki Product, Gameplay, Behavior, Asset, and Experience Directive

---

1. PURPOSE OF THIS DOCUMENT

This document defines the intended product, gameplay, behavior, interaction model, visual direction, progression philosophy, technical boundaries, and long-term vision for Yuki, the virtual companion system inside TwoHearts.

This document belongs in:

"Yuki Assets/Yuki_Game_Engine.md"

It is an authoritative design and product directive for Yuki.

Any future developer, coding agent, design agent, or AI agent working specifically on Yuki should read and understand this document before making substantial changes to the Yuki system.

This document is intentionally independent from any particular implementation.

The Kotlin/Android/Jetpack Compose implementation may change over time. Screens may be redesigned. Data structures may be refactored. Animation systems may be replaced. Components may be rebuilt.

The underlying Yuki experience and product intent described here should remain the source of truth unless this document itself is deliberately revised.

This document is not a migration stage.

The TwoHearts native migration has already been completed.

Yuki should therefore be treated as a native product subsystem that is now being developed, refined, expanded, and polished within the completed native TwoHearts application.

Do not restart, repeat, or extend the old React/Vite/Capacitor migration process.

---

2. WHAT YUKI IS

Yuki is the virtual companion cat of TwoHearts.

Yuki is intended to feel like a small living companion inside the app rather than a decorative mascot.

The user should be able to open TwoHearts and feel that Yuki has its own presence, state, personality, reactions, activities, and progression.

Yuki should be:

- cute
- expressive
- cozy
- playful
- emotionally responsive
- visually polished
- persistent
- offline-first
- lightweight
- approachable
- rewarding without being addictive in a manipulative way
- simple enough to understand immediately
- deep enough to remain interesting over time

Yuki is an orange cat.

Yuki is not intended to become a generic RPG character.

Yuki is not intended to become a full social network.

Yuki is not intended to become an online multiplayer game.

Yuki is not intended to require a server.

Yuki is not intended to replace the core relationship functionality of TwoHearts.

Yuki exists as a companion experience within TwoHearts.

The ideal feeling is:

«"Yuki is our little cat inside TwoHearts."»

The system should make the user want to check on Yuki because Yuki is enjoyable to interact with, not because the application is punishing the user for leaving.

---

3. RELATIONSHIP TO TWOHEARTS

Yuki is part of TwoHearts but should remain architecturally and conceptually self-contained.

Yuki may integrate with the rest of TwoHearts where doing so provides genuine value.

Examples include:

- the TwoHearts theme
- the official TwoHearts branding
- local notifications
- relationship milestones
- reminders
- settings
- app-wide navigation
- local storage
- app lock/privacy
- accessibility settings
- reduced-motion preferences
- system theme preferences

However, Yuki should not unnecessarily couple its internal gameplay logic to unrelated TwoHearts features.

Yuki should be capable of functioning as a coherent subsystem.

A change to Memories should not require rewriting Yuki.

A change to Notes should not break Yuki.

A future redesign of the TwoHearts Home screen should not require rebuilding Yuki's core simulation.

Yuki's game state, behavior, progression, inventory, and gameplay logic should have clear boundaries.

---

4. CORE DESIGN PHILOSOPHY

Yuki should follow five primary principles.

4.1 Yuki should feel alive

Yuki should not constantly stand in exactly the same position.

Yuki should have:

- idle variations
- blinking
- breathing
- subtle movement
- looking behavior
- reactions
- transitions
- activity states
- emotional states
- occasional spontaneous behaviors

Even when the user does nothing, Yuki should feel like a character rather than an image.

However, animation should remain subtle.

Constant motion is not the goal.

Life is the goal.

---

4.2 Yuki should reward care

Interacting with Yuki should have meaningful consequences.

Feeding Yuki should feel different from doing nothing.

Playing with Yuki should affect Yuki.

Petting or affection should affect Yuki.

Resting should matter.

Cleaning should matter if cleanliness is part of the final system.

The player should be able to understand that their actions affect Yuki.

---

4.3 Yuki should never become a chore

Yuki should not require constant maintenance.

The player should not feel punished because they were busy with school, work, sleep, travel, or other activities.

Stat decay should be gradual.

Consequences should be gentle.

Long absences should be handled gracefully.

Yuki can acknowledge that the user was away, but should not aggressively punish them.

Avoid mechanics such as:

- severe stat depletion after a few hours
- permanent loss caused by inactivity
- forced daily login requirements
- artificial urgency
- excessive notification spam
- energy systems designed purely to restrict play
- monetization-style timers
- manipulative streak mechanics

Yuki should be a comfort feature.

---

4.4 Yuki should be easy to understand

The player should not need to read a manual to understand the basics.

The first interaction should naturally communicate:

- who Yuki is
- what Yuki needs
- what the user can do
- what Yuki is feeling
- what actions are available

Advanced mechanics can be discovered gradually.

---

4.5 Yuki should have room to grow

The first implementation does not need every imaginable feature.

The architecture should nevertheless avoid painting the system into a corner.

Yuki may eventually support:

- additional activities
- additional animations
- more food
- toys
- furniture
- environments
- cosmetic items
- personality variations
- milestones
- achievements
- special events
- seasonal content
- additional interactions
- additional behavioral states

Future possibilities should not require rewriting the entire Yuki engine.

---

5. AUTHORITATIVE YUKI ASSETS

The repository contains a dedicated:

"Yuki Assets/"

directory.

This directory contains the visual resources supplied specifically for Yuki.

The current asset collection includes approximately 22 PNG fundamental assets.

These assets are extremely important.

They represent the authoritative visual foundation for Yuki.

They should not be casually replaced.

They should not be discarded simply because a different implementation technique is more convenient.

They should not be replaced with generic placeholder cats.

They should not be replaced by unrelated stock artwork.

They should not be replaced by AI-generated artwork that changes Yuki's identity without deliberate approval.

---

6. FUNDAMENTAL ASSETS VS ANIMATION

The supplied assets are not necessarily expected to represent every animation frame.

The 22 assets are the fundamental visual building blocks.

The implementation may create motion between them.

For example, a supplied:

"idle"

pose may be animated through:

- subtle vertical movement
- breathing
- slight scaling
- tiny head movement
- blinking
- tail movement if appropriate
- timing variation

A supplied:

"walk"

pose may be used to construct:

- walking cycles
- movement across the environment
- transitions into and out of walking
- directional movement where supported

A supplied:

"run"

pose may become part of a faster movement sequence.

A supplied:

"eat"

pose may be used during the eating state with timing, effects, food interaction, and reactions.

The goal is to use the supplied art intelligently.

The existence of only a limited number of fundamental PNGs does NOT mean Yuki must have limited animation.

The engine may create animation through:

- frame sequencing
- interpolation
- translation
- scaling
- rotation
- easing
- alpha transitions
- procedural motion
- timing variation
- particle effects
- screen effects
- state transitions

provided these techniques preserve the visual integrity of Yuki.

---

7. ASSET INTEGRITY

Yuki's proportions and visual identity must remain consistent.

Do not:

- stretch Yuki unnaturally
- squash Yuki excessively
- distort the face
- change the color palette arbitrarily
- introduce inconsistent art styles
- mix low-quality assets with polished assets
- place Yuki in environments that visually clash with the character
- use unrelated cat artwork as replacements

Animation should enhance the artwork rather than compensate for poor asset handling.

Transparency should be preserved where the source assets support it.

PNG assets should generally remain PNG where transparency is required.

JPEG assets may be used for backgrounds or other artwork where transparency is not required.

Do not convert assets merely for the sake of conversion.

---

8. YUKI'S VISUAL IDENTITY

Yuki is an orange cat.

The visual personality should be:

- soft
- cute
- expressive
- rounded
- approachable
- warm
- polished
- slightly playful
- cozy

The visual experience may use:

- soft shading
- subtle highlights
- rounded forms
- gentle shadows
- restrained particles
- warm backgrounds
- cozy environmental details
- expressive eyes
- subtle motion

The visual direction can be inspired by the general feeling of cozy virtual-pet and companion games.

It must remain original.

Do not copy another game's character design or interface.

---

9. YUKI'S PERSONALITY

Yuki should have a recognizable personality.

Yuki should not behave as though every action is a mathematical state transition.

Personality should influence how Yuki behaves.

Possible personality expressions include:

- curiosity
- affection
- playfulness
- laziness
- excitement
- sleepiness
- hunger
- satisfaction
- mild annoyance
- shyness
- attention-seeking
- calmness
- energetic behavior

These should be expressed through behavior and animation rather than constant text.

For example:

A hungry Yuki may:

- look toward the food area
- appear less energetic
- react strongly when food is presented

A happy Yuki may:

- perform a playful idle
- move more energetically
- react positively to interaction

A sleepy Yuki may:

- yawn
- blink slowly
- move less
- eventually sleep

A curious Yuki may:

- look around
- investigate an object
- perform a special idle

Personality should feel organic.

---

10. BEHAVIOR STATE MACHINE

Yuki should have an explicit internal behavioral model.

Potential states include:

- IDLE
- BLINKING
- LOOKING
- WALKING
- RUNNING
- PLAYING
- EATING
- DRINKING
- GROOMING
- STRETCHING
- SLEEPING
- HAPPY
- SAD
- EXCITED
- CURIOUS
- TIRED
- HUNGRY
- AFFECTIONATE
- INTERACTING
- SPECIAL_ACTIVITY

The exact list may evolve.

Do not create states merely for the sake of having many states.

Each state should exist because it produces meaningful behavior.

The engine should know:

- what state Yuki is currently in
- what triggered the state
- how long the state should last
- what states can follow it
- whether the user can interrupt it
- what animation represents it
- what stats are affected
- whether it has cooldowns

---

11. IDLE BEHAVIOR

Idle behavior is one of the most important parts of Yuki.

Yuki should not simply remain motionless when the user is looking at the screen.

The engine should provide controlled idle variation.

Possible idle behaviors:

- normal idle
- blink
- slow blink
- look around
- small stretch
- yawn
- sit
- subtle bounce
- curious look
- playful movement
- grooming
- tail movement
- short walk
- return to position
- spontaneous interaction with the environment

Idle behavior should be weighted.

Some actions should happen frequently.

Others should be uncommon.

Rare behaviors can create a feeling of discovery.

Do not make rare behavior so rare that the user never sees it.

---

12. CONTROLLED RANDOMNESS

Random behavior should be deterministic enough to feel intentional but varied enough to prevent repetition.

Use:

- weighted probabilities
- cooldowns
- state requirements
- contextual conditions
- recent-action memory

Do not randomly choose any animation at any time.

For example:

Yuki should not immediately fall asleep after running.

Yuki should not play if energy is critically low.

Yuki should not repeatedly perform the same special animation five times in a row.

Yuki should not continuously blink as though the animation were broken.

The behavior engine should avoid obvious repetition.

---

13. CORE NEEDS

Yuki may have a collection of needs.

Candidate needs include:

Hunger

Represents how much Yuki needs food.

Feeding improves hunger.

Hunger should gradually decline over time.

Hunger should influence:

- behavior
- energy
- mood
- food reactions

Energy

Represents Yuki's available energy.

Energy decreases through:

- running
- playing
- active activities

Energy recovers through:

- resting
- sleeping
- inactivity

Happiness

Represents Yuki's general mood.

Happiness can improve through:

- playing
- affection
- satisfying needs
- positive interactions
- discovering activities

Happiness should not decline rapidly simply because the player closes the app.

Cleanliness

Only implement this if it contributes meaningful gameplay.

If implemented, cleaning should be a positive interaction rather than a punishment mechanic.

Affection

Represents the relationship between the user and Yuki.

Affection can increase through:

- interaction
- petting
- playing
- feeding
- spending time together

Affection should provide meaningful feedback.

Health / Wellbeing

If implemented, health should primarily represent overall wellbeing.

Do not turn Yuki into a medical simulator.

Avoid overly complex health systems.

The player should understand what to do when Yuki's wellbeing is low.

---

14. STAT DESIGN RULE

Every stat must answer:

«Why does this exist?»

If a stat does not create interesting gameplay, remove it.

Avoid creating ten different bars simply because a virtual pet traditionally has many meters.

A small number of meaningful systems is better than a large number of shallow systems.

---

15. STAT RANGES

Stats should use a consistent internal model.

A normalized 0–100 range is recommended unless there is a strong reason to use another system.

Possible interpretation:

- 0–20 = critical/very low
- 21–40 = low
- 41–60 = normal
- 61–80 = good
- 81–100 = excellent

These thresholds are conceptual rather than mandatory.

The visual UI should not overwhelm the user with numbers.

Whenever possible, communicate state through:

- icons
- subtle indicators
- Yuki's behavior
- short descriptions
- visual feedback

Numbers may exist for precision but should not dominate the experience.

---

16. FEEDING

Feeding should be one of Yuki's primary interactions.

Food should have:

- an identity
- an effect
- appropriate visual feedback
- an animation or reaction
- sensible cooldown or quantity rules if necessary

Feeding may affect:

- hunger
- happiness
- affection
- energy in limited circumstances

Food should not create an infinite stat exploit.

If inventory exists, food quantities should persist.

Eating should feel like an actual interaction.

Potential sequence:

1. User selects food.
2. Yuki notices.
3. Yuki reacts.
4. Food appears or is presented.
5. Yuki moves into eating behavior.
6. Eating animation plays.
7. Appropriate effects occur.
8. Yuki reacts to being satisfied.
9. State updates.
10. Yuki returns to an appropriate state.

The sequence should be interruptible where appropriate.

---

17. DRINKING

If drinks are part of the final design, drinking should be treated as its own interaction rather than simply another food button.

Yuki should have a distinct reaction and animation where available.

Do not add drinks merely to increase menu complexity.

---

18. PLAY

Play should be a major source of interaction.

Playing may affect:

- happiness
- affection
- energy
- progression

Potential play activities include:

- toy interaction
- chasing
- simple reaction games
- object interaction
- playful animations
- short activities

The user should not need to navigate through several screens for a one-second interaction.

Play should feel immediate.

---

19. TOYS

Toys can become collectible or unlockable items.

Possible toy categories:

- ball
- feather
- plush
- string
- box
- small interactive objects

Toys should not all behave identically.

Different toys may produce different reactions.

Some may be:

- energetic
- calming
- funny
- rare
- special
- cosmetic

Do not introduce a large inventory simply to create artificial depth.

---

20. GROOMING

Grooming may include:

- brushing
- cleaning
- bathing where appropriate
- simple care interactions

The interaction should be visually satisfying.

Do not make grooming unpleasant or tedious.

---

21. SLEEP

Sleep should be a meaningful Yuki state.

Yuki should be able to become sleepy.

When sleeping:

- movement should decrease
- animation should become calm
- energy should recover
- the environment may become quieter
- visual presentation may change subtly

The player should be able to understand that Yuki is sleeping.

Do not make sleep a hard lock that prevents the user from interacting with Yuki for an arbitrary amount of time.

The user should still be able to observe Yuki.

---

22. MOVEMENT

Yuki should be able to move naturally where the visual environment supports it.

Potential movement:

- walking
- running
- approaching the user interaction point
- moving toward food
- moving toward toys
- returning to a resting position

Movement should use easing.

Avoid robotic linear movement.

Use appropriate acceleration/deceleration.

Yuki should not teleport between states unless a teleportation effect is intentionally designed.

---

23. ANIMATION ENGINE

Animation should be state-driven.

The animation system should know:

- current behavior
- current emotion
- current activity
- current asset
- transition state
- animation duration
- loop status
- interruption rules

Animations should have:

- appropriate easing
- sensible duration
- intentional timing
- no unnecessary CPU usage

Use frame-based animation where the assets support it.

Use Compose/Android animation APIs where procedural movement is sufficient.

Do not create expensive rendering loops unnecessarily.

---

24. ANIMATION PRIORITY

When multiple animations could happen simultaneously, the system should have priorities.

Example conceptual hierarchy:

1. Critical interaction
2. User-triggered action
3. Major activity
4. Emotional reaction
5. Special behavior
6. Environmental behavior
7. Idle behavior

A user action should generally interrupt an idle animation.

A sleeping animation should not randomly be interrupted by a minor idle event.

A major activity should have appropriate continuity.

The exact priority model may evolve.

---

25. USER INTERACTION

The user should be able to interact with Yuki naturally.

Possible interactions:

- tap
- pet
- feed
- play
- give item
- clean
- observe
- interact with environment

Tapping Yuki should not always produce exactly the same response.

Yuki can sometimes:

- look at the user
- blink
- react happily
- become curious
- move slightly
- produce a small animation
- occasionally ignore the interaction if busy or sleeping

Do not make Yuki feel broken by ignoring too many interactions.

---

26. EMOTIONAL RESPONSE

Yuki should visibly react to the user's actions.

Examples:

Positive action:

- Yuki reacts positively
- animation changes
- happiness/affection may increase

Poorly timed action:

- Yuki may show mild annoyance
- but should not become hostile

Long absence:

- Yuki may acknowledge the return
- but should not guilt-trip the user

Full hunger:

- Yuki may show excitement about food

Full energy:

- Yuki may become more playful

Low energy:

- Yuki may become sleepy

This creates emotional continuity.

---

27. THE YUKI HUB

Yuki needs a dedicated overall Hub experience inside TwoHearts.

The Hub should be treated as Yuki's home rather than simply another generic app screen.

The exact final layout is intentionally not locked by this document.

The Hub should be designed around Yuki's gameplay.

It should likely provide access to:

- Yuki itself
- Yuki's current state
- primary interactions
- care actions
- play
- inventory
- progression
- environment
- settings

The Hub should make Yuki immediately visible.

The character should be the focal point.

Do not bury Yuki underneath excessive menus.

Do not create a dashboard consisting entirely of cards and numerical statistics.

Yuki should remain the visual center.

The Hub should feel like a place.

---

28. ENVIRONMENT

The Yuki experience may eventually include an environment such as:

- room
- home
- cozy space
- garden
- play area
- sleeping area

The environment should support Yuki's identity.

The environment can contain:

- bed
- food area
- toys
- decorations
- furniture
- plants
- small objects

Environment elements should serve gameplay or atmosphere.

Avoid decorative clutter that makes Yuki difficult to see.

---

29. INVENTORY

If implemented, Yuki's inventory should remain simple.

Possible categories:

- food
- drinks
- toys
- care items
- cosmetics
- special items

Inventory should be locally persisted.

Items should have:

- unique identifiers
- names
- descriptions
- quantities where applicable
- effects
- optional rarity/category

Do not introduce an unnecessarily complicated item economy.

---

30. PROGRESSION

Yuki should have long-term progression.

Progression can come from:

- affection
- milestones
- interactions
- activities
- discoveries
- unlocks
- achievements
- environmental upgrades

Progression should make the player feel:

«"Yuki is becoming more complete because we spend time with Yuki."»

Progression should not primarily mean:

«"Grind the same action 500 times."»

Avoid excessive grinding.

---

31. LEVELS

A level system is optional.

If levels are used, they should communicate meaningful progress.

A level should ideally unlock:

- new behavior
- new toy
- new food
- new environment item
- new interaction
- special animation
- cosmetic option
- milestone

Do not add levels solely because games traditionally have levels.

---

32. REWARDS

Rewards should reinforce interaction.

Possible rewards:

- items
- toys
- food
- decorations
- cosmetics
- animations
- special behaviors
- milestones

Rewards should not require real money.

Yuki is an offline-first private TwoHearts feature.

Avoid designing Yuki around monetization.

---

33. ACHIEVEMENTS AND MILESTONES

Milestones can provide long-term goals.

Examples:

- first feeding
- first play session
- first week together
- number of interactions
- discovering a rare behavior
- caring for Yuki consistently
- unlocking a special item

Milestones should feel celebratory.

They should not pressure the user.

---

34. DAILY CONTENT

Daily activities are optional.

If implemented, they should be lightweight.

Examples:

- daily interaction
- small challenge
- random behavior
- special food
- daily discovery

Do not make daily content mandatory.

Do not punish missed days.

Do not create artificial streak anxiety.

---

35. RANDOM EVENTS

Yuki may occasionally perform special events.

Examples:

- unusual idle
- rare animation
- discovering an object
- playful moment
- sleeping in an unusual location
- reacting to a toy
- special emotional animation

Rare events can make the companion feel less predictable.

However, rarity should be balanced.

The player should eventually see meaningful variety.

---

36. NOTIFICATIONS

Yuki may eventually integrate with TwoHearts' local notification system.

Potential notifications:

- Yuki is awake
- Yuki has finished resting
- a gentle reminder to check Yuki
- a special event occurred

Notifications must be:

- local
- optional
- configurable
- non-spammy

Do not send constant notifications.

Do not make the user feel obligated to return.

Yuki should remain enjoyable even when notifications are disabled.

---

37. OFFLINE-FIRST REQUIREMENT

Yuki must remain fully functional without internet access.

Yuki must not depend on:

- Firebase
- Supabase
- cloud databases
- cloud storage
- remote authentication
- online APIs
- remote image hosting
- remote animation assets
- server-side simulation

The Yuki gameplay loop must work completely offline.

---

38. PERSISTENCE

Yuki's important state must survive app restarts.

Potential persisted state:

- Yuki needs
- Yuki wellbeing
- affection
- progression
- experience
- inventory
- unlocked items
- environment
- last active timestamp
- last interaction timestamp
- sleep state
- activity state
- milestones
- preferences

Persistence should use the native TwoHearts architecture where appropriate.

Do not introduce a second unnecessary database system.

Do not duplicate state across unrelated storage mechanisms without a clear reason.

---

39. DATA CONSISTENCY

Yuki should have one authoritative source for its game state.

Avoid situations where:

- the UI believes Yuki is hungry
- the database says Yuki is full
- the animation says Yuki is sleeping
- the ViewModel says Yuki is playing

These states should be coordinated.

The engine should expose a coherent state model.

UI should observe state rather than independently inventing it.

---

40. APP LIFECYCLE

Yuki must behave correctly across:

- app launch
- app backgrounding
- app foregrounding
- configuration changes where applicable
- process death
- device restart
- screen rotation if supported
- app recreation
- returning after a long period

The engine should not rely on a continuously running screen.

Do not create unnecessary background processes.

---

41. PERFORMANCE

Yuki must perform well on the target Android hardware.

The user specifically wants Yuki to remain efficient on devices such as the Tecno Spark 10 Pro.

Performance considerations include:

- image memory
- bitmap size
- animation frequency
- recomposition
- unnecessary state updates
- CPU usage
- battery usage
- GPU workload
- particle count
- background work
- resource loading

Do not preload every possible asset into memory if unnecessary.

Use lazy loading or appropriate caching where beneficial.

Avoid continuously recomposing large parts of the screen because Yuki blinked.

Do not run expensive calculations every frame unless required.

---

42. ASSET MEMORY MANAGEMENT

PNG assets can consume substantial memory when decoded.

Where appropriate:

- scale images to the required display size
- avoid unnecessarily huge bitmaps
- cache commonly used assets
- release assets that are no longer required
- avoid loading duplicate copies

Do not reduce asset quality unnecessarily.

Optimize based on actual requirements rather than blindly compressing everything.

---

43. ACCESSIBILITY

Yuki should respect TwoHearts accessibility settings.

Consider:

- reduced motion
- large text
- contrast
- touch target size
- content descriptions
- screen reader behavior
- color independence

Yuki's animations should have a reduced-motion mode.

When reduced motion is enabled:

- minimize unnecessary movement
- reduce large transitions
- reduce particle effects
- avoid excessive scaling
- retain essential feedback

Yuki should remain understandable without relying entirely on animation.

---

44. SOUND

Sound is optional and should not be assumed.

If sound is eventually added:

- it must be locally available
- it must be optional
- it must respect system/app sound preferences
- it must not interfere with alarms or notifications
- it must not become repetitive

Do not introduce sound simply because a virtual pet usually has sound.

---

45. VISUAL FEEDBACK

User actions should have immediate feedback.

Examples:

Feeding:

- food appears
- Yuki reacts
- eating animation
- stat update
- satisfaction reaction

Playing:

- Yuki becomes energetic
- activity begins
- result appears
- reward/stat changes occur

Petting:

- Yuki reacts
- affection changes if appropriate

The feedback should feel connected to the action.

Avoid updating a number with no visible response.

---

46. UI PRINCIPLES FOR YUKI

The Yuki UI should not become a generic collection of:

- rectangular cards
- tiny labels
- excessive borders
- endless menus
- spreadsheet-like stats
- generic Material layouts

Yuki is a character experience.

The interface should support that.

The character should remain prominent.

Controls should be understandable.

Primary actions should be easy to reach.

Secondary systems can be tucked away.

The final UI can be redesigned independently of the current TwoHearts visual layout.

Do not assume that the current Stage 11 Yuki screen is the final design.

Stage 11 is the migrated baseline.

This directive represents the intended future direction.

---

47. DO NOT CONFUSE MIGRATION WITH PRODUCT COMPLETION

The existing Stage 11 implementation represents a migrated version of the previous Yuki system.

It does NOT automatically represent the final Yuki experience.

It is acceptable to:

- refactor Yuki
- redesign the Hub
- improve state handling
- replace weak architecture
- improve animations
- improve persistence
- replace simplistic behavior
- add missing gameplay systems
- improve asset usage
- replace placeholder mechanics
- redesign navigation within Yuki

provided the changes serve the Yuki product described here.

Do not preserve poor implementation merely because it existed during migration.

---

48. DO NOT BLINDLY COPY THE LEGACY YUKI SYSTEM

The legacy React/Vite/Capacitor implementation may be inspected for historical context.

It is not the final authority over Yuki.

The native implementation should be designed for:

- Kotlin
- Android SDK
- Jetpack Compose
- native lifecycle
- native persistence
- native performance
- modern Android architecture

Do not translate bad legacy patterns merely because they existed previously.

Preserve the intended product behavior where useful.

Improve implementation where appropriate.

---

49. ARCHITECTURAL BOUNDARIES

Yuki should preferably be organized into clear conceptual layers.

Possible separation:

Game State

Stores authoritative state.

Game Engine

Calculates:

- stat changes
- elapsed time
- behavior decisions
- activity results
- progression
- rewards

Behavior System

Determines what Yuki should do.

Animation System

Determines how Yuki visually performs that behavior.

Persistence

Stores and restores Yuki state.

ViewModel / Presentation

Exposes state to Compose.

UI

Displays Yuki and provides controls.

The exact implementation may differ.

The important requirement is separation of concerns.

Do not put all Yuki logic inside one enormous composable.

Do not put gameplay rules inside individual button click handlers.

---

50. GAME ENGINE VS UI

The game engine should not depend heavily on Compose UI.

A future UI redesign should not require rewriting:

- hunger calculations
- progression
- inventory
- timestamps
- behavior rules
- persistence

Likewise, changing Yuki's animation should not alter the underlying hunger system.

The engine should be testable independently wherever practical.

---

51. GAME ENGINE VS ASSETS

The game engine should refer to semantic animation/state identifiers rather than hardcoding arbitrary filenames throughout the application.

For example, conceptually:

"YukiAnimation.EAT"

rather than scattering:

""yuki_eat_final_v3_revised.png""

throughout the codebase.

Asset mapping should exist in one sensible location.

This allows assets to be reorganized later without rewriting gameplay logic.

---

52. ERROR HANDLING

Yuki should fail gracefully.

If an asset is missing:

- do not crash the entire application
- fall back to a safe known asset where possible
- log the problem appropriately
- expose useful developer information

If saved state is corrupt:

- attempt safe recovery
- preserve as much data as possible
- avoid crashing at startup

If a timestamp is invalid:

- use a safe fallback
- avoid enormous stat changes

If an item identifier is unknown:

- ignore or safely migrate it
- do not crash the game

Yuki should be resilient.

---

53. TIME MANIPULATION

Because Yuki is offline-first, the device clock can be manipulated.

Do not attempt to create an elaborate anti-cheat system.

Yuki is a private companion, not a competitive online game.

However, the engine should avoid catastrophic behavior caused by:

- clock moving backward
- clock moving forward by years
- timezone changes
- invalid timestamps

Use reasonable clamps.

For example, if a user opens the app after an extremely large elapsed period, the engine should not necessarily simulate every second individually.

Calculate the result mathematically.

---

54. SAVE FREQUENCY

Do not write to storage on every animation frame.

Gameplay state should be persisted when meaningful changes occur, such as:

- feeding
- activity completion
- stat change
- progression
- inventory modification
- exiting the relevant experience
- app lifecycle changes

Use batching/debouncing where appropriate.

Animation state does not necessarily need persistent storage.

---

55. GAMEPLAY BALANCE

Yuki should favor positive feedback.

Avoid:

- harsh punishment
- excessive grinding
- impossible requirements
- extremely slow progression
- repetitive tasks
- forced daily activity
- artificial scarcity

Progression should be satisfying.

A player should be able to enjoy Yuki casually.

A player who interacts more frequently can discover more, but someone who checks Yuki less frequently should not feel like they have failed.

---

56. LONG-TERM ENGAGEMENT

Yuki should have reasons to return.

These can include:

- new animations
- new behaviors
- new items
- new toys
- environmental changes
- milestones
- discoveries
- personality moments
- progression
- rare interactions

The strongest reason to return should be:

«"I want to see what Yuki is doing."»

Not:

«"I have to prevent Yuki from losing everything."»

---

57. YUKI'S HOME

The Yuki Hub should eventually feel like Yuki's own space.

The environment may evolve over time.

Possible elements:

- sleeping area
- food area
- play area
- toys
- decorations
- furniture
- small environmental animations

The environment should reinforce the feeling that Yuki belongs there.

---

58. ENVIRONMENT INTERACTION

If the environment supports it, Yuki should be able to interact with it.

Examples:

- sit near an object
- walk to a toy
- sleep on a bed
- investigate something
- play with an object
- move around the room

These interactions can be mostly cosmetic initially.

Do not implement complicated physics unless it provides meaningful value.

---

59. PROGRESSION OF THE ENVIRONMENT

Environmental progression can eventually unlock:

- new furniture
- new decorations
- new toys
- new locations
- visual themes

This can provide a secondary progression path without requiring Yuki itself to become more complicated.

---

60. YUKI AND THE COUPLE EXPERIENCE

Yuki exists inside a couples application.

This does not mean every Yuki mechanic needs multiplayer functionality.

V1 remains offline-first.

However, the emotional identity of Yuki can reflect the idea that Yuki belongs to the TwoHearts space.

Potential future concepts may include:

- shared milestones
- couple-related decorations
- special memories involving Yuki
- two-person interactions in a future online bridge

These should remain future considerations unless explicitly implemented.

Do not introduce online functionality into the current offline Yuki engine merely to support hypothetical future features.

---

61. PRIVACY

Yuki should not collect analytics.

Do not add:

- tracking
- advertising SDKs
- remote telemetry
- behavioral analytics
- third-party tracking

Yuki's state is private local application data.

---

62. NO CLOUD DEPENDENCY

The current Yuki experience must not depend on:

- Firebase
- Supabase
- cloud save
- remote APIs
- online AI
- remote image generation
- cloud game state

A user should be able to put the phone into airplane mode and still use Yuki.

---

63. NO ONLINE CHAT

Yuki is not a chatbot.

Do not turn Yuki into an AI conversational assistant unless that becomes a separately approved future feature.

Yuki's personality should primarily be expressed through:

- animation
- reactions
- behavior
- activities
- visual feedback

not through pretending to be a generative AI companion.

---

64. OPTIONAL TEXT

Text may be used sparingly.

Examples:

- "Yuki is hungry."
- "Yuki is sleepy."
- "Yuki is happy."
- "Yuki found something!"

Text should support the character rather than dominate the experience.

Avoid turning the Hub into a dialogue-heavy interface.

---

65. FUTURE AI

If AI functionality is ever considered, it must be treated as a separate future design decision.

It must not become a hidden dependency of the core Yuki engine.

Core Yuki must remain functional without AI.

---

66. SECURITY

Yuki should respect TwoHearts' privacy model.

Do not expose internal Yuki data unnecessarily.

Do not write sensitive information into logs.

Do not create exported files containing private data unless explicitly requested through the application's data-management features.

---

67. TESTING REQUIREMENTS

Yuki should eventually have tests for core gameplay logic.

At minimum, test:

- stat changes
- feeding
- activity completion
- energy changes
- happiness changes
- affection
- progression
- inventory
- time simulation
- long absence
- timestamp handling
- save/load
- corrupted state recovery
- invalid item handling
- state transitions

Animation appearance can be tested manually where automated visual testing is impractical.

---

68. REAL-DEVICE TESTING

Yuki must eventually be tested on an actual Android device.

Pay particular attention to:

- startup
- returning from background
- process death
- long sessions
- memory usage
- animation smoothness
- touch responsiveness
- image loading
- app restart
- notification behavior
- reduced motion
- dark mode if supported
- large text
- low battery conditions
- long idle periods

Do not assume emulator behavior represents real-device behavior.

---

69. PERFORMANCE TESTING

Observe whether:

- CPU usage rises unnecessarily
- animations consume excessive battery
- memory grows over time
- repeated navigation leaks resources
- assets remain in memory unnecessarily
- recomposition becomes excessive
- the app becomes slower after prolonged use

A Yuki session should remain stable over long periods.

---

70. OFFLINE TESTING

Explicitly test Yuki with:

- Wi-Fi disabled
- mobile data disabled
- airplane mode
- app restart
- device restart

Yuki must remain functional.

---

71. STATE RECOVERY

If Yuki's saved state cannot be loaded perfectly, the app should recover gracefully.

A safe fallback state is better than crashing.

Do not silently erase all progress unless absolutely necessary.

If recovery requires migration of old Yuki data, implement a clear migration path.

---

72. FUTURE EXPANSION MODEL

The system should be designed so additional content can be added without rewriting the engine.

Future additions may include:

- new food
- new toys
- new animations
- new behaviors
- new environments
- new furniture
- new cosmetic accessories
- special events
- seasonal content
- achievements
- milestones
- additional interactions

Content should ideally be represented through data/configuration where practical rather than deeply hardcoded behavior.

Do not over-engineer this prematurely.

---

73. DO NOT OVER-ENGINEER

The goal is not to create a AAA game engine.

Yuki is a focused virtual companion.

Avoid unnecessary:

- frameworks
- dependencies
- abstraction layers
- networking systems
- complicated ECS architectures
- physics engines
- third-party animation libraries
- background services
- cloud systems

Use the native Android stack whenever it is sufficient.

Prefer simple, understandable architecture.

---

74. YUKI SHOULD FEEL POLISHED, NOT TECHNICAL

The user should never need to know that Yuki has:

- a state machine
- repositories
- ViewModels
- Room
- SharedPreferences
- StateFlow
- animation controllers

Those are implementation details.

The visible experience should simply feel natural.

---

75. VISUAL POLISH

Polish matters.

Look for:

- awkward animation transitions
- sudden snapping
- inconsistent scale
- strange positioning
- assets appearing too large
- assets appearing too small
- poor framing
- abrupt state changes
- generic UI
- excessive empty space
- clutter
- inconsistent spacing
- weak feedback
- repetitive behavior

Fix these where appropriate.

---

76. TRANSITIONS

Transitions between activities should feel intentional.

Examples:

IDLE → EAT

Yuki notices food before eating.

IDLE → PLAY

Yuki reacts before starting.

PLAY → TIRED

Yuki slows down naturally.

TIRED → SLEEP

Yuki transitions calmly.

SLEEP → AWAKE

Yuki gradually becomes active.

Do not abruptly switch sprites unless the visual design intentionally calls for it.

---

77. EMOTION AND ANIMATION MUST AGREE

The displayed animation should correspond to Yuki's actual state.

Do not show:

- happy animation while happiness is critically low
- energetic animation while exhausted
- sleeping animation while actively playing
- eating animation without an eating action
- celebration animation after a failed action

State and visual presentation must remain synchronized.

---

78. UI STATE AND GAME STATE MUST AGREE

If the UI displays:

"Hungry"

the engine should actually consider Yuki hungry.

If the UI displays:

"Sleeping"

the behavior system should actually be in a sleep-compatible state.

Avoid duplicate logic where each UI component calculates its own interpretation.

---

79. USER FEEDBACK

Yuki should provide feedback immediately after meaningful actions.

Examples:

- animation
- particle effect
- stat change
- sound if later enabled
- item count update
- progression update
- small celebratory effect

The response should be proportionate.

A tiny action should not trigger a huge cinematic.

A major milestone can receive stronger feedback.

---

80. ACCESSIBLE INTERACTION

Do not make interactions dependent on extremely precise gestures.

Buttons and touch targets should be appropriately sized.

If tapping Yuki is supported, provide another way to trigger important actions.

Core gameplay should not be inaccessible because a user cannot perform a specific gesture.

---

81. REDUCED MOTION

When reduced motion is enabled:

- preserve state feedback
- reduce movement
- reduce particle effects
- reduce scaling
- avoid excessive bouncing
- shorten or simplify transitions

Do not simply disable all visual feedback.

Yuki should remain understandable.

---

82. DARK MODE

If TwoHearts supports dark mode, Yuki should remain visually coherent.

Do not simply invert colors.

Yuki's orange appearance should remain recognizable.

Backgrounds and UI should adapt appropriately.

---

83. LARGE TEXT

Yuki should remain usable with increased system text size.

Do not allow text to overlap critical controls.

Do not make the core character dependent on tiny labels.

---

84. CONTENT HIERARCHY

The Yuki Hub should have clear hierarchy:

1. Yuki
2. immediate state
3. primary interactions
4. secondary systems
5. progression/inventory
6. settings

Do not give every control equal visual weight.

---

85. THE CHARACTER IS THE HERO

This is a critical principle.

The Yuki interface should never make the character feel like an icon sitting beside a dashboard.

Yuki is the primary experience.

The UI should frame Yuki.

The UI should support Yuki.

The UI should not overpower Yuki.

---

86. FUTURE ASSET ADDITIONS

When additional Yuki assets are added later:

- preserve the existing visual style
- maintain consistent dimensions where practical
- use clear names
- place them in the appropriate Yuki Assets directory
- document their purpose
- map them to semantic animation/state identifiers
- do not scatter assets across unrelated project directories

If an asset represents a new behavior, update this document where necessary.

---

87. ASSET NAMING

Use clear, semantic names.

Prefer:

"yuki_idle.png"

"yuki_sleep.png"

"yuki_eat.png"

"yuki_run.png"

over meaningless names such as:

"IMG_0938.png"

If the supplied filenames are already meaningful, preserve them unless there is a strong reason to rename them.

Do not rename large groups of assets unnecessarily.

---

88. ASSET DOCUMENTATION

The asset directory should eventually contain enough information for an agent to understand the assets without guessing.

Where useful, document:

- filename
- purpose
- state
- animation role
- intended scale
- transparency
- whether it is a key pose
- whether it loops
- whether it transitions into another state

Do not create unnecessary documentation for obvious files.

---

89. GAME DESIGN SHOULD COME BEFORE UI DECISIONS

When designing a Yuki screen, first determine:

What is the user trying to do?

What is Yuki doing?

What state changes?

What feedback should occur?

Only then determine the UI.

Do not start with:

"What cards should we put on this screen?"

Start with:

"What should this experience feel like?"

---

90. GAMEPLAY SHOULD COME BEFORE STATISTICS

Do not create a UI full of:

- Hunger: 72
- Happiness: 83
- Energy: 51
- Cleanliness: 64
- Affection: 91

and assume that this automatically makes a virtual pet.

The player should experience Yuki first.

Numbers should support gameplay.

---

91. YUKI SHOULD HAVE MOMENTS

A strong companion experience comes from memorable moments.

Examples:

- Yuki unexpectedly falls asleep
- Yuki reacts excitedly to a favorite food
- Yuki discovers a toy
- Yuki performs a rare idle
- Yuki greets the user after a long absence
- Yuki celebrates a milestone
- Yuki becomes curious about something in the room

These moments make Yuki feel like a character.

---

92. RARITY WITHOUT FRUSTRATION

Rare behaviors can exist.

However:

Do not make essential gameplay dependent on extremely rare events.

Rare events should feel like discoveries.

The user should not need to manipulate the system for hours just to see basic content.

---

93. NO ARTIFICIAL FOMO

Do not create mechanics designed around fear of missing out.

Avoid:

- permanent limited-time punishment
- irreversible missed rewards
- forced daily streaks
- aggressive timers
- constant reminders

If seasonal content is eventually added, it should be optional and friendly.

---

94. COZY FIRST

When choosing between:

A complicated mechanic that adds numerical depth

and

A simple mechanic that makes Yuki feel more alive,

prefer the latter unless the complex mechanic provides substantial value.

Yuki's identity is companionship.

---

95. DEVELOPMENT PRIORITY

When improving Yuki, prioritize in roughly this order:

1. Stability
2. Correct game state
3. Persistence
4. Core interaction loop
5. Character behavior
6. Animation quality
7. Visual presentation
8. Progression
9. Content variety
10. Advanced expansion

Do not polish a broken mechanic while ignoring state corruption.

Do not add ten new foods while Yuki's save system is unreliable.

---

96. WHAT NOT TO DO

Do NOT:

- rebuild the entire TwoHearts application unnecessarily
- redo the completed migration
- reintroduce React/Vite/Capacitor
- introduce cloud infrastructure
- introduce online multiplayer
- introduce analytics
- introduce advertisements
- replace Yuki with a generic cat
- discard the supplied Yuki assets
- treat the assets as disposable placeholders
- make Yuki a static image
- create a dashboard that overwhelms the character
- create excessive stat systems
- create manipulative engagement mechanics
- require constant user attention
- create unnecessary background services
- over-engineer the engine
- introduce unnecessary third-party dependencies
- hardcode everything into one Compose screen
- put all gameplay logic inside UI click handlers
- create duplicate sources of truth
- break unrelated TwoHearts features without necessity
- change unrelated application architecture simply because Yuki is being improved

---

97. SCOPE CONTROL

Yuki work should remain Yuki-focused.

Changes outside Yuki should only be made when genuinely required for:

- navigation
- shared services
- shared persistence
- notifications
- shared theme integration
- accessibility
- required application infrastructure

Do not use Yuki work as an excuse to redesign the entire TwoHearts application.

The broader TwoHearts UI/UX will be addressed separately.

---

98. FUTURE TWOHEARTS UI REDESIGN

The current overall TwoHearts UI/UX is not considered final.

That does NOT mean Yuki should be designed poorly.

Yuki should have a strong internal experience now.

Later, the Yuki Hub can be visually integrated into the broader TwoHearts redesign.

Therefore:

- preserve clean boundaries
- avoid assumptions about permanent global navigation
- avoid tightly coupling Yuki's UI to current global layout
- keep Yuki's core experience modular

---

99. AGENT AUTONOMY

An agent working on Yuki is expected to inspect the existing implementation and make informed engineering decisions.

The agent should not ask for permission for every small implementation decision.

The agent should use this document as the product direction.

The agent may:

- refactor
- reorganize
- improve architecture
- improve animation
- improve state handling
- add missing gameplay systems
- redesign Yuki screens
- improve asset handling
- create helper classes
- add tests
- optimize performance

when these actions are clearly consistent with this directive.

However, the agent should not invent major unrelated product features without justification.

---

100. WHEN SOMETHING IS AMBIGUOUS

When this document leaves a design decision open, prefer the solution that best supports:

1. Yuki feeling alive
2. simplicity
3. coziness
4. meaningful interaction
5. offline operation
6. performance
7. maintainability
8. long-term extensibility

Do not automatically choose the most complicated implementation.

---

101. DOCUMENTATION REQUIREMENT

Whenever a substantial Yuki system is introduced or changed, update this document or create appropriate supporting documentation if the change affects the intended behavior.

Documentation should explain:

- what changed
- why it exists
- how it behaves
- what states it affects
- what assets it uses
- what future developers should know

Do not allow the implementation to become the only source of truth.

---

102. CHANGE SAFETY

Before removing an existing Yuki feature, determine:

- whether it is still used
- whether it is referenced elsewhere
- whether it is part of persistence
- whether navigation depends on it
- whether assets depend on it
- whether another feature depends on it

Do not delete working functionality merely because it is inconvenient.

If a system is obsolete, replace it intentionally and document the decision.

---

103. BUILD REQUIREMENTS

Yuki changes must remain compatible with the native TwoHearts build system.

The project uses:

- Kotlin
- Android SDK
- Jetpack Compose
- native Android architecture
- GitHub Actions for APK builds

Do not reintroduce legacy web build dependencies.

Do not require Android Studio to be installed on the user's device.

Do not introduce tooling that prevents the existing GitHub Actions build from functioning.

---

104. DEPENDENCY DISCIPLINE

Prefer existing project dependencies.

Before adding a dependency, determine whether Android/Kotlin/Compose already provides the required functionality.

Avoid adding large libraries for small features.

Every new dependency should have a clear justification.

---

105. TESTABILITY

Core Yuki logic should be separable from UI enough to allow unit testing.

Tests should ideally cover:

- state initialization
- time progression
- stat decay
- stat recovery
- feeding
- playing
- sleeping
- inventory
- progression
- state transitions
- save/load
- edge cases

UI tests may cover:

- primary actions
- navigation
- state presentation
- accessibility
- interaction feedback

---

106. FINAL QUALITY STANDARD

Yuki should not be considered complete merely because:

- the screen opens
- the cat appears
- buttons work
- PNGs load
- stats change
- the app builds

Completion requires the experience to feel coherent.

Ask:

Does Yuki feel alive?

Does Yuki react naturally?

Does the character remain visually attractive?

Does the user understand what to do?

Does the user receive satisfying feedback?

Does Yuki remain interesting after repeated sessions?

Does Yuki persist correctly?

Does Yuki behave correctly after the app has been closed?

Does Yuki remain offline?

Does it perform well?

Does the interface keep Yuki as the focus?

Does the experience feel like a virtual companion rather than a settings screen?

If the answer is no, further work is appropriate.

---

107. DEFINITION OF A SUCCESSFUL YUKI EXPERIENCE

A successful Yuki implementation should make the user able to:

- open Yuki quickly
- immediately understand Yuki's current state
- see Yuki behaving naturally
- interact with Yuki
- feed Yuki
- play with Yuki
- care for Yuki
- observe Yuki's reactions
- discover behaviors
- progress naturally
- return later and see persistent consequences of time
- maintain Yuki completely offline
- enjoy Yuki without feeling pressured
- recognize Yuki as a unique TwoHearts character

The user should feel that Yuki is more than a collection of screens.

Yuki should feel like a small world living inside TwoHearts.

---

108. THE 22 FUNDAMENTAL PNG ASSETS

The currently supplied Yuki PNG assets should be treated as the initial visual foundation.

The agent must inspect all of them before implementing substantial animation work.

The agent should determine:

- exact contents
- state represented
- possible animation relationships
- dimensions
- transparency
- visual consistency
- intended use

Do not assume filenames alone are sufficient.

Visual inspection matters.

If a supplied asset can be reused to create a transition or animation, prefer reuse over unnecessary asset duplication.

---

109. INTERMEDIATE ANIMATION GENERATION

The engine may generate motion between fundamental assets.

Examples include:

- idle breathing
- subtle bounce
- walking cycles
- running movement
- jumping arcs
- head movement
- blinking
- entering/exiting activities
- smooth movement across the environment

The animation system should avoid making the character look like a sprite being mechanically translated around the screen.

Motion should have:

- anticipation where appropriate
- acceleration
- deceleration
- natural timing
- appropriate pauses

Do not animate every property continuously.

Subtlety is valuable.

---

110. FRAME RATE AND PERFORMANCE

Animation should target smooth rendering without forcing unnecessary workload.

Avoid:

- uncontrolled infinite loops
- multiple simultaneous timers performing duplicate work
- frame-by-frame database writes
- frame-by-frame logging
- excessive particle systems
- huge bitmap allocations
- unnecessary recomposition

Animation should stop or reduce activity when Yuki is not visible.

---

111. BACKGROUND BEHAVIOR

Yuki does not need to continuously simulate animation when the app is not visible.

When the app returns:

- calculate elapsed gameplay state
- determine Yuki's appropriate current state
- resume presentation

This is preferable to keeping a constant background animation process.

---

112. BATTERY

Yuki should be battery-conscious.

Do not:

- maintain unnecessary wake locks
- run continuous background loops
- poll storage
- poll the clock continuously
- keep the app alive solely for Yuki

Use timestamp-based simulation and local notifications where appropriate.

---

113. MEMORY

Yuki should avoid memory leaks.

Particular attention should be paid to:

- image loading
- Compose lifecycle
- coroutine scopes
- animation loops
- listeners
- references to Activities/Contexts
- cached bitmaps

Yuki should remain stable after repeated entering and leaving of the Hub.

---

114. COROUTINES AND ASYNC WORK

Asynchronous work should be lifecycle-aware.

Do not launch uncontrolled global coroutines.

ViewModel-level work should respect ViewModel lifecycle.

UI-level work should respect Compose lifecycle.

Persistence should not block the main thread unnecessarily.

---

115. GAME ENGINE CLOCK

Yuki's simulation should have a clear concept of time.

Do not depend on UI frame time as the authoritative gameplay clock.

Persist meaningful timestamps.

When recalculating state:

"elapsedTime = currentTime - lastSimulationTime"

Use elapsed time to calculate state changes.

Do not simulate millions of individual seconds through loops.

Use mathematical/state-based calculations.

---

116. STAT DECAY MODEL

Stat decay should be predictable.

For example, hunger can gradually increase over elapsed time while energy decreases or recovers depending on Yuki's state.

The exact values should be determined through balancing.

Avoid hardcoding arbitrary values in many different locations.

Centralize gameplay constants where practical.

---

117. GAMEPLAY CONSTANTS

Potential constants include:

- maximum stat values
- decay rates
- recovery rates
- activity costs
- reward values
- progression thresholds
- cooldowns
- random behavior weights

Keep these understandable and centralized.

Avoid magic numbers scattered across UI code.

---

118. BALANCING AFTER IMPLEMENTATION

Initial values are not guaranteed to be correct.

After implementation, observe:

- how quickly hunger changes
- how often Yuki becomes tired
- how frequently the user needs to interact
- how quickly progression occurs
- whether rewards feel meaningful
- whether behavior variety is sufficient

Adjust based on actual experience.

---

119. NO NEED FOR PERFECT COMPLETENESS IN ONE PASS

Yuki can be developed incrementally.

A strong initial system may contain:

- core state
- persistence
- basic needs
- fundamental animations
- feeding
- play
- sleeping
- idle behavior
- Hub

Later iterations can add:

- more toys
- more environments
- more behaviors
- progression
- special events
- additional animation
- cosmetics

Do not rush to implement every future idea simultaneously.

---

120. BUT DO NOT BUILD A SHELL

Incremental development does not mean creating fake placeholders.

Avoid:

- empty buttons
- fake inventory
- nonfunctional stats
- "coming soon" screens everywhere
- static placeholder behavior
- decorative controls that do nothing

If a feature is presented as implemented, it should work.

If it is not implemented, do not disguise it as complete.

---

121. VISUAL QA

When possible, inspect Yuki visually through the available development/live-preview workflow.

Check:

- character scale
- alignment
- animation transitions
- clipping
- transparency
- asset quality
- UI hierarchy
- touch targets
- empty space
- environment balance
- visual consistency

Do not rely solely on source code to determine whether Yuki looks good.

---

122. DEVICE QA

Test on real Android hardware whenever possible.

Pay attention to:

- touch latency
- image scaling
- animation smoothness
- memory pressure
- app resume
- notification behavior
- screen density
- text scaling

A layout that looks correct in a desktop preview may behave differently on the actual phone.

---

123. YUKI SHOULD REMAIN ORIGINAL

The following are inspirations only:

- classic virtual pets
- cozy companion games
- cute cat games
- soft 2D character games

Yuki must remain its own character and experience.

Do not reproduce:

- another game's UI
- another game's character
- another game's exact mechanics
- another game's assets
- another game's branding
- another game's screen layout

Use broad genre inspiration, not direct imitation.

---

124. PRODUCT IDENTITY

Yuki should feel like something that belongs specifically to TwoHearts.

The combination should communicate:

TwoHearts + Yuki = our private little companion world.

Yuki can be playful while TwoHearts remains romantic and personal.

The visual language can use appropriate TwoHearts colors and styling, but Yuki should still have its own character identity.

---

125. FUTURE V2 COMPATIBILITY

The current Yuki engine is offline-first.

A future TwoHearts V2 may eventually introduce limited online functionality.

If that happens, Yuki could theoretically gain:

- shared state
- shared activities
- couple interactions
- remote synchronization

However, this is future functionality.

Do not implement V2 networking now.

The current engine must remain fully functional offline.

---

126. EXPORT / IMPORT

If TwoHearts provides data export/import functionality, Yuki state should be considered where appropriate.

Yuki data should be:

- serializable
- versionable
- recoverable

Do not make Yuki's state impossible to migrate.

If the application's existing import/export system does not support Yuki yet, document the requirement rather than introducing a separate export system unnecessarily.

---

127. VERSIONING

Yuki's saved data should be versionable.

If future versions change the state model, use migrations or safe conversion.

Do not assume the saved state will always have the newest fields.

Missing fields should have safe defaults.

Unknown fields should generally be ignored.

---

128. FUTURE AGENT INSTRUCTION

Any future agent receiving this document should:

1. Read this document completely.
2. Inspect "Yuki Assets/".
3. Inspect the current native Yuki implementation.
4. Inspect relevant TwoHearts architecture.
5. Understand what already exists.
6. Identify what is missing.
7. Avoid rebuilding unrelated TwoHearts systems.
8. Preserve the authoritative supplied Yuki assets.
9. Improve the experience according to this directive.
10. Verify the result.
11. Document substantial changes.

Do not make assumptions about the current implementation without inspecting it.

---

129. IF THE CURRENT IMPLEMENTATION CONFLICTS WITH THIS DOCUMENT

If the existing Yuki implementation conflicts with this document, determine whether the existing behavior is:

- required TwoHearts infrastructure
- an intentional product decision
- a temporary implementation
- a legacy migration artifact
- an incomplete feature

If it is merely an old implementation detail, the current Yuki directive takes precedence.

If the conflict affects a major product decision, document the conflict and choose the solution that best preserves Yuki's core identity.

---

130. IF THE ASSETS CONFLICT WITH THE IMPLEMENTATION

The supplied Yuki assets are authoritative visual resources.

If the current implementation does not use them properly, improve the implementation.

Do not discard the assets merely because the existing UI was designed around different placeholders.

The UI should adapt to the character.

---

131. IF THE CURRENT UI IS BAD

Do not preserve poor UI simply because it already exists.

Yuki's Hub can be redesigned.

The current TwoHearts global UI is not the focus of this work.

Yuki can receive a focused internal UX improvement while keeping the rest of TwoHearts untouched.

The goal is to make Yuki good first.

---

132. DEFINITION OF DONE

Yuki should not be declared complete merely because all code compiles.

A meaningful Yuki milestone should satisfy:

Technical

- builds successfully
- does not crash
- state persists
- offline operation works
- lifecycle behavior works
- assets load reliably
- no obvious memory leaks
- no unnecessary background processing

Gameplay

- Yuki has meaningful state
- user interactions have consequences
- behavior responds to state
- time progression works
- activities work
- progression works where implemented
- the experience remains enjoyable

Visual

- supplied assets are used correctly
- transitions feel intentional
- Yuki is properly framed
- animations are smooth
- visual hierarchy is clear
- the character remains the focal point

UX

- user understands what to do
- controls are accessible
- feedback is immediate
- navigation is understandable
- the experience does not feel like a generic settings dashboard

Product

- Yuki feels like a companion
- Yuki feels like a TwoHearts feature
- Yuki remains offline-first
- Yuki does not feel manipulative
- Yuki has room for future growth

---

133. FINAL PRINCIPLE

The most important rule of this entire document is:

Do not build Yuki as a collection of features. Build Yuki as a character.

A hunger bar is not Yuki.

A feeding button is not Yuki.

A PNG is not Yuki.

An animation is not Yuki.

A level system is not Yuki.

An inventory is not Yuki.

Those are tools.

Yuki exists in the combination of:

- appearance
- behavior
- personality
- reaction
- memory
- progression
- interaction
- time
- animation
- persistence
- environment
- the user's relationship with the character

The user should be able to open TwoHearts, see Yuki, and immediately understand that this is not just another screen in an application.

It should feel like entering Yuki's little world.

That is the standard this directive exists to establish.

---

134. AUTHORITY AND MAINTENANCE

This document is the primary Yuki product directive.

It should remain inside:

"Yuki Assets/Yuki Game Engine.md"

The document may be expanded as Yuki evolves.

When major gameplay decisions are finalized, update this document so future agents do not need to infer those decisions from source code.

When a future decision intentionally changes this specification, update the relevant section rather than leaving contradictory rules in multiple documents.

The goal is for a future developer or agent to be able to open the Yuki Assets directory, read this file, inspect the supplied assets, inspect the native Yuki implementation, and immediately understand:

what Yuki is, why it exists, how it should behave, what it should feel like, how it should use its assets, and what standards it must meet.

Yuki is not a temporary feature.

Yuki is a dedicated companion experience within TwoHearts.

Build it accordingly.

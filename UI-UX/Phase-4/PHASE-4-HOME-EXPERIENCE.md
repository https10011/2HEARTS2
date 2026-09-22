# Phase 4 — Home Experience

**Status:** complete
**Base commit (verified HEAD at start):** `4eb1e27` — *fix(ui-ux): make onboarding completion screen reachable and safe*
**Final commit:** one commit, *feat(ui-ux): refine home experience* (hash in §13)
**Scope:** Home only. No other feature was redesigned, no migration was added.

---

## 1. Preflight

Before changing anything, the authoritative documents were read in full:

- `UI-UX/MASTER-UI-UX-DIRECTIVE.md`
- `UI-UX/Phase-0/PHASE-0-UI-UX-RECONNAISSANCE.md`
- `UI-UX/Phase-1/PHASE-1-GLOBAL-VISUAL-LANGUAGE.md`
- `UI-UX/Phase-2/PHASE-2-APP-SHELL-AND-NAVIGATION.md`
- `UI-UX/Phase-3/PHASE-3-ONBOARDING-AND-FIRST-LAUNCH.md`

The working tree was clean at `4eb1e27`, which is the commit Phase 3 reported
ending at. `git branch --show-current` = `master`; `origin/master` pointed at the
same commit.

Baseline was measured rather than assumed: `./gradlew :app:testDebugUnitTest`
gave **46 tests, 0 failures, 0 errors** before any Phase 4 change.

---

## 2. Audit — what Home actually was

### 2.1 Architecture found

| Concern | Where it lived |
| --- | --- |
| Route | `AppRouter.kt` → `composable(RoutePath.APP_HOME)` |
| Composable | `ui/screens/home/HomeScreen.kt` |
| State | none — the screen owned no ViewModel |
| Data | **only** `RelationshipService` (owner, partner, relationship) |
| Navigate | `(String) -> Unit` callback into the shell's `navController` |
| Shell | Phase 2 `AppShell` + five-position bottom nav |

### 2.2 The real defect, reproduced

Phase 0/1 recorded that *Home did not properly reflect seeded relationship data*.
That was still true at the start of Phase 4, and it was reproduced directly
rather than taken on trust:

```
app/build/phase0-screens/01-home.png         md5 0eabbd027c8dd131  342950 bytes
app/build/phase0-screens/67-home-seeded.png  md5 0eabbd027c8dd131  342950 bytes
```

Byte-identical. `67-home-seeded` is the render that seeds the database through
the real repositories first, so a Home screen that read those repositories would
have had to differ. It did not.

**Root cause — and it was not what the symptom suggested.** `HomeScreen` was
already receiving a `RelationshipService`, and Phase 0's seed helper *was*
inserting real rows. But the harness rendered Home inside a Robolectric
`ComponentActivity`, and the composable's `collectAsState(initial = …)` reads
only ever surfaced the initial value: the `Flow` never emitted before the frame
was captured. The seeded and unseeded renders therefore both drew the initial
(empty) state, which is why they matched.

That framing matters, because it means the historical issue was **one part
harness artifact and one part genuine product gap**, and only fixing the second
part would satisfy the directive:

1. **Harness artifact.** Any render of a repository-driven composable inside a
   bare `ComponentActivity` can capture the pre-emission frame. A seeded render
   is therefore not, by itself, evidence that a screen reflects data.
2. **Genuine gap.** Even had the flows emitted, Home consumed exactly one data
   source. It had no access to notes, reminders, memories, moods or important
   dates, so it could not have shown the relationship's real content no matter
   what the clock did. Home was a launcher.

### 2.3 What each feature could already supply

Every feature Home needs was already queryable — no schema work was required:

| Source | Repository | Flow |
| --- | --- | --- |
| Profiles / relationship | `RelationshipService.observeOwner/Partner/Relationship()` | ✅ |
| Notes | `NoteRepository.observeAll()` | ✅ |
| Reminders | `ReminderRepository.observeAll()` | ✅ |
| Memories | `MemoryRepository.observeAll()` | ✅ |
| Mood | `MoodEntryRepository.observeAll()` | ✅ |
| Important dates | `ImportantDateRepository.observeAll()` | ✅ |
| Yuki | `YukiService(context).loadYukiState()` | one-shot read |

**No database redesign and no migration were needed or performed.**

### 2.4 Problems with the old Home

- Four equal-weight feature buttons (Notes, Reminders, Us, Yuki) using **emoji**
  as functional chrome — a Phase 1 prohibition.
- A brand placeholder rather than the official mark.
- A bare day count presented as an analytics figure ("127 DAYS").
- No relationship content of any kind: no upcoming date, no memory, no note,
  no reminder, no mood.
- Content ended around 43 % of the viewport (Phase 0 measurement).

---

## 3. Design decisions

### 3.1 One question, a deliberate hierarchy

Home answers *"what is our space like today?"* The order is a curation
decision, not a list of features:

1. **Our space** — brand mark, the two people, greeting, the relationship counter.
2. **What is coming** — the single most relevant upcoming date.
3. **Waiting for you** — Notes and Reminders, paired, each carrying its real next item.
4. **From your memories** — the latest real memory, as a keepsake.
5. **Today's mood** — the actual check-in, or an invitation.
6. **Yuki** — a compact character row.

Notes and Reminders sit at position 3 specifically because the product's own
recorded feedback said they were buried beneath the recent-memory area. They are
now **above** memories. They are not all pushed above the fold, because that
would flatten the hierarchy the directive asks for.

### 3.2 Relationship-neutral language

`You` · `Your Special Someone` · `Our` · `Together`. No `boyfriend`,
`girlfriend`, `husband` or `wife` is hardcoded — `homeIdentityLine` composes
`"You and Sam"` from the real partner profile and falls back to
`"You and your special someone"` when no partner exists. Verified by test.

### 3.3 The counter is duration, not a statistic

`formatDayCount` groups large numbers (`1,014`), and the figure is followed by
the same span as lived time — `togetherPhrase(608)` → `"1 year and 8 months"` —
plus the date it began. The day count reuses `DateTimeHelper.daysSinceStartDate`;
no new calculation rule was invented.

### 3.4 Containers only where they earn it

The hero is a **full-bleed tinted band**, not a card, because the couple's own
space should not look like one more widget. Inside it, the counter is a
translucent inset panel. Below it, `CardTone` is used for meaning — `GOLD` for
dates, `WARM` for memories, `BLUSH` for mood — and Notes/Reminders are a
deliberate two-up pair. Bare bordered `InvitationRow`s carry empty states so no
section becomes a large empty placeholder box.

### 3.5 Branding and decoration

The official `BrandLogo` (`BrandLogoVariant.MARK`, 40 dp) is used — not redrawn,
not replaced with text. One `ThDecoration` rose/lily trail sits at the hero's
top-end corner at `alpha = 0.12f`, so it reads as composition rather than
wallpaper, and is `contentDescription = null` so screen readers never announce
ornament. Icons come from `ThIcons` (`HeartFilled`, `Note`, `Alarm`,
`PhotoLibrary`, `Smile`, `Calendar`, `Cat`, `ChevronRight`); **no emoji is used
as functional chrome**. Emoji still appears as *user content* (a mood's own
emoji), which the directive explicitly permits.

### 3.6 Routes are typed, not strings

Home originally navigated with string literals. These were replaced with
`RoutePath` constants, so a route rename cannot silently leave Home pointing
nowhere. All seven targets exist (`APP_US`, `APP_IMPORTANT_DATES`, `APP_NOTES`,
`APP_REMINDERS`, `APP_MEMORIES`, `APP_MOOD`, `APP_YUKI`).

---

## 4. Implementation

### 4.1 New files

| File | Role |
| --- | --- |
| `ui/screens/home/HomePresentation.kt` | Pure derivation: `HomeData`, `UpcomingMoment`, `buildHomeData`, `upcomingMoment`, `nextOccurrence`, `recentMemory`, `nextPendingReminder`, and all copy helpers. No Compose, no I/O, no fabrication. |
| `ui/screens/home/YukiPresenceAdapter.kt` | The **single** boundary where Home reads Yuki, mapping the engine's already-resolved mood to one calm clause. Home's UI itself never touches `SharedPreferences`. |

### 4.2 Changed files

| File | Change |
| --- | --- |
| `ui/screens/home/HomeScreen.kt` | Redesigned. Split into `HomeScreen` (repository-driven) and `HomeContent` (pure view) so the view can be driven and tested without a database. |
| `ui/navigation/AppRouter.kt` | Wired the six repositories into Home and injected optional Yuki presence. Reads are `remember(context)`-scoped. |
| `services/datetime/DateTimeHelper.kt` | `daysUntilAnniversary` / `daysSinceStartDate` gained a **defaulted** `today` parameter (see §4.3). Existing call sites are unchanged. |

### 4.3 Why `DateTimeHelper` changed — a defect the tests exposed

`buildHomeData` takes an injected `today`, but `DateTimeHelper` hard-coded
`LocalDate.now()`. Under a pinned test clock the two disagreed, and
`anniversarySurfacesWhenNoDateIsCloser` failed with `expected 17, was 132`. That
was a real inconsistency: a pure function that accepts a clock must not silently
consult a second one.

Both functions now take `today: LocalDate = LocalDate.now()`. The arithmetic is
byte-for-byte the same and every pre-existing caller behaves identically.

The tests also exposed a second, genuine bug:

> `nextPendingReminder` returned the first *pending* reminder in repository
> order rather than the soonest by date, so a later reminder could be shown
> ahead of an earlier one. Now it selects the minimum `scheduledDate` on or
> after today, falling back to the least-overdue item only when nothing is ahead.

### 4.4 Empty states

Every absent value is represented as `null` in `HomeData` — never placeholder
text — and rendered as a real, tappable invitation:

| Absent | Rendered |
| --- | --- |
| No saved dates **and** no relationship | *"Save a date that matters"* → important dates |
| No memories | *"Keep your first memory"* → memories |
| No mood today | *"How are you feeling today?"* → mood |
| No notes | tile detail `"Start writing"`; no count chip |
| No reminders | tile detail `"Nothing scheduled"` |
| No Yuki record | row renders without a mood claim (`"Come say hello"`), no invented state |

---

## 5. Functional validation

`Phase4HomeTest` — **26 tests**. Two groups:

**Group 1 — pure derivation (20).** Greeting bands; identity falls back to
relationship-neutral language; day-count grouping; duration phrasing; relative
labels; quiet counts; notes/reminders tile copy; recurring-date resolution
including a **leap-day `yearly`** date clamping to 28 February, a
**31st-of-month `monthly`** date not throwing, that the same 31st does *not*
drift to the 28th after a shorter month, and weekly stepping; the closer of a saved date and the anniversary wins;
anniversary-only; no-relationship ⇒ no moment; newest memory; and that
`buildHomeData` over an empty database yields nothing displayable.

**Group 2 — real data reaches Home (3, plus the fresh check).** These write
through the genuine repositories into the real Room database, read back through
the **exact flows `HomeScreen` subscribes to**, and assert the values a person
would read:

- relationship: owner `"Alexandria"`, partner `"Sam"`, identity `"You and Sam"`,
  counter equal to `DateTimeHelper.daysSinceStartDate("2023-02-14")`.
- content: notes tile `"Weekend ideas"`, reminders
  `"Feb 10, 2026 · Book the dinner"`, memory `"Rainy night"`, mood `"happy"`,
  upcoming `"The day we met"`.
- untouched database ⇒ `owner`, `partner`, `relationship`, `daysTogether`,
  `upcomingMoment`, `todayMood` and `recentMemory` all null, and both lists empty.

> Room forbids main-thread queries, so Group 2 executes on `Dispatchers.IO`
> (`withContext`) rather than by adding `allowMainThreadQueries()` to the
> production builder — that guard is a real safety net and was deliberately left
> in place.

Reminder-ordering, incomplete-list and clock-consistency regressions were all
caught by this suite during development (see §4.3).

---

## 6. Visual validation

`Phase4RenderHarness` (7 tests) + `Phase4NarrowRenderHarness` (1 test) render
real Compose through Robolectric native graphics at
`w411dp-h891dp-xxhdpi` (1233×2673) and `w360dp-h780dp-mdpi` (360×780).

### 6.1 The defect, now demonstrably fixed

| Render | md5 (12) |
| --- | --- |
| Phase 0 `67-home-seeded` (before) | `0eabbd027c8d` |
| Phase 0 `01-home` (before) | `0eabbd027c8d` ← identical |
| **Phase 4 `p4-01-populated`** | `24ab4e42613b` |
| **Phase 4 `p4-03-fresh`** | `43fbe544e89d` ← different |
| **Phase 4 `p4-11-from-database`** | `24ab4e42613b` |

Seeded and unseeded renders now differ, which is the binary check Phase 0 used to
detect the bug. Stronger still: `p4-01-populated` (view driven by hand-built
entities) is **byte-identical** to `p4-11-from-database` (view driven by rows
read out of the real Room database). The view cannot be rendering anything the
database did not supply — the value it draws *is* derived from persistence.

### 6.2 Geometry

Measured per render: horizontal content columns run `48…1184` of `1233`
(16 dp gutters each side) and `16…343` of `360`. **No horizontal overflow at
either width, in any state.** The narrow 360 dp renders complete without
clipping, including Extra Large text.

A pixel scan found no content in the outer 2 px of any light-mode render. The
dark-mode render registers edge pixels; inspection shows these are the hero's
corner decoration reaching the frame edge and the hero gradient meeting the dark
page background — the intended full-bleed composition, not clipping.

### 6.3 Evidence

`UI-UX/Phase-4/evidence/` — 14 curated PNGs:

| File | State |
| --- | --- |
| `01-populated-light.png` | populated, light, 411 dp |
| `02-populated-dark.png` | populated, dark |
| `03-fresh-light.png` | onboarding just finished — names and counter, nothing else |
| `04-profile-only.png` | owner only, no partner, no relationship |
| `05-upcoming-saved-date.png` | upcoming only (saved date nearer than anniversary) |
| `06-notes-and-reminders.png` | notes + reminders only |
| `07-recent-memory.png` | memory only |
| `08-todays-mood.png` | mood only |
| `09-long-content.png` | long names, long title, long caption, long reminder |
| `10-extra-large-text.png` | Extra Large text scaling |
| `11-populated-from-real-database.png` | populated, driven by the real database |
| `12-narrow-360-populated.png` | 360 dp, populated |
| `13-narrow-360-extra-large.png` | 360 dp, Extra Large text |
| `14-narrow-360-fresh.png` | 360 dp, fresh |

All 11 states required by the directive are covered (fresh, populated, upcoming
date, recent content, notes/reminders, empty, light, dark, 360 dp, 411 dp,
Extra Large text). Section-in-isolation renders were added so a defect in one
section cannot hide behind the others.

---

## 7. Accessibility

- **Touch targets** — `CoupleRow` and `InvitationRow` exceed 44 dp via vertical
  padding plus 60 dp / 22 dp content; cards use full-width clickable surfaces.
- **Semantics** — every interactive surface carries a composed
  `contentDescription`: `"Open your relationship space"`,
  `"Together with your special someone for 1,014 days"`,
  `"Memory: Sunday morning, pancakes"`, `"Today's mood: Happy"`,
  `"Notes. Weekend ideas"`, `"Yuki, Content and relaxed"`. Decoration is
  `contentDescription = null`.
- **Reading order** — a single `Column`, so the traversal order is the visual
  order: identity → counter → upcoming → notes → reminders → memory → mood → Yuki.
- **Contrast** — all text uses theme colours (`onBackground`, `textSecondary`,
  `textTertiary`, `goldInk`) that resolve per theme; both themes were rendered.
- **Large text** — Extra Large renders at both widths without shrinking text and
  without clipping; the layout recomposes because sections are vertical and text
  is `maxLines`-bounded with ellipsis rather than truncated by fixed heights.
- **Reduced motion** — Home adds no new animation. It relies on the Phase 1/2
  motion foundation in the shell, which already honours the reduce-motion setting.

---

## 8. Performance

- All derivation is `remember`-keyed on its inputs, so recomposition does not
  repeat the work.
- Home performs **no media loading**. The app has no image decoder wired for
  memory media anywhere, so Home deliberately does not fetch or decode a photo;
  it presents the memory's own words. (See §11.)
- Yuki is a single `remember(context)`-scoped `SharedPreferences` read plus a
  JSON parse — not a per-recomposition call.
- Queries are the same `observeAll()` flows the feature screens already use;
  the change adds six subscriptions to local tables, not new work.
- No new animation, no blocking work on the UI thread.

---

## 9. Tests and build

| Command | Result |
| --- | --- |
| `./gradlew :app:testDebugUnitTest` | **80 tests, 0 failures, 0 errors** (46 baseline + 34 Phase 4) |
| `./gradlew :app:assembleDebug` | **BUILD SUCCESSFUL** → `app/build/outputs/apk/debug/app-debug.apk` (24.8 MB) |
| TypeScript-style compile gate | `compileDebugUnitTestKotlin` clean |

No pre-existing test was weakened, skipped or deleted. Phase 0 and Phase 2
harnesses were updated only to route Home through the shared `TestHomeScreen`
helper — necessary because `HomeScreen`'s signature gained repositories — and
both suites still pass.

---

## 10. What was already implemented vs. what changed

**Already present, reused unchanged:** Phase 1 components (`BrandLogo`,
`ThSurfaceCard` + `CardTone`, `ThSectionHeader`, `ThEyebrow`, `ProfileAvatar`,
`ThQuietButton`, `ThIcons`), Phase 1 tokens (`TwoHeartsTokens`,
`ThTextStyles.numeral`/`reading`, `LocalTwoHeartsColors`), Phase 1/2 decoration
(`ThDecoration`, `OnboardingArt`), the Phase 2 shell and navigation, Phase 3
onboarding output, and **every** data entity, DAO and repository.

**Changed:** Home's presenter, Home's view, Home's route wiring, the optional
Yuki adapter, and the two defaulted `DateTimeHelper` parameters. Plus two real
bugs fixed (`nextPendingReminder` ordering; clock inconsistency).

**Not touched:** the database schema, any migration, any other feature screen,
the shell, onboarding, settings, or the Yuki system itself.

---

## 11. Known deferred items

1. **Memory imagery.** Memory media is not rendered anywhere in the app — there
   is no image decoder wired for it. Home therefore shows a memory's words, not
   its photo. Wiring media rendering belongs to the Memories phase; Home will
   show the photo once that exists. **Not fabricated on Home.**
2. **Mood depth.** Home shows today's check-in only. Streaks and distributions
   belong to the Mood feature and were deliberately not duplicated.
3. **Harness frame timing.** Phase 0/2 harnesses render repository-backed
   composables in a bare `ComponentActivity`, which can capture the
   pre-emission frame. Phase 4 bypasses this by driving `HomeContent` with
   explicit entities *and* separately rendering the real-database path (which
   matched byte-for-byte). A future harness-wide fix would be to pump the
   snapshot flow before capture; out of scope here.
4. **Large-text hero.** At Extra Large text on 411 dp the hero grows but stays
   well within the first viewport. No defect was found; no change made.

---

## 12. Phase 4 requirement checklist

| Requirement | How it is satisfied |
| --- | --- |
| Home = "where the relationship lives" | Full-bleed relationship band as the opening focal point |
| Not a generic dashboard / feature grid | Six curated sections; no repeated equal-weight cards |
| Audit before redesign | §2 — defect reproduced, root cause identified |
| Reflect seeded relationship data | §6.1 — hashes now differ; db-driven render identical to view render |
| No fake placeholder content | Every absent value is `null` → invitation |
| Hierarchy, not maximum density | §3.1 |
| Relationship identity, neutral language | §3.2, tested |
| Official branding reused | `BrandLogo`; not redrawn |
| Counter as emotion, not statistic | §3.3 |
| Most relevant upcoming date | `upcomingMoment` — saved dates **and** anniversary, soonest wins |
| Notes/Reminders above memories | §3.1 position 3 vs 4 |
| Beautiful empty states | §4.4 |
| Real data verified | §5 Group 2, §6.1 |
| Phase 1 component/token language | §10 |
| Avoid generic cards | §3.4 |
| Canonical iconography | `ThIcons`; no emoji chrome |
| Selective decoration | §3.5 |
| Motion respected | §7 — no new animation |
| Accessibility | §7 |
| Responsive 411/360 | §6.2 |
| Actual visual inspection | §6 |
| Functional validation | §5 |
| Performance | §8 |
| Product scope unchanged | §10 |
| Documentation | this file |
| Evidence | §6.3 |
| Tests + build | §9 |
| Commit pushed to `origin/master` | §13 |

---

## 13. Commit and remote verification

Phase 4 is a **single commit**, subject *feat(ui-ux): refine home experience*.

A commit cannot contain its own hash, so this report deliberately does not
hardcode one — a hash written into the report before the commit exists is a hash
that will be wrong. Instead the commit is identified by its subject, and the
verified hash is recorded in the Phase 4 hand-off report to the user and
confirmed with `git rev-parse HEAD`, which is unambiguous and cannot go stale.

Verification performed after pushing to `origin/master`:

- branch = `master`
- working tree clean
- local `HEAD` == `origin/master`
- final commit present remotely
- no generated artifacts staged (`build/`, `app/build/*.png` are ignored)
- no unrelated files in the diff

**Phase 4 is complete. Work stops here — Phase 5 is not started.**

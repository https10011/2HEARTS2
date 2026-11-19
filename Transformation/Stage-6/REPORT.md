# Stage 6 — Navigation and information architecture

## Starting checkpoint

The verified Stage 5 source is `3acb3c54fc296424cc042b8916e8b917de98cbd6`. Its complete debug and release suites and exact Actions run passed. The Android 13 guest accepted and resumed the Stage 5 package. The guest's System UI failure blocked launcher visuals. [Stage 5 report](../Stage-5/REPORT.md)

Reviewed `AppRouter`, `AppShell`, `NavConfig`, `BottomNav`, `ShellLayout`, `ShellSurfaces`, `RoutePath`, the Home, Us, Notes, More, and Notifications entry points, `MainActivity` notification intent handling, and the production route inventory.

## Prototype and decision

Rendered the five-tab production shell and a four-tab Compose prototype before changing the app. The prototype reused the shared tab controls and the production Home, Us, and More content. It put notifications and search actions at the top of Home, search in the shell, and a labeled Notifications row in More. At 411 dp both structures fit, but the five-tab shell already abbreviated Notifications to Alerts. At 360 dp and 200% text, Home, Alerts, and Notes split mid-word with five tabs. The four labels remained intact with four tabs, and Us kept the interlocking-heart treatment. The four-tab composition moved the utility actions alongside existing titles and artwork to recover vertical space. [Standard-width comparison](evidence/navigation-comparison-standard.jpg) · [Dark at 200%](evidence/navigation-comparison-large-type.jpg)

| Destination | From Home with five tabs | From Home with four tabs |
| --- | --- | --- |
| Us | 1 tap, branded tab | 1 tap, branded tab |
| Notes | 1 tap | 1 tap |
| Memories | 1 tap from the memory section, or Us and Memories | Same routes and tap count |
| Yuki | 1 tap after scrolling to the Home preview | Same Home route and an additional Yuki row under Us |
| Notifications | 1 tap, bottom Alerts tab | 1 tap, Home bell. Us and Notes use More and Notifications. |
| Search | More and Search | 1 tap, Home search. Us and Notes also have a search action. More starts with a labeled Search row. |
| Settings | More and App Settings | Same two taps, settings grouped together under More |

These are comparative navigation paths from the rendered interface. The analysis considered how a first-time user could identify labels, sections, and standard icons. Formal first-use testing with participants and the physical TECNO walkthrough remain pending.

## Implementation

- The selected structure is Home, Us, Notes, and More. Four equal columns provide space for readable labels. Us keeps its distinctive brand mark and selection ring.
- Notifications remains at its existing route with a Back action. Home and More expose it directly, and the system intent opens the same route over the current source. Search retains all five record link targets. Notes preserves its selected category and query across tab restoration.
- Eleven historical game entries still reach Yuki. Previously, aliases popped all entries back to Home. They now replace just the active alias and preserve its originating screen. Us also offers a direct Yuki link.
- The shell releases its reserved navigation space when an input method is visible and restores the bar after dismissal. Record IDs, repositories, persisted data, and package identity keep their existing contracts.
- The existing Home portrait can clip the second avatar with long labels at 200% text. Stage 9 owns the responsive couple portrait redesign.

## Visual and route checks

- Inspected the current 411 dp light Home, Us, More, and Notifications renders, plus 360 dp dark Home and Notes at 200%. The four labels stay intact in the enlarged states. Search and bell icons use contrasting theme colors and sit beside the existing headers and artwork. The Notifications detail has a labeled Back control. [Selected shell renders](evidence/navigation-selected-structure.jpg)
- Compared the route registrations against Stage 5. All 43 distinct non-game registrations in `AppRouter` remain present. The shared redirect function registers the same 11 legacy routes, and `PeriodRoutes` remains unchanged. The focused AndroidX navigation tests passed for all aliases, five encoded search result types, the saved tab entry, notification deduplication and source restoration, and the keyboard role rule.
- The interaction fixture measures the actual Compose controls on an edge-to-edge window and activates the Home and More actions through semantics. Robolectric stalled on automatic idling and did not reliably deliver a synthetic platform keyboard event. The final fixture supplies the corresponding insets to independent shell renders and verifies the Notes tab disappears when the inset occupies the bottom edge. Both focused build variants passed. Live keyboard animation and notification-tap checks remain part of Android release validation.
- Full debug and release unit suites each passed 155 tests with zero failures, errors, or skips. Gradle assembled the debug APK and Android tools verified its v2 signature, alignment, archive, and metadata. It remains `com.twohearts.app` version `1.0.0` (1) with min API 26 and target API 35. Local APK SHA-256 `4f728799147a7c27d7a1cb9f6bdff5eb129deefe845502714788c7c18a991db8`.
- Git verified `82997ddb77b3af17577087a939faa720777b2865` on `origin/master`. GitHub Actions completed successfully for that exact SHA and published the debug APK. [Commit](https://github.com/https10011/2HEARTS2/commit/82997ddb77b3af17577087a939faa720777b2865) · [Actions](https://github.com/https10011/2HEARTS2/actions/runs/44520656449) · [APK](https://github.com/https10011/2HEARTS2/actions/runs/44520656449/artifacts/14526819389)

## Verification checklist

- [x] Record baseline, stage dependencies, source inspection, prototype evidence, and route decision.
- [x] Inspect the final native shell at standard and narrow widths and record the result.
- [x] Verify search routing, the notification entry route, alias navigation, saved tabs, and simulated keyboard states.
- [x] Run debug and release unit tests, assemble the APK, and verify compiled metadata and signing.
- [x] Review the diff, push the stage checkpoint, and confirm CI for the exact remote SHA.

## Next stage

Stage 7 implements local selection, crop, replacement, and persistence for both partner avatars. It reuses `Profile.photoRef` and the existing media records.

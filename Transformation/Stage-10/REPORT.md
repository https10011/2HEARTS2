# Stage 10 — Us

## Starting checkpoint

Stage 9 is verified at `779e69d0c9d764f96dc45f347b8630f3bf9776ac`. Home shows both local portraits, a narrow large-type layout, and the daily story. Us still uses its previous 56 dp portrait treatment in a tinted rectangle. The hub already reads important dates, memories, timeline, places, and mood from local repositories and offers direct access to Yuki, Period tracker, and Vault. [Stage 9 report](../Stage-9/REPORT.md) · [Previous Us implementation](../../UI-UX/Phase-5/PHASE-5-US-RELATIONSHIP-HUB.md)

Reviewed the production Us route, Compose screen and projection, Home portrait layout, Room stream ordering, route inventory, and the existing native Phase 5 suite. Inspected the current empty, saved, and large-type Us renders.

## Acceptance targets

- Use the shared avatar and name treatment. Lead with the saved relationship milestone and next important date. Keep title, search, and the couple readable in narrow and large-type layouts.
- Keep saved memory, timeline, place and mood previews genuine. Provide contextual empty actions. Verify entry and Back behavior and preserve all Us destinations, including Yuki, Period tracker and Vault.
- Verify late local record updates, long content, both themes, access to relationship settings, SDK package checks, the full test variants and exact remote CI.

## Implementation

- Extracted Home's responsive couple portrait into a shared component. Us uses the same private-file renderer, portrait frame, name typography, missing-name treatment, and large-type reflow. Its normal portraits use an 88 dp maximum so the relationship date and calendar preview stay close to the top.
- The opening composition groups the pair with the stored shared date and duration. If a profile exists without a valid date, it offers relationship settings. The next saved occasion sits under the persistent Important dates heading.
- The featured memory includes its saved caption and opens its detail. Timeline, Places, and Mood keep their real saved previews. Their rows use a simple icon, text, and divider. Yuki, Period tracker, and Vault retain their entry points. The private tools reveal no record content in Us.
- Us keeps its existing lifecycle-aware repository streams and Room schema. The added native tests cover the missing-date action, route checks, and name geometry on a short viewport at 200% font scale.

## Verification

- Inspected fresh, populated, and missing-date Compose renders in light mode and the saved-story and 200% renders in dark mode. The initial large-type layout split or shortened names. The revised stacked rows render both names in full on the 360 × 640 dp fixture. The test checks each name's geometry and Compose text-overflow result. The milestone and feature rows scroll below the portraits on that viewport. [Milestones and portraits](evidence/relationship-milestones-and-portraits.jpg)
- Inspected the saved calendar event, memory caption, timeline event, place and mood preview, and the private entry points on a taller native viewport. Sampled light and dark text pairs meet 4.5:1, with 4.97:1 for the light calendar label and at least 5.65:1 for the remaining sampled pairs. [Saved story and private tools](evidence/saved-story-and-private-tools.jpg)
- The native tests exercised shared-date settings, Important Dates, new memory, memory detail, Memories, Timeline, Places, Mood, Yuki, Vault, Period tracker, and Search. The Room-backed memory test verified the repository emission after deletion and restored the first-memory action. Home's existing private-photo and shortcut tests also passed against the shared portraits.
- Each full test variant passed 178 tests with zero failures, errors, or skips. The assembled debug APK passed v2 signing, alignment, and archive integrity checks. It reports `com.twohearts.app` 1.0.0 (1), min API 26, target API 35, and the existing permission declarations. Local APK SHA-256 `5ebb7ffc668d1861ab13cf4ce84e998948f2e8d29cce2bdad9d1b43eafe05294`.
- The UI checks used the Android API 34 native Compose harness. Installed-candidate and handset walkthroughs remain part of release validation.

- Git verified `161fc6137ba06d099ce19679e2aa4c358f634989` on `origin/master`. The Android workflow passed and published the Us checkpoint. [Commit](https://github.com/https10011/2HEARTS2/commit/161fc6137ba06d099ce19679e2aa4c358f634989) · [CI](https://github.com/https10011/2HEARTS2/actions/runs/44573198539) · [APK](https://github.com/https10011/2HEARTS2/actions/runs/44573198539/artifacts/14554014588)

## Completion checklist

- [x] Record the starting checkpoint, source reviewed, initial findings, implementation and dependencies.
- [x] Verify the saved and missing records, stream update, available routes, and private entry points.
- [x] Render and inspect both themes, short and standard layouts, and 200% text.
- [x] Run both full test variants, inspect the APK, and review the diff.
- [x] Commit, push, verify the exact SHA and CI artifact, and update the progress tracker.

## Next stage

Stage 11 revisits the Memories gallery, detail, editors, selection and playback. It verifies ordered local attachment persistence, safe replacement and removal, and recovery from partial failures.

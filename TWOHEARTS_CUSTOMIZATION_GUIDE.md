# TwoHearts — Owner Customization Guide

This guide explains how to customize TwoHearts **without editing feature logic**
(MasterPrompt §17, §58, §74). Every path below is a real path in this repository.

> The customization source of truth is one directory: `src/customization/`.
> Replaceable binary assets live under `src/assets/`.

---

## Quick index — "I want to change…"

| I want to change… | Go here |
|---|---|
| App logo | `src/assets/branding/twohearts-logo.svg` |
| App icon (launcher) | `src/assets/branding/twohearts-app-icon.svg` (reference vector; Android launchers live in `android/app/src/main/res/mipmap-*/`) |
| Colors | `src/customization/theme/ownerTheme.ts` |
| Font families | `src/customization/theme/ownerTheme.ts` (`fontFamilyBase`, `fontFamilyDisplay`) |
| Font-size scaling tokens | `src/theme/tokens.ts` (`TEXT_SIZE_SCALE`) |
| App display name | `src/customization/defaults/ownerDefaults.ts` (`appName`) |
| First-launch default text size | `src/customization/defaults/ownerDefaults.ts` (`defaultTextSize`) |
| Partner language terms | `src/customization/defaults/ownerDefaults.ts` (`partnerTerm`, `partnerTermShort`) |
| Game questions/answers | `src/customization/games/` (populated in the Games phases) |
| Reminder defaults | `src/customization/defaults/` (populated in the Reminders phase) |
| Period Tracker defaults | `src/customization/defaults/` (populated in the Period Tracker phase) |
| Notification wording | `src/customization/defaults/` (populated in the Notifications phase) |
| Design tokens (spacing/radii/shadows) | `src/theme/tokens.css` |
| Feature implementation code | `src/features/<feature>/` (added in later phases) |

---

## CHANGE APP LOGO

- **Path:** `src/assets/branding/twohearts-logo.svg`
- **Format:** SVG (vector). Keep the 120×120 viewBox for stable layout, or update
  consumers if you change dimensions.
- **How:** Replace the file contents with your SVG. No code changes required.
- The logo is referenced via the assets pipeline; the owner need not touch React components.

## CHANGE APP ICON (ANDROID LAUNCHER)

- **Reference vector:** `src/assets/branding/twohearts-app-icon.svg`
- **Installed launchers:** `android/app/src/main/res/mipmap-ic_launcher/`,
  `mipmap-ic_launcher_round/` (PNG at multiple densities).
- **How:** Regenerate launcher PNGs from your vector at the required densities,
  or replace the PNGs directly. Keep the same filenames so the manifest resolves.

## CHANGE COLORS

- **Path:** `src/customization/theme/ownerTheme.ts`
- **What:** HEX color values in the `ownerTheme` object.
- **Effect:** Feeds the CSS variables in `src/theme/tokens.css` consumed app-wide.
- Changing a color here must not break storage, navigation, or games.

## CHANGE FONT DEFAULTS

- **Families:** `src/customization/theme/ownerTheme.ts` (`fontFamilyBase`, `fontFamilyDisplay`).
- **Size scale:** `src/theme/tokens.ts` — `TEXT_SIZE_SCALE` maps the user-facing
  Small/Default/Large/Extra-Large options (MasterPrompt §28) to multipliers.
- To bundle custom fonts offline, add `@font-face` sources under `src/assets/`
  and import them in CSS (keep them bundled for offline use — MasterPrompt §65).

## CHANGE APP NAME

- **Path:** `src/customization/defaults/ownerDefaults.ts` → `appName`.
- **Android string:** `android/app/src/main/res/values/strings.xml` → `app_name`.
  Both must match for consistency.

## CHANGE DEFAULT TEXT SIZE

- **Path:** `src/customization/defaults/ownerDefaults.ts` → `defaultTextSize`.
- One of `'small' | 'default' | 'large' | 'extra-large'`.

## CHANGE GAME QUESTIONS / ANSWERS (future)

- **Path:** `src/customization/games/` — per-game files added in the Games phases.
- Each file documents its fields, formats, and how to add/remove entries.
- The owner edits content; the game engine is untouched (MasterPrompt §43).

## ADD GAME QUESTIONS

- Each game content file (added in later phases) includes a comment block showing
  the required structure. Append an entry following the documented shape.

## REMOVE GAME QUESTIONS

- Delete the entry object from the relevant array in the game content file.
- Do not edit the game engine.

## FIND FEATURE CODE

- Feature screens and logic live under `src/features/<feature>/`.
- Shared domain layers: `src/repositories/`, `src/services/`, `src/data/`.
- These are populated in their respective phases.

---

## What NOT to do

- Do not hard-code personal names/dates/photos inside feature components
  (MasterPrompt §56). Use replaceable assets + local data.
- Do not use Unicode emoji as permanent UI icons (MasterPrompt §22). Use the
  vector icon set in `src/components/Icon.tsx` or assets in `src/assets/icons/`.
- Do not introduce Firebase/Supabase/cloud for V1 (V1/V2 boundary).

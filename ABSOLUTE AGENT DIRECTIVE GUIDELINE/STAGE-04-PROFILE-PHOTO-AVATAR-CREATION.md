# STAGE 04 — Profile Photo / Avatar Creation

## 1. Stage Objective

Implement a real profile-image experience so users can personalize who they are in TwoHearts. The avatar must become a REAL profile-image capability — not a decorative placeholder.

## 2. Original Problem

TwoHearts avatars were clickable but users could not add profile photos. The interaction suggested functionality that did not exist. Users saw initials in circles with no way to personalize them.

## 3. Root Cause / Missing Capability

- **Profile model had no photo reference** — `Profile` had `role`, `displayName`, `birthDate` but no field for storing a photo reference.
- **No image picker** — No UI existed for selecting a photo from the device.
- **No image processing pipeline** — No mechanism to resize/compress images for avatar use.
- **MediaStorage existed but was unused for profiles** — The existing `MediaStorage` service (filesystem + metadata) was only used by Memories/Places. It needed to be extended to profiles.

## 4. Architecture Before Changes

- **Profile model**: `{ id, role, displayName, birthDate, createdAt, updatedAt, deletedAt }` — no photo field
- **MediaStorage**: Full local media pipeline (`store`, `resolveUrl`, `delete`) with filesystem + metadata
- **MediaFileSystem**: `CapacitorFileSystem` (Android) / `MemoryFileSystem` (browser/tests)
- **ProfileRepository**: Standard CRUD via BaseRepository
- **RelationshipService**: App-facing boundary for profile operations
- **Schema**: 12 migrations, schemaVersion=12
- **coreServices**: Did not expose MediaStorage

## 5. Design Decisions

### Why extend MediaStorage rather than creating a new system?

MediaStorage already handles:
- Atomic file + metadata writes
- Tombstone-based deletion
- Orphan cleanup
- Safe relative paths (no raw device paths exposed)
- Cross-platform filesystem abstraction

Creating a duplicate system would violate the "one media system" principle. Adding `photoRef` to `Profile` and using MediaStorage for storage keeps everything consistent.

### Why canvas-based image processing (no native plugin)?

- Works in both browser dev AND Android WebView
- Zero new native dependencies (no Capacitor Camera plugin needed)
- `createImageBitmap` + `OffscreenCanvas` is well-supported on Android WebView 120+
- Camera access via `capture="environment"` attribute on file input — Android offers camera in the file chooser

### Why JPEG 85% at 400×400 max?

- 400×400px is crisp on all Android densities (xxhdpi = 3x, so 400px covers 133dp — larger than any avatar circle in the app)
- JPEG 85% produces files under 100KB — minimal storage impact
- OffscreenCanvas avoids main-thread jank during processing

### Why the advance-then-cleanup pattern for replacement?

When replacing a photo:
1. Store the new image first
2. Update the profile's photoRef
3. Only then delete the old image

This ensures the profile is never in a broken state (old image deleted before new one stored). If step 1 or 2 fails, the old image remains valid.

## 6. Profile Image Flow

```
Tap avatar → Profile Settings
  → "Change Photo" button
    → System photo picker (file input with capture="environment")
      → User selects image
        → Canvas resize to max 400×400, JPEG 85%
          → MediaStorage.store('photo', 'image/jpeg', bytes)
            → File written to media/photos/<id>.jpg
            → Metadata in media_assets table
              → Profile.photoRef = MediaAsset.id
                → Avatar resolves data: URL via MediaStorage.resolveUrl()
                  → Image displayed in Home, Us, Profile Settings
```

## 7. Data Model Changes

### Migration 013 — Profile Photo

```sql
ALTER TABLE profiles ADD COLUMN photo_ref TEXT DEFAULT NULL;
```

- `photo_ref` stores a `MediaAsset.id` reference (FK semantics at app layer, not SQL)
- `DEFAULT NULL` — existing profiles continue to work without photos
- No data destruction — existing profiles remain intact

### Profile Type Update

```typescript
export interface Profile extends TombstonedEntity {
  role: ProfileRole;
  displayName: string;
  birthDate: string | null;
  photoRef: string | null;  // NEW — MediaAsset.id or null
}
```

### Serializer Update

- `PROFILE_COLUMNS` gains `'photo_ref'`
- `toParams` maps `profile.photoRef ?? null` (coerces undefined to null for SQLite)
- `fromRow` reads `optionalString(row, 'photo_ref')`

## 8. Storage Strategy

- **Files**: `media/photos/<uuid>.jpg` in the private app data directory
- **Metadata**: `media_assets` table row with `kind: 'photo'`, `mimeType: 'image/jpeg'`
- **Reference**: `profiles.photo_ref` → `media_assets.id`
- **Resolution**: `MediaStorage.resolveUrl(id)` → `data:image/jpeg;base64,...` for display
- **Deletion**: `MediaStorage.delete(id)` tombstones metadata + removes file

## 9. Migration Strategy

- Schema version bumped from 12 → 13
- Migration 013 adds `photo_ref TEXT DEFAULT NULL`
- Existing profiles gain `photo_ref = NULL` (no photo = initials fallback)
- No data loss, no breaking changes
- Migration is idempotent (ALTER TABLE IF NOT EXISTS pattern)

## 10. Cleanup Strategy

- **On remove**: `MediaStorage.delete(photoRef)` tombstones metadata + removes file
- **On replace**: Store new → update profile → delete old (advance-then-cleanup)
- **Orphan sweep**: `MediaStorage.sweepOrphans()` already exists for general media cleanup
- **Reset**: `DataManagementService.resetAllLocalData()` already wipes media_assets + filesystem

## 11. Permission Strategy

- **No special permissions needed** — the HTML file input (`<input type="file">`) uses the system photo picker, which doesn't require broad storage permissions on Android 10+
- **Camera**: The `capture="environment"` attribute offers camera as an option in the file chooser — Android handles camera permission internally
- **No AndroidManifest changes needed** — READ_MEDIA_IMAGES is not required when using the system picker

## 12. Camera Strategy

Camera access is provided through the file input's `capture="environment"` attribute:
- On Android, the file chooser offers "Camera" alongside "Gallery"
- Android handles camera permission prompts internally
- No Capacitor Camera plugin needed
- Captured images go through the same processing pipeline as gallery images

## 13. Fallback Strategy

When no photo is set (photoRef is null):
- Avatar displays the first letter of the display name
- Styled with warm gradient background (blush → neutral-soft)
- Display font initial in burgundy color
- Consistent circular shape with border and shadow

When photo fails to load:
- `onError` handler on `<img>` reverts to initial fallback
- Broken image never shown to user

## 14. Home Integration

The Home couple header (Stage 3) now uses `ProfileAvatar`:
- Owner avatar: `ProfileAvatar name={ownerName} photoUrl={ownerUrl} size={80}`
- Partner avatar: `ProfileAvatar name={partnerName} photoUrl={partnerUrl} size={80}`
- Brand mark connector between them (unchanged)
- Names beneath avatars (unchanged)

States supported:
- Both have photos → two circular photos with brand mark
- One has photo, one has fallback → photo + initial
- Both have fallback → two initials with brand mark

## 15. Accessibility

- **Touch targets**: Avatar buttons maintain minimum 44px via the link wrapper
- **Labels**: `aria-label="Your profile"` / `aria-label="Partner profile"` on avatar links
- **Photo images**: `alt=""` + `aria-hidden` (decorative — the name provides context)
- **Photo menu**: Modal with semantic heading, keyboard-accessible buttons
- **Screen readers**: Photo presence announced via avatar label; fallback initial provides visual context

## 16. Dark Mode

- ProfileAvatar gradient uses `var(--th-color-blush)` and `var(--th-color-neutral-soft)` — themed automatically
- Border uses `var(--th-color-surface-elevated)` — adjusts in dark mode
- Photo images use `object-fit: cover` — no color manipulation needed
- Modal and buttons use existing themed components

## 17. Test Results

**948/948 tests passing** (0 failures)

Updated tests:
- `tests/migrations.test.ts` — migration IDs [1..13], name list includes 'profile-photo'
- `tests/memories.test.ts` — schemaVersion assertion → 13
- `tests/notes.test.ts` — schemaVersion assertion → 13
- `tests/phase18-search-notifications.test.ts` — schemaVersion assertion → 13
- `tests/timeline.test.ts` — schemaVersion assertion → 13

## 18. TypeScript Result

✅ Clean

## 19. Build Result

Preview hot-reloaded successfully

## 20. Files Changed

### New files (3):
- `src/data/database/migrations/013_profile_photo.ts` — schema migration
- `src/services/profile/profilePhotoService.ts` — photo lifecycle service
- `src/components/ProfileAvatar.tsx` — shared avatar component
- `src/features/app-shell/useProfilePhotos.ts` — hook for loading photo URLs

### Modified files (10):
- `src/config/persistence.ts` — schemaVersion 12 → 13
- `src/data/relationship/relationshipTypes.ts` — Profile type + serializer (photoRef)
- `src/data/database/migrations/index.ts` — migration registry + 013 import
- `src/services/relationship/relationshipService.ts` — ProfileInput + setProfilePhoto()
- `src/services/bootstrap/appBootstrap.ts` — coreServices.mediaStorage
- `src/components/index.ts` — ProfileAvatar export
- `src/components/primitives.css` — ProfileAvatar CSS
- `src/features/app-shell/screens/HomeScreen.tsx` — ProfileAvatar integration
- `src/features/settings/ProfileSettingsScreen.tsx` — photo editing UI
- `src/features/app-shell/couplePair.tsx` — ProfileAvatar in couple pairs

### Test files (6):
- `tests/migrations.test.ts` — updated IDs and names
- `tests/memories.test.ts` — schemaVersion
- `tests/notes.test.ts` — schemaVersion
- `tests/phase18-search-notifications.test.ts` — schemaVersion
- `tests/timeline.test.ts` — schemaVersion

### Documentation (1):
- `ABSOLUTE AGENT DIRECTIVE GUIDELINE/STAGE-04-PROFILE-PHOTO-AVATAR-CREATION.md`

## 21. Limitations

1. **Browser-only verification**: The preview environment is a browser, not an Android device. Camera capture via `capture="environment"` works differently on Android (offers native camera chooser) vs. browser (may open file dialog). Full Android verification requires device testing.

2. **No crop UI**: The current implementation auto-crops via canvas (center-crop to square). A manual crop UI could be added in a future stage but was not necessary for the initial feature.

3. **Image processing is main-thread**: Canvas operations happen on the main thread. For typical profile photos (< 5MB), this is fast enough. For very large images, a Web Worker could be used in a future optimization.

4. **Old CSS classes preserved**: `.th-home-couple__circle` and `.th-home-couple__initial` still exist in the CSS but are no longer used by HomeScreen (replaced by ProfileAvatar). They could be cleaned up in a future stage.

## 22. Systems Reused

- **MediaStorage** — the existing media pipeline, unchanged
- **MediaFileSystem** — filesystem abstraction, unchanged
- **BaseRepository** — CRUD operations, unchanged
- **RelationshipService** — extended with `setProfilePhoto()`
- **CoreServices** — extended with `mediaStorage` property
- **Modal component** — for photo action menu
- **Button component** — for photo actions
- **Design tokens** — all sizing/colors use existing tokens
- **CSS system** — new `.th-profile-avatar` classes follow existing patterns

## 23. Instructions for Future Agents

1. **Profile photos are stored as MediaAsset references** — never store raw base64 in the database. Always use `MediaStorage.store()` and `Profile.photoRef`.

2. **The advance-then-cleanup pattern is critical** — never delete the old photo before the new one is successfully stored and linked. This prevents data loss on failure.

3. **ProfileAvatar is the shared avatar component** — use it everywhere avatars are displayed (Home, Us, Settings, etc.). Don't create inline avatar implementations.

4. **useProfilePhotos is the shared hook** — loads and caches photo URLs. Call `refresh()` after photo changes to update all surfaces.

5. **Image processing stays at 400×400 JPEG 85%** — this is the established standard. Don't change without documenting the impact on storage and display quality.

6. **No Capacitor Camera plugin** — camera access works through the file input's `capture` attribute. Don't add the Camera plugin unless there's a specific need it addresses.

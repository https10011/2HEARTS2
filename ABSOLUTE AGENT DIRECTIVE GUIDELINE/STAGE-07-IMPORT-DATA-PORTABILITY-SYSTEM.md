# STAGE 07 — Import / Data Portability System

## 1. Stage Objective

Create a dedicated, coherent way to bring existing user data and local media into TwoHearts. The system must feel safe, understandable, and intentional — not like a developer utility.

## 2. Original Problem

TwoHearts had no import capability. Users could not bring in notes, reminders, or other data from external sources. The only way to populate the app was manual entry. The app's data was locked inside the device with no practical way to move it in (or out, though export is deferred).

## 3. Existing Architecture Discovered

### Data Models

**Note**: id, title, content, category (general|shared|private|love-letter|gratitude|idea|reminder), createdAt, updatedAt, deletedAt

**Reminder**: id, title, description, scheduledDate (yyyy-mm-dd), scheduledTime (HH:mm), recurrence (none|daily|weekly|monthly|yearly), status, notificationOwnerRef, notificationEnabled, createdAt, updatedAt, deletedAt

**VaultItem**: id, title, contentType (photo|video|note|file), mediaRef, filePath, content, description, profileId, createdAt, updatedAt, deletedAt

**Profile**: id, role (owner|partner), displayName, birthDate, photoRef, createdAt, updatedAt, deletedAt

### Services
- `NoteService` — create, update, delete, list
- `ReminderService` — create, update, delete, list, scheduleNotification
- `VaultService` — create, update, delete, list (behind VaultPinGate)
- `RelationshipService` — saveOwner, savePartner, setProfilePhoto

### Media
- `MediaStorage` — store, resolveUrl, delete (filesystem + metadata)
- `ProfilePhotoService` — pick, process (400×400 JPEG 85%), store, delete

## 4. Import Format Decision

**Chosen format: Simple JSON**

```json
{
  "format": "twohearts-import",
  "version": 1,
  "exportedAt": "2024-01-01T00:00:00Z",
  "content": {
    "notes": [...],
    "reminders": [...]
  }
}
```

### Why JSON?

- Simple, human-readable, extensible
- No custom parser needed
- Works with any text editor for manual creation
- Easy to validate with schema checks
- Future export can produce the same format
- No binary format complexity

### Why NOT other formats?

- **CSV**: Notes have multi-line content; CSV is awkward for rich text
- **XML**: Overly verbose for this use case
- **SQLite dump**: Too fragile, couples to internal schema
- **ZIP bundle**: Added complexity without benefit for V1

## 5. Import Targets

| Target | Format | Notes |
|--------|--------|-------|
| Notes | JSON array in import file | Full note fields preserved |
| Reminders | JSON array in import file | Date/time/recurrence preserved |
| Profile photos | System photo picker | Uses existing Stage 4 flow |
| Partner photos | System photo picker | Uses existing Stage 4 flow |
| Vault files | Individual file import | Uses existing Vault security pipeline |

Profile photos and Vault items use their existing dedicated flows rather than the JSON import system. This avoids duplicating media storage infrastructure.

## 6. Import Architecture

```
User selects file (.json)
  → FileReader reads text
    → JSON.parse()
      → validateImportFile()
        → If valid: show Preview
          → User confirms
            → executeImport()
              → importNotes() / importReminders()
                → Per-record: NoteRepository.create() / ReminderRepository.create()
                  → Track success/failure per record
                    → Show result summary
```

## 7. Validation Strategy

**Pre-import validation** (before showing preview):
- File must be valid JSON
- Must have `format: "twohearts-import"` and `version: 1`
- Must have `content` object
- Notes: each must have a `title` (non-empty string)
- Reminders: each must have `title`, `scheduledDate` (yyyy-mm-dd), `scheduledTime` (HH:mm)
- Categories/recurrence validated against known values

**During import** (per-record):
- Repository-level validation catches any remaining issues
- Each record imported independently — one failure doesn't block others

## 8. Duplicate Handling

**Strategy: Always create new records (no deduplication in V1)**

Rationale:
- Simple and predictable
- No risk of accidental data loss from incorrect dedup
- User can always delete duplicates manually
- Deduplication logic would require stable IDs or content hashing, which adds complexity without clear benefit for V1

## 9. Partial Failure Handling

Each import operation tracks per-record success/failure:
- Successfully imported records are committed to the database
- Failed records are logged with error messages
- User receives a summary: "Imported X items, Y failed"
- Individual error messages shown in the result screen
- No transactional rollback — partial imports are preserved

## 10. Security Considerations

- Import files are treated as untrusted input
- All fields validated before processing
- No code execution from imported content
- No path traversal possible (repositories use parameterized queries)
- Vault import not implemented in JSON flow — uses existing Vault security pipeline
- No file system writes beyond database records

## 11. Vault Import

**NOT implemented in the JSON import flow.** Vault items have their own security model:
- Vault requires VaultPinGate access
- Vault items have `profileId` ownership
- Vault media uses separate storage paths
- Importing Vault content through a JSON file would bypass the security model

Vault import remains available through:
- The existing Vault UI (add individual items)
- Future dedicated Vault import flow (deferred)

## 12. Profile/Partner Image Import

**NOT part of the JSON import flow.** Profile photos use:
- Existing system photo picker via `<input type="file">`
- Existing `ProfilePhotoService` processing pipeline
- Existing `MediaStorage` for persistence
- Existing `RelationshipService.setProfilePhoto()` for assignment

The import screen documents this as a supported import target and directs users to the existing Profile Settings flow.

## 13. UX Design

### Flow
1. **Select**: User sees explanation of supported formats + "Select File" button
2. **Preview**: Shows count of notes/reminders + sample items (up to 10 each)
3. **Importing**: Spinner with progress text
4. **Result**: Success/failure summary with per-category breakdown

### Visual Language
- Uses existing OnboardingArt illustration for branded feel
- RoseLilyDecoration accent
- Cards with existing `.th-card` styling
- Existing design tokens for all colors/spacing
- Existing Icon set (IconFile, IconFileText, IconInfo, IconCheck)
- Toast notifications for success/failure feedback

## 14. Files Changed

### New files (3):
- `src/services/import/importService.ts` — Parsing, validation, import logic
- `src/features/settings/ImportScreen.tsx` — Full import UI flow
- `ABSOLUTE AGENT DIRECTIVE GUIDELINE/STAGE-07-IMPORT-DATA-PORTABILITY-SYSTEM.md` — Documentation

### Modified files (5):
- `src/navigation/routes.ts` — Added `appMoreSettingsImport` route
- `src/navigation/AppRouter.tsx` — Added import route + ImportScreen import
- `src/features/settings/SettingsHomeScreen.tsx` — Added Import Data row
- `src/features/settings/index.ts` — Added ImportScreen export
- `src/components/primitives.css` — Added import screen CSS classes

## 15. CSS Classes Added

```
.th-import-screen              — Full-screen import layout
.th-import-content             — Scrollable content area
.th-import-hero                — Hero section with illustration
.th-import-hero__title         — Hero heading
.th-import-hero__description   — Hero description
.th-import-info-card           — Supported formats card
.th-import-info-card__title    — Card heading
.th-import-info-card__list     — Format list
.th-import-info-card__item     — Individual format row
.th-import-info-card__note     — Privacy note
.th-import-errors              — Validation error container
.th-import-error               — Individual error message
.th-import-actions             — Button group
.th-import-preview-summary     — Preview summary with badges
.th-import-preview-badge       — Item count badge
.th-import-preview-section     — Preview section (notes/reminders)
.th-import-preview-list        — Preview item list
.th-import-preview-item        — Individual preview item
.th-import-preview-more        — "…and N more" text
.th-import-progress-text       — Importing progress text
.th-import-result              — Result hero section
.th-import-result__icon        — Success/failure icon
.th-import-result__title       — Result heading
.th-import-result__description — Result description
.th-import-result-card         — Imported items breakdown
.th-import-result-errors       — Error details card
```

## 16. Test Results

**948/948 tests passing** (0 failures)

## 17. TypeScript Result

✅ Clean

## 18. Build Result

Preview hot-reloaded successfully

## 19. Accessibility Verification

- File input has `aria-label="Select import file"`
- Error messages use `role="alert"`
- Semantic headings throughout
- Touch targets maintain 44px minimum
- Large text: content flows naturally
- Dark mode: all CSS uses themed tokens
- Reduced motion: no animation-dependent layout

## 20. Offline Verification

- Entire import system works offline
- No network calls
- No cloud dependencies
- All processing local
- JSON file read via FileReader (local only)

## 21. Regression Verification

| Stage | Feature | Status |
|-------|---------|--------|
| Stage 1 | Welcome screen onboarding | ✅ Unchanged |
| Stage 2 | "You're All Set" responsive | ✅ Unchanged |
| Stage 3 | Home couple header + brand | ✅ Unchanged |
| Stage 4 | Profile photos / avatars | ✅ Unchanged |
| Stage 5 | Home layout | ✅ Unchanged |
| Stage 6 | Permission experience | ✅ Unchanged |

## 22. Limitations

1. **No export yet**: The import format is designed for future export compatibility, but export is not implemented in this stage.

2. **No deduplication**: Imported records always create new entries. Users may end up with duplicates if they import the same file twice.

3. **No Vault JSON import**: Vault items require their own security pipeline and are not included in the JSON import flow.

4. **No profile photo JSON import**: Profile photos use the system picker, not the JSON format.

5. **No CSV support**: Only JSON is supported. Users with CSV data would need to convert it first.

6. **No import preview for individual items**: Preview shows up to 10 items per category. Large imports show a summary rather than full content.

## 23. Deferred Work

- **Export system**: The JSON format is designed for bidirectional use. Export can be added in a future stage.
- **Vault import flow**: Dedicated Vault import UI (beyond the existing add-item flow).
- **CSV import**: Could be added as an alternative format.
- **Import from other apps**: Could support importing from specific third-party formats.
- **Batch profile photo import**: Multiple photos at once.
- **Import history/log**: Track past imports for reference.

## 24. Instructions for Future Agents

1. **The import format is `twohearts-import` version 1** — Future changes must maintain backward compatibility.

2. **Profile photos and Vault items have their own flows** — Don't try to force them into the JSON import system.

3. **Each record is imported independently** — Partial failure is expected and handled gracefully.

4. **No deduplication in V1** — Always create new records. Future versions may add dedup logic.

5. **The import format is designed for future export** — If implementing export, produce the same `twohearts-import` format.

6. **Security: imported files are untrusted** — Validate all fields. Never execute imported content.

7. **Offline-first preserved** — No network calls, no remote dependencies.

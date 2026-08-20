# Phase 7 — Memories

## Overview

The Memories feature provides a local-first photo/video diary for couples. All data stays on-device — no cloud storage, no network sync.

## Architecture

```
UI (MemoriesHome, AddMemory, MemoryDetail)
  ↓
useMemoryService hook (local state + service coordination)
  ↓
MemoryService (validation, media coordination, error normalization)
  ↓
MemoryRepository (CRUD via BaseRepository)
  ↓
MemoryAdapter (SQLite via DatabaseAdapter)
  ↓
SQLite / sql.js (local persistence)
```

## Database Schema (Migration 004)

### `memories` table

| Column | Type | Notes |
|---|---|---|
| `id` | TEXT PK | UUID v4 |
| `title` | TEXT | Required, 1–200 chars |
| `caption` | TEXT | Optional |
| `memory_date` | TEXT | LOCAL calendar key (`yyyy-mm-dd`) |
| `created_at` | TEXT | ISO UTC timestamp |
| `updated_at` | TEXT | ISO UTC timestamp |
| `deleted_at` | TEXT | Nullable tombstone |

### `memory_media` table

| Column | Type | Notes |
|---|---|---|
| `id` | TEXT PK | UUID v4 |
| `memory_id` | TEXT FK | References `memories.id` |
| `media_asset_id` | TEXT FK | References `media_assets.id` |
| `sort_order` | INTEGER | Display ordering |
| `created_at` | TEXT | ISO UTC timestamp |

**Indexes:**
- `idx_memories_memory_date` ON `memories(memory_date DESC)`
- `idx_memories_deleted_at` ON `memories(deleted_at)` WHERE `deleted_at IS NULL`
- `idx_memory_media_memory_id` ON `memory_media(memory_id)`
- `idx_memory_media_sort` ON `memory_media(memory_id, sort_order)`

**Soft-delete cascade:** When a memory is soft-deleted, `memory_media` rows are also soft-deleted by setting their `sort_order` to `-1`.

## Domain Model

### `Memory` (entity.ts conventions)
- UUID v4 id
- `title: string` — required, 1–200 chars
- `caption?: string` — optional free text
- `memoryDate: string` — LOCAL calendar key (`yyyy-mm-dd`)
- `createdAt: string` — ISO UTC timestamp
- `updatedAt: string` — ISO UTC timestamp
- `deletedAt?: string` — nullable tombstone

### `MemoryMedia`
- UUID v4 id
- `memoryId: string`
- `mediaAssetId: string`
- `sortOrder: number`
- `createdAt: string`

## Repository

`MemoryRepository` extends `BaseRepository<Memory>` and adds:
- `list()` — returns non-deleted memories ordered by `memory_date DESC`
- `getWithMedia(id)` — returns memory with associated media
- `get(id)` — single memory by id
- `create(memory)` — inserts with timestamps
- `update(id, changes)` — updates with `updatedAt` refresh
- `softDelete(id)` — sets `deletedAt` and cascade-deletes media
- `addMedia(memoryId, media)` — associates media asset
- `removeMedia(memoryId, mediaAssetId)` — removes media association

## Service

`MemoryService` provides:
- `createMemory(input)` — validates → creates memory → returns it
- `updateMemory(id, input)` — validates → updates → returns it
- `deleteMemory(id)` — soft-deletes memory + media references
- `getMemory(id)` — returns memory with media or null
- `listMemories()` — returns all non-deleted memories
- `addMedia(memoryId, mediaAssetId, sortOrder)` — attaches media
- `removeMedia(memoryId, mediaAssetId)` — detaches media

Validation:
- Title required, 1–200 chars
- Memory date must be valid `yyyy-mm-dd` format

Errors are wrapped as `AppError` with `category: 'validation'` or `'persistence'`.

## Media Handling

- Photos/videos are stored via the existing `MediaStorage` + `FileSystemAdapter` (Phase 2/3)
- `media_assets` table tracks actual file references (path, MIME type, size)
- `memory_media` join table links memories to their media assets
- Media limits: photo 25MB, video 500MB (from Phase 3 `mediaUtils`)
- Files stored in `media/` root via `@capacitor/filesystem`

## Screens

### MemoriesHome
- Grid gallery of memory cards (title, date, thumbnail)
- Empty state when no memories exist
- "Add Memory" floating action button
- Tapping a card navigates to MemoryDetail

### AddMemory
- Title input (required)
- Caption input (optional)
- Date picker (defaults to today)
- Photo/video selection (file input)
- Media preview thumbnails
- Validation errors shown inline
- Save creates memory + media associations

### MemoryDetail
- Full memory display (title, caption, date)
- Photo gallery with full-size viewing
- Video playback where supported
- Edit/delete actions
- Back navigation to MemoriesHome

## Navigation

Routes added in Phase 7:
- `/app/us/memories` — MemoriesHome (nested under Us tab)
- `/app/us/memories/add` — AddMemory
- `/app/us/memories/:id` — MemoryDetail

These are lazy-loaded via `React.lazy()`.

## Error Handling

All errors flow through the existing `AppError` architecture:
- Validation errors → `category: 'validation'`
- Persistence errors → `category: 'persistence'`
- Media errors → `category: 'persistence'`
- Safe user messages via `AppError.safeMessage`
- No internal paths or stack traces exposed to UI

## Testing

31 Phase 7 tests covering:
- Memory serialization/deserialization
- Database migration (memories + memory_media tables)
- Repository CRUD
- Repository ordering
- Repository cascade soft-delete
- Service validation
- Service create/update/delete
- Service not-found handling
- Memory date conventions
- MemoryMedia associations
- Timestamp conventions
- Title constraints
- Multiple media handling

## Limitations

- **Android native behavior not physically verified**: No Java/Android SDK in sandbox.
- **Media selection**: Uses HTML file input; native camera/gallery picker would be a Phase 8+ enhancement.
- **Video playback**: Basic HTML5 video; native player is a future enhancement.
- **Dark mode CSS tokens**: Only light-mode CSS tokens exist today.
- **No batch operations**: Bulk select/delete is not implemented.
- **No memory search**: Search is a future feature.

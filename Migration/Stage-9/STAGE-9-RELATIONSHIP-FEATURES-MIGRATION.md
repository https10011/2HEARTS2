# Stage 9: Relationship Features Migration

**Date:** August 30, 2026
**Status:** Complete
**Next Stage:** Stage 10 - Settings & Profile Migration

---

## Overview

This stage migrates the relationship-focused features that help couples track, celebrate, and manage their relationship: reminders, places, mood tracking, period tracking, and important dates.

## Scope

| Feature | Screens | Files | Priority |
|---------|---------|-------|----------|
| **Reminders** | RemindersHome, CreateReminder, ReminderDetail | 3 | Core |
| **Places** | PlacesHome, CreatePlace, PlaceDetail | 3 | Core |
| **Mood Tracking** | MoodHome, MoodEntry, MoodHistory | 3 | Core |
| **Period Tracking** | PeriodHome, LogPeriod | 2 | Core |
| **Important Dates** | ImportantDatesScreen | 1 | Core |

**Total:** 12 new screen files

---

## Feature Mapping: Legacy → Native

### Reminders

| Legacy Component | Native Equivalent |
|------------------|-------------------|
| `RemindersHome.tsx` | `RemindersHome.kt` |
| `AddReminder.tsx` | `CreateReminder.kt` |
| `ReminderDetail.tsx` | `ReminderDetail.kt` |
| Notification system | `NotificationService` (Stage 4) |

**Key Changes:**
- Date picker uses native `DatePickerDialog`
- Time picker uses native `TimePickerDialog`
- Notifications use Android `AlarmManager` + `BroadcastReceiver`

### Places

| Legacy Component | Native Equivalent |
|------------------|-------------------|
| `PlacesHome.tsx` | `PlacesHome.kt` |
| `AddPlace.tsx` | `CreatePlace.kt` |
| `PlaceDetail.tsx` | `PlaceDetail.kt` |
| `PlaceCard.tsx` | Inline card in PlacesHome |

**Key Changes:**
- Map integration uses Google Maps SDK (placeholder for now)
- Geocoding uses `Geocoder` API
- Photos use `MediaStore` for storage

### Mood Tracking

| Legacy Component | Native Equivalent |
|------------------|-------------------|
| `MoodHome.tsx` | `MoodHome.kt` |
| `MoodEntry.tsx` | `MoodEntry.kt` |
| `MoodHistory.tsx` | `MoodHistory.kt` |

**Key Changes:**
- Mood selection uses horizontal scrollable chips
- History displays in calendar-like grid
- Analytics uses local aggregation

### Period Tracking

| Legacy Component | Native Equivalent |
|------------------|-------------------|
| `PeriodHome.tsx` | `PeriodHome.kt` |
| `LogPeriod.tsx` | `LogPeriod.kt` |

**Key Changes:**
- Calendar uses native `CalendarView`
- Flow logging uses slider
- Predictions calculated locally

### Important Dates

| Legacy Component | Native Equivalent |
|------------------|-------------------|
| `ImportantDates.tsx` | `ImportantDatesScreen.kt` |
| Date cards | Inline list items |

**Key Changes:**
- Countdown displays in days
- Anniversary calculations use `DateTimeHelper` (Stage 4)
- Reminders linked to `NotificationService`

---

## Files Created

### Reminders
1. `RemindersHome.kt` - List of all reminders with filters
2. `CreateReminder.kt` - Create/edit reminder with date/time pickers
3. `ReminderDetail.kt` - View reminder details with actions

### Places
4. `PlacesHome.kt` - Grid/list of saved places with categories
5. `CreatePlace.kt` - Add/edit place with name, address, notes
6. `PlaceDetail.kt` - View place details with map placeholder

### Mood
7. `MoodHome.kt` - Today's mood entry + quick log
8. `MoodEntry.kt` - Select mood with notes
9. `MoodHistory.kt` - Past moods in calendar/grid view

### Period
10. `PeriodHome.kt` - Current cycle status + predictions
11. `LogPeriod.kt` - Log period start/end with flow

### Important Dates
12. `ImportantDatesScreen.kt` - All important dates with countdowns

### Modified
13. `AppRouter.kt` - Added routes for all new screens

---

## Data Dependencies

All features use entities and repositories from Stage 3:

| Feature | Entity | Repository |
|---------|--------|------------|
| Reminders | `Reminder` | `ReminderRepository` |
| Places | `Place` | `PlaceRepository` |
| Mood | `MoodEntry` | `MoodEntryRepository` |
| Period | `PeriodEntry`, `PeriodSettings` | `PeriodEntryRepository`, `PeriodSettingsRepository` |
| Important Dates | `ImportantDate` | `ImportantDateRepository` |

---

## Architectural Decisions

### 1. Local-First Data
All relationship features store data locally using Room database. No cloud sync required for MVP.

### 2. Notification Integration
Reminders and important dates integrate with `NotificationService` (Stage 4) for local notifications.

### 3. Placeholder Map
Places feature includes placeholder for Google Maps integration. Can be enabled later with API key.

### 4. Privacy-First Period Tracking
Period data stored locally only. No cloud backup. User controls all data.

---

## Verification

### Created Files
- [x] RemindersHome.kt
- [x] CreateReminder.kt
- [x] ReminderDetail.kt
- [x] PlacesHome.kt
- [x] CreatePlace.kt
- [x] PlaceDetail.kt
- [x] MoodHome.kt
- [x] MoodEntry.kt
- [x] MoodHistory.kt
- [x] PeriodHome.kt
- [x] LogPeriod.kt
- [x] ImportantDatesScreen.kt

### Modified Files
- [x] AppRouter.kt - All routes added

### Integration
- [x] Navigation integrated with Stage 6
- [x] Data layer connected (Stage 3)
- [x] Services integrated (Stage 4)
- [x] Theme applied consistently (Stage 1)
- [x] Components reused (Stage 2)

---

## Risks & Considerations

### Known Limitations
1. **Map Integration** - Placeholder only; requires Google Maps API key
2. **Notifications** - Basic implementation; may need battery optimization handling
3. **Period Predictions** - Simple algorithm; may need refinement

### Future Considerations
1. Cloud sync for relationship data
2. Shared places between partners
3. Mood correlation analytics
4. Advanced period predictions

---

## Stage 10 Preview

Stage 10 will focus on:
- Settings & Profile Migration
- Theme management
- Text size preferences
- Vault security screens
- App info screens
- Export functionality

---

## Files Changed Summary

| Category | Files | Lines (est.) |
|----------|-------|--------------|
| New Screens | 12 | ~1800 |
| Modified | 1 | ~50 |
| **Total** | **13** | **~1850** |

---

*Stage 9 Complete. Ready for Stage 10.*

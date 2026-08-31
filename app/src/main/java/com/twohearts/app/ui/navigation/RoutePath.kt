package com.twohearts.app.ui.navigation

/**
 * RoutePath — all route constants matching legacy routes exactly.
 *
 * Must preserve all route paths to maintain deep links/bookmarks.
 */
object RoutePath {
    // Onboarding
    const val ONBOARDING_WELCOME = "/onboarding/welcome"
    const val ONBOARDING_PROFILE = "/onboarding/profile"
    const val ONBOARDING_RELATIONSHIP = "/onboarding/relationship"
    const val ONBOARDING_PERSONALIZATION = "/onboarding/personalization"
    const val ONBOARDING_APP_LOCK = "/onboarding/app-lock"
    const val ONBOARDING_COMPLETE = "/onboarding/complete"

    // Main app
    const val APP_HOME = "/app/home"
    const val APP_US = "/app/us"
    const val APP_MORE = "/app/more"
    const val APP_NOTIFICATIONS = "/app/notifications"
    const val APP_SEARCH = "/app/search"
    const val APP_NOTES = "/app/notes"
    const val APP_NOTES_ADD = "/app/notes/add"
    const val APP_NOTES_EDIT = "/app/notes/:noteId/edit"
    const val APP_NOTES_DETAIL = "/app/notes/:noteId"

    // Memories
    const val APP_MEMORIES = "/app/memories"
    const val APP_MEMORIES_ADD = "/app/memories/add"
    const val APP_MEMORIES_EDIT = "/app/memories/:memoryId/edit"
    const val APP_MEMORIES_DETAIL = "/app/memories/:memoryId"

    // Timeline
    const val APP_TIMELINE = "/app/timeline"
    const val APP_TIMELINE_ADD = "/app/timeline/add"
    const val APP_TIMELINE_EDIT = "/app/timeline/:eventId/edit"
    const val APP_TIMELINE_DETAIL = "/app/timeline/:eventId"

    // Reminders
    const val APP_REMINDERS = "/app/reminders"
    const val APP_REMINDERS_ADD = "/app/reminders/add"
    const val APP_REMINDERS_EDIT = "/app/reminders/:reminderId/edit"
    const val APP_REMINDERS_DETAIL = "/app/reminders/:reminderId"

    // Places
    const val APP_PLACES = "/app/places"
    const val APP_PLACES_ADD = "/app/places/add"
    const val APP_PLACES_EDIT = "/app/places/:placeId/edit"
    const val APP_PLACES_DETAIL = "/app/places/:placeId"

    // Mood
    const val APP_MOOD = "/app/mood"
    const val APP_MOOD_ADD = "/app/mood/add"
    const val APP_MOOD_EDIT = "/app/mood/:entryId/edit"
    const val APP_MOOD_HISTORY = "/app/mood/history"

    // Period
    const val APP_PERIOD = "/app/period"
    const val APP_PERIOD_LOG = "/app/period/log"
    const val APP_PERIOD_LOG_EDIT = "/app/period/:entryId/edit"
    const val APP_PERIOD_CALENDAR = "/app/period/calendar"
    const val APP_PERIOD_HISTORY = "/app/period/history"
    const val APP_PERIOD_SETTINGS = "/app/period/settings"

    // Vault
    const val APP_VAULT = "/app/vault"
    const val APP_VAULT_ADD = "/app/vault/add"
    const val APP_VAULT_CONTENT = "/app/vault/content/:contentId"

    // Yuki
    const val APP_YUKI = "/app/yuki"

    // Games — ARCHIVED (Stage 12).
    // Legacy games (Memory Match, Word Scramble, Couple Trivia, Who Knows,
    // Would You Rather, This Or That, Guess My Answer, Casual Trivia,
    // Riddle Room) were superseded by Yuki companion (Stage 11).
    // All game routes redirect to APP_YUKI for backward compatibility.
    // Full legacy game source preserved in Archive/Legacy-React-Vite-Capacitor/
    const val APP_GAMES = "/app/games"
    const val APP_GAMES_MEMORY_MATCH = "/app/games/memory-match"
    const val APP_GAMES_WORD_SCRAMBLE = "/app/games/word-scramble"
    const val APP_GAMES_COUPLE_TRIVIA = "/app/games/couple-trivia"
    const val APP_GAMES_WHO_KNOWS = "/app/games/who-knows-who-better"
    const val APP_GAMES_WOULD_YOU_RATHER = "/app/games/would-you-rather"
    const val APP_GAMES_THIS_OR_THAT = "/app/games/this-or-that"
    const val APP_GAMES_GUESS_MY_ANSWER = "/app/games/guess-my-answer"
    const val APP_GAMES_CASUAL_TRIVIA = "/app/games/casual-trivia"
    const val APP_GAMES_RIDDLE_ROOM = "/app/games/riddle-room"
    const val APP_GAMES_RESULTS = "/app/games/results"

    // Settings
    const val APP_SETTINGS = "/app/settings"
    const val APP_SETTINGS_PROFILE = "/app/settings/profile"
    const val APP_SETTINGS_RELATIONSHIP = "/app/settings/relationship"
    const val APP_SETTINGS_APPEARANCE = "/app/settings/appearance"
    const val APP_SETTINGS_NOTIFICATIONS = "/app/settings/notifications"
    const val APP_SETTINGS_SECURITY = "/app/settings/security"
    const val APP_SETTINGS_STORAGE = "/app/settings/storage"
    const val APP_SETTINGS_IMPORT = "/app/settings/import"
    const val APP_ABOUT = "/app/about"

    // Important dates
    const val APP_IMPORTANT_DATES = "/app/us/important-dates"
}

package com.twohearts.app.services.datetime

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs

/**
 * DateTimeHelper — date/time utilities matching legacy helpers.
 *
 * Pure functions for:
 * - Age calculation
 * - Anniversary math
 * - Local-day diffs
 * - Date formatting
 * - Date parsing
 */
object DateTimeHelper {

    private val isoFormatter = DateTimeFormatter.ISO_INSTANT
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val displayFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
    private val shortDisplayFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

    /**
     * Get current ISO 8601 UTC timestamp.
     */
    fun nowUtc(): String = Instant.now().toString()

    /**
     * Get current LOCAL date key (yyyy-MM-dd).
     */
    fun todayLocal(): String = LocalDate.now().format(dateFormatter)

    /**
     * Calculate age from birthday.
     * Matches legacy calculateAge() exactly.
     */
    fun calculateAge(birthday: String): Int {
        return try {
            val birthDate = LocalDate.parse(birthday, dateFormatter)
            val today = LocalDate.now()
            ChronoUnit.YEARS.between(birthDate, today).toInt()
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Calculate days until anniversary.
     * Matches legacy daysUntilAnniversary() exactly.
     */
    fun daysUntilAnniversary(startDate: String): Int {
        return try {
            val start = LocalDate.parse(startDate, dateFormatter)
            val today = LocalDate.now()
            var nextAnniversary = start.withYear(today.year)
            if (nextAnniversary.isBefore(today)) {
                nextAnniversary = nextAnniversary.plusYears(1)
            }
            ChronoUnit.DAYS.between(today, nextAnniversary).toInt()
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Calculate days since start date.
     * Matches legacy daysSinceStartDate() exactly.
     */
    fun daysSinceStartDate(startDate: String): Int {
        return try {
            val start = LocalDate.parse(startDate, dateFormatter)
            val today = LocalDate.now()
            abs(ChronoUnit.DAYS.between(start, today).toInt())
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Calculate days between two dates.
     * Matches legacy daysBetween() exactly.
     */
    fun daysBetween(date1: String, date2: String): Int {
        return try {
            val d1 = LocalDate.parse(date1, dateFormatter)
            val d2 = LocalDate.parse(date2, dateFormatter)
            abs(ChronoUnit.DAYS.between(d1, d2).toInt())
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Format date for display.
     */
    fun formatDisplay(date: String): String {
        return try {
            val localDate = LocalDate.parse(date, dateFormatter)
            localDate.format(displayFormatter)
        } catch (e: Exception) {
            date
        }
    }

    /**
     * Format date for short display.
     */
    fun formatShortDisplay(date: String): String {
        return try {
            val localDate = LocalDate.parse(date, dateFormatter)
            localDate.format(shortDisplayFormatter)
        } catch (e: Exception) {
            date
        }
    }

    /**
     * Parse ISO timestamp to LocalDateTime.
     */
    fun parseTimestamp(timestamp: String): LocalDateTime? {
        return try {
            LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_DATE_TIME)
        } catch (e: Exception) {
            try {
                Instant.parse(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime()
            } catch (e2: Exception) {
                null
            }
        }
    }

    /**
     * Format LocalDateTime to ISO timestamp.
     */
    fun formatTimestamp(dateTime: LocalDateTime): String {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toString()
    }

    /**
     * Check if date is today.
     */
    fun isToday(date: String): Boolean {
        return try {
            val localDate = LocalDate.parse(date, dateFormatter)
            localDate == LocalDate.now()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if date is in the past.
     */
    fun isPast(date: String): Boolean {
        return try {
            val localDate = LocalDate.parse(date, dateFormatter)
            localDate.isBefore(LocalDate.now())
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if date is in the future.
     */
    fun isFuture(date: String): Boolean {
        return try {
            val localDate = LocalDate.parse(date, dateFormatter)
            localDate.isAfter(LocalDate.now())
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Add days to a date.
     */
    fun addDays(date: String, days: Int): String {
        return try {
            val localDate = LocalDate.parse(date, dateFormatter)
            localDate.plusDays(days.toLong()).format(dateFormatter)
        } catch (e: Exception) {
            date
        }
    }

    /**
     * Get month name from date.
     */
    fun getMonthName(date: String): String {
        return try {
            val localDate = LocalDate.parse(date, dateFormatter)
            localDate.month.name.lowercase().replaceFirstChar { it.uppercase() }
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Get year from date.
     */
    fun getYear(date: String): Int {
        return try {
            val localDate = LocalDate.parse(date, dateFormatter)
            localDate.year
        } catch (e: Exception) {
            0
        }
    }
}

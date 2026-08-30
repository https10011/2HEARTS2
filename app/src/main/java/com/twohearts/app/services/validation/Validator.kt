package com.twohearts.app.services.validation

/**
 * ValidationResult — result of a validation check.
 * Matches legacy {ok, errors} pattern exactly.
 */
data class ValidationResult(
    val ok: Boolean,
    val errors: List<ValidationError> = emptyList()
) {
    companion object {
        fun success() = ValidationResult(ok = true)
        fun failure(vararg errors: ValidationError) = ValidationResult(ok = false, errors = errors.toList())
    }
}

/**
 * ValidationError — a single validation error.
 */
data class ValidationError(
    val field: String,
    val message: String,
    val code: String
)

/**
 * Validator — pure validation functions matching legacy validators.
 *
 * All validators are pure functions that return ValidationResult.
 * No side effects, no database calls, no async.
 */
object Validator {

    /**
     * Validate required field.
     */
    fun required(value: String?, fieldName: String): ValidationResult {
        return if (value.isNullOrBlank()) {
            ValidationResult.failure(
                ValidationError(fieldName, "$fieldName is required", "REQUIRED")
            )
        } else {
            ValidationResult.success()
        }
    }

    /**
     * Validate string length.
     */
    fun length(
        value: String?,
        fieldName: String,
        min: Int = 0,
        max: Int = Int.MAX_VALUE
    ): ValidationResult {
        if (value == null) {
            return if (min > 0) {
                ValidationResult.failure(
                    ValidationError(fieldName, "$fieldName is required", "REQUIRED")
                )
            } else {
                ValidationResult.success()
            }
        }

        val errors = mutableListOf<ValidationError>()
        if (value.length < min) {
            errors.add(ValidationError(fieldName, "$fieldName must be at least $min characters", "TOO_SHORT"))
        }
        if (value.length > max) {
            errors.add(ValidationError(fieldName, "$fieldName must be at most $max characters", "TOO_LONG"))
        }

        return if (errors.isEmpty()) ValidationResult.success() else ValidationResult(ok = false, errors = errors)
    }

    /**
     * Validate date format (yyyy-mm-dd).
     */
    fun date(value: String?, fieldName: String): ValidationResult {
        if (value.isNullOrBlank()) {
            return ValidationResult.failure(
                ValidationError(fieldName, "$fieldName is required", "REQUIRED")
            )
        }

        val dateRegex = """^\d{4}-\d{2}-\d{2}$""".toRegex()
        return if (dateRegex.matches(value)) {
            ValidationResult.success()
        } else {
            ValidationResult.failure(
                ValidationError(fieldName, "$fieldName must be in yyyy-mm-dd format", "INVALID_DATE")
            )
        }
    }

    /**
     * Validate email format.
     */
    fun email(value: String?, fieldName: String): ValidationResult {
        if (value.isNullOrBlank()) {
            return ValidationResult.failure(
                ValidationError(fieldName, "$fieldName is required", "REQUIRED")
            )
        }

        val emailRegex = """^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$""".toRegex()
        return if (emailRegex.matches(value)) {
            ValidationResult.success()
        } else {
            ValidationResult.failure(
                ValidationError(fieldName, "Invalid email format", "INVALID_EMAIL")
            )
        }
    }

    /**
     * Validate PIN (4-8 digits).
     */
    fun pin(value: String?): ValidationResult {
        if (value.isNullOrBlank()) {
            return ValidationResult.failure(
                ValidationError("pin", "PIN is required", "REQUIRED")
            )
        }

        val pinRegex = """^\d{4,8}$""".toRegex()
        return if (pinRegex.matches(value)) {
            ValidationResult.success()
        } else {
            ValidationResult.failure(
                ValidationError("pin", "PIN must be 4-8 digits", "INVALID_PIN")
            )
        }
    }

    /**
     * Validate note category.
     */
    fun noteCategory(value: String?): ValidationResult {
        val validCategories = setOf(
            "general", "shared", "private", "love-letter",
            "gratitude", "idea", "reminder"
        )

        return if (value in validCategories) {
            ValidationResult.success()
        } else {
            ValidationResult.failure(
                ValidationError("category", "Invalid note category", "INVALID_CATEGORY")
            )
        }
    }

    /**
     * Validate mood value.
     */
    fun moodValue(value: String?): ValidationResult {
        val validMoods = setOf(
            "happy", "love", "excited", "calm", "grateful",
            "neutral", "tired", "sad", "anxious", "stressed"
        )

        return if (value in validMoods) {
            ValidationResult.success()
        } else {
            ValidationResult.failure(
                ValidationError("mood", "Invalid mood value", "INVALID_MOOD")
            )
        }
    }

    /**
     * Validate period flow level.
     */
    fun flowLevel(value: String?): ValidationResult {
        val validLevels = setOf("light", "medium", "heavy")

        return if (value in validLevels) {
            ValidationResult.success()
        } else {
            ValidationResult.failure(
                ValidationError("flowLevel", "Invalid flow level", "INVALID_FLOW_LEVEL")
            )
        }
    }

    /**
     * Validate recurrence.
     */
    fun recurrence(value: String?): ValidationResult {
        val validRecurrences = setOf("none", "daily", "weekly", "monthly", "yearly")

        return if (value in validRecurrences) {
            ValidationResult.success()
        } else {
            ValidationResult.failure(
                ValidationError("recurrence", "Invalid recurrence", "INVALID_RECURRENCE")
            )
        }
    }

    /**
     * Validate place category.
     */
    fun placeCategory(value: String?): ValidationResult {
        val validCategories = setOf(
            "general", "restaurant", "park", "home", "travel", "other"
        )

        return if (value in validCategories) {
            ValidationResult.success()
        } else {
            ValidationResult.failure(
                ValidationError("category", "Invalid place category", "INVALID_CATEGORY")
            )
        }
    }

    /**
     * Combine multiple validation results.
     */
    fun combine(vararg results: ValidationResult): ValidationResult {
        val allErrors = results.flatMap { it.errors }
        return if (allErrors.isEmpty()) {
            ValidationResult.success()
        } else {
            ValidationResult(ok = false, errors = allErrors)
        }
    }
}

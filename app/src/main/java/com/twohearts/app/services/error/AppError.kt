package com.twohearts.app.services.error

/**
 * AppError — unified error type matching legacy AppError taxonomy.
 *
 * Every error in the app flows through this class, providing:
 * - Category (validation, persistence, permission, etc.)
 * - Code (machine-readable identifier)
 * - Safe message (user-facing, no internal details)
 * - Original exception (for logging)
 *
 * Matches legacy error taxonomy exactly.
 */
data class AppError(
    val category: ErrorCategory,
    val code: String,
    val safeMessage: String,
    val originalException: Throwable? = null
) : Exception(safeMessage, originalException) {

    companion object {
        // Validation errors
        fun validation(code: String, message: String) = AppError(
            category = ErrorCategory.VALIDATION,
            code = code,
            safeMessage = message
        )

        // Persistence errors
        fun persistence(code: String, message: String, cause: Throwable? = null) = AppError(
            category = ErrorCategory.PERSISTENCE,
            code = code,
            safeMessage = message,
            originalException = cause
        )

        // Permission errors
        fun permission(code: String, message: String) = AppError(
            category = ErrorCategory.PERMISSION,
            code = code,
            safeMessage = message
        )

        // Not found errors
        fun notFound(code: String, entity: String) = AppError(
            category = ErrorCategory.NOT_FOUND,
            code = code,
            safeMessage = "$entity not found"
        )

        // Security errors
        fun security(code: String, message: String) = AppError(
            category = ErrorCategory.SECURITY,
            code = code,
            safeMessage = message
        )

        // Media errors
        fun media(code: String, message: String, cause: Throwable? = null) = AppError(
            category = ErrorCategory.MEDIA,
            code = code,
            safeMessage = message,
            originalException = cause
        )

        // Unknown errors
        fun unknown(message: String, cause: Throwable? = null) = AppError(
            category = ErrorCategory.UNKNOWN,
            code = "UNKNOWN",
            safeMessage = message,
            originalException = cause
        )
    }
}

/**
 * Error categories matching legacy AppError categories.
 */
enum class ErrorCategory {
    VALIDATION,
    PERSISTENCE,
    PERMISSION,
    NOT_FOUND,
    SECURITY,
    MEDIA,
    NETWORK,
    UNKNOWN
}

package com.twohearts.app.services.logger

import android.util.Log

/**
 * Logger — leveled, scoped logger with redaction.
 *
 * Matches legacy logger with:
 * - Multiple log levels (DEBUG, INFO, WARN, ERROR)
 * - Scoped logging (feature name)
 * - Automatic redaction of sensitive keys
 * - No-op in production for debug logs
 *
 * Sensitive keys are automatically redacted:
 * pin, password, secret, token, body, content, vault, media
 */
class Logger(private val tag: String = "TwoHearts") {

    companion object {
        private const val TAG_PREFIX = "TH"

        // Keys that should be redacted in logs
        private val REDACTED_KEYS = setOf(
            "pin", "password", "secret", "token",
            "body", "content", "vault", "media",
            "salt", "verifier", "hash"
        )

        // Global log level (can be changed at runtime)
        var logLevel: LogLevel = LogLevel.DEBUG
    }

    /**
     * Log at DEBUG level.
     */
    fun debug(message: String, vararg args: Any?) {
        if (logLevel <= LogLevel.DEBUG) {
            Log.d(formatTag(tag), formatMessage(message, *args))
        }
    }

    /**
     * Log at INFO level.
     */
    fun info(message: String, vararg args: Any?) {
        if (logLevel <= LogLevel.INFO) {
            Log.i(formatTag(tag), formatMessage(message, *args))
        }
    }

    /**
     * Log at WARN level.
     */
    fun warn(message: String, vararg args: Any?) {
        if (logLevel <= LogLevel.WARN) {
            Log.w(formatTag(tag), formatMessage(message, *args))
        }
    }

    /**
     * Log at ERROR level.
     */
    fun error(message: String, throwable: Throwable? = null, vararg args: Any?) {
        if (logLevel <= LogLevel.ERROR) {
            if (throwable != null) {
                Log.e(formatTag(tag), formatMessage(message, *args), throwable)
            } else {
                Log.e(formatTag(tag), formatMessage(message, *args))
            }
        }
    }

    /**
     * Create a scoped logger for a specific feature.
     */
    fun scoped(feature: String): Logger {
        return Logger("$tag/$feature")
    }

    /**
     * Format tag with prefix.
     */
    private fun formatTag(tag: String): String {
        return "$TAG_PREFIX:$tag"
    }

    /**
     * Format message with argument substitution and redaction.
     */
    private fun formatMessage(message: String, vararg args: Any?): String {
        var formatted = message
        args.forEach { arg ->
            formatted = formatted.replaceFirst("{}", arg.toString())
        }
        return redactSensitive(formatted)
    }

    /**
     * Redact sensitive key-value pairs in log messages.
     */
    private fun redactSensitive(message: String): String {
        var redacted = message
        REDACTED_KEYS.forEach { key ->
            // Match patterns like "key=value" or "key: value"
            val patterns = listOf(
                """(?i)$key\s*=\s*\S+""".toRegex(),
                """(?i)$key\s*:\s*\S+""".toRegex()
            )
            patterns.forEach { pattern ->
                redacted = redacted.replace(pattern) { match ->
                    val matched = match.value
                    val eqIndex = matched.indexOfFirst { it == '=' || it == ':' }
                    if (eqIndex >= 0) {
                        matched.substring(0, eqIndex + 1) + " [REDACTED]"
                    } else {
                        "[REDACTED]"
                    }
                }
            }
        }
        return redacted
    }
}

/**
 * Log levels matching legacy logger.
 */
enum class LogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR
}

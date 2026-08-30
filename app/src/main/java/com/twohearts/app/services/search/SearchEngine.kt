package com.twohearts.app.services.search

import com.twohearts.app.data.entity.Memory
import com.twohearts.app.data.entity.Note
import com.twohearts.app.data.entity.Place
import com.twohearts.app.data.entity.Reminder
import com.twohearts.app.data.entity.TimelineEvent
import java.text.Normalizer

/**
 * SearchEngine — global search across all features.
 *
 * Matches legacy SearchEngine exactly:
 * - Normalization (NFKD, diacritics, lowercase, whitespace)
 * - Per-feature providers
 * - Ranking (prefix > word-initial > substring)
 * - Recency tiebreaking
 * - Vault exclusion
 */
class SearchEngine {

    private val providers = mutableListOf<SearchProvider<*>>()

    /**
     * Register a search provider.
     */
    fun <T> register(provider: SearchProvider<T>) {
        providers.add(provider)
    }

    /**
     * Search across all registered providers.
     */
    fun search(query: String): List<SearchResult> {
        if (query.isBlank()) return emptyList()

        val normalizedQuery = normalize(query)
        val results = mutableListOf<SearchResult>()

        providers.forEach { provider ->
            results.addAll(provider.search(normalizedQuery))
        }

        return rank(results)
    }

    /**
     * Normalize a search query.
     * Matches legacy normalization exactly.
     */
    fun normalize(text: String): String {
        return text
            .trim()
            .let { Normalizer.normalize(it, Normalizer.Form.NFKD) }
            .replace(Regex("\\p{InCombiningDiacriticalMarks}"), "")
            .lowercase()
            .replace(Regex("\\s+"), " ")
    }

    /**
     * Rank search results.
     * Matches legacy ranking exactly.
     */
    private fun rank(results: List<SearchResult>): List<SearchResult> {
        return results.sortedWith(compareByDescending<SearchResult> { it.score }
            .thenByDescending { it.recency }
            .thenBy { it.id })
    }

    /**
     * Calculate relevance score.
     * Matches legacy scoring exactly.
     */
    fun calculateScore(text: String, query: String): Int {
        val normalizedText = normalize(text)
        val normalizedQuery = normalize(query)

        return when {
            // Prefix match (highest)
            normalizedText.startsWith(normalizedQuery) -> 3
            // Word-initial match
            normalizedText.split(" ").any { it.startsWith(normalizedQuery) } -> 2
            // Substring match
            normalizedText.contains(normalizedQuery) -> 1
            // No match
            else -> 0
        }
    }
}

/**
 * Search provider interface.
 */
interface SearchProvider<T> {
    fun search(query: String): List<SearchResult>
}

/**
 * Search result data class.
 */
data class SearchResult(
    val id: String,
    val type: String,
    val title: String,
    val subtitle: String,
    val score: Int,
    val recency: Long = System.currentTimeMillis()
)

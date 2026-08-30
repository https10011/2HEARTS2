package com.twohearts.app.services.relationship

import com.twohearts.app.data.entity.CoupleRelationship
import com.twohearts.app.data.entity.Profile
import com.twohearts.app.data.repository.CoupleRelationshipRepository
import com.twohearts.app.data.repository.ProfileRepository
import com.twohearts.app.services.datetime.DateTimeHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * RelationshipService — couple relationship management.
 *
 * Matches legacy RelationshipService with:
 * - Get owner/partner profiles
 * - Get couple relationship
 * - Calculate relationship counter
 * - Update relationship info
 */
class RelationshipService(
    private val profileRepository: ProfileRepository,
    private val relationshipRepository: CoupleRelationshipRepository
) {

    /**
     * Observe the couple relationship.
     */
    fun observeRelationship(): Flow<CoupleRelationship?> {
        return relationshipRepository.observe()
    }

    /**
     * Observe owner profile.
     */
    fun observeOwner(): Flow<Profile?> {
        return profileRepository.observeAll().map { profiles ->
            profiles.find { it.role == "owner" }
        }
    }

    /**
     * Observe partner profile.
     */
    fun observePartner(): Flow<Profile?> {
        return profileRepository.observeAll().map { profiles ->
            profiles.find { it.role == "partner" }
        }
    }

    /**
     * Get owner profile (non-reactive).
     */
    suspend fun getOwner(): Profile? {
        return profileRepository.getByRole("owner")
    }

    /**
     * Get partner profile (non-reactive).
     */
    suspend fun getPartner(): Profile? {
        return profileRepository.getByRole("partner")
    }

    /**
     * Get couple relationship (non-reactive).
     */
    suspend fun getRelationship(): CoupleRelationship? {
        return relationshipRepository.get()
    }

    /**
     * Create owner profile.
     */
    suspend fun createOwner(name: String, birthday: String?): Profile {
        val profile = Profile(
            name = name,
            birthday = birthday,
            role = "owner",
            id = com.twohearts.app.data.repository.generateId(),
            createdAt = DateTimeHelper.nowUtc(),
            updatedAt = DateTimeHelper.nowUtc()
        )
        return profileRepository.create(profile)
    }

    /**
     * Create partner profile.
     */
    suspend fun createPartner(name: String, birthday: String?): Profile {
        val profile = Profile(
            name = name,
            birthday = birthday,
            role = "partner",
            id = com.twohearts.app.data.repository.generateId(),
            createdAt = DateTimeHelper.nowUtc(),
            updatedAt = DateTimeHelper.nowUtc()
        )
        return profileRepository.create(profile)
    }

    /**
     * Create couple relationship.
     */
    suspend fun createRelationship(ownerId: String, partnerId: String, startDate: String): CoupleRelationship {
        val relationship = CoupleRelationship(
            ownerId = ownerId,
            partnerId = partnerId,
            startDate = startDate,
            id = com.twohearts.app.data.repository.generateId(),
            createdAt = DateTimeHelper.nowUtc(),
            updatedAt = DateTimeHelper.nowUtc()
        )
        return relationshipRepository.create(relationship)
    }

    /**
     * Calculate days since start date.
     */
    fun calculateDaysTogether(startDate: String): Int {
        return DateTimeHelper.daysSinceStartDate(startDate)
    }

    /**
     * Calculate days until next anniversary.
     */
    fun calculateDaysUntilAnniversary(startDate: String): Int {
        return DateTimeHelper.daysUntilAnniversary(startDate)
    }

    /**
     * Update owner profile.
     */
    suspend fun updateOwner(id: String, name: String?, birthday: String?) {
        val changes = mutableMapOf<String, Any?>()
        name?.let { changes["name"] = it }
        birthday?.let { changes["birthday"] = it }
        profileRepository.update(id, changes)
    }

    /**
     * Update partner profile.
     */
    suspend fun updatePartner(id: String, name: String?, birthday: String?) {
        val changes = mutableMapOf<String, Any?>()
        name?.let { changes["name"] = it }
        birthday?.let { changes["birthday"] = it }
        profileRepository.update(id, changes)
    }

    /**
     * Update relationship.
     */
    suspend fun updateRelationship(id: String, startDate: String?) {
        val changes = mutableMapOf<String, Any?>()
        startDate?.let { changes["startDate"] = it }
        relationshipRepository.update(id, changes)
    }
}

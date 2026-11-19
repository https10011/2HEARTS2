package com.twohearts.app.ui.onboarding

import com.twohearts.app.data.settings.OnboardingDraft
import com.twohearts.app.services.media.ProfilePhotoChange
import com.twohearts.app.services.media.ProfilePhotoStaging
import com.twohearts.app.services.media.ProfilePhotoWriter
import com.twohearts.app.services.relationship.RelationshipService

/** Commits both roles, their photos, and relationship data together. */
internal class OnboardingDomainWriter(
    private val photos: ProfilePhotoWriter,
    private val staging: ProfilePhotoStaging,
    private val relationships: RelationshipService,
) {
    suspend fun save(draft: OnboardingDraft) {
        require(resumeOnboardingStage(OnboardingStage.APP_LOCK, draft) == OnboardingStage.APP_LOCK)
        val ownerChange = restoredPhoto(draft.ownerPhotoDraft)
        val partnerChange = restoredPhoto(draft.partnerPhotoDraft)
        photos.commit(listOf(ownerChange, partnerChange)) { (ownerReplacement, partnerReplacement) ->
            val existingOwner = relationships.getOwner()
            val ownerRef = ownerChange.reference(existingOwner?.photoRef, ownerReplacement)
            val owner = if (existingOwner == null) {
                relationships.createOwner(draft.ownerName, draft.ownerBirthday, ownerRef)
            } else {
                relationships.saveProfileDetails(existingOwner.id, draft.ownerName, draft.ownerBirthday, ownerRef)
            }

            val existingPartner = relationships.getPartner()
            val partnerRef = partnerChange.reference(existingPartner?.photoRef, partnerReplacement)
            val partner = if (existingPartner == null) {
                relationships.createPartner(draft.partnerName, draft.partnerBirthday, partnerRef)
            } else {
                relationships.saveProfileDetails(existingPartner.id, draft.partnerName, draft.partnerBirthday, partnerRef)
            }

            val existingRelationship = relationships.getRelationship()
            if (existingRelationship == null) {
                relationships.createRelationship(owner.id, partner.id, draft.startDate)
            } else {
                relationships.updateRelationship(existingRelationship.id, draft.startDate)
            }
            listOfNotNull(
                existingOwner?.photoRef?.takeUnless { it == ownerRef },
                existingPartner?.photoRef?.takeUnless { it == partnerRef },
            )
        }
    }

    private fun restoredPhoto(draftId: String?): ProfilePhotoChange =
        if (draftId != null && staging.croppedUri(draftId) != null) ProfilePhotoChange.Replace(draftId)
        else ProfilePhotoChange.Keep
}

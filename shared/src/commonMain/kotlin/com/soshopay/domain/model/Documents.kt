package com.soshopay.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Documents(
    val proofOfResidence: Document? = null,
    val nationalId: Document? = null,
) {
    fun getAllDocuments(): List<Document> = listOfNotNull(proofOfResidence, nationalId)

    fun isAllDocumentsUploaded(): Boolean = proofOfResidence != null && nationalId != null

    fun hasVerifiedDocuments(): Boolean = getAllDocuments().any { it.isVerified() }
}

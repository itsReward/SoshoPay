package com.soshopay.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Document(
    val id: String,
    val url: String,
    val fileName: String,
    val fileSize: String,
    val uploadDate: Long,
    val lastUpdated: Long,
    val verificationStatus: VerificationStatus,
    val verificationDate: Long? = null,
    val verificationNotes: String? = null,
    val documentType: DocumentType,
) {
    fun isVerified(): Boolean = verificationStatus == VerificationStatus.VERIFIED

    fun isExpired(): Boolean {
        // Document expiry logic - for now, documents don't expire
        return false
    }
}

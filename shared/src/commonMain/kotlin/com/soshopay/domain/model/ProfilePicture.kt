package com.soshopay.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ProfilePicture(
    val url: String,
    val uploadDate: Long,
    val lastUpdated: Long,
    val fileName: String? = null,
    val fileSize: String? = null,
) {
    fun isValid(): Boolean = url.isNotBlank() && uploadDate > 0
}

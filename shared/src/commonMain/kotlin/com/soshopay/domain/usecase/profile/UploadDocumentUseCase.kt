package com.soshopay.domain.usecase.profile

import com.soshopay.domain.model.DocumentType
import com.soshopay.domain.repository.ProfileRepository
import com.soshopay.domain.util.Result

class UploadDocumentUseCase(
    private val profileRepository: ProfileRepository,
) {
    suspend fun uploadDocuments(documents: Map<DocumentType, Pair<ByteArray, String>>): Result<Unit> =
        profileRepository.uploadDocuments(documents)

    suspend fun replaceDocument(
        documentType: DocumentType,
        document: ByteArray,
        fileName: String,
    ): Result<Unit> = profileRepository.replaceDocument(documentType, document, fileName)
}

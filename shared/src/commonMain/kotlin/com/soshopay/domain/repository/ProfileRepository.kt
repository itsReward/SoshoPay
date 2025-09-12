package com.soshopay.domain.repository

import com.soshopay.domain.model.Address
import com.soshopay.domain.model.DocumentType
import com.soshopay.domain.model.Documents
import com.soshopay.domain.model.NextOfKin
import com.soshopay.domain.model.PersonalDetails
import com.soshopay.domain.model.User
import com.soshopay.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    suspend fun getUserProfile(forceRefresh: Boolean = false): Result<User>

    suspend fun updatePersonalDetails(personalDetails: PersonalDetails): Result<Unit>

    suspend fun updateAddress(address: Address): Result<Unit>

    suspend fun uploadProfilePicture(
        imageBytes: ByteArray,
        fileName: String,
    ): Result<String>

    suspend fun uploadDocuments(documents: Map<DocumentType, Pair<ByteArray, String>>): Result<Unit>

    suspend fun replaceDocument(
        documentType: DocumentType,
        document: ByteArray,
        fileName: String,
    ): Result<Unit>

    suspend fun getDocuments(): Result<Documents>

    suspend fun updateNextOfKin(nextOfKin: NextOfKin): Result<Unit>

    suspend fun getNextOfKin(): Result<NextOfKin>

    suspend fun getAvailableClientTypes(): Result<List<String>>

    suspend fun requestClientTypeChange(newType: String): Result<Unit>

    suspend fun syncProfile(): Result<Unit>

    fun observeProfile(): Flow<User?>
}

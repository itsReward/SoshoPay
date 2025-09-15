package com.soshopay.domain.repository

import com.soshopay.domain.model.Address
import com.soshopay.domain.model.DocumentType
import com.soshopay.domain.model.Documents
import com.soshopay.domain.model.NextOfKin
import com.soshopay.domain.model.PersonalDetails
import com.soshopay.domain.model.User
import com.soshopay.domain.util.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository abstraction for user profile management in the SoshoPay domain.
 *
 * Implementations of this interface handle all profile-related workflows, including retrieval and update
 * of personal details, address, profile picture, documents, next of kin, client type changes, and profile
 * synchronization. All operations return a [Result] type for robust error handling and success/failure reporting,
 * or a [Flow] for reactive updates.
 *
 * **Profile Retrieval & Update:**
 * - Get and update user profile, personal details, address, and next of kin.
 *
 * **Document Management:**
 * - Upload profile picture, upload/replace documents, and retrieve documents.
 *
 * **Client Type & Sync:**
 * - Get available client types, request client type change, and synchronize profile data.
 *
 * **Reactive Updates:**
 * - Observe profile changes as a [Flow].
 *
 * All suspend functions support asynchronous/coroutine-based execution.
 */
interface ProfileRepository {
    /**
     * Retrieves the user's profile, optionally forcing a refresh from the remote source.
     * @param forceRefresh If true, forces a refresh from the remote source.
     * @return [Result] containing the [User] if successful, or an error.
     */
    suspend fun getUserProfile(forceRefresh: Boolean = false): Result<User>

    /**
     * Updates the user's personal details.
     * @param personalDetails The new personal details to update.
     * @return [Result] containing [Unit] if successful, or an error.
     */
    suspend fun updatePersonalDetails(personalDetails: PersonalDetails): Result<Unit>

    /**
     * Updates the user's address information.
     * @param address The new address to update.
     * @return [Result] containing [Unit] if successful, or an error.
     */
    suspend fun updateAddress(address: Address): Result<Unit>

    /**
     * Uploads a new profile picture for the user.
     * @param imageBytes The image data as a byte array.
     * @param fileName The name of the image file.
     * @return [Result] containing the image URL or ID if successful, or an error.
     */
    suspend fun uploadProfilePicture(
        imageBytes: ByteArray,
        fileName: String,
    ): Result<String>

    /**
     * Uploads multiple documents for the user.
     * @param documents A map of [DocumentType] to a pair of document bytes and file name.
     * @return [Result] containing [Unit] if successful, or an error.
     */
    suspend fun uploadDocuments(documents: Map<DocumentType, Pair<ByteArray, String>>): Result<Unit>

    /**
     * Replaces a specific document for the user.
     * @param documentType The type of document to replace.
     * @param document The document data as a byte array.
     * @param fileName The name of the document file.
     * @return [Result] containing [Unit] if successful, or an error.
     */
    suspend fun replaceDocument(
        documentType: DocumentType,
        document: ByteArray,
        fileName: String,
    ): Result<Unit>

    /**
     * Retrieves all documents for the user.
     * @return [Result] containing [Documents] if successful, or an error.
     */
    suspend fun getDocuments(): Result<Documents>

    /**
     * Updates the user's next of kin information.
     * @param nextOfKin The new next of kin details.
     * @return [Result] containing [Unit] if successful, or an error.
     */
    suspend fun updateNextOfKin(nextOfKin: NextOfKin): Result<Unit>

    /**
     * Retrieves the user's next of kin information.
     * @return [Result] containing [NextOfKin] if successful, or an error.
     */
    suspend fun getNextOfKin(): Result<NextOfKin>

    /**
     * Retrieves the available client types for the user.
     * @return [Result] containing a list of client type names if successful, or an error.
     */
    suspend fun getAvailableClientTypes(): Result<List<String>>

    /**
     * Requests a change to a new client type for the user.
     * @param newType The new client type to request.
     * @return [Result] containing [Unit] if successful, or an error.
     */
    suspend fun requestClientTypeChange(newType: String): Result<Unit>

    /**
     * Synchronizes the user's profile data with the remote source.
     * @return [Result] containing [Unit] if successful, or an error.
     */
    suspend fun syncProfile(): Result<Unit>

    /**
     * Observes changes to the user's profile as a [Flow].
     * @return A [Flow] emitting the current [User] or null when the profile changes.
     */
    fun observeProfile(): Flow<User?>
}

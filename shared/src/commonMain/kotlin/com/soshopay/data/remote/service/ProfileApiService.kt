package com.soshopay.data.remote.api

import com.soshopay.data.remote.dto.*
import com.soshopay.domain.util.Logger
import com.soshopay.domain.util.Result
import com.soshopay.domain.util.SoshoPayException
import com.soshopay.domain.util.safeCall
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*

interface ProfileApiService {
    suspend fun getUserProfile(): Result<ClientDto>

    suspend fun updatePersonalDetails(personalDetails: PersonalDetailsDto): Result<Unit>

    suspend fun updateAddress(address: AddressDto): Result<Unit>

    suspend fun uploadProfilePicture(
        imageBytes: ByteArray,
        fileName: String,
    ): Result<String>

    suspend fun uploadDocuments(documents: Map<String, Pair<ByteArray, String>>): Result<Unit>

    suspend fun replaceDocument(
        documentType: String,
        document: ByteArray,
        fileName: String,
    ): Result<Unit>

    suspend fun getDocuments(): Result<DocumentsDto>

    suspend fun updateNextOfKin(nextOfKin: NextOfKinDto): Result<Unit>

    suspend fun getNextOfKin(): Result<NextOfKinDto>

    suspend fun getClientTypes(): Result<List<String>>

    suspend fun updateClientType(clientType: String): Result<Unit>
}

class ProfileApiServiceImpl(
    private val httpClient: HttpClient,
    private val fileUploadService: FileUploadService,
) : ProfileApiService {
    companion object {
        private const val BASE_PATH = "api/mobile/client"
    }

    override suspend fun getUserProfile(): Result<ClientDto> =
        safeCall {
            Logger.logApiRequest("$BASE_PATH/me", "GET")

            val response = httpClient.get("$BASE_PATH/me")

            Logger.logApiResponse("$BASE_PATH/me", response.status.value)

            when (response.status) {
                HttpStatusCode.OK -> {
                    val responseBody = response.body<Map<String, ClientDto>>()
                    responseBody["client"] ?: throw SoshoPayException.ServerException("Invalid response format", 200)
                }
                HttpStatusCode.Unauthorized -> {
                    throw SoshoPayException.TokenExpiredException("Session has expired")
                }
                else -> {
                    val error = response.body<ErrorResponse>()
                    throw SoshoPayException.ServerException(error.message, response.status.value)
                }
            }
        }

    override suspend fun updatePersonalDetails(personalDetails: PersonalDetailsDto): Result<Unit> =
        safeCall {
            Logger.logApiRequest("$BASE_PATH/personal-details", "PUT")

            val response =
                httpClient.put("$BASE_PATH/personal-details") {
                    contentType(ContentType.Application.Json)
                    setBody(personalDetails)
                }

            Logger.logApiResponse("$BASE_PATH/personal-details", response.status.value)

            when (response.status) {
                HttpStatusCode.OK -> Unit
                HttpStatusCode.BadRequest -> {
                    val error = response.body<ErrorResponse>()
                    throw SoshoPayException.ValidationException(error.message)
                }
                HttpStatusCode.Unauthorized -> {
                    throw SoshoPayException.TokenExpiredException("Session has expired")
                }
                else -> {
                    val error = response.body<ErrorResponse>()
                    throw SoshoPayException.ServerException(error.message, response.status.value)
                }
            }
        }

    override suspend fun updateAddress(address: AddressDto): Result<Unit> =
        safeCall {
            Logger.logApiRequest("$BASE_PATH/address", "PUT")

            val response =
                httpClient.put("$BASE_PATH/address") {
                    contentType(ContentType.Application.Json)
                    setBody(address)
                }

            Logger.logApiResponse("$BASE_PATH/address", response.status.value)

            when (response.status) {
                HttpStatusCode.OK -> Unit
                HttpStatusCode.BadRequest -> {
                    val error = response.body<ErrorResponse>()
                    throw SoshoPayException.ValidationException(error.message)
                }
                HttpStatusCode.Unauthorized -> {
                    throw SoshoPayException.TokenExpiredException("Session has expired")
                }
                else -> {
                    val error = response.body<ErrorResponse>()
                    throw SoshoPayException.ServerException(error.message, response.status.value)
                }
            }
        }

    override suspend fun uploadProfilePicture(
        imageBytes: ByteArray,
        fileName: String,
    ): Result<String> =
        safeCall {
            Logger.logFileUpload(fileName, imageBytes.size.toLong(), false)

            val uploadResult =
                fileUploadService.uploadFile(
                    fileBytes = imageBytes,
                    fileName = fileName,
                    uploadPath = "$BASE_PATH/upload-picture",
                    fieldName = "profile_picture",
                )

            when (uploadResult) {
                is Result.Success -> {
                    Logger.logFileUpload(fileName, imageBytes.size.toLong(), true)
                    uploadResult.data
                }
                is Result.Error -> throw uploadResult.exception
                is Result.Loading -> throw SoshoPayException.UnknownException("Unexpected loading state")
            }
        }

    override suspend fun uploadDocuments(documents: Map<String, Pair<ByteArray, String>>): Result<Unit> =
        safeCall {
            Logger.logApiRequest("$BASE_PATH/upload-documents", "POST")

            val uploadResult =
                fileUploadService.uploadMultipleFiles(
                    files = documents,
                    uploadPath = "$BASE_PATH/upload-documents",
                )

            when (uploadResult) {
                is Result.Success -> Unit
                is Result.Error -> throw uploadResult.exception
                is Result.Loading -> throw SoshoPayException.UnknownException("Unexpected loading state")
            }
        }

    override suspend fun replaceDocument(
        documentType: String,
        document: ByteArray,
        fileName: String,
    ): Result<Unit> =
        safeCall {
            Logger.logFileUpload(fileName, document.size.toLong(), false)

            val uploadResult =
                fileUploadService.uploadFile(
                    fileBytes = document,
                    fileName = fileName,
                    uploadPath = "$BASE_PATH/documents/$documentType",
                    fieldName = "document",
                )

            when (uploadResult) {
                is Result.Success -> {
                    Logger.logFileUpload(fileName, document.size.toLong(), true)
                    Unit
                }
                is Result.Error -> throw uploadResult.exception
                is Result.Loading -> throw SoshoPayException.UnknownException("Unexpected loading state")
            }
        }

    override suspend fun getDocuments(): Result<DocumentsDto> =
        safeCall {
            Logger.logApiRequest("$BASE_PATH/documents", "GET")

            val response = httpClient.get("$BASE_PATH/documents")

            Logger.logApiResponse("$BASE_PATH/documents", response.status.value)

            when (response.status) {
                HttpStatusCode.OK -> response.body<DocumentsDto>()
                HttpStatusCode.Unauthorized -> {
                    throw SoshoPayException.TokenExpiredException("Session has expired")
                }
                else -> {
                    val error = response.body<ErrorResponse>()
                    throw SoshoPayException.ServerException(error.message, response.status.value)
                }
            }
        }

    override suspend fun updateNextOfKin(nextOfKin: NextOfKinDto): Result<Unit> =
        safeCall {
            Logger.logApiRequest("$BASE_PATH/next-of-kin", "PUT")

            val response =
                httpClient.put("$BASE_PATH/next-of-kin") {
                    contentType(ContentType.Application.Json)
                    setBody(nextOfKin)
                }

            Logger.logApiResponse("$BASE_PATH/next-of-kin", response.status.value)

            when (response.status) {
                HttpStatusCode.OK -> Unit
                HttpStatusCode.BadRequest -> {
                    val error = response.body<ErrorResponse>()
                    throw SoshoPayException.ValidationException(error.message)
                }
                HttpStatusCode.Unauthorized -> {
                    throw SoshoPayException.TokenExpiredException("Session has expired")
                }
                else -> {
                    val error = response.body<ErrorResponse>()
                    throw SoshoPayException.ServerException(error.message, response.status.value)
                }
            }
        }

    override suspend fun getNextOfKin(): Result<NextOfKinDto> =
        safeCall {
            Logger.logApiRequest("$BASE_PATH/next-of-kin", "GET")

            val response = httpClient.get("$BASE_PATH/next-of-kin")

            Logger.logApiResponse("$BASE_PATH/next-of-kin", response.status.value)

            when (response.status) {
                HttpStatusCode.OK -> response.body<NextOfKinDto>()
                HttpStatusCode.NotFound -> {
                    throw SoshoPayException.ValidationException("Next of kin not found")
                }
                HttpStatusCode.Unauthorized -> {
                    throw SoshoPayException.TokenExpiredException("Session has expired")
                }
                else -> {
                    val error = response.body<ErrorResponse>()
                    throw SoshoPayException.ServerException(error.message, response.status.value)
                }
            }
        }

    override suspend fun getClientTypes(): Result<List<String>> =
        safeCall {
            Logger.logApiRequest("$BASE_PATH/client-types", "GET")

            val response = httpClient.get("$BASE_PATH/client-types")

            Logger.logApiResponse("$BASE_PATH/client-types", response.status.value)

            when (response.status) {
                HttpStatusCode.OK -> {
                    val responseBody = response.body<Map<String, List<String>>>()
                    responseBody["client_types"] ?: emptyList()
                }
                else -> {
                    val error = response.body<ErrorResponse>()
                    throw SoshoPayException.ServerException(error.message, response.status.value)
                }
            }
        }

    override suspend fun updateClientType(clientType: String): Result<Unit> =
        safeCall {
            Logger.logApiRequest("$BASE_PATH/client-type", "PUT")

            val response =
                httpClient.put("$BASE_PATH/client-type") {
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("client_type" to clientType))
                }

            Logger.logApiResponse("$BASE_PATH/client-type", response.status.value)

            when (response.status) {
                HttpStatusCode.OK -> Unit
                HttpStatusCode.BadRequest -> {
                    val error = response.body<ErrorResponse>()
                    throw SoshoPayException.ValidationException(error.message)
                }
                HttpStatusCode.Unauthorized -> {
                    throw SoshoPayException.TokenExpiredException("Session has expired")
                }
                else -> {
                    val error = response.body<ErrorResponse>()
                    throw SoshoPayException.ServerException(error.message, response.status.value)
                }
            }
        }
}

package com.soshopay.data.remote.api

import com.soshopay.data.remote.dto.ErrorResponse
import com.soshopay.domain.util.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode

/**
 * Service interface for handling file upload operations in the SoshoPay application.
 * Provides methods for single and multiple file uploads, with future support for signed URL uploads.
 */
interface FileUploadService {
    /**
     * Uploads a single file to the specified path on the server.
     *
     * @param fileBytes The binary content of the file to upload
     * @param fileName The name of the file, including extension
     * @param uploadPath The server endpoint path where the file should be uploaded
     * @param fieldName The form field name to use for the file upload (defaults to "file")
     * @return [Result] containing the URL of the uploaded file if successful, or an error if the upload fails
     * @throws SoshoPayException.FileUploadException if the upload fails for any reason
     */
    suspend fun uploadFile(
        fileBytes: ByteArray,
        fileName: String,
        uploadPath: String,
        fieldName: String = "file",
    ): Result<String>

    /**
     * Uploads multiple files simultaneously to the specified path.
     *
     * @param files Map of field names to pairs of (file bytes, filename)
     * @param uploadPath The server endpoint path where the files should be uploaded
     * @return [Result] indicating success or failure of the upload operation
     * @throws SoshoPayException.FileUploadException if any file upload fails
     */
    suspend fun uploadMultipleFiles(
        files: Map<String, Pair<ByteArray, String>>, // fieldName to (bytes, fileName)
        uploadPath: String,
    ): Result<Unit>

    /**
     * Gets a signed URL for direct file upload to cloud storage.
     * Currently not implemented - planned for future use.
     *
     * @param fileName The name of the file to be uploaded
     * @param fileType The MIME type of the file
     * @return [Result] containing the signed URL if successful
     * @throws SoshoPayException.UnknownException as this feature is not yet implemented
     */
    suspend fun getSignedUploadUrl(
        fileName: String,
        fileType: String,
    ): Result<String>

    /**
     * Uploads a file directly to cloud storage using a signed URL.
     * Currently not implemented - planned for future use.
     *
     * @param signedUrl The pre-signed URL to upload to
     * @param fileBytes The binary content of the file to upload
     * @return [Result] indicating success or failure of the upload
     * @throws SoshoPayException.UnknownException as this feature is not yet implemented
     */
    suspend fun uploadToSignedUrl(
        signedUrl: String,
        fileBytes: ByteArray,
    ): Result<Unit>
}

/**
 * Implementation of [FileUploadService] that handles file uploads using Ktor's [HttpClient].
 *
 * This implementation:
 * - Supports single and multiple file uploads using multipart form data
 * - Handles various HTTP response status codes and converts them to appropriate exceptions
 * - Provides detailed error messages for common upload failures
 * - Has placeholder implementations for future cloud storage integration
 *
 * @property httpClient Ktor HTTP client used for making upload requests
 */
class FileUploadServiceImpl(
    private val httpClient: HttpClient,
) : FileUploadService {
    override suspend fun uploadFile(
        fileBytes: ByteArray,
        fileName: String,
        uploadPath: String,
        fieldName: String,
    ): Result<String> =
        com.soshopay.domain.util.safeCall {
            val response =
                httpClient.submitFormWithBinaryData(
                    url = uploadPath,
                    formData =
                        formData {
                            append(
                                fieldName,
                                fileBytes,
                                Headers.build {
                                    append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                                },
                            )
                        },
                )

            when (response.status) {
                HttpStatusCode.OK -> {
                    val responseBody = response.body<Map<String, String>>()
                    responseBody["url"] ?: throw com.soshopay.domain.util.SoshoPayException.FileUploadException(
                        "Upload successful but no URL returned",
                        fileName,
                    )
                }
                HttpStatusCode.BadRequest -> {
                    val error = response.body<ErrorResponse>()
                    throw com.soshopay.domain.util.SoshoPayException
                        .FileUploadException(error.message, fileName)
                }
                HttpStatusCode.PayloadTooLarge -> {
                    throw com.soshopay.domain.util.SoshoPayException
                        .FileUploadException("File too large to upload", fileName)
                }
                HttpStatusCode.UnsupportedMediaType -> {
                    throw com.soshopay.domain.util.SoshoPayException
                        .FileUploadException("Unsupported file type", fileName)
                }
                else -> {
                    val error = response.body<ErrorResponse>()
                    throw com.soshopay.domain.util.SoshoPayException
                        .FileUploadException(error.message, fileName)
                }
            }
        }

    override suspend fun uploadMultipleFiles(
        files: Map<String, Pair<ByteArray, String>>,
        uploadPath: String,
    ): Result<Unit> =
        com.soshopay.domain.util.safeCall {
            val formData =
                formData {
                    files.forEach { (fieldName, fileData) ->
                        val (bytes, fileName) = fileData
                        append(
                            fieldName,
                            bytes,
                            Headers.build {
                                append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                            },
                        )
                    }
                }

            val response =
                httpClient.submitFormWithBinaryData(
                    url = uploadPath,
                    formData = formData,
                )

            when (response.status) {
                HttpStatusCode.OK -> Unit
                HttpStatusCode.BadRequest -> {
                    val error = response.body<ErrorResponse>()
                    throw com.soshopay.domain.util.SoshoPayException
                        .FileUploadException(error.message)
                }
                HttpStatusCode.PayloadTooLarge -> {
                    throw com.soshopay.domain.util.SoshoPayException
                        .FileUploadException("File too large to upload")
                }
                HttpStatusCode.UnsupportedMediaType -> {
                    throw com.soshopay.domain.util.SoshoPayException
                        .FileUploadException("Unsupported file type")
                }
                else -> {
                    val error = response.body<ErrorResponse>()
                    throw com.soshopay.domain.util.SoshoPayException
                        .FileUploadException(error.message)
                }
            }
        }

    override suspend fun getSignedUploadUrl(
        fileName: String,
        fileType: String,
    ): Result<String> =
        com.soshopay.domain.util.safeCall {
            // Implementation for future cloud storage signed URLs
            throw com.soshopay.domain.util.SoshoPayException
                .UnknownException("Signed URL upload not yet implemented")
        }

    override suspend fun uploadToSignedUrl(
        signedUrl: String,
        fileBytes: ByteArray,
    ): Result<Unit> =
        com.soshopay.domain.util.safeCall {
            // Implementation for future cloud storage uploads
            throw com.soshopay.domain.util.SoshoPayException
                .UnknownException("Signed URL upload not yet implemented")
        }
}

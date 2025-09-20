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

interface FileUploadService {
    suspend fun uploadFile(
        fileBytes: ByteArray,
        fileName: String,
        uploadPath: String,
        fieldName: String = "file",
    ): Result<String>

    suspend fun uploadMultipleFiles(
        files: Map<String, Pair<ByteArray, String>>, // fieldName to (bytes, fileName)
        uploadPath: String,
    ): Result<Unit>

    suspend fun getSignedUploadUrl(
        fileName: String,
        fileType: String,
    ): Result<String>

    suspend fun uploadToSignedUrl(
        signedUrl: String,
        fileBytes: ByteArray,
    ): Result<Unit>
}

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

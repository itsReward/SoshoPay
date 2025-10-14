package com.soshopay.domain.usecase.loan

import com.soshopay.domain.model.CollateralDocument
import com.soshopay.domain.repository.LoanRepository
import com.soshopay.domain.repository.Result

/**
 * Use Case for uploading collateral documents for cash loan applications.
 *
 * Following SOLID principles:
 * - Single Responsibility: Handles only collateral document upload
 * - Dependency Inversion: Depends on LoanRepository abstraction
 * - Interface Segregation: Focused interface for one specific operation
 *
 * This use case validates the file before uploading and handles the upload process.
 *
 * @property loanRepository Repository for loan operations
 */
class UploadCollateralDocumentUseCase(
    private val loanRepository: LoanRepository,
) {
    /**
     * Uploads a collateral document for a loan application.
     *
     * @param fileBytes The binary content of the file to upload
     * @param fileName The name of the file including extension
     * @param fileType The MIME type of the file (e.g., "image/jpeg", "application/pdf")
     * @param applicationId The ID of the loan application
     * @return [Result] containing the uploaded [CollateralDocument] or an error
     */
    suspend operator fun invoke(
        fileBytes: ByteArray,
        fileName: String,
        fileType: String,
        applicationId: String,
    ): Result<CollateralDocument> {
        // Pre-validation before calling repository
        if (fileName.isBlank()) {
            return Result.Error(Exception("File name cannot be empty"))
        }

        if (fileBytes.isEmpty()) {
            return Result.Error(Exception("File cannot be empty"))
        }

        if (applicationId.isBlank()) {
            return Result.Error(Exception("Application ID is required"))
        }

        // Delegate to repository
        return loanRepository.uploadCollateralDocument(
            fileBytes = fileBytes,
            fileName = fileName,
            fileType = fileType,
            applicationId = applicationId,
        )
    }
}

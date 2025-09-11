package com.soshopay.domain.usecase.profile

import com.soshopay.domain.model.User
import com.soshopay.domain.model.VerificationStatus
import com.soshopay.domain.util.ValidationResult

class ValidateProfileCompletionUseCase {
    data class ProfileCompletionResult(
        val isComplete: Boolean,
        val canApplyForLoan: Boolean,
        val missingFields: List<String>,
        val nextSteps: List<String>,
    )

    operator fun invoke(user: User): ProfileCompletionResult {
        val missingFields = mutableListOf<String>()
        val nextSteps = mutableListOf<String>()

        // Check personal details
        if (user.personalDetails == null) {
            missingFields.add("Personal Details")
            nextSteps.add("Complete your personal information")
        } else {
            if (!user.personalDetails.isComplete()) {
                missingFields.add("Complete Personal Details")
                nextSteps.add("Fill in all personal details fields")
            }
        }

        // Check address
        if (user.address == null) {
            missingFields.add("Address")
            nextSteps.add("Add your address information")
        } else {
            if (!user.address.isComplete()) {
                missingFields.add("Complete Address")
                nextSteps.add("Fill in all address fields")
            }
        }

        // Check documents
        if (user.documents == null) {
            missingFields.add("Documents")
            nextSteps.add("Upload required documents (National ID, Proof of Residence)")
        } else {
            if (!user.documents.isAllDocumentsUploaded()) {
                missingFields.add("Documents")
                if (user.documents.proofOfResidence == null) {
                    nextSteps.add("Upload Proof of Residence")
                }
                if (user.documents.nationalId == null) {
                    nextSteps.add("Upload National ID")
                }
            } else if (!user.documents.hasVerifiedDocuments()) {
                nextSteps.add("Wait for document verification")
            }
        }

        // Check verification status
        val isVerified = user.verificationStatus == VerificationStatus.VERIFIED
        if (!isVerified && missingFields.isEmpty()) {
            nextSteps.add("Wait for profile verification")
        }

        val isComplete = missingFields.isEmpty()
        val canApplyForLoan = isComplete && isVerified && user.canApplyForLoan

        return ProfileCompletionResult(
            isComplete = isComplete,
            canApplyForLoan = canApplyForLoan,
            missingFields = missingFields,
            nextSteps = nextSteps,
        )
    }
}

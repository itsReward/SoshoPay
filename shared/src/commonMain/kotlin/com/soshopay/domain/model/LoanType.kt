package com.soshopay.domain.model

import com.soshopay.domain.model.DocumentType.NATIONAL_ID
import com.soshopay.domain.model.DocumentType.PROFILE_PICTURE
import com.soshopay.domain.model.DocumentType.PROOF_OF_RESIDENCE

// ========== LOAN TYPES ==========
enum class LoanType {
    CASH,
    PAYGO,
    ;

    fun getDisplayName(): String =
        when (this) {
            CASH -> "Cash Loan"
            PAYGO -> "PayGo Loan"
        }

    fun getDescription(): String =
        when (this) {
            CASH -> "Cash loan for personal and commercial use"
            PAYGO -> "Pay as you go solar and appliance loan"
        }
}

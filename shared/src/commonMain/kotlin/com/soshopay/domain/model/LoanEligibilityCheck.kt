package com.soshopay.domain.model

data class LoanEligibilityCheck(
    val isEligible: Boolean,
    val reasons: List<String>,
    val recommendations: List<String>,
)

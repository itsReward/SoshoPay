package com.soshopay.domain.model

enum class LoanHistoryFilter(
    val value: String,
) {
    ALL("all"),
    APPROVED("approved"),
    REJECTED("rejected"),
    ACTIVE("active"),
    COMPLETED("completed"),
    CASH("cash"),
    PAYGO("paygo"),
}

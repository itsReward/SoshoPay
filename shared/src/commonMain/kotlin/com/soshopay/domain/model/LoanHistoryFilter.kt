package com.soshopay.domain.model

enum class LoanHistoryFilter(
    val value: String,
) {
    ALL("all"),
    ACTIVE("active"),
    COMPLETED("completed"),
    CASH("cash"),
    PAYGO("paygo"),
}

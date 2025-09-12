package com.soshopay.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PaymentHistoryResponse(
    val payments: List<Payment>,
    val currentPage: Int,
    val totalPages: Int,
    val totalCount: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean,
)

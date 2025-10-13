package com.soshopay.domain.model

// ========== PAYMENT ENUMS ==========
enum class PaymentStatus {
    PENDING,
    PROCESSING,
    SUCCESSFUL,
    FAILED,
    CANCELLED,
    OVERDUE,
    CURRENT,
    COMPLETED,
}

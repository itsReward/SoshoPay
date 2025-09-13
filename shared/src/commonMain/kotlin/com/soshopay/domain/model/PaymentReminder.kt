package com.soshopay.domain.model

import kotlinx.datetime.Clock

data class PaymentReminder(
    val loanId: String,
    val amount: Double,
    val dueDate: Long,
    val reminderDate: Long,
    val message: String,
    val isActive: Boolean,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
)

package com.soshopay.domain.model

data class LoanApplicationProgress(
    val currentStep: Int,
    val totalSteps: Int,
    val completedSteps: List<Int>,
    val canProceedToNext: Boolean,
) {
    val progressPercentage: Float = (completedSteps.size.toFloat() / totalSteps) * 100
    val isComplete: Boolean = completedSteps.size == totalSteps
}

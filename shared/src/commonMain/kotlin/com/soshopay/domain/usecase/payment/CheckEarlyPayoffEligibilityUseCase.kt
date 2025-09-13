package com.soshopay.domain.usecase.payment

import com.soshopay.domain.model.EarlyPayoffEligibility
import com.soshopay.domain.model.Loan
import com.soshopay.domain.model.LoanType
import com.soshopay.domain.repository.Result

class CheckEarlyPayoffEligibilityUseCase {
    operator fun invoke(
        loan: Loan,
        paymentsCompleted: Int = loan.paymentsCompleted,
    ): Result<EarlyPayoffEligibility> {
        val reasons = mutableListOf<String>()
        var isEligible = true

        // Check if loan is active
        if (!loan.isActive()) {
            isEligible = false
            reasons.add("Only active loans are eligible for early payoff")
        }

        // Check minimum payments completed
        val minimumPayments =
            when (loan.loanType) {
                LoanType.CASH -> 2
                LoanType.PAYGO -> 4
            }

        if (paymentsCompleted < minimumPayments) {
            isEligible = false
            reasons.add("Minimum $minimumPayments payments required for early payoff")
        }

        // Check remaining balance
        if (loan.remainingBalance <= 0) {
            isEligible = false
            reasons.add("Loan is already fully paid")
        }

        val eligibility =
            EarlyPayoffEligibility(
                isEligible = isEligible,
                reasons = reasons,
                minimumPaymentsRequired = minimumPayments,
                paymentsCompleted = paymentsCompleted,
                estimatedSavings = if (isEligible) loan.remainingBalance * 0.05 else 0.0, // Estimated 5% savings
            )

        return Result.Success(eligibility)
    }
}

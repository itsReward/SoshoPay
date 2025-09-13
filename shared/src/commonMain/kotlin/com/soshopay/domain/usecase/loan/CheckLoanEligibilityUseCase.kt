package com.soshopay.domain.usecase.loan

import com.soshopay.domain.model.LoanEligibilityCheck
import com.soshopay.domain.model.LoanType
import com.soshopay.domain.repository.LoanRepository
import com.soshopay.domain.repository.Result

class CheckLoanEligibilityUseCase(
    private val loanRepository: LoanRepository,
) {
    suspend operator fun invoke(
        loanType: LoanType,
        monthlyIncome: Double,
        existingLoans: Int = 0,
    ): Result<LoanEligibilityCheck> {
        val reasons = mutableListOf<String>()
        val recommendations = mutableListOf<String>()
        var isEligible = true

        // Basic eligibility checks
        if (monthlyIncome < 500) {
            isEligible = false
            reasons.add("Minimum monthly income of $500 required")
            recommendations.add("Increase your income or consider a smaller loan amount")
        }

        if (existingLoans >= 3) {
            isEligible = false
            reasons.add("Maximum of 3 active loans allowed")
            recommendations.add("Complete existing loans before applying for new ones")
        }

        // Loan type specific checks
        when (loanType) {
            LoanType.CASH -> {
                if (monthlyIncome < 1000) {
                    recommendations.add("Higher income improves cash loan terms")
                }
            }
            LoanType.PAYGO -> {
                if (monthlyIncome < 300) {
                    isEligible = false
                    reasons.add("Minimum monthly income of $300 required for PayGo loans")
                }
            }
        }

        return Result.Success(
            LoanEligibilityCheck(
                isEligible = isEligible,
                reasons = reasons,
                recommendations = recommendations,
            ),
        )
    }
}

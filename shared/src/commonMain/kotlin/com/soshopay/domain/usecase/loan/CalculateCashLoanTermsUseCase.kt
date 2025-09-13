package com.soshopay.domain.usecase.loan

import com.soshopay.domain.model.CashLoanCalculationRequest
import com.soshopay.domain.model.CashLoanTerms
import com.soshopay.domain.model.ValidationResult
import com.soshopay.domain.repository.LoanRepository
import com.soshopay.domain.repository.Result

class CalculateCashLoanTermsUseCase(
    private val loanRepository: LoanRepository,
) {
    suspend operator fun invoke(
        loanAmount: Double,
        repaymentPeriod: String,
        employerIndustry: String,
        collateralValue: Double,
        monthlyIncome: Double,
    ): Result<CashLoanTerms> {
        val request =
            CashLoanCalculationRequest(
                loanAmount = loanAmount,
                repaymentPeriod = repaymentPeriod,
                employerIndustry = employerIndustry,
                collateralValue = collateralValue,
                monthlyIncome = monthlyIncome,
            )

        // Validate input
        val validation = validateCalculationRequest(request)
        if (!validation.isValid) {
            return Result.Error(Exception(validation.getErrorMessage()))
        }

        return loanRepository.calculateCashLoanTerms(request)
    }

    private fun validateCalculationRequest(request: CashLoanCalculationRequest): ValidationResult {
        val errors = mutableListOf<String>()

        if (request.loanAmount <= 0) {
            errors.add("Loan amount must be greater than zero")
        }

        if (request.repaymentPeriod.isBlank()) {
            errors.add("Repayment period is required")
        }

        if (request.employerIndustry.isBlank()) {
            errors.add("Employer industry is required")
        }

        if (request.collateralValue <= 0) {
            errors.add("Collateral value must be greater than zero")
        }

        if (request.monthlyIncome <= 0) {
            errors.add("Monthly income must be greater than zero")
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
        )
    }
}

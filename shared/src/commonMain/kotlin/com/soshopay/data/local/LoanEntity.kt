package com.soshopay.data.local

import com.soshopay.domain.model.Loan
import com.soshopay.domain.model.LoanStatus
import com.soshopay.domain.model.LoanType
import kotlinx.serialization.Serializable

// ========== DATABASE ENTITIES (for room/sqldelight mapping) ==========
@Serializable
data class LoanEntity(
    val id: String,
    val userId: String,
    val applicationId: String,
    val loanType: String, // Store as string for database
    val originalAmount: Double,
    val totalAmount: Double,
    val remainingBalance: Double,
    val interestRate: Double,
    val repaymentPeriod: String,
    val disbursementDate: Long,
    val maturityDate: Long,
    val status: String, // Store as string for database
    val nextPaymentDate: Long?,
    val nextPaymentAmount: Double?,
    val paymentsCompleted: Int,
    val totalPayments: Int,
    val productName: String?,
    val loanPurpose: String?,
    val installationDate: Long?,
    val rejectionReason: String?,
    val rejectionDate: Long?,
    val createdAt: Long,
    val updatedAt: Long,
) {
    fun toDomain(): Loan =
        Loan(
            id = id,
            userId = userId,
            applicationId = applicationId,
            loanType = LoanType.valueOf(loanType),
            originalAmount = originalAmount,
            totalAmount = totalAmount,
            remainingBalance = remainingBalance,
            interestRate = interestRate,
            repaymentPeriod = repaymentPeriod,
            disbursementDate = disbursementDate,
            maturityDate = maturityDate,
            status = LoanStatus.valueOf(status),
            nextPaymentDate = nextPaymentDate,
            nextPaymentAmount = nextPaymentAmount,
            paymentsCompleted = paymentsCompleted,
            totalPayments = totalPayments,
            productName = productName,
            loanPurpose = loanPurpose,
            installationDate = installationDate,
            rejectionReason = rejectionReason,
            rejectionDate = rejectionDate,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    companion object {
        fun fromDomain(loan: Loan): LoanEntity =
            LoanEntity(
                id = loan.id,
                userId = loan.userId,
                applicationId = loan.applicationId,
                loanType = loan.loanType.name,
                originalAmount = loan.originalAmount,
                totalAmount = loan.totalAmount,
                remainingBalance = loan.remainingBalance,
                interestRate = loan.interestRate,
                repaymentPeriod = loan.repaymentPeriod,
                disbursementDate = loan.disbursementDate,
                maturityDate = loan.maturityDate,
                status = loan.status.name,
                nextPaymentDate = loan.nextPaymentDate,
                nextPaymentAmount = loan.nextPaymentAmount,
                paymentsCompleted = loan.paymentsCompleted,
                totalPayments = loan.totalPayments,
                productName = loan.productName,
                loanPurpose = loan.loanPurpose,
                installationDate = loan.installationDate,
                rejectionReason = loan.rejectionReason,
                rejectionDate = loan.rejectionDate,
                createdAt = loan.createdAt,
                updatedAt = loan.updatedAt,
            )
    }
}

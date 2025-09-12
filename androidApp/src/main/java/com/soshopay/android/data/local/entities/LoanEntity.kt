package com.soshopay.android.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.soshopay.domain.model.Loan
import com.soshopay.domain.model.LoanStatus
import com.soshopay.domain.model.LoanType

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val applicationId: String,
    val loanType: String,
    val originalAmount: Double,
    val totalAmount: Double,
    val remainingBalance: Double,
    val interestRate: Double,
    val repaymentPeriod: String,
    val disbursementDate: Long,
    val maturityDate: Long,
    val status: String,
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
}

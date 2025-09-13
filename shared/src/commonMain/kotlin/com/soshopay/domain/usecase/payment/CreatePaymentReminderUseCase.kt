package com.soshopay.domain.usecase.payment

import com.soshopay.domain.model.Loan
import com.soshopay.domain.model.PaymentReminder
import com.soshopay.domain.repository.Result

class CreatePaymentReminderUseCase {
    operator fun invoke(
        loan: Loan,
        daysBeforeDue: Int = 3,
    ): Result<PaymentReminder> {
        if (!loan.isActive()) {
            return Result.Error(Exception("Cannot create reminder for inactive loan"))
        }

        val nextPaymentDate =
            loan.nextPaymentDate
                ?: return Result.Error(Exception("No payment date available"))

        val reminderDate = nextPaymentDate - (daysBeforeDue * 24 * 60 * 60 * 1000)

        val reminder =
            PaymentReminder(
                loanId = loan.id,
                amount = loan.nextPaymentAmount ?: 0.0,
                dueDate = nextPaymentDate,
                reminderDate = reminderDate,
                message = "Payment of ${loan.nextPaymentAmount} is due in $daysBeforeDue days",
                isActive = true,
            )

        return Result.Success(reminder)
    }
}

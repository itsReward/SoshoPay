package com.soshopay.domain.usecase.payment

import com.soshopay.domain.model.PaymentReceipt
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ShareReceiptUseCase {
    operator fun invoke(receipt: PaymentReceipt): String =
        buildString {
            appendLine("Payment Receipt")
            appendLine("================")
            appendLine("Receipt Number: ${receipt.receiptNumber}")
            appendLine("Amount: $${receipt.amount}")
            appendLine("Payment Method: ${receipt.paymentMethod}")
            appendLine("Phone: ${receipt.phoneNumber}")
            appendLine("Date: ${formatTimestamp(receipt.processedAt)}")
            appendLine("Transaction Ref: ${receipt.transactionReference}")

            if (receipt.productName != null) {
                appendLine("Product: ${receipt.productName}")
            }

            receipt.principal?.let { appendLine("Principal: $$it") }
            receipt.interest?.let { appendLine("Interest: $$it") }
            receipt.penalties?.let { appendLine("Penalties: $$it") }

            appendLine("\nThank you for your payment!")
            appendLine("SoshoPay - Your Financial Partner")
        }

    private fun formatTimestamp(timestamp: Long): String {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

        return "${dateTime.dayOfMonth.toString().padStart(2, '0')}/" +
            "${dateTime.monthNumber.toString().padStart(2, '0')}/" +
            "${dateTime.year}/" +
            "${dateTime.hour.toString().padStart(2, '0')}:" +
            "${dateTime.minute.toString().padStart(2, '0')}"
    }
}

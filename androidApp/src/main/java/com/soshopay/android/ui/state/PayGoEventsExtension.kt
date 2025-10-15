package com.soshopay.android.ui.state

import com.soshopay.domain.model.Guarantor
import com.soshopay.domain.model.PayGoProduct

/**
 * Helper extension to check if an event is PayGo-related
 */
fun LoanPaymentEvent.isPayGoEvent(): Boolean =
    when (this) {
        is LoanPaymentEvent.InitializePayGoApplication,
        is LoanPaymentEvent.LoadPayGoDraft,
        is LoanPaymentEvent.SavePayGoDraft,
        is LoanPaymentEvent.NextPayGoStep,
        is LoanPaymentEvent.PreviousPayGoStep,
        is LoanPaymentEvent.UpdatePayGoCategory,
        is LoanPaymentEvent.UpdatePayGoProduct,
        is LoanPaymentEvent.UpdatePayGoUsage,
        is LoanPaymentEvent.UpdatePayGoRepaymentPeriod,
        is LoanPaymentEvent.UpdatePayGoSalaryBand,
        is LoanPaymentEvent.UpdatePayGoGuarantor,
        is LoanPaymentEvent.CalculatePayGoTerms,
        is LoanPaymentEvent.SubmitPayGoApplication,
        -> true
        else -> false
    }

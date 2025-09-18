package com.soshopay.android.di

import com.soshopay.android.ui.viewmodel.AuthViewModel
import com.soshopay.android.ui.viewmodel.LoanViewModel
import com.soshopay.android.ui.viewmodel.PaymentViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Android-specific dependency injection module.
 *
 * This module provides Android-specific dependencies including ViewModels.
 * It follows the Single Responsibility Principle by handling only Android UI layer dependencies.
 */
val androidModule =
    module {

        // ========== VIEW MODELS ==========

        /**
         * AuthViewModel for managing authentication UI state.
         *
         * Dependencies:
         * - SendOtpUseCase: For sending OTP codes
         * - VerifyOtpUseCase: For verifying OTP codes
         * - SetPinUseCase: For setting user PIN
         * - LoginUseCase: For user login
         * - IsLoggedInUseCase: For checking login status
         */
        viewModel {
            AuthViewModel(
                sendOtpUseCase = get(),
                verifyOtpUseCase = get(),
                setPinUseCase = get(),
                loginUseCase = get(),
                isLoggedInUseCase = get(),
            )
        }

        /**
         * AuthViewModel for managing authentication UI state.
         *
         * Dependencies:
         * - SendOtpUseCase: For sending OTP codes
         * - VerifyOtpUseCase: For verifying OTP codes
         * - SetPinUseCase: For setting user PIN
         * - LoginUseCase: For user login
         * - IsLoggedInUseCase: For checking login status
         */
        viewModel {
            AuthViewModel(
                sendOtpUseCase = get(),
                verifyOtpUseCase = get(),
                setPinUseCase = get(),
                loginUseCase = get(),
                isLoggedInUseCase = get(),
            )
        }

        /**
         * LoanViewModel for managing loan-related UI state.
         *
         * Dependencies:
         * - Cash Loan Use Cases: Form data, calculation, submission, drafts
         * - PayGo Use Cases: Categories, products, terms calculation, submission
         * - General Loan Use Cases: History, details, current loans, agreements
         * - Profile Use Cases: Profile completion validation
         */
        viewModel {
            LoanViewModel(
                getCashLoanFormDataUseCase = get(),
                calculateCashLoanTermsUseCase = get(),
                submitCashLoanApplicationUseCase = get(),
                saveCashLoanDraftUseCase = get(),
                getCashLoanDraftUseCase = get(),
                getPayGoCategoriesUseCase = get(),
                getPayGoProductsUseCase = get(),
                calculatePayGoTermsUseCase = get(),
                submitPayGoApplicationUseCase = get(),
                getLoanHistoryUseCase = get(),
                getLoanDetailsUseCase = get(),
                getCurrentLoansUseCase = get(),
                downloadLoanAgreementUseCase = get(),
                validateProfileCompletionUseCase = get(),
                getUserProfileUseCase = get(),
            )
        }

        /**
         * PaymentViewModel for managing payment-related UI state.
         *
         * Dependencies:
         * - Payment Dashboard Use Cases: Dashboard data, summaries
         * - Payment Processing Use Cases: Methods, processing, status checking
         * - Payment History Use Cases: History retrieval, receipts
         * - Early Payment Use Cases: Calculations and processing
         */
        viewModel {
            PaymentViewModel(
                getPaymentDashboardUseCase = get(),
                getPaymentMethodsUseCase = get(),
                processPaymentUseCase = get(),
                getPaymentHistoryUseCase = get(),
                getPaymentStatusUseCase = get(),
                downloadReceiptUseCase = get(),
                calculateEarlyPayoffUseCase = get(),
            )
        }
    }

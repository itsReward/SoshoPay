package com.soshopay.di

import com.soshopay.data.remote.LoanApiService
import com.soshopay.data.remote.PaymentApiService
import com.soshopay.data.remote.api.AuthApiService
import com.soshopay.data.remote.api.AuthApiServiceImpl
import com.soshopay.data.remote.api.FileUploadService
import com.soshopay.data.remote.api.FileUploadServiceImpl
import com.soshopay.data.remote.api.ProfileApiService
import com.soshopay.data.remote.api.ProfileApiServiceImpl
import com.soshopay.data.remote.createHttpClient
import com.soshopay.data.repository.AuthRepositoryImpl
import com.soshopay.data.repository.LoanRepositoryImpl
import com.soshopay.data.repository.PaymentRepositoryImpl
import com.soshopay.data.repository.ProfileRepositoryImpl
import com.soshopay.domain.repository.AuthRepository
import com.soshopay.domain.repository.LoanRepository
import com.soshopay.domain.repository.PaymentRepository
import com.soshopay.domain.repository.ProfileRepository
import com.soshopay.domain.usecase.auth.ChangeMobileNumberUseCase
import com.soshopay.domain.usecase.auth.CreateClientUseCase
import com.soshopay.domain.usecase.auth.IsLoggedInUseCase
import com.soshopay.domain.usecase.auth.LoginUseCase
import com.soshopay.domain.usecase.auth.LogoutUseCase
import com.soshopay.domain.usecase.auth.RefreshTokenUseCase
import com.soshopay.domain.usecase.auth.SendOtpUseCase
import com.soshopay.domain.usecase.auth.SetPinUseCase
import com.soshopay.domain.usecase.auth.UpdatePinUseCase
import com.soshopay.domain.usecase.auth.VerifyOtpUseCase
import com.soshopay.domain.usecase.loan.CalculateCashLoanTermsUseCase
import com.soshopay.domain.usecase.loan.CalculatePayGoTermsUseCase
import com.soshopay.domain.usecase.loan.CheckLoanEligibilityUseCase
import com.soshopay.domain.usecase.loan.DeleteCashLoanDraftUseCase
import com.soshopay.domain.usecase.loan.DeletePayGoLoanDraftUseCase
import com.soshopay.domain.usecase.loan.DownloadLoanAgreementUseCase
import com.soshopay.domain.usecase.loan.GetCachedLoansUseCase
import com.soshopay.domain.usecase.loan.GetCashLoanDraftUseCase
import com.soshopay.domain.usecase.loan.GetCashLoanFormDataUseCase
import com.soshopay.domain.usecase.loan.GetCurrentLoansUseCase
import com.soshopay.domain.usecase.loan.GetLoanDetailsUseCase
import com.soshopay.domain.usecase.loan.GetLoanHistoryUseCase
import com.soshopay.domain.usecase.loan.GetPayGoCategoriesUseCase
import com.soshopay.domain.usecase.loan.GetPayGoLoanDraftUseCase
import com.soshopay.domain.usecase.loan.GetPayGoProductsUseCase
import com.soshopay.domain.usecase.loan.ObserveApplicationStatusUseCase
import com.soshopay.domain.usecase.loan.ObserveLoansUseCase
import com.soshopay.domain.usecase.loan.SaveCashLoanDraftUseCase
import com.soshopay.domain.usecase.loan.SavePayGoLoanDraftUseCase
import com.soshopay.domain.usecase.loan.SubmitCashLoanApplicationUseCase
import com.soshopay.domain.usecase.loan.SubmitPayGoApplicationUseCase
import com.soshopay.domain.usecase.loan.SyncLoansUseCase
import com.soshopay.domain.usecase.loan.ValidateCashLoanApplicationUseCase
import com.soshopay.domain.usecase.loan.ValidatePayGoApplicationUseCase
import com.soshopay.domain.usecase.loan.WithdrawLoanApplicationUseCase
import com.soshopay.domain.usecase.payment.CalculateEarlyPayoffUseCase
import com.soshopay.domain.usecase.payment.CancelPaymentUseCase
import com.soshopay.domain.usecase.payment.CheckEarlyPayoffEligibilityUseCase
import com.soshopay.domain.usecase.payment.CheckPaymentEligibilityUseCase
import com.soshopay.domain.usecase.payment.CreatePaymentReminderUseCase
import com.soshopay.domain.usecase.payment.DownloadReceiptUseCase
import com.soshopay.domain.usecase.payment.GeneratePaymentReportUseCase
import com.soshopay.domain.usecase.payment.GetActivePaymentMethodsUseCase
import com.soshopay.domain.usecase.payment.GetCachedPaymentsUseCase
import com.soshopay.domain.usecase.payment.GetPaymentAnalyticsUseCase
import com.soshopay.domain.usecase.payment.GetPaymentDashboardUseCase
import com.soshopay.domain.usecase.payment.GetPaymentHistoryUseCase
import com.soshopay.domain.usecase.payment.GetPaymentMethodsUseCase
import com.soshopay.domain.usecase.payment.GetPaymentReceiptUseCase
import com.soshopay.domain.usecase.payment.GetPaymentStatusUseCase
import com.soshopay.domain.usecase.payment.GetRecommendedPaymentAmountUseCase
import com.soshopay.domain.usecase.payment.ObservePaymentDashboardUseCase
import com.soshopay.domain.usecase.payment.ObservePaymentStatusUseCase
import com.soshopay.domain.usecase.payment.ObservePaymentUpdatesUseCase
import com.soshopay.domain.usecase.payment.ProcessEarlyPayoffUseCase
import com.soshopay.domain.usecase.payment.ProcessPaymentNotificationUseCase
import com.soshopay.domain.usecase.payment.ProcessPaymentUseCase
import com.soshopay.domain.usecase.payment.ResendReceiptToEmailUseCase
import com.soshopay.domain.usecase.payment.RetryFailedPaymentUseCase
import com.soshopay.domain.usecase.payment.ShareReceiptUseCase
import com.soshopay.domain.usecase.payment.SyncPaymentsUseCase
import com.soshopay.domain.usecase.payment.ValidatePaymentRequestUseCase
import com.soshopay.domain.usecase.profile.GetUserProfileUseCase
import com.soshopay.domain.usecase.profile.ManageClientTypeUseCase
import com.soshopay.domain.usecase.profile.ManageNextOfKinUseCase
import com.soshopay.domain.usecase.profile.UpdateAddressUseCase
import com.soshopay.domain.usecase.profile.UpdatePersonalDetailsUseCase
import com.soshopay.domain.usecase.profile.UploadDocumentUseCase
import com.soshopay.domain.usecase.profile.UploadProfilePictureUseCase
import com.soshopay.domain.usecase.profile.ValidateProfileCompletionUseCase
import org.koin.dsl.module

val sharedModule =
    module {
        // Network
        single { createHttpClient() }

        // API Services
        single<AuthApiService> { AuthApiServiceImpl(get()) }
        single<ProfileApiService> { ProfileApiServiceImpl(get(), get()) }
        single<FileUploadService> { FileUploadServiceImpl(get()) }
        single<LoanApiService> { LoanApiService(get()) }
        single<PaymentApiService> { PaymentApiService(get()) }

        // Repositories
        single<AuthRepository> {
            AuthRepositoryImpl(
                authApiService = get(),
                tokenStorage = get(),
                profileCache = get(),
                userPreferences = get(),
            )
        }

        single<ProfileRepository> {
            ProfileRepositoryImpl(
                profileApiService = get(),
                profileCache = get(),
            )
        }

        single<LoanRepository> {
            LoanRepositoryImpl(
                loanApiService = get(),
                localStorage = get(),
                cacheManager = get(),
            )
        }

        single<PaymentRepository> {
            PaymentRepositoryImpl(
                paymentApiService = get(),
                localStorage = get(),
                cacheManager = get(),
            )
        }

        // Auth Use Cases
        factory { SendOtpUseCase(get()) }
        factory { VerifyOtpUseCase(get()) }
        factory { SetPinUseCase(get()) }
        factory { LoginUseCase(get()) }
        factory { LogoutUseCase(get()) }
        factory { RefreshTokenUseCase(get()) }
        factory { IsLoggedInUseCase(get()) }
        factory { CreateClientUseCase(get()) }
        factory { UpdatePinUseCase(get()) }
        factory { ChangeMobileNumberUseCase(get()) }

        // Profile Use Cases
        factory { GetUserProfileUseCase(get()) }
        factory { UpdatePersonalDetailsUseCase(get()) }
        factory { UpdateAddressUseCase(get()) }
        factory { UploadProfilePictureUseCase(get()) }
        factory { UploadDocumentUseCase(get()) }
        factory { ManageNextOfKinUseCase(get()) }
        factory { ManageClientTypeUseCase(get()) }
        factory { ValidateProfileCompletionUseCase() }

        // ========== CASH LOAN USE CASES ==========
        factory { GetCashLoanFormDataUseCase(get()) }
        factory { CalculateCashLoanTermsUseCase(get()) }
        factory { SubmitCashLoanApplicationUseCase(get()) }
        factory { SaveCashLoanDraftUseCase(get()) }
        factory { GetCashLoanDraftUseCase(get()) }
        factory { DeleteCashLoanDraftUseCase(get()) }

        // ========== PAYGO LOAN USE CASES ==========
        factory { GetPayGoCategoriesUseCase(get()) }
        factory { GetPayGoProductsUseCase(get()) }
        factory { CalculatePayGoTermsUseCase(get()) }
        factory { SubmitPayGoApplicationUseCase(get()) }
        factory { SavePayGoLoanDraftUseCase(get()) }
        factory { GetPayGoLoanDraftUseCase(get()) }
        factory { DeletePayGoLoanDraftUseCase(get()) }

        // ========== GENERAL LOAN USE CASES ==========
        factory { GetLoanHistoryUseCase(get()) }
        factory { GetLoanDetailsUseCase(get()) }
        factory { GetCurrentLoansUseCase(get()) }
        factory { ObserveLoansUseCase(get()) }
        factory { ObserveApplicationStatusUseCase(get()) }
        factory { WithdrawLoanApplicationUseCase(get()) }
        factory { DownloadLoanAgreementUseCase(get()) }
        factory { SyncLoansUseCase(get()) }
        factory { GetCachedLoansUseCase(get()) }

        // ========== LOAN VALIDATION USE CASES ==========
        factory { ValidateCashLoanApplicationUseCase(get()) }
        factory { ValidatePayGoApplicationUseCase(get()) }
        factory { CheckLoanEligibilityUseCase(get()) }

        // ========== PAYMENT DASHBOARD USE CASES ==========
        factory { GetPaymentDashboardUseCase(get()) }
        factory { ObservePaymentDashboardUseCase(get()) }
        factory { GetPaymentHistoryUseCase(get()) }
        factory { GetPaymentMethodsUseCase(get()) }
        factory { GetActivePaymentMethodsUseCase(get()) }

        // ========== PAYMENT PROCESSING USE CASES ==========
        factory { ProcessPaymentUseCase(get()) }
        factory { GetPaymentStatusUseCase(get()) }
        factory { ObservePaymentStatusUseCase(get()) }
        factory { CancelPaymentUseCase(get()) }
        factory { RetryFailedPaymentUseCase(get()) }
        factory { ValidatePaymentRequestUseCase(get()) }

        // ========== PAYMENT RECEIPT USE CASES ==========
        factory { GetPaymentReceiptUseCase(get()) }
        factory { DownloadReceiptUseCase(get()) }
        factory { ResendReceiptToEmailUseCase(get()) }
        factory { ShareReceiptUseCase() } // No dependencies

        // ========== EARLY PAYOFF USE CASES ==========
        factory { CalculateEarlyPayoffUseCase(get()) }
        factory { ProcessEarlyPayoffUseCase(get()) }
        factory { CheckEarlyPayoffEligibilityUseCase() } // No dependencies

        // ========== PAYMENT ANALYTICS USE CASES ==========
        factory { GetPaymentAnalyticsUseCase(get()) }
        factory { GeneratePaymentReportUseCase(get()) }

        // ========== PAYMENT UTILITY USE CASES ==========
        factory { GetRecommendedPaymentAmountUseCase(get()) }
        factory { CheckPaymentEligibilityUseCase(get()) }
        factory { SyncPaymentsUseCase(get()) }
        factory { GetCachedPaymentsUseCase(get()) }
        factory { ObservePaymentUpdatesUseCase(get()) }

        // ========== PAYMENT NOTIFICATION USE CASES ==========
        factory { ProcessPaymentNotificationUseCase(get()) }
        factory { CreatePaymentReminderUseCase() } // No dependencies
    }

package com.soshopay.android.ui.state

/**
 * Sealed class representing different authentication UI states
 */
sealed class AuthUiState {
    object Idle : AuthUiState()

    object Loading : AuthUiState()

    data class Success<T>(
        val data: T,
    ) : AuthUiState()

    data class Error(
        val message: String,
        val throwable: Throwable? = null,
    ) : AuthUiState()
}

/**
 * Data class representing the login screen state
 */
data class LoginScreenState(
    val phoneNumber: String = "",
    val pin: String = "",
    val isPhoneNumberValid: Boolean = true,
    val isPinValid: Boolean = true,
    val phoneNumberError: String? = null,
    val pinError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginEnabled: Boolean = false,
) {
    fun hasErrors(): Boolean = phoneNumberError != null || pinError != null || errorMessage != null
}

/**
 * Data class representing the sign up screen state
 */
data class SignUpScreenState(
    val phoneNumber: String = "",
    val isPhoneNumberValid: Boolean = true,
    val phoneNumberError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSignUpEnabled: Boolean = false,
    val showUserExistsDialog: Boolean = false,
) {
    fun hasErrors(): Boolean = phoneNumberError != null || errorMessage != null
}

/**
 * Data class representing the OTP verification screen state
 */
data class OtpVerificationState(
    val phoneNumber: String = "",
    val otpCode: String = "",
    val isOtpValid: Boolean = true,
    val otpError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isVerificationEnabled: Boolean = false,
    val canResend: Boolean = false,
    val timeRemaining: Int = 60, // seconds
    val attemptsRemaining: Int = 3,
    val sessionId: String? = null,
) {
    fun hasErrors(): Boolean = otpError != null || errorMessage != null

    fun isTimerExpired(): Boolean = timeRemaining <= 0

    fun isMaxAttemptsReached(): Boolean = attemptsRemaining <= 0
}

/**
 * Data class representing the PIN setup screen state
 */
data class PinSetupState(
    val pin: String = "",
    val confirmPin: String = "",
    val isPinValid: Boolean = true,
    val isConfirmPinValid: Boolean = true,
    val pinError: String? = null,
    val confirmPinError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPinSetupEnabled: Boolean = false,
    val tempToken: String? = null,
    val phoneNumber: String = "",
) {
    fun hasErrors(): Boolean = pinError != null || confirmPinError != null || errorMessage != null

    fun pinsMatch(): Boolean = pin == confirmPin && pin.isNotEmpty()
}

/**
 * Sealed class representing different authentication events
 */
sealed class AuthEvent {
    // Login Events
    data class UpdatePhoneNumber(
        val phoneNumber: String,
    ) : AuthEvent()

    data class UpdatePin(
        val pin: String,
    ) : AuthEvent()

    object LoginClicked : AuthEvent()

    object ClearLoginError : AuthEvent()

    // Sign Up Events
    data class UpdateSignUpPhoneNumber(
        val phoneNumber: String,
    ) : AuthEvent()

    object SignUpClicked : AuthEvent()

    object ClearSignUpError : AuthEvent()

    object DismissUserExistsDialog : AuthEvent()

    object NavigateToLogin : AuthEvent()

    // OTP Events
    data class UpdateOtpCode(
        val code: String,
    ) : AuthEvent()

    object VerifyOtpClicked : AuthEvent()

    object ResendOtpClicked : AuthEvent()

    object ClearOtpError : AuthEvent()

    data class UpdateOtpTimer(
        val timeRemaining: Int,
    ) : AuthEvent()

    // PIN Setup Events
    data class UpdateSetupPin(
        val pin: String,
    ) : AuthEvent()

    data class UpdateConfirmPin(
        val confirmPin: String,
    ) : AuthEvent()

    object SetupPinClicked : AuthEvent()

    object ClearPinSetupError : AuthEvent()

    // Navigation Events
    object NavigateToSignUp : AuthEvent()

    object NavigateToHome : AuthEvent()

    object NavigateBack : AuthEvent()
}

/**
 * Sealed class representing different authentication navigation destinations
 */
sealed class AuthNavigation {
    object ToLogin : AuthNavigation()

    object ToSignUp : AuthNavigation()

    object ToOtpVerification : AuthNavigation()

    object ToPinSetup : AuthNavigation()

    object ToHome : AuthNavigation()

    object Back : AuthNavigation()
}

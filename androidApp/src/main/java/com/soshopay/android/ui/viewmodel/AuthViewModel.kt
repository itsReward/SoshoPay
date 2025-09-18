package com.soshopay.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soshopay.android.ui.state.AuthEvent
import com.soshopay.android.ui.state.AuthNavigation
import com.soshopay.android.ui.state.LoginScreenState
import com.soshopay.android.ui.state.OtpVerificationState
import com.soshopay.android.ui.state.PinSetupState
import com.soshopay.android.ui.state.SignUpScreenState
import com.soshopay.domain.model.OtpSession
import com.soshopay.domain.usecase.auth.IsLoggedInUseCase
import com.soshopay.domain.usecase.auth.LoginUseCase
import com.soshopay.domain.usecase.auth.SendOtpUseCase
import com.soshopay.domain.usecase.auth.SetPinUseCase
import com.soshopay.domain.usecase.auth.VerifyOtpUseCase
import com.soshopay.domain.util.Result
import com.soshopay.domain.util.SoshoPayException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Main ViewModel for authentication operations following MVVM and Clean Architecture patterns.
 *
 * This ViewModel coordinates all authentication workflows including login, sign up, OTP verification,
 * and PIN setup. It depends only on Use Cases (Domain layer) and manages UI state reactively.
 *
 * Key principles followed:
 * - Single Responsibility: Each method handles one specific auth operation
 * - Dependency Inversion: Depends on Use Case abstractions, not implementations
 * - Open/Closed: Extensible for new auth features without modifying existing code
 * - Interface Segregation: Uses focused Use Cases rather than monolithic services
 *
 * @param sendOtpUseCase Use case for sending OTP codes
 * @param verifyOtpUseCase Use case for verifying OTP codes
 * @param setPinUseCase Use case for setting up user PIN
 * @param loginUseCase Use case for user login
 * @param isLoggedInUseCase Use case for checking login status
 */
class AuthViewModel(
    private val sendOtpUseCase: SendOtpUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val setPinUseCase: SetPinUseCase,
    private val loginUseCase: LoginUseCase,
    private val isLoggedInUseCase: IsLoggedInUseCase,
) : ViewModel() {
    // ========== STATE MANAGEMENT ==========

    private val _loginState = MutableStateFlow(LoginScreenState())
    val loginState: StateFlow<LoginScreenState> = _loginState.asStateFlow()

    private val _signUpState = MutableStateFlow(SignUpScreenState())
    val signUpState: StateFlow<SignUpScreenState> = _signUpState.asStateFlow()

    private val _otpVerificationState = MutableStateFlow(OtpVerificationState())
    val otpVerificationState: StateFlow<OtpVerificationState> = _otpVerificationState.asStateFlow()

    private val _pinSetupState = MutableStateFlow(PinSetupState())
    val pinSetupState: StateFlow<PinSetupState> = _pinSetupState.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<AuthNavigation>()
    val navigationEvents: SharedFlow<AuthNavigation> = _navigationEvents.asSharedFlow()

    // Internal state
    private var currentOtpSession: OtpSession? = null
    private var otpTimerJob: Job? = null

    // ========== EVENT HANDLING ==========

    fun onEvent(event: AuthEvent) {
        when (event) {
            // Login Events
            is AuthEvent.UpdatePhoneNumber -> updateLoginPhoneNumber(event.phoneNumber)
            is AuthEvent.UpdatePin -> updateLoginPin(event.pin)
            is AuthEvent.LoginClicked -> performLogin()
            is AuthEvent.ClearLoginError -> clearLoginError()

            // Sign Up Events
            is AuthEvent.UpdateSignUpPhoneNumber -> updateSignUpPhoneNumber(event.phoneNumber)
            is AuthEvent.SignUpClicked -> performSignUp()
            is AuthEvent.ClearSignUpError -> clearSignUpError()
            is AuthEvent.DismissUserExistsDialog -> dismissUserExistsDialog()
            is AuthEvent.NavigateToLogin -> navigateToLogin()

            // OTP Events
            is AuthEvent.UpdateOtpCode -> updateOtpCode(event.code)
            is AuthEvent.VerifyOtpClicked -> verifyOtp()
            is AuthEvent.ResendOtpClicked -> resendOtp()
            is AuthEvent.ClearOtpError -> clearOtpError()
            is AuthEvent.UpdateOtpTimer -> updateOtpTimer(event.timeRemaining)

            // PIN Setup Events
            is AuthEvent.UpdateSetupPin -> updateSetupPin(event.pin)
            is AuthEvent.UpdateConfirmPin -> updateConfirmPin(event.confirmPin)
            is AuthEvent.SetupPinClicked -> setupPin()
            is AuthEvent.ClearPinSetupError -> clearPinSetupError()

            // Navigation Events
            is AuthEvent.NavigateToSignUp -> navigateToSignUp()
            is AuthEvent.NavigateToHome -> navigateToHome()
            is AuthEvent.NavigateBack -> navigateBack()
        }
    }

    // ========== LOGIN OPERATIONS ==========

    private fun updateLoginPhoneNumber(phoneNumber: String) {
        val error = validatePhoneNumber(phoneNumber)
        _loginState.value =
            _loginState.value.copy(
                phoneNumber = phoneNumber,
                phoneNumberError = error,
                isPhoneNumberValid = error == null,
                isLoginEnabled = error == null && _loginState.value.pin.isNotEmpty(),
            )
    }

    private fun updateLoginPin(pin: String) {
        val error = validatePin(pin)
        _loginState.value =
            _loginState.value.copy(
                pin = pin,
                pinError = error,
                isPinValid = error == null,
                isLoginEnabled = error == null && _loginState.value.phoneNumberError == null,
            )
    }

    private fun performLogin() {
        if (!_loginState.value.isLoginEnabled) return

        _loginState.value = _loginState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result =
                loginUseCase(
                    phoneNumber = _loginState.value.phoneNumber,
                    pin = _loginState.value.pin,
                )

            when (result) {
                is Result.Success -> {
                    _loginState.value = _loginState.value.copy(isLoading = false)
                    _navigationEvents.emit(AuthNavigation.ToHome)
                }
                is Result.Error -> {
                    _loginState.value =
                        _loginState.value.copy(
                            isLoading = false,
                            errorMessage = getErrorMessage(result.exception),
                        )
                }
                is Result.Loading -> {
                    _loginState.value = _loginState.value.copy(isLoading = true)
                }
            }
        }
    }

    private fun clearLoginError() {
        _loginState.value = _loginState.value.copy(errorMessage = null)
    }

    // ========== SIGN UP OPERATIONS ==========

    private fun updateSignUpPhoneNumber(phoneNumber: String) {
        val error = validatePhoneNumber(phoneNumber)
        _signUpState.value =
            _signUpState.value.copy(
                phoneNumber = phoneNumber,
                phoneNumberError = error,
                isPhoneNumberValid = error == null,
                isSignUpEnabled = error == null,
            )
    }

    private fun performSignUp() {
        if (!_signUpState.value.isSignUpEnabled) return

        _signUpState.value = _signUpState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = sendOtpUseCase(_signUpState.value.phoneNumber)

            when (result) {
                is Result.Success -> {
                    currentOtpSession = result.data
                    _signUpState.value = _signUpState.value.copy(isLoading = false)
                    _otpVerificationState.value =
                        _otpVerificationState.value.copy(
                            phoneNumber = _signUpState.value.phoneNumber,
                            sessionId = result.data.id,
                        )
                    startOtpTimer()
                    _navigationEvents.emit(AuthNavigation.ToOtpVerification)
                }
                is Result.Error -> {
                    val errorMessage = getErrorMessage(result.exception)

                    // Check if user already exists
                    if (result.exception is SoshoPayException.UserAlreadyExistsException) {
                        _signUpState.value =
                            _signUpState.value.copy(
                                isLoading = false,
                                showUserExistsDialog = true,
                            )
                    } else {
                        _signUpState.value =
                            _signUpState.value.copy(
                                isLoading = false,
                                errorMessage = errorMessage,
                            )
                    }
                }
                is Result.Loading -> {
                    _signUpState.value = _signUpState.value.copy(isLoading = true)
                }
            }
        }
    }

    private fun clearSignUpError() {
        _signUpState.value = _signUpState.value.copy(errorMessage = null)
    }

    private fun dismissUserExistsDialog() {
        _signUpState.value = _signUpState.value.copy(showUserExistsDialog = false)
    }

    // ========== OTP OPERATIONS ==========

    private fun updateOtpCode(code: String) {
        if (code.length <= 6) {
            val error = if (code.isNotEmpty() && !code.all { it.isDigit() }) "OTP must contain only digits" else null
            _otpVerificationState.value =
                _otpVerificationState.value.copy(
                    otpCode = code,
                    otpError = error,
                    isOtpValid = error == null,
                    isVerificationEnabled = code.length == 6 && error == null,
                )
        }
    }

    private fun verifyOtp() {
        val currentSession = currentOtpSession ?: return
        if (!_otpVerificationState.value.isVerificationEnabled) return

        _otpVerificationState.value = _otpVerificationState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = verifyOtpUseCase(currentSession, _otpVerificationState.value.otpCode)

            when (result) {
                is Result.Success -> {
                    otpTimerJob?.cancel()
                    _otpVerificationState.value = _otpVerificationState.value.copy(isLoading = false)
                    _pinSetupState.value =
                        _pinSetupState.value.copy(
                            tempToken = result.data,
                            phoneNumber = _otpVerificationState.value.phoneNumber,
                        )
                    _navigationEvents.emit(AuthNavigation.ToPinSetup)
                }
                is Result.Error -> {
                    val currentState = _otpVerificationState.value
                    val newAttemptsRemaining = maxOf(0, currentState.attemptsRemaining - 1)

                    _otpVerificationState.value =
                        currentState.copy(
                            isLoading = false,
                            errorMessage = getErrorMessage(result.exception),
                            attemptsRemaining = newAttemptsRemaining,
                        )
                }
                is Result.Loading -> {
                    _otpVerificationState.value = _otpVerificationState.value.copy(isLoading = true)
                }
            }
        }
    }

    private fun resendOtp() {
        if (!_otpVerificationState.value.canResend) return

        _otpVerificationState.value = _otpVerificationState.value.copy(isLoading = true)

        viewModelScope.launch {
            val result = sendOtpUseCase(_otpVerificationState.value.phoneNumber)

            when (result) {
                is Result.Success -> {
                    currentOtpSession = result.data
                    _otpVerificationState.value =
                        _otpVerificationState.value.copy(
                            isLoading = false,
                            otpCode = "",
                            attemptsRemaining = 3,
                            sessionId = result.data.id,
                            errorMessage = null,
                        )
                    startOtpTimer()
                }
                is Result.Error -> {
                    _otpVerificationState.value =
                        _otpVerificationState.value.copy(
                            isLoading = false,
                            errorMessage = getErrorMessage(result.exception),
                        )
                }
                is Result.Loading -> {
                    _otpVerificationState.value = _otpVerificationState.value.copy(isLoading = true)
                }
            }
        }
    }

    private fun startOtpTimer() {
        otpTimerJob?.cancel()
        otpTimerJob =
            viewModelScope.launch {
                repeat(60) { second ->
                    val remaining = 60 - second
                    _otpVerificationState.value =
                        _otpVerificationState.value.copy(
                            timeRemaining = remaining,
                            canResend = false,
                        )
                    delay(1000)
                }
                _otpVerificationState.value =
                    _otpVerificationState.value.copy(
                        timeRemaining = 0,
                        canResend = true,
                    )
            }
    }

    private fun updateOtpTimer(timeRemaining: Int) {
        _otpVerificationState.value =
            _otpVerificationState.value.copy(
                timeRemaining = timeRemaining,
                canResend = timeRemaining <= 0,
            )
    }

    private fun clearOtpError() {
        _otpVerificationState.value = _otpVerificationState.value.copy(errorMessage = null)
    }

    // ========== PIN SETUP OPERATIONS ==========

    private fun updateSetupPin(pin: String) {
        val error = validatePin(pin)
        val currentState = _pinSetupState.value
        _pinSetupState.value =
            currentState.copy(
                pin = pin,
                pinError = error,
                isPinValid = error == null,
                isPinSetupEnabled = error == null && currentState.confirmPin.isNotEmpty() && pin == currentState.confirmPin,
            )
    }

    private fun updateConfirmPin(confirmPin: String) {
        val currentState = _pinSetupState.value
        val error =
            when {
                confirmPin.isEmpty() -> null
                confirmPin != currentState.pin -> "PINs do not match"
                else -> null
            }

        _pinSetupState.value =
            currentState.copy(
                confirmPin = confirmPin,
                confirmPinError = error,
                isConfirmPinValid = error == null,
                isPinSetupEnabled = error == null && currentState.pinError == null && currentState.pin.isNotEmpty(),
            )
    }

    private fun setupPin() {
        val currentState = _pinSetupState.value
        if (!currentState.isPinSetupEnabled || currentState.tempToken == null) return

        _pinSetupState.value = currentState.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result =
                setPinUseCase(
                    tempToken = currentState.tempToken!!,
                    pin = currentState.pin,
                    phoneNumber = currentState.phoneNumber,
                )

            when (result) {
                is Result.Success -> {
                    _pinSetupState.value = currentState.copy(isLoading = false)
                    _navigationEvents.emit(AuthNavigation.ToHome)
                }
                is Result.Error -> {
                    _pinSetupState.value =
                        currentState.copy(
                            isLoading = false,
                            errorMessage = getErrorMessage(result.exception),
                        )
                }
                is Result.Loading -> {
                    _pinSetupState.value = currentState.copy(isLoading = true)
                }
            }
        }
    }

    private fun clearPinSetupError() {
        _pinSetupState.value = _pinSetupState.value.copy(errorMessage = null)
    }

    // ========== NAVIGATION OPERATIONS ==========

    private fun navigateToLogin() {
        viewModelScope.launch {
            _navigationEvents.emit(AuthNavigation.ToLogin)
        }
    }

    private fun navigateToSignUp() {
        viewModelScope.launch {
            _navigationEvents.emit(AuthNavigation.ToSignUp)
        }
    }

    private fun navigateToHome() {
        viewModelScope.launch {
            _navigationEvents.emit(AuthNavigation.ToHome)
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _navigationEvents.emit(AuthNavigation.Back)
        }
    }

    // ========== VALIDATION HELPERS ==========

    private fun validatePhoneNumber(phoneNumber: String): String? =
        when {
            phoneNumber.isEmpty() -> "Phone number is required"
            phoneNumber.length < 10 -> "Phone number must be at least 10 digits"
            !phoneNumber.startsWith("07") && !phoneNumber.startsWith("+263") -> "Please enter a valid Zimbabwe phone number"
            else -> null
        }

    private fun validatePin(pin: String): String? =
        when {
            pin.isEmpty() -> "PIN is required"
            pin.length != 4 -> "PIN must be exactly 4 digits"
            !pin.all { it.isDigit() } -> "PIN must contain only digits"
            else -> null
        }

    private fun getErrorMessage(throwable: Throwable): String =
        when (throwable) {
            is SoshoPayException.ValidationException -> throwable.message ?: "Validation error"
            is SoshoPayException.NetworkException -> "Network error. Please check your connection."
            is SoshoPayException.UnauthorizedException -> "Invalid phone number or PIN"
            is SoshoPayException.OtpExpiredException -> "OTP has expired. Please request a new one."
            is SoshoPayException.MaxAttemptsExceededException -> "Maximum attempts exceeded. Please request a new OTP."
            is SoshoPayException.UserAlreadyExistsException -> "User already exists. Please login instead."
            else -> throwable.message ?: "An unexpected error occurred"
        }

    // ========== INITIALIZATION ==========

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            if (isLoggedInUseCase()) {
                _navigationEvents.emit(AuthNavigation.ToHome)
            }
        }
    }

    // ========== CLEANUP ==========

    override fun onCleared() {
        super.onCleared()
        otpTimerJob?.cancel()
    }
}

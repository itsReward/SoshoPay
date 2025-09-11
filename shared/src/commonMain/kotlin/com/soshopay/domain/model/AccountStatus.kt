package com.soshopay.domain.model

enum class AccountStatus {
    INCOMPLETE,
    COMPLETE,
    VERIFIED,
    ;

    fun getDisplayName(): String =
        when (this) {
            INCOMPLETE -> "Profile Incomplete"
            COMPLETE -> "Profile Complete"
            VERIFIED -> "Account Verified"
        }

    fun getDescription(): String =
        when (this) {
            INCOMPLETE -> "Please complete your profile information to access all features"
            COMPLETE -> "Profile completed - awaiting verification"
            VERIFIED -> "Your account is fully verified and active"
        }

    fun getProgressPercentage(): Int =
        when (this) {
            INCOMPLETE -> 33
            COMPLETE -> 66
            VERIFIED -> 100
        }

    fun canApplyForLoan(): Boolean = this == VERIFIED

    fun canMakePayments(): Boolean = this == COMPLETE || this == VERIFIED
}

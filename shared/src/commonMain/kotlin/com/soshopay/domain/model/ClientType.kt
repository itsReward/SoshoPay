package com.soshopay.domain.model

enum class ClientType {
    PRIVATE_SECTOR_EMPLOYEE,
    GOVERNMENT_EMPLOYEE,
    ENTREPRENEUR,
    ;

    fun getDisplayName(): String =
        when (this) {
            PRIVATE_SECTOR_EMPLOYEE -> "Private Sector Employee"
            GOVERNMENT_EMPLOYEE -> "Government Employee"
            ENTREPRENEUR -> "Entrepreneur"
        }

    fun getDescription(): String =
        when (this) {
            PRIVATE_SECTOR_EMPLOYEE -> "Employee of a private company or organization"
            GOVERNMENT_EMPLOYEE -> "Employee of government or state-owned entity"
            ENTREPRENEUR -> "Self-employed or business owner"
        }

    fun getLoanEligibilityRequirements(): List<String> =
        when (this) {
            PRIVATE_SECTOR_EMPLOYEE ->
                listOf(
                    "Employment contract or letter",
                    "3 months payslips",
                    "Bank statements",
                )
            GOVERNMENT_EMPLOYEE ->
                listOf(
                    "Employment letter",
                    "Latest payslip",
                    "Bank statements",
                )
            ENTREPRENEUR ->
                listOf(
                    "Business registration certificate",
                    "6 months bank statements",
                    "Tax clearance certificate",
                    "Business financial statements",
                )
        }

    fun getMaxLoanAmount(): Double =
        when (this) {
            PRIVATE_SECTOR_EMPLOYEE -> 50000.0
            GOVERNMENT_EMPLOYEE -> 75000.0
            ENTREPRENEUR -> 100000.0
        }

    fun requiresAdditionalVerification(): Boolean =
        when (this) {
            PRIVATE_SECTOR_EMPLOYEE -> false
            GOVERNMENT_EMPLOYEE -> false
            ENTREPRENEUR -> true
        }
}

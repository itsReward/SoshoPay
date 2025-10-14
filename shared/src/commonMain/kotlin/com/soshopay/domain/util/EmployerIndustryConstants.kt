package com.soshopay.domain.util

/**
 * Constants for employer industries used in loan applications.
 *
 * Following the Open/Closed Principle, this object can be extended
 * with new industries without modifying existing code.
 */
object EmployerIndustryConstants {
    /**
     * List of common employer industries
     * The last item is always "Other" for industries not in the list
     */
    val INDUSTRIES =
        listOf(
            "Agriculture & Farming",
            "Automotive",
            "Banking & Financial Services",
            "Construction",
            "Education",
            "Energy & Utilities",
            "Entertainment & Media",
            "Food & Beverage",
            "Government & Public Sector",
            "Healthcare & Medical",
            "Hospitality & Tourism",
            "Information Technology",
            "Insurance",
            "Legal Services",
            "Manufacturing",
            "Mining & Extraction",
            "Non-Profit & NGO",
            "Real Estate",
            "Retail & Wholesale",
            "Telecommunications",
            "Transportation & Logistics",
            "Other",
        )

    /**
     * Gets the industry value or "Other" if not in the predefined list
     * @param industry The industry string to validate
     * @return The industry if valid, otherwise "Other"
     */
    fun getValidIndustry(industry: String): String = if (INDUSTRIES.contains(industry)) industry else "Other"

    /**
     * Checks if an industry requires additional information
     * @param industry The industry to check
     * @return true if "Other" is selected
     */
    fun requiresAdditionalInfo(industry: String): Boolean = industry == "Other"

    /**
     * Gets all industries except "Other"
     * @return List of predefined industries
     */
    fun getPredefinedIndustries(): List<String> = INDUSTRIES.filter { it != "Other" }
}

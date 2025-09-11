package com.soshopay.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ClientType(
    val current: String = "PRIVATE_SECTOR_EMPLOYEE",
    val pending: String? = null,
    val approvalStatus: ApprovalStatus = ApprovalStatus.APPROVED,
    val lastChanged: Long =
        kotlinx.datetime.Clock.System
            .now()
            .toEpochMilliseconds(),
    val requiresAdminApproval: Boolean = false,
) {
    fun isPendingApproval(): Boolean = pending != null && approvalStatus == ApprovalStatus.PENDING

    fun canChangeType(): Boolean = !isPendingApproval()
}

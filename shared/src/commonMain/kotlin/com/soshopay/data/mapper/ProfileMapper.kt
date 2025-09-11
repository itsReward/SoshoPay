package com.soshopay.data.mapper

import com.soshopay.data.remote.dto.*
import com.soshopay.domain.model.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

object ProfileMapper {
    fun mapToUser(dto: ClientDto): User =
        User(
            id = dto.id,
            phoneNumber = dto.mobile,
            profilePicture = dto.profilePicture?.let { mapToProfilePicture(it) },
            personalDetails = dto.personalDetails?.let { mapToPersonalDetails(it) },
            address = dto.address?.let { mapToAddress(it) },
            documents = dto.documents?.let { mapToDocuments(it) },
            nextOfKin = dto.nextOfKin?.let { mapToNextOfKin(it) },
            clientType = mapToClientType(dto.clientType),
            verificationStatus = mapToVerificationStatus(dto.verificationStatus),
            canApplyForLoan = dto.canApplyForLoan,
            accountStatus = mapToAccountStatus(dto.accountStatus),
            createdAt =
                parseDateTime(dto.createdAt) ?: kotlinx.datetime.Clock.System
                    .now()
                    .toEpochMilliseconds(),
            updatedAt =
                parseDateTime(dto.updatedAt) ?: kotlinx.datetime.Clock.System
                    .now()
                    .toEpochMilliseconds(),
        )

    fun mapToPersonalDetails(dto: PersonalDetailsDto): PersonalDetails =
        PersonalDetails(
            firstName = dto.firstName,
            lastName = dto.lastName,
            dateOfBirth = parseDateTime(dto.dateOfBirth) ?: 0L,
            gender = dto.gender,
            nationality = dto.nationality,
            occupation = dto.occupation,
            monthlyIncome = dto.monthlyIncome,
            lastUpdated =
                parseDateTime(dto.lastUpdated) ?: Clock.System
                    .now()
                    .toEpochMilliseconds(),
        )

    fun mapToAddress(dto: AddressDto): Address =
        Address(
            streetAddress = dto.streetAddress,
            suburb = dto.suburb,
            city = dto.city,
            province = dto.province,
            postalCode = dto.postalCode,
            residenceType = dto.residenceType,
            lastUpdated =
                parseDateTime(dto.lastUpdated) ?: Clock.System
                    .now()
                    .toEpochMilliseconds(),
        )

    fun mapToProfilePicture(url: String): ProfilePicture =
        ProfilePicture(
            url = url,
            uploadDate =
                kotlinx.datetime.Clock.System
                    .now()
                    .toEpochMilliseconds(),
            lastUpdated =
                kotlinx.datetime.Clock.System
                    .now()
                    .toEpochMilliseconds(),
        )

    fun mapToDocuments(dto: DocumentsDto): Documents =
        Documents(
            proofOfResidence = dto.proofOfResidence?.let { mapToDocument(it) },
            nationalId = dto.nationalId?.let { mapToDocument(it) },
        )

    fun mapToDocument(dto: DocumentDto): Document =
        Document(
            id = dto.id,
            url = dto.url,
            fileName = dto.fileName,
            fileSize = dto.fileSize,
            uploadDate =
                parseDateTime(dto.uploadDate) ?: kotlinx.datetime.Clock.System
                    .now()
                    .toEpochMilliseconds(),
            lastUpdated =
                parseDateTime(dto.lastUpdated) ?: kotlinx.datetime.Clock.System
                    .now()
                    .toEpochMilliseconds(),
            verificationStatus = mapToVerificationStatus(dto.verificationStatus),
            verificationDate = parseDateTime(dto.verificationDate),
            verificationNotes = dto.verificationNotes,
            documentType = mapToDocumentType(dto.documentType),
        )

    fun mapToNextOfKin(dto: NextOfKinDto): NextOfKin =
        NextOfKin(
            id = dto.id,
            userId = dto.userId,
            fullName = dto.fullName,
            relationship = dto.relationship,
            phoneNumber = dto.phoneNumber,
            address = mapToAddress(dto.address),
            documents = dto.documents?.let { mapToDocuments(it) },
            createdAt =
                parseDateTime(dto.createdAt) ?: kotlinx.datetime.Clock.System
                    .now()
                    .toEpochMilliseconds(),
            updatedAt =
                parseDateTime(dto.updatedAt) ?: kotlinx.datetime.Clock.System
                    .now()
                    .toEpochMilliseconds(),
        )

    // Reverse mapping - Domain to DTO
    fun mapToPersonalDetailsDto(personalDetails: PersonalDetails): PersonalDetailsDto =
        PersonalDetailsDto(
            firstName = personalDetails.firstName,
            lastName = personalDetails.lastName,
            dateOfBirth = formatDateTime(personalDetails.dateOfBirth),
            gender = personalDetails.gender,
            nationality = personalDetails.nationality,
            occupation = personalDetails.occupation,
            monthlyIncome = personalDetails.monthlyIncome,
            lastUpdated = formatDateTime(personalDetails.lastUpdated),
        )

    fun mapToAddressDto(address: Address): AddressDto =
        AddressDto(
            streetAddress = address.streetAddress,
            suburb = address.suburb,
            city = address.city,
            province = address.province,
            postalCode = address.postalCode,
            residenceType = address.residenceType,
            lastUpdated = formatDateTime(address.lastUpdated),
        )

    fun mapToNextOfKinDto(nextOfKin: NextOfKin): NextOfKinDto =
        NextOfKinDto(
            id = nextOfKin.id,
            userId = nextOfKin.userId,
            fullName = nextOfKin.fullName,
            relationship = nextOfKin.relationship,
            phoneNumber = nextOfKin.phoneNumber,
            address = mapToAddressDto(nextOfKin.address),
            documents = nextOfKin.documents?.let { mapToDocumentsDto(it) },
            createdAt = formatDateTime(nextOfKin.createdAt),
            updatedAt = formatDateTime(nextOfKin.updatedAt),
        )

    fun mapToDocumentsDto(documents: Documents): DocumentsDto =
        DocumentsDto(
            proofOfResidence = documents.proofOfResidence?.let { mapToDocumentDto(it) },
            nationalId = documents.nationalId?.let { mapToDocumentDto(it) },
        )

    fun mapToDocumentDto(document: Document): DocumentDto =
        DocumentDto(
            id = document.id,
            url = document.url,
            fileName = document.fileName,
            fileSize = document.fileSize,
            uploadDate = formatDateTime(document.uploadDate),
            lastUpdated = formatDateTime(document.lastUpdated),
            verificationStatus = mapFromVerificationStatus(document.verificationStatus),
            verificationDate = document.verificationDate?.let { formatDateTime(it) },
            verificationNotes = document.verificationNotes,
            documentType = mapFromDocumentType(document.documentType),
        )

    // Enum mapping functions
    private fun mapToClientType(clientType: String): ClientType =
        when (clientType.uppercase()) {
            "PRIVATE_SECTOR_EMPLOYEE" -> ClientType.PRIVATE_SECTOR_EMPLOYEE
            "GOVERNMENT_EMPLOYEE" -> ClientType.GOVERNMENT_EMPLOYEE
            "ENTREPRENEUR" -> ClientType.ENTREPRENEUR
            else -> ClientType.PRIVATE_SECTOR_EMPLOYEE
        }

    private fun mapToVerificationStatus(status: String): VerificationStatus =
        when (status.uppercase()) {
            "UNVERIFIED" -> VerificationStatus.UNVERIFIED
            "PENDING" -> VerificationStatus.PENDING
            "VERIFIED" -> VerificationStatus.VERIFIED
            "REJECTED" -> VerificationStatus.REJECTED
            else -> VerificationStatus.UNVERIFIED
        }

    private fun mapToAccountStatus(status: String): AccountStatus =
        when (status.uppercase()) {
            "INCOMPLETE" -> AccountStatus.INCOMPLETE
            "COMPLETE" -> AccountStatus.COMPLETE
            "VERIFIED" -> AccountStatus.VERIFIED
            else -> AccountStatus.INCOMPLETE
        }

    private fun mapToDocumentType(type: String): DocumentType =
        when (type.uppercase()) {
            "PROOF_OF_RESIDENCE" -> DocumentType.PROOF_OF_RESIDENCE
            "NATIONAL_ID" -> DocumentType.NATIONAL_ID
            "PROFILE_PICTURE" -> DocumentType.PROFILE_PICTURE
            else -> DocumentType.PROOF_OF_RESIDENCE
        }

    private fun mapFromVerificationStatus(status: VerificationStatus): String = status.name

    private fun mapFromDocumentType(type: DocumentType): String = type.name

    private fun parseDateTime(dateTimeString: String?): Long? =
        dateTimeString?.let {
            try {
                Instant.parse(it).toEpochMilliseconds()
            } catch (e: Exception) {
                null
            }
        }

    private fun formatDateTime(timestamp: Long): String = Instant.fromEpochMilliseconds(timestamp).toString()
}

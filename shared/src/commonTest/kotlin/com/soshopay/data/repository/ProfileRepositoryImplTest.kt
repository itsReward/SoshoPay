package com.soshopay.data.repository

import com.soshopay.data.mapper.ProfileMapper
import com.soshopay.data.remote.api.ProfileApiService
import com.soshopay.data.remote.dto.AddressDto
import com.soshopay.data.remote.dto.ClientDto
import com.soshopay.data.remote.dto.DocumentDto
import com.soshopay.data.remote.dto.DocumentsDto
import com.soshopay.data.remote.dto.NextOfKinDto
import com.soshopay.data.remote.dto.PersonalDetailsDto
import com.soshopay.domain.model.AccountStatus
import com.soshopay.domain.model.Address
import com.soshopay.domain.model.ClientType
import com.soshopay.domain.model.Document
import com.soshopay.domain.model.DocumentType
import com.soshopay.domain.model.Documents
import com.soshopay.domain.model.NextOfKin
import com.soshopay.domain.model.PersonalDetails
import com.soshopay.domain.model.ProfilePicture
import com.soshopay.domain.model.User
import com.soshopay.domain.model.VerificationStatus
import com.soshopay.domain.storage.ProfileCache
import com.soshopay.domain.util.Result
import com.soshopay.domain.util.SoshoPayException
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProfileRepositoryImplTest {
    private lateinit var profileApiService: ProfileApiService
    private lateinit var profileCache: ProfileCache
    private lateinit var repository: ProfileRepositoryImpl

    private val testUserId = "test-user-123"
    private val testPhoneNumber = "263771234567"
    private val currentTime = Clock.System.now().toEpochMilliseconds()

    @BeforeTest
    fun setup() {
        profileApiService = mockk()
        profileCache = mockk()
        repository =
            ProfileRepositoryImpl(
                profileApiService = profileApiService,
                profileCache = profileCache,
            )

        // Mock Logger static methods
        mockkObject(com.soshopay.domain.util.Logger)
        every {
            com.soshopay.domain.util.Logger
                .d(any(), any())
        } just Runs
        every {
            com.soshopay.domain.util.Logger
                .logProfileEvent(any(), any())
        } just Runs
        every {
            com.soshopay.domain.util.Logger
                .logFileUpload(any(), any(), any())
        } just Runs
    }

    @AfterTest
    fun tearDown() {
        unmockkAll()
    }

    // ==================== getUserProfile Tests ====================

    @Test
    fun `getUserProfile returns cached user when cache is valid and not forcing refresh`() =
        runTest {
            // Given
            val cachedUser = createTestUser()
            coEvery { profileCache.getCurrentUser() } returns cachedUser
            coEvery { profileCache.isProfileCacheValid(24) } returns true

            // When
            val result = repository.getUserProfile(forceRefresh = false)

            // Then
            assertTrue(result is Result.Success)
            assertEquals(cachedUser, (result as Result.Success).data)
            coVerify(exactly = 0) { profileApiService.getUserProfile() }
            coVerify { profileCache.getCurrentUser() }
            coVerify { profileCache.isProfileCacheValid(24) }
        }

    @Test
    fun `getUserProfile fetches from API when cache is invalid`() =
        runTest {
            // Given
            val clientDto = createTestClientDto()
            val user = createTestUser()

            coEvery { profileCache.getCurrentUser() } returns null
            coEvery { profileCache.isProfileCacheValid(24) } returns false
            coEvery { profileApiService.getUserProfile() } returns Result.Success(clientDto)
            coEvery { profileCache.saveUser(any()) } returns true
            coEvery { profileCache.setLastProfileSync(any()) } returns true

            mockkObject(ProfileMapper)
            every { ProfileMapper.mapToUser(clientDto) } returns user

            // When
            val result = repository.getUserProfile(forceRefresh = false)

            // Then
            assertTrue(result is Result.Success)
            assertEquals(user, (result as Result.Success).data)
            coVerify { profileApiService.getUserProfile() }
            coVerify { profileCache.saveUser(user) }
            coVerify { profileCache.setLastProfileSync(any()) }
        }

    @Test
    fun `getUserProfile fetches from API when forceRefresh is true`() =
        runTest {
            // Given
            val clientDto = createTestClientDto()
            val user = createTestUser()

            coEvery { profileApiService.getUserProfile() } returns Result.Success(clientDto)
            coEvery { profileCache.saveUser(any()) } returns true
            coEvery { profileCache.setLastProfileSync(any()) } returns true

            mockkObject(ProfileMapper)
            every { ProfileMapper.mapToUser(clientDto) } returns user

            // When
            val result = repository.getUserProfile(forceRefresh = true)

            // Then
            assertTrue(result is Result.Success)
            assertEquals(user, (result as Result.Success).data)
            coVerify(exactly = 0) { profileCache.isProfileCacheValid(any()) }
            coVerify { profileApiService.getUserProfile() }
        }

    @Test
    fun `getUserProfile returns cached user when API fails and cache exists`() =
        runTest {
            // Given
            val cachedUser = createTestUser()
            val apiError = Result.Error(SoshoPayException.NetworkException("Network error"))

            coEvery { profileCache.getCurrentUser() } returns null andThen cachedUser
            coEvery { profileCache.isProfileCacheValid(24) } returns false
            coEvery { profileApiService.getUserProfile() } returns apiError

            // When
            val result = repository.getUserProfile(forceRefresh = false)

            // Then
            assertTrue(result is Result.Success)
            assertEquals(cachedUser, (result as Result.Success).data)
        }

    @Test
    fun `getUserProfile returns error when API fails and no cache exists`() =
        runTest {
            // Given
            val apiError = Result.Error(SoshoPayException.NetworkException("Network error"))

            coEvery { profileCache.getCurrentUser() } returns null
            coEvery { profileCache.isProfileCacheValid(24) } returns false
            coEvery { profileApiService.getUserProfile() } returns apiError

            // When
            val result = repository.getUserProfile(forceRefresh = false)

            // Then
            assertTrue(result is Result.Error)
            assertTrue((result as Result.Error).exception is SoshoPayException.NetworkException)
        }

    // ==================== updatePersonalDetails Tests ====================

    @Test
    fun `updatePersonalDetails validates before updating`() =
        runTest {
            // Given
            val personalDetails = createTestPersonalDetails()

            mockkObject(com.soshopay.domain.util.ValidationUtils.PersonalDetails)
            every {
                com.soshopay.domain.util.ValidationUtils.PersonalDetails.validatePersonalDetails(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            } returns
                com.soshopay.domain.util
                    .ValidationResult(isValid = false, errors = listOf("Invalid name"))

            // When
            val result = repository.updatePersonalDetails(personalDetails)

            // Then
            assertTrue(result is Result.Error)
            assertTrue((result as Result.Error).exception is SoshoPayException.ValidationException)
            coVerify(exactly = 0) { profileApiService.updatePersonalDetails(any()) }
        }

    @Test
    fun `updatePersonalDetails succeeds and updates cache`() =
        runTest {
            // Given
            val personalDetails = createTestPersonalDetails()
            val currentUser = createTestUser()
            val personalDetailsDto = createTestPersonalDetailsDto()

            mockkObject(com.soshopay.domain.util.ValidationUtils.PersonalDetails)
            every {
                com.soshopay.domain.util.ValidationUtils.PersonalDetails.validatePersonalDetails(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            } returns
                com.soshopay.domain.util
                    .ValidationResult(isValid = true, errors = emptyList())

            mockkObject(ProfileMapper)
            every { ProfileMapper.mapToPersonalDetailsDto(personalDetails) } returns personalDetailsDto

            coEvery { profileApiService.updatePersonalDetails(personalDetailsDto) } returns Result.Success(Unit)
            coEvery { profileCache.getCurrentUser() } returns currentUser
            coEvery { profileCache.saveUser(any()) } returns true

            // When
            val result = repository.updatePersonalDetails(personalDetails)

            // Then
            assertTrue(result is Result.Success)
            coVerify { profileApiService.updatePersonalDetails(personalDetailsDto) }
            coVerify {
                profileCache.saveUser(
                    match {
                        it.personalDetails == personalDetails &&
                            it.verificationStatus == VerificationStatus.UNVERIFIED
                    },
                )
            }
        }

    @Test
    fun `updatePersonalDetails returns error when API fails`() =
        runTest {
            // Given
            val personalDetails = createTestPersonalDetails()
            val personalDetailsDto = createTestPersonalDetailsDto()
            val apiError = Result.Error(SoshoPayException.ServerException("Server error", 500))

            mockkObject(com.soshopay.domain.util.ValidationUtils.PersonalDetails)
            every {
                com.soshopay.domain.util.ValidationUtils.PersonalDetails.validatePersonalDetails(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            } returns
                com.soshopay.domain.util
                    .ValidationResult(isValid = true, errors = emptyList())

            mockkObject(ProfileMapper)
            every { ProfileMapper.mapToPersonalDetailsDto(personalDetails) } returns personalDetailsDto

            coEvery { profileApiService.updatePersonalDetails(personalDetailsDto) } returns apiError

            // When
            val result = repository.updatePersonalDetails(personalDetails)

            // Then
            assertTrue(result is Result.Error)
            assertTrue((result as Result.Error).exception is SoshoPayException.ServerException)
        }

    // ==================== updateAddress Tests ====================

    @Test
    fun `updateAddress validates before updating`() =
        runTest {
            // Given
            val address = createTestAddress()

            mockkObject(com.soshopay.domain.util.ValidationUtils.Address)
            every {
                com.soshopay.domain.util.ValidationUtils.Address.validateAddress(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            } returns
                com.soshopay.domain.util
                    .ValidationResult(isValid = false, errors = listOf("Invalid province"))

            // When
            val result = repository.updateAddress(address)

            // Then
            assertTrue(result is Result.Error)
            assertTrue((result as Result.Error).exception is SoshoPayException.ValidationException)
            coVerify(exactly = 0) { profileApiService.updateAddress(any()) }
        }

    @Test
    fun `updateAddress succeeds and updates cache`() =
        runTest {
            // Given
            val address = createTestAddress()
            val currentUser = createTestUser()
            val addressDto = createTestAddressDto()

            mockkObject(com.soshopay.domain.util.ValidationUtils.Address)
            every {
                com.soshopay.domain.util.ValidationUtils.Address.validateAddress(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            } returns
                com.soshopay.domain.util
                    .ValidationResult(isValid = true, errors = emptyList())

            mockkObject(ProfileMapper)
            every { ProfileMapper.mapToAddressDto(address) } returns addressDto

            coEvery { profileApiService.updateAddress(addressDto) } returns Result.Success(Unit)
            coEvery { profileCache.getCurrentUser() } returns currentUser
            coEvery { profileCache.saveUser(any()) } returns true

            // When
            val result = repository.updateAddress(address)

            // Then
            assertTrue(result is Result.Success)
            coVerify { profileApiService.updateAddress(addressDto) }
            coVerify {
                profileCache.saveUser(
                    match {
                        it.address == address &&
                            it.verificationStatus == VerificationStatus.UNVERIFIED
                    },
                )
            }
        }

    // ==================== uploadProfilePicture Tests ====================

    @Test
    fun `uploadProfilePicture validates file size`() =
        runTest {
            // Given
            val imageBytes = ByteArray(10 * 1024 * 1024) // 10MB - exceeds 5MB limit
            val fileName = "profile.jpg"

            mockkObject(com.soshopay.domain.util.ValidationUtils.File)
            every {
                com.soshopay.domain.util.ValidationUtils.File
                    .getFileSizeError(any())
            } returns "File too large"

            // When
            val result = repository.uploadProfilePicture(imageBytes, fileName)

            // Then
            assertTrue(result is Result.Error)
            assertTrue((result as Result.Error).exception is SoshoPayException.FileUploadException)
            coVerify(exactly = 0) { profileApiService.uploadProfilePicture(any(), any()) }
        }

    @Test
    fun `uploadProfilePicture validates file type`() =
        runTest {
            // Given
            val imageBytes = ByteArray(1024)
            val fileName = "profile.txt"

            mockkObject(com.soshopay.domain.util.ValidationUtils.File)
            every {
                com.soshopay.domain.util.ValidationUtils.File
                    .getFileSizeError(any())
            } returns null
            every {
                com.soshopay.domain.util.ValidationUtils.File
                    .getFileTypeError(fileName, false)
            } returns "Invalid file type"

            // When
            val result = repository.uploadProfilePicture(imageBytes, fileName)

            // Then
            assertTrue(result is Result.Error)
            assertTrue((result as Result.Error).exception is SoshoPayException.FileUploadException)
        }

    @Test
    fun `uploadProfilePicture succeeds and updates cache`() =
        runTest {
            // Given
            val imageBytes = ByteArray(1024)
            val fileName = "profile.jpg"
            val imageUrl = "https://example.com/profile.jpg"
            val currentUser = createTestUser()

            mockkObject(com.soshopay.domain.util.ValidationUtils.File)
            every {
                com.soshopay.domain.util.ValidationUtils.File
                    .getFileSizeError(any())
            } returns null
            every {
                com.soshopay.domain.util.ValidationUtils.File
                    .getFileTypeError(fileName, false)
            } returns null

            coEvery { profileApiService.uploadProfilePicture(imageBytes, fileName) } returns Result.Success(imageUrl)
            coEvery { profileCache.getCurrentUser() } returns currentUser
            coEvery { profileCache.saveUser(any()) } returns true

            // When
            val result = repository.uploadProfilePicture(imageBytes, fileName)

            // Then
            assertTrue(result is Result.Success)
            assertEquals(imageUrl, (result as Result.Success).data)
            coVerify { profileCache.saveUser(match { it.profilePicture?.url == imageUrl }) }
        }

    // ==================== uploadDocuments Tests ====================

    @Test
    fun `uploadDocuments validates all documents before uploading`() =
        runTest {
            // Given
            val documents =
                mapOf(
                    DocumentType.NATIONAL_ID to Pair(ByteArray(1024), "id.jpg"),
                    DocumentType.PROOF_OF_RESIDENCE to Pair(ByteArray(10 * 1024 * 1024), "residence.pdf"),
                )

            mockkObject(com.soshopay.domain.util.ValidationUtils.File)
            every {
                com.soshopay.domain.util.ValidationUtils.File
                    .getFileSizeError(match { it < 5 * 1024 * 1024 })
            } returns null
            every {
                com.soshopay.domain.util.ValidationUtils.File
                    .getFileSizeError(match { it >= 5 * 1024 * 1024 })
            } returns "File too large"
            every {
                com.soshopay.domain.util.ValidationUtils.File
                    .getFileTypeError(any(), true)
            } returns null

            // When
            val result = repository.uploadDocuments(documents)

            // Then
            assertTrue(result is Result.Error)
            assertTrue((result as Result.Error).exception is SoshoPayException.FileUploadException)
            coVerify(exactly = 0) { profileApiService.uploadDocuments(any()) }
        }

    @Test
    fun `uploadDocuments succeeds and updates cache with pending status`() =
        runTest {
            // Given
            val documents =
                mapOf(
                    DocumentType.NATIONAL_ID to Pair(ByteArray(1024), "id.jpg"),
                    DocumentType.PROOF_OF_RESIDENCE to Pair(ByteArray(1024), "residence.pdf"),
                )
            val currentUser = createTestUser()

            mockkObject(com.soshopay.domain.util.ValidationUtils.File)
            every {
                com.soshopay.domain.util.ValidationUtils.File
                    .getFileSizeError(any())
            } returns null
            every {
                com.soshopay.domain.util.ValidationUtils.File
                    .getFileTypeError(any(), true)
            } returns null

            coEvery { profileApiService.uploadDocuments(any()) } returns Result.Success(Unit)
            coEvery { profileCache.getCurrentUser() } returns currentUser
            coEvery { profileCache.saveUser(any()) } returns true

            // When
            val result = repository.uploadDocuments(documents)

            // Then
            assertTrue(result is Result.Success)
            coVerify {
                profileCache.saveUser(
                    match {
                        it.verificationStatus == VerificationStatus.PENDING &&
                            it.documents?.nationalId != null &&
                            it.documents?.proofOfResidence != null
                    },
                )
            }
        }

    // ==================== replaceDocument Tests ====================

    @Test
    fun `replaceDocument validates document before replacing`() =
        runTest {
            // Given
            val document = ByteArray(1024)
            val fileName = "id.txt"

            mockkObject(com.soshopay.domain.util.ValidationUtils.File)
            every {
                com.soshopay.domain.util.ValidationUtils.File
                    .getFileSizeError(any())
            } returns null
            every {
                com.soshopay.domain.util.ValidationUtils.File
                    .getFileTypeError(fileName, true)
            } returns "Invalid file type"

            // When
            val result = repository.replaceDocument(DocumentType.NATIONAL_ID, document, fileName)

            // Then
            assertTrue(result is Result.Error)
            assertTrue((result as Result.Error).exception is SoshoPayException.FileUploadException)
        }

    @Test
    fun `replaceDocument succeeds and updates cache`() =
        runTest {
            // Given
            val document = ByteArray(1024)
            val fileName = "id.pdf"
            val currentUser = createTestUser()

            mockkObject(com.soshopay.domain.util.ValidationUtils.File)
            every {
                com.soshopay.domain.util.ValidationUtils.File
                    .getFileSizeError(any())
            } returns null
            every {
                com.soshopay.domain.util.ValidationUtils.File
                    .getFileTypeError(fileName, true)
            } returns null

            coEvery { profileApiService.replaceDocument("national_id", document, fileName) } returns Result.Success(Unit)
            coEvery { profileCache.getCurrentUser() } returns currentUser
            coEvery { profileCache.saveUser(any()) } returns true

            // When
            val result = repository.replaceDocument(DocumentType.NATIONAL_ID, document, fileName)

            // Then
            assertTrue(result is Result.Success)
            coVerify {
                profileCache.saveUser(
                    match {
                        it.verificationStatus == VerificationStatus.PENDING
                    },
                )
            }
        }

    // ==================== getDocuments Tests ====================

    @Test
    fun `getDocuments fetches from API and updates cache`() =
        runTest {
            // Given
            val documentsDto = createTestDocumentsDto()
            val documents = createTestDocuments()
            val currentUser = createTestUser()

            mockkObject(ProfileMapper)
            every { ProfileMapper.mapToDocuments(documentsDto) } returns documents

            coEvery { profileApiService.getDocuments() } returns Result.Success(documentsDto)
            coEvery { profileCache.getCurrentUser() } returns currentUser
            coEvery { profileCache.saveUser(any()) } returns true

            // When
            val result = repository.getDocuments()

            // Then
            assertTrue(result is Result.Success)
            assertEquals(documents, (result as Result.Success).data)
            coVerify { profileCache.saveUser(match { it.documents == documents }) }
        }

    @Test
    fun `getDocuments returns cached documents when API fails`() =
        runTest {
            // Given
            val cachedDocuments = createTestDocuments()
            val currentUser = createTestUser(documents = cachedDocuments)
            val apiError = Result.Error(SoshoPayException.NetworkException("Network error"))

            coEvery { profileApiService.getDocuments() } returns apiError
            coEvery { profileCache.getCurrentUser() } returns currentUser

            // When
            val result = repository.getDocuments()

            // Then
            assertTrue(result is Result.Success)
            assertEquals(cachedDocuments, (result as Result.Success).data)
        }

    // ==================== updateNextOfKin Tests ====================

    @Test
    fun `updateNextOfKin validates address before updating`() =
        runTest {
            // Given
            val nextOfKin = createTestNextOfKin()

            mockkObject(com.soshopay.domain.util.ValidationUtils.Address)
            every {
                com.soshopay.domain.util.ValidationUtils.Address.validateAddress(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            } returns
                com.soshopay.domain.util
                    .ValidationResult(isValid = false, errors = listOf("Invalid address"))

            // When
            val result = repository.updateNextOfKin(nextOfKin)

            // Then
            assertTrue(result is Result.Error)
            assertTrue((result as Result.Error).exception is SoshoPayException.ValidationException)
            coVerify(exactly = 0) { profileApiService.updateNextOfKin(any()) }
        }

    @Test
    fun `updateNextOfKin validates phone number before updating`() =
        runTest {
            // Given
            val nextOfKin = createTestNextOfKin()

            mockkObject(com.soshopay.domain.util.ValidationUtils.Address)
            every {
                com.soshopay.domain.util.ValidationUtils.Address.validateAddress(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            } returns
                com.soshopay.domain.util
                    .ValidationResult(isValid = true, errors = emptyList())

            mockkObject(com.soshopay.domain.util.ValidationUtils.Phone)
            every {
                com.soshopay.domain.util.ValidationUtils.Phone
                    .getValidationError(any())
            } returns "Invalid phone"

            // When
            val result = repository.updateNextOfKin(nextOfKin)

            // Then
            assertTrue(result is Result.Error)
            assertTrue((result as Result.Error).exception is SoshoPayException.ValidationException)
        }

    @Test
    fun `updateNextOfKin succeeds and updates cache`() =
        runTest {
            // Given
            val nextOfKin = createTestNextOfKin()
            val nextOfKinDto = createTestNextOfKinDto()
            val currentUser = createTestUser()

            mockkObject(com.soshopay.domain.util.ValidationUtils.Address)
            every {
                com.soshopay.domain.util.ValidationUtils.Address.validateAddress(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            } returns
                com.soshopay.domain.util
                    .ValidationResult(isValid = true, errors = emptyList())

            mockkObject(com.soshopay.domain.util.ValidationUtils.Phone)
            every {
                com.soshopay.domain.util.ValidationUtils.Phone
                    .getValidationError(any())
            } returns null

            mockkObject(ProfileMapper)
            every { ProfileMapper.mapToNextOfKinDto(nextOfKin) } returns nextOfKinDto

            coEvery { profileApiService.updateNextOfKin(nextOfKinDto) } returns Result.Success(Unit)
            coEvery { profileCache.getCurrentUser() } returns currentUser
            coEvery { profileCache.saveUser(any()) } returns true

            // When
            val result = repository.updateNextOfKin(nextOfKin)

            // Then
            assertTrue(result is Result.Success)
            coVerify { profileCache.saveUser(match { it.nextOfKin == nextOfKin }) }
        }

    // ==================== getNextOfKin Tests ====================

    @Test
    fun `getNextOfKin fetches from API and updates cache`() =
        runTest {
            // Given
            val nextOfKinDto = createTestNextOfKinDto()
            val nextOfKin = createTestNextOfKin()
            val currentUser = createTestUser()

            mockkObject(ProfileMapper)
            every { ProfileMapper.mapToNextOfKin(nextOfKinDto) } returns nextOfKin

            coEvery { profileApiService.getNextOfKin() } returns Result.Success(nextOfKinDto)
            coEvery { profileCache.getCurrentUser() } returns currentUser
            coEvery { profileCache.saveUser(any()) } returns true

            // When
            val result = repository.getNextOfKin()

            // Then
            assertTrue(result is Result.Success)
            assertEquals(nextOfKin, (result as Result.Success).data)
            coVerify { profileCache.saveUser(match { it.nextOfKin == nextOfKin }) }
        }

    @Test
    fun `getNextOfKin returns cached next of kin when API fails`() =
        runTest {
            // Given
            val cachedNextOfKin = createTestNextOfKin()
            val currentUser = createTestUser(nextOfKin = cachedNextOfKin)
            val apiError = Result.Error(SoshoPayException.NetworkException("Network error"))

            coEvery { profileApiService.getNextOfKin() } returns apiError
            coEvery { profileCache.getCurrentUser() } returns currentUser

            // When
            val result = repository.getNextOfKin()

            // Then
            assertTrue(result is Result.Success)
            assertEquals(cachedNextOfKin, (result as Result.Success).data)
        }

    // ==================== Client Type Tests ====================

    @Test
    fun `getAvailableClientTypes returns client types from API`() =
        runTest {
            // Given
            val clientTypes = listOf("PRIVATE_SECTOR_EMPLOYEE", "GOVERNMENT_EMPLOYEE", "ENTREPRENEUR")
            coEvery { profileApiService.getClientTypes() } returns Result.Success(clientTypes)

            // When
            val result = repository.getAvailableClientTypes()

            // Then
            assertTrue(result is Result.Success)
            assertEquals(clientTypes, (result as Result.Success).data)
        }

    @Test
    fun `requestClientTypeChange validates client type`() =
        runTest {
            // Given
            val newType = "INVALID_TYPE"
            val availableTypes = listOf("PRIVATE_SECTOR_EMPLOYEE", "GOVERNMENT_EMPLOYEE")

            coEvery { profileApiService.getClientTypes() } returns Result.Success(availableTypes)

            // When
            val result = repository.requestClientTypeChange(newType)

            // Then
            assertTrue(result is Result.Error)
            assertTrue((result as Result.Error).exception is SoshoPayException.ValidationException)
            coVerify(exactly = 0) { profileApiService.updateClientType(any()) }
        }

    @Test
    fun `requestClientTypeChange succeeds and updates cache`() =
        runTest {
            // Given
            val newType = "GOVERNMENT_EMPLOYEE"
            val availableTypes = listOf("PRIVATE_SECTOR_EMPLOYEE", "GOVERNMENT_EMPLOYEE", "ENTREPRENEUR")
            val currentUser = createTestUser()

            coEvery { profileApiService.getClientTypes() } returns Result.Success(availableTypes)
            coEvery { profileApiService.updateClientType(newType) } returns Result.Success(Unit)
            coEvery { profileCache.getCurrentUser() } returns currentUser
            coEvery { profileCache.saveUser(any()) } returns true

            // When
            val result = repository.requestClientTypeChange(newType)

            // Then
            assertTrue(result is Result.Success)
            coVerify { profileCache.saveUser(match { it.clientType == ClientType.GOVERNMENT_EMPLOYEE }) }
        }

    // ==================== Sync and Observe Tests ====================

    @Test
    fun `syncProfile calls getUserProfile with forceRefresh true`() =
        runTest {
            // Given
            val clientDto = createTestClientDto()
            val user = createTestUser()

            mockkObject(ProfileMapper)
            every { ProfileMapper.mapToUser(clientDto) } returns user

            coEvery { profileApiService.getUserProfile() } returns Result.Success(clientDto)
            coEvery { profileCache.saveUser(any()) } returns true
            coEvery { profileCache.setLastProfileSync(any()) } returns true

            // When
            val result = repository.syncProfile()

            // Then
            assertTrue(result is Result.Success)
            coVerify { profileApiService.getUserProfile() }
        }

    @Test
    fun `observeProfile returns flow from cache`() =
        runTest {
            // Given
            val user = createTestUser()
            val flow = flowOf(user)
            coEvery { profileCache.observeUser() } returns flow

            // When
            val result = repository.observeProfile()

            // Then
            assertEquals(flow, result)
            coVerify { profileCache.observeUser() }
        }

    // ==================== Helper Functions ====================

    private fun createTestUser(
        documents: Documents? = null,
        nextOfKin: NextOfKin? = null,
    ) = User(
        id = testUserId,
        phoneNumber = testPhoneNumber,
        profilePicture =
            ProfilePicture(
                url = "https://example.com/profile.jpg",
                uploadDate = currentTime,
                lastUpdated = currentTime,
            ),
        personalDetails = createTestPersonalDetails(),
        address = createTestAddress(),
        documents = documents,
        nextOfKin = nextOfKin,
        clientType = ClientType.PRIVATE_SECTOR_EMPLOYEE,
        verificationStatus = VerificationStatus.UNVERIFIED,
        canApplyForLoan = false,
        accountStatus = AccountStatus.INCOMPLETE,
        createdAt = currentTime,
        updatedAt = currentTime,
    )

    private fun createTestPersonalDetails() =
        PersonalDetails(
            firstName = "John",
            lastName = "Doe",
            dateOfBirth = currentTime - (25 * 365 * 24 * 60 * 60 * 1000L),
            gender = "Male",
            nationality = "Zimbabwean",
            occupation = "Engineer",
            monthlyIncome = 5000.0,
            lastUpdated = currentTime,
        )

    private fun createTestAddress() =
        Address(
            streetAddress = "123 Main Street",
            suburb = "Avondale",
            city = "Harare",
            province = "Harare",
            postalCode = "00263",
            residenceType = "Owned",
            lastUpdated = currentTime,
        )

    private fun createTestNextOfKin() =
        NextOfKin(
            id = "nok-123",
            userId = testUserId,
            fullName = "Jane Doe",
            relationship = "Sister",
            phoneNumber = "263771234567",
            address = createTestAddress(),
            documents = null,
            createdAt = currentTime,
            updatedAt = currentTime,
        )

    private fun createTestDocuments() =
        Documents(
            proofOfResidence =
                Document(
                    id = "doc-1",
                    url = "https://example.com/residence.pdf",
                    fileName = "residence.pdf",
                    fileSize = "1024KB",
                    uploadDate = currentTime,
                    lastUpdated = currentTime,
                    verificationStatus = VerificationStatus.VERIFIED,
                    verificationDate = currentTime,
                    verificationNotes = null,
                    documentType = DocumentType.PROOF_OF_RESIDENCE,
                ),
            nationalId =
                Document(
                    id = "doc-2",
                    url = "https://example.com/id.pdf",
                    fileName = "id.pdf",
                    fileSize = "512KB",
                    uploadDate = currentTime,
                    lastUpdated = currentTime,
                    verificationStatus = VerificationStatus.VERIFIED,
                    verificationDate = currentTime,
                    verificationNotes = null,
                    documentType = DocumentType.NATIONAL_ID,
                ),
        )

    private fun createTestClientDto() =
        ClientDto(
            id = testUserId,
            firstName = "John",
            lastName = "Doe",
            mobile = testPhoneNumber,
            profilePicture = "https://example.com/profile.jpg",
            personalDetails = createTestPersonalDetailsDto(),
            address = createTestAddressDto(),
            documents = createTestDocumentsDto(),
            nextOfKin = createTestNextOfKinDto(),
            clientType = "PRIVATE_SECTOR_EMPLOYEE",
            verificationStatus = "UNVERIFIED",
            canApplyForLoan = false,
            accountStatus = "INCOMPLETE",
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z",
        )

    private fun createTestPersonalDetailsDto() =
        PersonalDetailsDto(
            firstName = "John",
            lastName = "Doe",
            dateOfBirth = "1999-01-01T00:00:00Z",
            gender = "Male",
            nationality = "Zimbabwean",
            occupation = "Engineer",
            monthlyIncome = 5000.0,
            lastUpdated = "2024-01-01T00:00:00Z",
        )

    private fun createTestAddressDto() =
        AddressDto(
            streetAddress = "123 Main Street",
            suburb = "Avondale",
            city = "Harare",
            province = "Harare",
            postalCode = "00263",
            residenceType = "Owned",
            lastUpdated = "2024-01-01T00:00:00Z",
        )

    private fun createTestDocumentsDto() =
        DocumentsDto(
            proofOfResidence =
                DocumentDto(
                    id = "doc-1",
                    url = "https://example.com/residence.pdf",
                    fileName = "residence.pdf",
                    fileSize = "1024KB",
                    uploadDate = "2024-01-01T00:00:00Z",
                    lastUpdated = "2024-01-01T00:00:00Z",
                    verificationStatus = "VERIFIED",
                    verificationDate = "2024-01-01T00:00:00Z",
                    verificationNotes = null,
                    documentType = "PROOF_OF_RESIDENCE",
                ),
            nationalId =
                DocumentDto(
                    id = "doc-2",
                    url = "https://example.com/id.pdf",
                    fileName = "id.pdf",
                    fileSize = "512KB",
                    uploadDate = "2024-01-01T00:00:00Z",
                    lastUpdated = "2024-01-01T00:00:00Z",
                    verificationStatus = "VERIFIED",
                    verificationDate = "2024-01-01T00:00:00Z",
                    verificationNotes = null,
                    documentType = "NATIONAL_ID",
                ),
        )

    private fun createTestNextOfKinDto() =
        NextOfKinDto(
            id = "nok-123",
            userId = testUserId,
            fullName = "Jane Doe",
            relationship = "Sister",
            phoneNumber = "263771234567",
            address = createTestAddressDto(),
            documents = null,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z",
        )
}

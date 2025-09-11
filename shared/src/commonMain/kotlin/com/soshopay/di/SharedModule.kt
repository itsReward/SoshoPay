package com.soshopay.di

import com.soshopay.data.remote.createHttpClient
import org.koin.dsl.module

val sharedModule =
    module {
        // Network
        single { createHttpClient() }

        // API Services
        single<AuthApiService> { AuthApiServiceImpl(get()) }
        single<ProfileApiService> { ProfileApiServiceImpl(get(), get()) }
        single<FileUploadService> { FileUploadServiceImpl(get()) }

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
    }

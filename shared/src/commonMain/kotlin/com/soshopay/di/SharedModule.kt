package com.soshopay.di

import com.soshopay.data.remote.api.AuthApiService
import com.soshopay.data.remote.api.AuthApiServiceImpl
import com.soshopay.data.remote.api.FileUploadService
import com.soshopay.data.remote.api.FileUploadServiceImpl
import com.soshopay.data.remote.api.ProfileApiService
import com.soshopay.data.remote.api.ProfileApiServiceImpl
import com.soshopay.data.remote.createHttpClient
import com.soshopay.data.repository.AuthRepositoryImpl
import com.soshopay.data.repository.ProfileRepositoryImpl
import com.soshopay.domain.repository.AuthRepository
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

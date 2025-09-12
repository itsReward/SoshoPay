package com.soshopay.android.di

import android.content.Context
import com.soshopay.android.data.local.AndroidCacheManager
import com.soshopay.android.data.local.AndroidLocalLoanStorage
import com.soshopay.android.data.local.AndroidLocalPaymentStorage
import com.soshopay.android.data.local.SoshoPayDatabase
import com.soshopay.data.local.CacheManager
import com.soshopay.data.local.LocalLoanStorage
import com.soshopay.data.local.LocalPaymentStorage
import org.koin.dsl.module

val androidDatabaseModule =
    module {

        // ========== ROOM DATABASE ==========
        single { SoshoPayDatabase.getInstance(get<Context>()) }

        // ========== DAOs ==========
        single { get<SoshoPayDatabase>().loanDao() }
        single { get<SoshoPayDatabase>().draftApplicationDao() }
        single { get<SoshoPayDatabase>().formDataDao() }
        single { get<SoshoPayDatabase>().paymentDao() }
        single { get<SoshoPayDatabase>().paymentDashboardDao() }
        single { get<SoshoPayDatabase>().paymentScheduleDao() }
        single { get<SoshoPayDatabase>().paymentReceiptDao() }
        single { get<SoshoPayDatabase>().syncMetadataDao() }

        // ========== LOCAL STORAGE IMPLEMENTATIONS ==========
        single<LocalLoanStorage> {
            AndroidLocalLoanStorage(
                loanDao = get(),
                draftDao = get(),
                formDataDao = get(),
                syncMetadataDao = get(),
            )
        }

        single<LocalPaymentStorage> {
            AndroidLocalPaymentStorage(
                paymentDao = get(),
                dashboardDao = get(),
                scheduleDao = get(),
                receiptDao = get(),
                syncMetadataDao = get(),
            )
        }

        single<CacheManager> {
            AndroidCacheManager(get<Context>())
        }
    }

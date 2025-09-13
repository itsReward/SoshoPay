package com.soshopay.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.soshopay.android.data.local.entities.SyncMetadataEntity

@Dao
interface SyncMetadataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(syncMetadata: SyncMetadataEntity)

    @Query("SELECT lastSyncTime FROM sync_metadata WHERE syncType = :syncType")
    suspend fun getLastSyncTime(syncType: String): Long?
}

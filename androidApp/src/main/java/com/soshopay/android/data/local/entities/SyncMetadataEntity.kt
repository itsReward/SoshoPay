package com.soshopay.android.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_metadata")
data class SyncMetadataEntity(
    @PrimaryKey val syncType: String,
    val lastSyncTime: Long,
    val updatedAt: Long,
)

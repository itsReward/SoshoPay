package com.soshopay.android.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "paygo_categories")
data class PayGoCategoriesEntity(
    @PrimaryKey val id: String,
    val categoriesJson: String,
    val createdAt: Long,
    val updatedAt: Long,
)

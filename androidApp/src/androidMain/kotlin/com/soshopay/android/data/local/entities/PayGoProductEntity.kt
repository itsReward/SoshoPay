package com.soshopay.android.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.soshopay.domain.model.PayGoProduct

@Entity(tableName = "paygo_products")
data class PayGoProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val price: Double,
    val description: String,
    val specifications: String,
    val image: String?,
    val installationFee: Double,
    val isAvailable: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
) {
    fun toDomain(): PayGoProduct =
        PayGoProduct(
            id = id,
            name = name,
            category = category,
            price = price,
            description = description,
            specifications = specifications,
            image = image,
            installationFee = installationFee,
            isAvailable = isAvailable,
        )
}

package com.soshopay.android.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// ========== TYPE CONVERTERS ==========
class DatabaseConverters {
    // Add any custom type converters here if needed
    // For example, if you need to store custom objects as JSON

    // If you need to store List<String> (for example, in some entities)
    @TypeConverter
    fun fromStringList(value: List<String>?): String? = value?.let { Json.encodeToString(it) }

    @TypeConverter
    fun toStringList(value: String?): List<String>? = value?.let { Json.decodeFromString(it) }

    // If you need to store Map<String, Any> (for JSON objects)
    @TypeConverter
    fun fromStringMap(value: Map<String, String>?): String? = value?.let { Json.encodeToString(it) }

    @TypeConverter
    fun toStringMap(value: String?): Map<String, String>? = value?.let { Json.decodeFromString(it) }
}

package com.soshopay.android.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Example migration for future database schema changes
object DatabaseMigrations {
    val MIGRATION_1_2 =
        object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Example: Add new column to loans table
                // database.execSQL("ALTER TABLE loans ADD COLUMN new_column TEXT")
            }
        }

    val MIGRATION_2_3 =
        object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Future migration logic
            }
        }

    // Array of all migrations for easy inclusion
    val ALL_MIGRATIONS =
        arrayOf(
            MIGRATION_1_2,
            MIGRATION_2_3,
        )
}

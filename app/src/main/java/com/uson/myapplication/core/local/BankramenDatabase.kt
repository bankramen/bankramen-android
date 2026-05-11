package com.uson.myapplication.core.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration

@Database(
    entities = [ParsedNotificationEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class BankramenDatabase : RoomDatabase() {
    abstract fun parsedNotificationDao(): ParsedNotificationDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                if (!db.hasColumn(tableName = "parsed_notifications", columnName = "transactionType")) {
                    db.execSQL(
                        """
                        ALTER TABLE parsed_notifications
                        ADD COLUMN transactionType TEXT NOT NULL DEFAULT 'UNKNOWN'
                        """.trimIndent(),
                    )
                }
                db.execSQL(
                    """
                    UPDATE parsed_notifications
                    SET transactionType = 'UNKNOWN'
                    WHERE transactionType IS NULL OR transactionType = ''
                    """.trimIndent(),
                )
            }
        }
    }
}

private fun SupportSQLiteDatabase.hasColumn(tableName: String, columnName: String): Boolean =
    query("PRAGMA table_info($tableName)").use { cursor ->
        val nameIndex = cursor.getColumnIndex("name")
        while (cursor.moveToNext()) {
            if (nameIndex >= 0 && cursor.getString(nameIndex) == columnName) {
                return true
            }
        }
        false
    }

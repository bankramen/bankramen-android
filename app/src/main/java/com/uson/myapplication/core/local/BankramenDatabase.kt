package com.uson.myapplication.core.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ParsedNotificationEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class BankramenDatabase : RoomDatabase() {
    abstract fun parsedNotificationDao(): ParsedNotificationDao
}

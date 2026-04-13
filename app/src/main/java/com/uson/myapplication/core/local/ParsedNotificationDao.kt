package com.uson.myapplication.core.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ParsedNotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ParsedNotificationEntity)

    @Query("SELECT * FROM parsed_notifications ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int = 20): Flow<List<ParsedNotificationEntity>>

    @Query("SELECT COUNT(*) FROM parsed_notifications")
    fun observeCount(): Flow<Int>

    @Query("SELECT * FROM parsed_notifications ORDER BY timestamp DESC LIMIT 1")
    fun observeLatest(): Flow<ParsedNotificationEntity?>
}

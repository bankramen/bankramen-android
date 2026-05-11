package com.uson.myapplication.core.local

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BankramenDatabaseMigrationTest {
    @Test
    fun migrateFromLegacySchemaWithoutTransactionType() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val databaseName = "bankramen-migration-test.db"
        context.deleteDatabase(databaseName)

        val dbPath = context.getDatabasePath(databaseName)
        dbPath.parentFile?.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(dbPath, null).use { sqliteDb ->
            sqliteDb.execSQL(
                """
                CREATE TABLE IF NOT EXISTS parsed_notifications (
                    sourceKey TEXT NOT NULL PRIMARY KEY,
                    packageName TEXT NOT NULL,
                    title TEXT NOT NULL,
                    body TEXT NOT NULL,
                    amount INTEGER,
                    merchant TEXT,
                    timestamp INTEGER NOT NULL,
                    paymentMethod TEXT NOT NULL
                )
                """.trimIndent(),
            )
            sqliteDb.execSQL(
                """
                INSERT INTO parsed_notifications
                (sourceKey, packageName, title, body, amount, merchant, timestamp, paymentMethod)
                VALUES
                ('legacy-key', 'com.kakaopay.app', '송금 완료', '홍길동님께 10,000원 송금', 10000, '홍길동', 1234, 'KAKAO_PAY')
                """.trimIndent(),
            )
            sqliteDb.execSQL("PRAGMA user_version = 1")
        }

        val roomDb = Room.databaseBuilder(
            context,
            BankramenDatabase::class.java,
            databaseName,
        ).addMigrations(
            BankramenDatabase.MIGRATION_1_2,
        ).build()

        val rows = roomDb.openHelper.writableDatabase.query(
            "SELECT transactionType FROM parsed_notifications WHERE sourceKey = 'legacy-key'",
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }

        assertEquals("UNKNOWN", rows)

        roomDb.close()
        context.deleteDatabase(databaseName)
    }
}

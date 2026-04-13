package com.uson.myapplication.core.local

import android.content.Context
import androidx.room.Room

object BankramenDatabaseProvider {
    @Volatile
    private var instance: BankramenDatabase? = null

    fun get(context: Context): BankramenDatabase = instance ?: synchronized(this) {
        instance ?: Room.databaseBuilder(
            context.applicationContext,
            BankramenDatabase::class.java,
            "bankramen.db",
        ).build().also { instance = it }
    }
}

package com.qadam.lastpuff

import android.app.Application
import androidx.room.Room
import com.qadam.lastpuff.data.datastore.PreferencesManager
import com.qadam.lastpuff.data.local.database.AppDatabase
import com.qadam.lastpuff.data.repository.UserRepository

class QadamApplication : Application() {
    lateinit var repository: UserRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "qadam_database"
        ).build()

        val preferencesManager = PreferencesManager(applicationContext)
        repository = UserRepository(
            userProfileDao = database.userProfileDao(),
            cravingEventDao = database.cravingEventDao(),
            relapseEventDao = database.relapseEventDao(),
            moneyGoalDao = database.moneyGoalDao(),
            sosContactDao = database.sosContactDao(),
            preferencesManager = preferencesManager
        )
    }
}

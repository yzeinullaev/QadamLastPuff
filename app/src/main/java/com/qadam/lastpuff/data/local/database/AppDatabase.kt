package com.qadam.lastpuff.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.qadam.lastpuff.data.local.converter.Converters
import com.qadam.lastpuff.data.local.dao.CravingEventDao
import com.qadam.lastpuff.data.local.dao.MoneyGoalDao
import com.qadam.lastpuff.data.local.dao.RelapseEventDao
import com.qadam.lastpuff.data.local.dao.SosContactDao
import com.qadam.lastpuff.data.local.dao.UserProfileDao
import com.qadam.lastpuff.data.local.entity.CravingEventEntity
import com.qadam.lastpuff.data.local.entity.MoneyGoalEntity
import com.qadam.lastpuff.data.local.entity.RelapseEventEntity
import com.qadam.lastpuff.data.local.entity.SosContactEntity
import com.qadam.lastpuff.data.local.entity.UserProfileEntity

@Database(
    entities = [
        UserProfileEntity::class,
        CravingEventEntity::class,
        RelapseEventEntity::class,
        MoneyGoalEntity::class,
        SosContactEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun cravingEventDao(): CravingEventDao
    abstract fun relapseEventDao(): RelapseEventDao
    abstract fun moneyGoalDao(): MoneyGoalDao
    abstract fun sosContactDao(): SosContactDao
}

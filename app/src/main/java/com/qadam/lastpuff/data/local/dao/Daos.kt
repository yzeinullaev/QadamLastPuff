package com.qadam.lastpuff.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.qadam.lastpuff.data.local.entity.CravingEventEntity
import com.qadam.lastpuff.data.local.entity.MoneyGoalEntity
import com.qadam.lastpuff.data.local.entity.RelapseEventEntity
import com.qadam.lastpuff.data.local.entity.SosContactEntity
import com.qadam.lastpuff.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun observeProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfile(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: UserProfileEntity)

    @Update
    suspend fun update(profile: UserProfileEntity)

    @Query("DELETE FROM user_profile")
    suspend fun deleteAll()
}

@Dao
interface CravingEventDao {
    @Query("SELECT * FROM craving_events ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<CravingEventEntity>>

    @Query("SELECT * FROM craving_events ORDER BY createdAt DESC")
    suspend fun getAll(): List<CravingEventEntity>

    @Insert
    suspend fun insert(event: CravingEventEntity): Long

    @Query("DELETE FROM craving_events")
    suspend fun deleteAll()
}

@Dao
interface RelapseEventDao {
    @Query("SELECT * FROM relapse_events ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<RelapseEventEntity>>

    @Query("SELECT COUNT(*) FROM relapse_events")
    suspend fun count(): Int

    @Insert
    suspend fun insert(event: RelapseEventEntity): Long

    @Query("DELETE FROM relapse_events")
    suspend fun deleteAll()
}

@Dao
interface MoneyGoalDao {
    @Query("SELECT * FROM money_goals ORDER BY id DESC LIMIT 1")
    fun observeActiveGoal(): Flow<MoneyGoalEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: MoneyGoalEntity): Long

    @Update
    suspend fun update(goal: MoneyGoalEntity)

    @Query("DELETE FROM money_goals")
    suspend fun deleteAll()
}

@Dao
interface SosContactDao {
    @Query("SELECT * FROM sos_contacts ORDER BY id DESC LIMIT 1")
    fun observeContact(): Flow<SosContactEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: SosContactEntity): Long

    @Update
    suspend fun update(contact: SosContactEntity)

    @Query("DELETE FROM sos_contacts")
    suspend fun deleteAll()
}

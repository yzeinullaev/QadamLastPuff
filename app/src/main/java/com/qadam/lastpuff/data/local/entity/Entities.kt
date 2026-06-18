package com.qadam.lastpuff.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Long = 1L,
    val smokeType: String,
    val cigarettesPerDay: Int,
    val packPrice: Double,
    val cigarettesInPack: Int,
    val lastSmokeDate: Long,
    val reasons: List<String>,
    val currency: String = "₸"
)

@Entity(tableName = "craving_events")
data class CravingEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val createdAt: Long,
    val intensity: Int,
    val trigger: String,
    val success: Boolean
)

@Entity(tableName = "relapse_events")
data class RelapseEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val createdAt: Long,
    val note: String? = null
)

@Entity(tableName = "money_goals")
data class MoneyGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val amount: Double
)

@Entity(tableName = "sos_contacts")
data class SosContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val phone: String,
    val message: String
)

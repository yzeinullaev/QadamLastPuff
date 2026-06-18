package com.qadam.lastpuff.data.mapper

import com.qadam.lastpuff.data.local.entity.CravingEventEntity
import com.qadam.lastpuff.data.local.entity.MoneyGoalEntity
import com.qadam.lastpuff.data.local.entity.RelapseEventEntity
import com.qadam.lastpuff.data.local.entity.SosContactEntity
import com.qadam.lastpuff.data.local.entity.UserProfileEntity
import com.qadam.lastpuff.domain.model.CravingEvent
import com.qadam.lastpuff.domain.model.MoneyGoal
import com.qadam.lastpuff.domain.model.RelapseEvent
import com.qadam.lastpuff.domain.model.SosContact
import com.qadam.lastpuff.domain.model.UserProfile

fun UserProfileEntity.toDomain() = UserProfile(
    id = id,
    smokeType = smokeType,
    cigarettesPerDay = cigarettesPerDay,
    packPrice = packPrice,
    cigarettesInPack = cigarettesInPack,
    lastSmokeDate = lastSmokeDate,
    reasons = reasons,
    currency = currency
)

fun UserProfile.toEntity() = UserProfileEntity(
    id = id,
    smokeType = smokeType,
    cigarettesPerDay = cigarettesPerDay,
    packPrice = packPrice,
    cigarettesInPack = cigarettesInPack,
    lastSmokeDate = lastSmokeDate,
    reasons = reasons,
    currency = currency
)

fun CravingEventEntity.toDomain() = CravingEvent(
    id = id,
    createdAt = createdAt,
    intensity = intensity,
    trigger = trigger,
    success = success
)

fun CravingEvent.toEntity() = CravingEventEntity(
    id = id,
    createdAt = createdAt,
    intensity = intensity,
    trigger = trigger,
    success = success
)

fun RelapseEventEntity.toDomain() = RelapseEvent(
    id = id,
    createdAt = createdAt,
    note = note
)

fun MoneyGoalEntity.toDomain() = MoneyGoal(
    id = id,
    title = title,
    amount = amount
)

fun MoneyGoal.toEntity() = MoneyGoalEntity(
    id = id,
    title = title,
    amount = amount
)

fun SosContactEntity.toDomain() = SosContact(
    id = id,
    name = name,
    phone = phone,
    message = message
)

fun SosContact.toEntity() = SosContactEntity(
    id = id,
    name = name,
    phone = phone,
    message = message
)

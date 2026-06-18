package com.qadam.lastpuff.data.local.converter

import androidx.room.TypeConverter

class Converters {
    private val delimiter = "|||"

    @TypeConverter
    fun fromStringList(value: List<String>): String = value.joinToString(delimiter)

    @TypeConverter
    fun toStringList(value: String): List<String> =
        if (value.isBlank()) emptyList() else value.split(delimiter)
}

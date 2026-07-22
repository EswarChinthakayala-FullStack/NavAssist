package com.navassist.android.data.local.db.converters

import androidx.room.TypeConverter
import com.navassist.android.core.network.JsonConfiguration
import kotlinx.serialization.encodeToString

class ListConverter {
    private val json = JsonConfiguration.instance

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let { json.decodeFromString(it) }
    }
}

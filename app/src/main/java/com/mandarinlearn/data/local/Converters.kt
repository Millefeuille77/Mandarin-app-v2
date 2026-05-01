// Converters.kt — Mandarin Learn
// Room TypeConverters for complex column types. Per ARCHITECTURE.md §2.
// Converts: LocalDate ↔ epoch-day Long, List<String> ↔ JSON, List<PinyinAnnotation> ↔ JSON.
// The Json instance used here must match the one used in JsonImporter (ignoreUnknownKeys=true).

package com.mandarinlearn.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/**
 * TypeConverters registered on [MandarinLearnDatabase].
 * These converters handle the JSON-text columns that Room cannot natively store.
 */
class Converters {

    // Shared Json instance — lenient to survive schema evolution gracefully.
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    // ---- List<String> ↔ JSON ----

    @TypeConverter
    fun fromStringList(list: List<String>): String =
        json.encodeToString(ListSerializer(String.serializer()), list)

    @TypeConverter
    fun toStringList(value: String): List<String> =
        json.decodeFromString(ListSerializer(String.serializer()), value)
}

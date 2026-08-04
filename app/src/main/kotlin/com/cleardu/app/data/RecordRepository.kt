package com.cleardu.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.cleardu.app.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray

/**
 * Persistence layer for data records.
 *
 * Uses DataStore to store records as a JSON-encoded list. Each record is
 * serialized/deserialized via [RecordData.toJson] / [RecordData.fromJson].
 */
class RecordRepository(private val context: Context) {

    companion object {
        private val KEY_RECORDS = stringPreferencesKey("records_json")
    }

    /** Observe all saved records as a [Flow]. */
    val recordsFlow: Flow<List<RecordData>> = context.dataStore.data.map { prefs ->
        val json = prefs[KEY_RECORDS] ?: "[]"
        parseRecords(json)
    }

    /** Save a new record. */
    suspend fun save(record: RecordData) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_RECORDS] ?: "[]"
            val records = parseRecords(current).toMutableList()
            records.add(0, record) // newest first
            prefs[KEY_RECORDS] = recordsToJson(records)
        }
    }

    /** Delete all records. */
    suspend fun clearAll() {
        context.dataStore.edit { prefs ->
            prefs[KEY_RECORDS] = "[]"
        }
    }

    // ---- JSON serialization helpers ----

    private fun parseRecords(json: String): List<RecordData> {
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                RecordData.fromJson(arr.getJSONObject(i))
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun recordsToJson(records: List<RecordData>): String {
        val arr = JSONArray()
        records.forEach { arr.put(it.toJson()) }
        return arr.toString()
    }
}
package com.cleardu.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.cleardu.app.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

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

// ---- JSON converters for RecordData ----

fun RecordData.toJson(): JSONObject = JSONObject().apply {
    put("ultrafiltrationMl", ultrafiltrationMl)
    put("ufGoalTarget", ufGoalTarget)
    put("ufTodayRecorded", ufTodayRecorded)
    put("systolic", systolic)
    put("diastolic", diastolic)
    put("heartRate", heartRate)
    put("weight", weight)
    put("temperature", temperature)
    put("potassium", potassium)
    put("phosphorus", phosphorus)
    put("sodium", sodium)
    put("calcium", calcium)
    put("quickNoteIndex", quickNoteIndex)
    put("noteText", noteText)
    put("timestamp", timestamp)
    put("medications", JSONArray().apply {
        selectedMedications.forEach { med ->
            put(JSONObject().apply {
                put("name", med.name)
                put("detail", med.detail)
                put("doseMultiplier", med.doseMultiplier)
            })
        }
    })
}

fun RecordData.Companion.fromJson(obj: JSONObject): RecordData = RecordData(
    ultrafiltrationMl = obj.optInt("ultrafiltrationMl", 0),
    ufGoalTarget = obj.optInt("ufGoalTarget", 2500),
    ufTodayRecorded = obj.optInt("ufTodayRecorded", 1200),
    systolic = obj.optInt("systolic", 120),
    diastolic = obj.optInt("diastolic", 80),
    heartRate = obj.optInt("heartRate", 75),
    weight = obj.optDouble("weight", 65.2),
    temperature = obj.optDouble("temperature", 36.5),
    potassium = obj.optDouble("potassium", 4.2),
    phosphorus = obj.optDouble("phosphorus", 1.5),
    sodium = obj.optDouble("sodium", 138.0),
    calcium = obj.optDouble("calcium", 2.3),
    quickNoteIndex = obj.optInt("quickNoteIndex", 0),
    noteText = obj.optString("noteText", ""),
    timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
    selectedMedications = parseMedications(obj.optJSONArray("medications"))
)

private fun parseMedications(arr: JSONArray?): List<MedicationDose> {
    if (arr == null) return emptyList()
    return (0 until arr.length()).map { i ->
        val obj = arr.getJSONObject(i)
        MedicationDose(
            name = obj.optString("name", ""),
            detail = obj.optString("detail", ""),
            doseMultiplier = obj.optDouble("doseMultiplier", 1.0)
        )
    }
}
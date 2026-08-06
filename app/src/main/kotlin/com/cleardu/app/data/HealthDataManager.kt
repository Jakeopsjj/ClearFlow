package com.cleardu.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Shared state holder that wraps [RecordRepository] and exposes
 * derived data flows for the dashboard, health data, reminder,
 * and medication screens.
 *
 * All screens observe the same [RecordRepository] instance, so a
 * record saved on the DataRecord page immediately propagates to
 * the Dashboard and Health Data pages.
 */
class HealthDataManager(private val repository: RecordRepository) {

    // ---- Raw records (newest first) ----

    val records: Flow<List<RecordData>> = repository.recordsFlow

    // ---- Latest record ----

    val latestRecord: Flow<RecordData?> = records.map { it.firstOrNull() }

    // ---- App Settings ----

    val settings: Flow<AppSettings> = repository.settingsFlow

    suspend fun saveSettings(settings: AppSettings) {
        repository.saveSettings(settings)
    }

    suspend fun updateSettings(update: (AppSettings) -> AppSettings) {
        val current = settings.first()
        repository.saveSettings(update(current))
    }

    // ---- Dashboard vitals ----

    val latestVitals: Flow<DashboardVitals> = records.map { records ->
        val latest = records.firstOrNull()
        DashboardVitals(
            systolic = latest?.systolic ?: 0,
            diastolic = latest?.diastolic ?: 0,
            heartRate = latest?.heartRate ?: 0,
            weight = latest?.weight ?: 0.0,
            temperature = latest?.temperature ?: 0.0,
            ultrafiltrationMl = latest?.ultrafiltrationMl ?: 0,
            ufGoalTarget = latest?.ufGoalTarget ?: 2500,
            medications = latest?.selectedMedications ?: emptyList()
        )
    }

    // ---- Health Data: UF trend (last 7 records, oldest first for chart) ----

    val ufTrendData: Flow<List<Float>> = records.map { records ->
        records.take(7).reversed().map { it.ultrafiltrationMl.toFloat() }
    }

    // ---- Health Data: BP/HR latest ----

    val latestBp: Flow<BpData> = records.map { records ->
        val latest = records.firstOrNull()
        BpData(
            systolic = latest?.systolic ?: 0,
            diastolic = latest?.diastolic ?: 0
        )
    }

    val latestHr: Flow<Int> = records.map { records ->
        records.firstOrNull()?.heartRate ?: 0
    }

    // ---- Health Data: Electrolytes latest ----

    val latestElectrolytes: Flow<ElectrolyteData> = records.map { records ->
        val latest = records.firstOrNull()
        ElectrolyteData(
            potassium = latest?.potassium ?: 0.0,
            phosphorus = latest?.phosphorus ?: 0.0,
            sodium = latest?.sodium ?: 0.0,
            calcium = latest?.calcium ?: 0.0
        )
    }

    // ---- Health Data: Weight latest + trend ----

    val latestWeight: Flow<Double> = records.map { records ->
        records.firstOrNull()?.weight ?: 0.0
    }

    val weightTrendData: Flow<List<Double>> = records.map { records ->
        records.take(7).reversed().map { it.weight }
    }

    // ---- Health Data: Temperature latest ----

    val latestTemperature: Flow<Double> = records.map { records ->
        records.firstOrNull()?.temperature ?: 0.0
    }

    /** Save a new record, merging with the latest existing record so that
     * fields not touched in the current tab retain their previous values. */
    suspend fun save(record: RecordData) {
        val latest = repository.recordsFlow.first().firstOrNull()
        val merged = if (latest != null) mergeRecords(latest, record) else record
        repository.save(merged)
    }

    /** Delete all records. */
    suspend fun clearAll() {
        repository.clearAll()
    }
}

/** Vitals snapshot for the Dashboard screen. */
data class DashboardVitals(
    val systolic: Int = 0,
    val diastolic: Int = 0,
    val heartRate: Int = 0,
    val weight: Double = 0.0,
    val temperature: Double = 0.0,
    val ultrafiltrationMl: Int = 0,
    val ufGoalTarget: Int = 2500,
    val medications: List<MedicationDose> = emptyList()
)

/** Blood pressure snapshot for the Health Data screen. */
data class BpData(
    val systolic: Int = 0,
    val diastolic: Int = 0
)

/** Electrolyte snapshot for the Health Data screen. */
data class ElectrolyteData(
    val potassium: Double = 0.0,
    val phosphorus: Double = 0.0,
    val sodium: Double = 0.0,
    val calcium: Double = 0.0
)

// ---- Merge helpers ----

/** Default values for [RecordData] — used to detect which fields were explicitly set by the user. */
private val defaultRecord = RecordData()

/**
 * Merge [newRecord] into [oldRecord]: for each field, if the new value differs from
 * the RecordData default, use the new value; otherwise keep the old value.
 *
 * This ensures that when the user saves only one tab (e.g. 超滤量), the fields from
 * other tabs (e.g. 血压心率) retain their previously saved values instead of resetting
 * to defaults.
 */
private fun mergeRecords(old: RecordData, new: RecordData): RecordData = old.copy(
    ultrafiltrationMl = if (new.ultrafiltrationMl != defaultRecord.ultrafiltrationMl) new.ultrafiltrationMl else old.ultrafiltrationMl,
    ufGoalTarget = if (new.ufGoalTarget != defaultRecord.ufGoalTarget) new.ufGoalTarget else old.ufGoalTarget,
    ufTodayRecorded = if (new.ufTodayRecorded != defaultRecord.ufTodayRecorded) new.ufTodayRecorded else old.ufTodayRecorded,
    systolic = if (new.systolic != defaultRecord.systolic) new.systolic else old.systolic,
    diastolic = if (new.diastolic != defaultRecord.diastolic) new.diastolic else old.diastolic,
    heartRate = if (new.heartRate != defaultRecord.heartRate) new.heartRate else old.heartRate,
    weight = if (new.weight != defaultRecord.weight) new.weight else old.weight,
    temperature = if (new.temperature != defaultRecord.temperature) new.temperature else old.temperature,
    potassium = if (new.potassium != defaultRecord.potassium) new.potassium else old.potassium,
    phosphorus = if (new.phosphorus != defaultRecord.phosphorus) new.phosphorus else old.phosphorus,
    sodium = if (new.sodium != defaultRecord.sodium) new.sodium else old.sodium,
    calcium = if (new.calcium != defaultRecord.calcium) new.calcium else old.calcium,
    selectedMedications = if (new.selectedMedications != defaultRecord.selectedMedications) new.selectedMedications else old.selectedMedications,
    quickNoteIndex = if (new.quickNoteIndex != defaultRecord.quickNoteIndex) new.quickNoteIndex else old.quickNoteIndex,
    noteText = if (new.noteText != defaultRecord.noteText) new.noteText else old.noteText,
    timestamp = new.timestamp
)
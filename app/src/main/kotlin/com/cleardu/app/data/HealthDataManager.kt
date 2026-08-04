package com.cleardu.app.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Shared state holder that wraps [RecordRepository] and exposes
 * derived data flows for the dashboard and health data screens.
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

    /** Save a new record (delegates to repository). */
    suspend fun save(record: RecordData) {
        repository.save(record)
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
package com.cleardu.app.data

import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

/**
 * Unified data model for the data-record screen.
 *
 * Holds all inputs across the five recording tabs, plus metadata
 * for the shared note and quick-note chip.
 */
data class RecordData(
    // 超滤量
    val ultrafiltrationMl: Int = 0,
    val ufGoalTarget: Int = 2500,
    val ufTodayRecorded: Int = 1200,

    // 血压心率
    val systolic: Int = 120,
    val diastolic: Int = 80,
    val heartRate: Int = 75,

    // 体重体温
    val weight: Double = 65.2,
    val temperature: Double = 36.5,

    // 元素检测
    val potassium: Double = 4.2,
    val phosphorus: Double = 1.5,
    val sodium: Double = 138.0,
    val calcium: Double = 2.3,

    // 用药
    val selectedMedications: List<MedicationDose> = listOf(
        MedicationDose(name = "降压药", detail = "缬沙坦 80mg", doseMultiplier = 1.0),
        MedicationDose(name = "磷结合剂", detail = "碳酸钙 500mg", doseMultiplier = 1.0),
        MedicationDose(name = "促红细胞生成素", detail = "3000 IU", doseMultiplier = 1.0)
    ),

    // 共享备注
    val quickNoteIndex: Int = 0,       // 0=透析前, 1=透析后, 2=晨起, 3=睡前, 4=运动后
    val noteText: String = "",

    // 元数据
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromJson(obj: JSONObject): RecordData = RecordData(
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
    }
}

data class MedicationDose(
    val name: String,
    val detail: String,
    val doseMultiplier: Double
)

/**
 * Quick-note chip labels (index → label).
 */
val QuickNoteLabels = listOf("透析前", "透析后", "晨起", "睡前", "运动后")

/**
 * Generate a human-readable date string for the record.
 */
fun RecordData.formattedDate(): String {
    val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH) + 1
    val day = cal.get(Calendar.DAY_OF_MONTH)
    val weekDays = arrayOf("星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")
    val weekDay = weekDays[cal.get(Calendar.DAY_OF_WEEK) - 1]
    return "${year}年${month}月${day}日 $weekDay"
}

/**
 * Generate a summary string for display after saving.
 */
fun RecordData.summary(): String {
    val parts = mutableListOf<String>()
    if (ultrafiltrationMl > 0) parts.add("超滤 ${ultrafiltrationMl}ml")
    if (systolic > 0 || diastolic > 0) parts.add("血压 ${systolic}/${diastolic}")
    if (heartRate > 0) parts.add("心率 ${heartRate}")
    if (weight > 0) parts.add("体重 ${weight}kg")
    if (temperature > 0) parts.add("体温 ${temperature}°C")
    if (selectedMedications.isNotEmpty()) parts.add("用药 ${selectedMedications.size}种")
    return parts.joinToString(" · ")
}

// ---- JSON converters ----

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
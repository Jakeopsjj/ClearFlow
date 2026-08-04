package com.cleardu.app.data

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
)

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
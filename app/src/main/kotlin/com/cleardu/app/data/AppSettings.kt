package com.cleardu.app.data

import org.json.JSONArray
import org.json.JSONObject

/**
 * App-level settings persisted in DataStore.
 * Includes hospital info, emergency contact, custom medications,
 * medication reminders, and refill requests.
 */
data class AppSettings(
    // 提醒中心 - 医院
    val hospitalName: String = "",
    val hospitalAddress: String = "",
    val hospitalLat: Double = 0.0,
    val hospitalLng: Double = 0.0,
    val hospitalIsCustom: Boolean = false,

    // 提醒中心 - 下次透析时间
    val nextDialysisTime: Long = 0L, // epoch millis

    // 提醒中心 - 紧急联系人
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val emergencyContactIsCustom: Boolean = false,

    // 用药管理 - 自定义药物列表
    val customMedications: List<CustomMedication> = emptyList(),

    // 用药管理 - 提醒设置
    val medicationReminderEnabled: Boolean = true,
    val medicationReminderAdvanceMinutes: Int = 15,

    // 用药管理 - 续药申请列表
    val refillRequests: List<RefillRequest> = emptyList()
) {
    companion object {
        fun fromJson(obj: JSONObject): AppSettings = AppSettings(
            hospitalName = obj.optString("hospitalName", ""),
            hospitalAddress = obj.optString("hospitalAddress", ""),
            hospitalLat = obj.optDouble("hospitalLat", 0.0),
            hospitalLng = obj.optDouble("hospitalLng", 0.0),
            hospitalIsCustom = obj.optBoolean("hospitalIsCustom", false),
            nextDialysisTime = obj.optLong("nextDialysisTime", 0L),
            emergencyContactName = obj.optString("emergencyContactName", ""),
            emergencyContactPhone = obj.optString("emergencyContactPhone", ""),
            emergencyContactIsCustom = obj.optBoolean("emergencyContactIsCustom", false),
            customMedications = parseCustomMeds(obj.optJSONArray("customMedications")),
            medicationReminderEnabled = obj.optBoolean("medicationReminderEnabled", true),
            medicationReminderAdvanceMinutes = obj.optInt("medicationReminderAdvanceMinutes", 15),
            refillRequests = parseRefillRequests(obj.optJSONArray("refillRequests"))
        )
    }
}

fun AppSettings.toJson(): JSONObject = JSONObject().apply {
    put("hospitalName", hospitalName)
    put("hospitalAddress", hospitalAddress)
    put("hospitalLat", hospitalLat)
    put("hospitalLng", hospitalLng)
    put("hospitalIsCustom", hospitalIsCustom)
    put("nextDialysisTime", nextDialysisTime)
    put("emergencyContactName", emergencyContactName)
    put("emergencyContactPhone", emergencyContactPhone)
    put("emergencyContactIsCustom", emergencyContactIsCustom)
    put("customMedications", JSONArray().apply {
        customMedications.forEach { put(it.toJson()) }
    })
    put("medicationReminderEnabled", medicationReminderEnabled)
    put("medicationReminderAdvanceMinutes", medicationReminderAdvanceMinutes)
    put("refillRequests", JSONArray().apply {
        refillRequests.forEach { put(it.toJson()) }
    })
}

/** User-defined custom medication. */
data class CustomMedication(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val detail: String,
    val dosage: String = "",
    val frequency: String = "每日", // 每日/隔日/每周
    val times: List<String> = listOf("08:00"), // e.g. ["08:00", "20:00"]
    val notes: String = "",
    val isActive: Boolean = true
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("detail", detail)
        put("dosage", dosage)
        put("frequency", frequency)
        put("times", JSONArray().apply { times.forEach { put(it) } })
        put("notes", notes)
        put("isActive", isActive)
    }
}

/** Refill request for a medication. */
data class RefillRequest(
    val id: String = java.util.UUID.randomUUID().toString(),
    val medicationName: String,
    val detail: String = "",
    val status: String = "pending", // pending / submitted / fulfilled
    val requestDate: Long = System.currentTimeMillis(),
    val notes: String = ""
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("medicationName", medicationName)
        put("detail", detail)
        put("status", status)
        put("requestDate", requestDate)
        put("notes", notes)
    }
}

private fun parseCustomMeds(arr: JSONArray?): List<CustomMedication> {
    if (arr == null) return emptyList()
    return (0 until arr.length()).map { i ->
        val obj = arr.getJSONObject(i)
        val timesArr = obj.optJSONArray("times")
        val times = if (timesArr != null) {
            (0 until timesArr.length()).map { timesArr.getString(it) }
        } else listOf("08:00")
        CustomMedication(
            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
            name = obj.optString("name", ""),
            detail = obj.optString("detail", ""),
            dosage = obj.optString("dosage", ""),
            frequency = obj.optString("frequency", "每日"),
            times = times,
            notes = obj.optString("notes", ""),
            isActive = obj.optBoolean("isActive", true)
        )
    }
}

private fun parseRefillRequests(arr: JSONArray?): List<RefillRequest> {
    if (arr == null) return emptyList()
    return (0 until arr.length()).map { i ->
        val obj = arr.getJSONObject(i)
        RefillRequest(
            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
            medicationName = obj.optString("medicationName", ""),
            detail = obj.optString("detail", ""),
            status = obj.optString("status", "pending"),
            requestDate = obj.optLong("requestDate", System.currentTimeMillis()),
            notes = obj.optString("notes", "")
        )
    }
}
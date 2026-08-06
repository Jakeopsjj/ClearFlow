package com.cleardu.app.data

import org.json.JSONArray
import org.json.JSONObject

/**
 * App-level settings persisted in DataStore — single source of truth for all
 * configuration across all screens.
 *
 * Every setting toggle, preference, and reminder flag lives here. All screens
 * read from the same [HealthDataManager.settings] Flow, guaranteeing that
 * any change in one screen immediately propagates to all others.
 */
data class AppSettings(
    // ===== 设置 - 通用 =====
    val darkMode: Boolean = true,
    val unitSystem: String = "kg/mmHg",       // kg/mmHg | lb/mmHg
    val language: String = "zh_CN",            // zh_CN | en_US

    // ===== 个人资料 =====
    val profileName: String = "张先生",
    val profileGender: String = "男",
    val profileBirthDate: String = "1968-05-12",
    val profileHeight: String = "172 cm",
    val profileBloodType: String = "A型 Rh阳性",
    val profileDialysisType: String = "血液透析",
    val profileFirstDialysisDate: String = "2023-03-15",
    val profileVascularAccess: String = "左前臂动静脉内瘘",
    val profilePatientId: String = "DC202403001",

    // ===== 设置 - 健康管理 =====
    val waterRestrictionReminder: Boolean = true,
    val dialysisPlan: String = "每周一三五 · 08:00",
    val dryWeightTarget: String = "65.0 kg",
    val emergencyContactCount: Int = 2,

    // ===== 设置 - 数据备份 =====
    val autoBackup: Boolean = true,
    val backupWifiOnly: Boolean = true,
    val backupFrequency: String = "每天",
    val backupContent: String = "全部数据",
    val backupEncryption: Boolean = true,
    val lastBackupTime: Long = 0L,  // epoch millis
    val backupHistory: List<BackupHistoryItem> = emptyList(),

    // ===== 通知 - 强提醒 =====
    val strongReminder: Boolean = true,

    // ===== 通知 - 透析相关 =====
    val dialysisDayReminder: Boolean = true,
    val dialysisDayReminderSub: String = "透析前1小时",
    val weightReminder: Boolean = true,
    val weightReminderSub: String = "透析前后",
    val waterControlReminder: Boolean = false,

    // ===== 通知 - 用药提醒 =====
    val medicationNotificationReminder: Boolean = true,
    val medicationNotificationReminderSub: String = "按您设置的用药计划",
    val epoInjectionReminder: Boolean = true,
    val epoInjectionReminderSub: String = "每周二、五 20:00",
    val ironSupplementReminder: Boolean = true,
    val missedDoseReminder: Boolean = true,
    val missedDoseReminderSub: String = "15分钟后二次提醒",

    // ===== 通知 - 健康监测 =====
    val bpMeasurementReminder: Boolean = true,
    val bpMeasurementReminderSub: String = "每日早晚",
    val abnormalDataWarning: Boolean = true,
    val abnormalDataWarningSub: String = "血压/钾/磷超标时",
    val weightGainWarning: Boolean = true,
    val weightGainWarningSub: String = "日增重>1.5kg时",
    val checkupReminder: Boolean = true,
    val checkupReminderSub: String = "每月一次",

    // ===== 通知 - 提醒方式 =====
    val soundReminder: String = "默认铃声",
    val vibrationReminder: Boolean = true,
    val lockScreenPopup: Boolean = true,
    val reminderTimePeriod: String = "全天",

    // ===== 提醒中心 - 医院（已有字段） =====
    val hospitalName: String = "",
    val hospitalAddress: String = "",
    val hospitalLat: Double = 0.0,
    val hospitalLng: Double = 0.0,
    val hospitalIsCustom: Boolean = false,

    // ===== 提醒中心 - 下次透析时间 =====
    val nextDialysisTime: Long = 0L, // epoch millis

    // ===== 提醒中心 - 紧急联系人 =====
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val emergencyContactRelation: String = "配偶",
    val emergencyContactIsCustom: Boolean = false,

    // ===== 用药管理 - 自定义药物列表 =====
    val customMedications: List<CustomMedication> = emptyList(),

    // ===== 用药管理 - 提醒设置 =====
    val medicationReminderEnabled: Boolean = true,
    val medicationReminderAdvanceMinutes: Int = 15,

    // ===== 用药管理 - 续药申请列表 =====
    val refillRequests: List<RefillRequest> = emptyList(),

    // ===== 天气背景 =====
    val weatherBackgroundEnabled: Boolean = false,  // 天气背景总开关，关闭时不调用任何网络

    // ===== 版本更新 =====
    val lastSeenVersion: String = ""  // 上次已查看过更新日志的版本号，为空表示首次使用
) {
    companion object {
        fun fromJson(obj: JSONObject): AppSettings = AppSettings(
            // 设置 - 通用
            darkMode = obj.optBoolean("darkMode", true),
            unitSystem = obj.optString("unitSystem", "kg/mmHg"),
            language = obj.optString("language", "zh_CN"),
            // 个人资料
            profileName = obj.optString("profileName", "张先生"),
            profileGender = obj.optString("profileGender", "男"),
            profileBirthDate = obj.optString("profileBirthDate", "1968-05-12"),
            profileHeight = obj.optString("profileHeight", "172 cm"),
            profileBloodType = obj.optString("profileBloodType", "A型 Rh阳性"),
            profileDialysisType = obj.optString("profileDialysisType", "血液透析"),
            profileFirstDialysisDate = obj.optString("profileFirstDialysisDate", "2023-03-15"),
            profileVascularAccess = obj.optString("profileVascularAccess", "左前臂动静脉内瘘"),
            profilePatientId = obj.optString("profilePatientId", "DC202403001"),
            // 设置 - 健康管理
            waterRestrictionReminder = obj.optBoolean("waterRestrictionReminder", true),
            dialysisPlan = obj.optString("dialysisPlan", "每周一三五 · 08:00"),
            dryWeightTarget = obj.optString("dryWeightTarget", "65.0 kg"),
            emergencyContactCount = obj.optInt("emergencyContactCount", 2),
            // 设置 - 数据备份
            autoBackup = obj.optBoolean("autoBackup", true),
            backupWifiOnly = obj.optBoolean("backupWifiOnly", true),
            backupFrequency = obj.optString("backupFrequency", "每天"),
            backupContent = obj.optString("backupContent", "全部数据"),
            backupEncryption = obj.optBoolean("backupEncryption", true),
            lastBackupTime = obj.optLong("lastBackupTime", 0L),
            backupHistory = parseBackupHistory(obj.optJSONArray("backupHistory")),
            // 通知 - 强提醒
            strongReminder = obj.optBoolean("strongReminder", true),
            // 通知 - 透析相关
            dialysisDayReminder = obj.optBoolean("dialysisDayReminder", true),
            dialysisDayReminderSub = obj.optString("dialysisDayReminderSub", "透析前1小时"),
            weightReminder = obj.optBoolean("weightReminder", true),
            weightReminderSub = obj.optString("weightReminderSub", "透析前后"),
            waterControlReminder = obj.optBoolean("waterControlReminder", false),
            // 通知 - 用药提醒
            medicationNotificationReminder = obj.optBoolean("medicationNotificationReminder", true),
            medicationNotificationReminderSub = obj.optString("medicationNotificationReminderSub", "按您设置的用药计划"),
            epoInjectionReminder = obj.optBoolean("epoInjectionReminder", true),
            epoInjectionReminderSub = obj.optString("epoInjectionReminderSub", "每周二、五 20:00"),
            ironSupplementReminder = obj.optBoolean("ironSupplementReminder", true),
            missedDoseReminder = obj.optBoolean("missedDoseReminder", true),
            missedDoseReminderSub = obj.optString("missedDoseReminderSub", "15分钟后二次提醒"),
            // 通知 - 健康监测
            bpMeasurementReminder = obj.optBoolean("bpMeasurementReminder", true),
            bpMeasurementReminderSub = obj.optString("bpMeasurementReminderSub", "每日早晚"),
            abnormalDataWarning = obj.optBoolean("abnormalDataWarning", true),
            abnormalDataWarningSub = obj.optString("abnormalDataWarningSub", "血压/钾/磷超标时"),
            weightGainWarning = obj.optBoolean("weightGainWarning", true),
            weightGainWarningSub = obj.optString("weightGainWarningSub", "日增重>1.5kg时"),
            checkupReminder = obj.optBoolean("checkupReminder", true),
            checkupReminderSub = obj.optString("checkupReminderSub", "每月一次"),
            // 通知 - 提醒方式
            soundReminder = obj.optString("soundReminder", "默认铃声"),
            vibrationReminder = obj.optBoolean("vibrationReminder", true),
            lockScreenPopup = obj.optBoolean("lockScreenPopup", true),
            reminderTimePeriod = obj.optString("reminderTimePeriod", "全天"),
            // 提醒中心 - 医院
            hospitalName = obj.optString("hospitalName", ""),
            hospitalAddress = obj.optString("hospitalAddress", ""),
            hospitalLat = obj.optDouble("hospitalLat", 0.0),
            hospitalLng = obj.optDouble("hospitalLng", 0.0),
            hospitalIsCustom = obj.optBoolean("hospitalIsCustom", false),
            // 提醒中心 - 下次透析
            nextDialysisTime = obj.optLong("nextDialysisTime", 0L),
            // 提醒中心 - 紧急联系人
            emergencyContactName = obj.optString("emergencyContactName", ""),
            emergencyContactPhone = obj.optString("emergencyContactPhone", ""),
            emergencyContactRelation = obj.optString("emergencyContactRelation", "配偶"),
            emergencyContactIsCustom = obj.optBoolean("emergencyContactIsCustom", false),
            // 用药管理
            customMedications = parseCustomMeds(obj.optJSONArray("customMedications")),
            medicationReminderEnabled = obj.optBoolean("medicationReminderEnabled", true),
            medicationReminderAdvanceMinutes = obj.optInt("medicationReminderAdvanceMinutes", 15),
            refillRequests = parseRefillRequests(obj.optJSONArray("refillRequests")),
            // 天气背景
            weatherBackgroundEnabled = obj.optBoolean("weatherBackgroundEnabled", false),
            // 版本更新
            lastSeenVersion = obj.optString("lastSeenVersion", "")
        )
    }
}

fun AppSettings.toJson(): JSONObject = JSONObject().apply {
    // 设置 - 通用
    put("darkMode", darkMode)
    put("unitSystem", unitSystem)
    put("language", language)
    // 个人资料
    put("profileName", profileName)
    put("profileGender", profileGender)
    put("profileBirthDate", profileBirthDate)
    put("profileHeight", profileHeight)
    put("profileBloodType", profileBloodType)
    put("profileDialysisType", profileDialysisType)
    put("profileFirstDialysisDate", profileFirstDialysisDate)
    put("profileVascularAccess", profileVascularAccess)
    put("profilePatientId", profilePatientId)
    // 设置 - 健康管理
    put("waterRestrictionReminder", waterRestrictionReminder)
    put("dialysisPlan", dialysisPlan)
    put("dryWeightTarget", dryWeightTarget)
    put("emergencyContactCount", emergencyContactCount)
    // 设置 - 数据备份
    put("autoBackup", autoBackup)
    put("backupWifiOnly", backupWifiOnly)
    put("backupFrequency", backupFrequency)
    put("backupContent", backupContent)
    put("backupEncryption", backupEncryption)
    put("lastBackupTime", lastBackupTime)
    put("backupHistory", JSONArray().apply {
        backupHistory.forEach { put(it.toJson()) }
    })
    // 通知 - 强提醒
    put("strongReminder", strongReminder)
    // 通知 - 透析相关
    put("dialysisDayReminder", dialysisDayReminder)
    put("dialysisDayReminderSub", dialysisDayReminderSub)
    put("weightReminder", weightReminder)
    put("weightReminderSub", weightReminderSub)
    put("waterControlReminder", waterControlReminder)
    // 通知 - 用药提醒
    put("medicationNotificationReminder", medicationNotificationReminder)
    put("medicationNotificationReminderSub", medicationNotificationReminderSub)
    put("epoInjectionReminder", epoInjectionReminder)
    put("epoInjectionReminderSub", epoInjectionReminderSub)
    put("ironSupplementReminder", ironSupplementReminder)
    put("missedDoseReminder", missedDoseReminder)
    put("missedDoseReminderSub", missedDoseReminderSub)
    // 通知 - 健康监测
    put("bpMeasurementReminder", bpMeasurementReminder)
    put("bpMeasurementReminderSub", bpMeasurementReminderSub)
    put("abnormalDataWarning", abnormalDataWarning)
    put("abnormalDataWarningSub", abnormalDataWarningSub)
    put("weightGainWarning", weightGainWarning)
    put("weightGainWarningSub", weightGainWarningSub)
    put("checkupReminder", checkupReminder)
    put("checkupReminderSub", checkupReminderSub)
    // 通知 - 提醒方式
    put("soundReminder", soundReminder)
    put("vibrationReminder", vibrationReminder)
    put("lockScreenPopup", lockScreenPopup)
    put("reminderTimePeriod", reminderTimePeriod)
    // 提醒中心 - 医院
    put("hospitalName", hospitalName)
    put("hospitalAddress", hospitalAddress)
    put("hospitalLat", hospitalLat)
    put("hospitalLng", hospitalLng)
    put("hospitalIsCustom", hospitalIsCustom)
    // 提醒中心 - 下次透析
    put("nextDialysisTime", nextDialysisTime)
    // 提醒中心 - 紧急联系人
    put("emergencyContactName", emergencyContactName)
    put("emergencyContactPhone", emergencyContactPhone)
    put("emergencyContactRelation", emergencyContactRelation)
    put("emergencyContactIsCustom", emergencyContactIsCustom)
    // 用药管理
    put("customMedications", JSONArray().apply {
        customMedications.forEach { put(it.toJson()) }
    })
    put("medicationReminderEnabled", medicationReminderEnabled)
    put("medicationReminderAdvanceMinutes", medicationReminderAdvanceMinutes)
    put("refillRequests", JSONArray().apply {
        refillRequests.forEach { put(it.toJson()) }
    })
    // 天气背景
    put("weatherBackgroundEnabled", weatherBackgroundEnabled)
    // 版本更新
    put("lastSeenVersion", lastSeenVersion)
}

/** Backup history record. */
data class BackupHistoryItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val fileSize: String = "2.4 MB",
    val success: Boolean = true
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("timestamp", timestamp)
        put("fileSize", fileSize)
        put("success", success)
    }
}

private fun parseBackupHistory(arr: JSONArray?): List<BackupHistoryItem> {
    if (arr == null) return emptyList()
    return (0 until arr.length()).map { i ->
        val obj = arr.getJSONObject(i)
        BackupHistoryItem(
            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
            fileSize = obj.optString("fileSize", "2.4 MB"),
            success = obj.optBoolean("success", true)
        )
    }
}

/** User-defined custom medication. */
data class CustomMedication(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val detail: String,
    val dosage: String = "",
    val frequency: String = "每日",
    val times: List<String> = listOf("08:00"),
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
    val status: String = "pending",
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
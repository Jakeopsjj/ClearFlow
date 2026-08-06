package com.cleardu.app.data

import androidx.compose.ui.graphics.Color

/**
 * Data models for the dashboard home screen.
 */
data class DashboardData(
    val greeting: String,
    val greetingSub: String,
    val fluidIntake: Int,
    val fluidTarget: Int,
    val fluidStatus: String,
    val vitals: List<VitalItem>,
    val medication: MedicationReminder,
    val quickActions: List<QuickAction>
)

data class VitalItem(
    val id: String,
    val label: String,
    val value: String,
    val unit: String,
    val status: VitalStatus,
    val subValue: String? = null,
    val accentColor: Color
)

enum class VitalStatus { Normal, Warning, Danger }

data class MedicationReminder(
    val title: String,
    val detail: String,
    val actionLabel: String
)

data class QuickAction(
    val id: String,
    val label: String,
    val accentColor: Color
)
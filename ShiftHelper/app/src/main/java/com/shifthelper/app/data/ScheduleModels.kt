package com.shifthelper.app.data

import com.google.gson.annotations.SerializedName

data class ScheduleData(
    val year: Int,
    @SerializedName("shift_names")
    val shiftNames: Map<String, String>,
    val cycle: Map<String, List<String>>,
    val schedules: Map<String, List<DayShift>>
)

data class DayShift(
    val date: String,
    val shift: String
)

data class ShiftConfig(
    val shiftCode: String,
    val shiftName: String,
    val alarmHour: Int,
    val alarmMinute: Int,
    val colorLight: Long,
    val colorDark: Long
)

val SHIFT_CONFIGS = mapOf(
    "白" to ShiftConfig(
        shiftCode = "白",
        shiftName = "白班",
        alarmHour = 7,
        alarmMinute = 10,
        colorLight = 0xFFFF9500,
        colorDark = 0xFFFF9500
    ),
    "中" to ShiftConfig(
        shiftCode = "中",
        shiftName = "中班",
        alarmHour = 14,
        alarmMinute = 50,
        colorLight = 0xFF34C759,
        colorDark = 0xFF30D158
    ),
    "夜" to ShiftConfig(
        shiftCode = "夜",
        shiftName = "夜班",
        alarmHour = 23,
        alarmMinute = 0,
        colorLight = 0xFF5856D6,
        colorDark = 0xFF5E5CE6
    ),
    "学" to ShiftConfig(
        shiftCode = "学",
        shiftName = "学习班",
        alarmHour = 8,
        alarmMinute = 55,
        colorLight = 0xFF007AFF,
        colorDark = 0xFF0A84FF
    ),
    "休" to ShiftConfig(
        shiftCode = "休",
        shiftName = "休息",
        alarmHour = -1,
        alarmMinute = -1,
        colorLight = 0xFF8E8E93,
        colorDark = 0xFF8E8E93
    )
)

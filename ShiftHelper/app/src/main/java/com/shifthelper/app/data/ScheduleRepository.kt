package com.shifthelper.app.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ScheduleRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("shift_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    companion object {
        private const val KEY_SELECTED_TEAM = "selected_team"
        private const val KEY_SCHEDULE_DATA = "schedule_data"
        private const val KEY_ALARMS_ENABLED = "alarms_enabled"
        private const val KEY_CALENDAR_SYNCED = "calendar_synced"
        private const val DEFAULT_TEAM = "三值"
    }

    fun getSelectedTeam(): String {
        return prefs.getString(KEY_SELECTED_TEAM, DEFAULT_TEAM) ?: DEFAULT_TEAM
    }

    fun setSelectedTeam(team: String) {
        prefs.edit().putString(KEY_SELECTED_TEAM, team).apply()
    }

    fun isAlarmsEnabled(): Boolean {
        return prefs.getBoolean(KEY_ALARMS_ENABLED, false)
    }

    fun setAlarmsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ALARMS_ENABLED, enabled).apply()
    }

    fun isCalendarSynced(): Boolean {
        return prefs.getBoolean(KEY_CALENDAR_SYNCED, false)
    }

    fun setCalendarSynced(synced: Boolean) {
        prefs.edit().putBoolean(KEY_CALENDAR_SYNCED, synced).apply()
    }

    fun saveScheduleData(data: ScheduleData) {
        val json = gson.toJson(data)
        prefs.edit().putString(KEY_SCHEDULE_DATA, json).apply()
    }

    fun getScheduleData(): ScheduleData? {
        val json = prefs.getString(KEY_SCHEDULE_DATA, null) ?: return null
        return try {
            gson.fromJson(json, ScheduleData::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun getTodayShift(): DayShift? {
        return getShiftForDate(LocalDate.now())
    }

    fun getShiftForDate(date: LocalDate): DayShift? {
        val data = getScheduleData() ?: return null
        val team = getSelectedTeam()
        val schedule = data.schedules[team] ?: return null
        val dateStr = date.format(dateFormatter)
        return schedule.find { it.date == dateStr }
    }

    fun getMonthShifts(year: Int, month: Int): List<DayShift> {
        val data = getScheduleData() ?: return emptyList()
        val team = getSelectedTeam()
        val schedule = data.schedules[team] ?: return emptyList()
        val prefix = String.format("%d-%02d", year, month)
        return schedule.filter { it.date.startsWith(prefix) }
    }

    fun getUpcomingShifts(days: Int = 7): List<DayShift> {
        val today = LocalDate.now()
        return (0 until days).mapNotNull { offset ->
            getShiftForDate(today.plusDays(offset.toLong()))
        }
    }

    fun getAllShiftsForTeam(team: String): List<DayShift> {
        val data = getScheduleData() ?: return emptyList()
        return data.schedules[team] ?: emptyList()
    }
}

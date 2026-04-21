package com.shifthelper.app.calendar

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import com.shifthelper.app.data.DayShift
import com.shifthelper.app.data.SHIFT_CONFIGS
import java.time.LocalDate
import java.time.ZoneId
import java.util.TimeZone

class CalendarSyncManager(private val context: Context) {

    companion object {
        private const val CALENDAR_NAME = "倒班日历"
        private const val ACCOUNT_NAME = "shifthelper"
        private const val ACCOUNT_TYPE = "com.shifthelper.local"
    }

    private fun getOrCreateCalendar(): Long {
        val cursor = context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            arrayOf(CalendarContract.Calendars._ID),
            "${CalendarContract.Calendars.NAME} = ?",
            arrayOf(CALENDAR_NAME),
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                return it.getLong(0)
            }
        }

        // 创建日历
        val values = ContentValues().apply {
            put(CalendarContract.Calendars.NAME, CALENDAR_NAME)
            put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CALENDAR_NAME)
            put(CalendarContract.Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
            put(CalendarContract.Calendars.ACCOUNT_TYPE, ACCOUNT_TYPE)
            put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER)
            put(CalendarContract.Calendars.OWNER_ACCOUNT, ACCOUNT_NAME)
            put(CalendarContract.Calendars.SYNC_EVENTS, 1)
            put(CalendarContract.Calendars.VISIBLE, 1)
            put(CalendarContract.Calendars.CALENDAR_COLOR, 0xFF007AFF.toInt())
        }

        val uri = CalendarContract.Calendars.CONTENT_URI.buildUpon()
            .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, ACCOUNT_TYPE)
            .build()

        val result = context.contentResolver.insert(uri, values)
        return result?.let { ContentUris.parseId(it) } ?: -1
    }

    fun syncShiftsToCalendar(shifts: List<DayShift>): Int {
        val calendarId = getOrCreateCalendar()
        if (calendarId < 0) return 0

        var count = 0
        val timeZone = TimeZone.getDefault().id

        for (shift in shifts) {
            val config = SHIFT_CONFIGS[shift.shift] ?: continue
            if (shift.shift == "休") continue // 休息日不写入日历

            val date = LocalDate.parse(shift.date)
            val startMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endMillis = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            // 检查是否已存在
            val existing = context.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                arrayOf(CalendarContract.Events._ID),
                "${CalendarContract.Events.CALENDAR_ID} = ? AND ${CalendarContract.Events.DTSTART} = ? AND ${CalendarContract.Events.TITLE} = ?",
                arrayOf(calendarId.toString(), startMillis.toString(), config.shiftName),
                null
            )

            val exists = existing?.use { it.count > 0 } ?: false
            if (exists) continue

            val values = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.TITLE, config.shiftName)
                put(CalendarContract.Events.DESCRIPTION, "发电部运行倒班 - ${config.shiftName}")
                put(CalendarContract.Events.DTSTART, startMillis)
                put(CalendarContract.Events.DTEND, endMillis)
                put(CalendarContract.Events.ALL_DAY, 1)
                put(CalendarContract.Events.EVENT_TIMEZONE, timeZone)
                put(CalendarContract.Events.EVENT_COLOR, config.colorLight.toInt())
            }

            context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            count++
        }

        return count
    }

    fun clearSyncedEvents(): Int {
        val calendarId = getOrCreateCalendar()
        if (calendarId < 0) return 0

        return context.contentResolver.delete(
            CalendarContract.Events.CONTENT_URI,
            "${CalendarContract.Events.CALENDAR_ID} = ?",
            arrayOf(calendarId.toString())
        )
    }
}

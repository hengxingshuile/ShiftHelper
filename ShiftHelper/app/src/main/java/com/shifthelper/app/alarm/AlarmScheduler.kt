package com.shifthelper.app.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.shifthelper.app.data.ScheduleRepository
import com.shifthelper.app.data.SHIFT_CONFIGS
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object AlarmScheduler {

    fun initialize(context: Context) {
        // 开机后重新调度
    }

    fun scheduleAllAlarms(context: Context) {
        val repo = ScheduleRepository(context)
        if (!repo.isAlarmsEnabled()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val team = repo.getSelectedTeam()
        val shifts = repo.getAllShiftsForTeam(team)

        // 取消现有闹钟
        cancelAllAlarms(context)

        val now = LocalDateTime.now()
        var scheduledCount = 0

        for (dayShift in shifts) {
            val config = SHIFT_CONFIGS[dayShift.shift] ?: continue
            if (config.alarmHour < 0) continue // 休息日不设闹钟

            val date = LocalDate.parse(dayShift.date)
            var alarmTime = date.atTime(config.alarmHour, config.alarmMinute)

            // 夜班的闹钟在当天晚上23:00，其他班次在当天早上/下午
            // 如果已经过了今天，跳过
            if (alarmTime.isBefore(now)) continue

            // 最多调度未来60天的闹钟
            if (date.isAfter(now.toLocalDate().plusDays(60))) continue

            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("shift_code", dayShift.shift)
                putExtra("shift_name", config.shiftName)
                putExtra("date", dayShift.date)
            }

            val requestCode = dayShift.date.replace("-", "").toInt()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val triggerAtMillis = alarmTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }

            scheduledCount++
        }
    }

    fun cancelAllAlarms(context: Context) {
        val repo = ScheduleRepository(context)
        val team = repo.getSelectedTeam()
        val shifts = repo.getAllShiftsForTeam(team)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        for (dayShift in shifts) {
            val intent = Intent(context, AlarmReceiver::class.java)
            val requestCode = dayShift.date.replace("-", "").toInt()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}

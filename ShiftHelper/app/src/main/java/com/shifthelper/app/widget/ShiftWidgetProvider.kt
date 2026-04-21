package com.shifthelper.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.shifthelper.app.R
import com.shifthelper.app.data.ScheduleRepository
import com.shifthelper.app.data.SHIFT_CONFIGS
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ShiftWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, ShiftWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.shifthelper.app.UPDATE_WIDGET"

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val repo = ScheduleRepository(context)
            val todayShift = repo.getTodayShift()
            val tomorrowShift = repo.getShiftForDate(LocalDate.now().plusDays(1))

            val views = RemoteViews(context.packageName, R.layout.widget_shift)

            val dateFormatter = DateTimeFormatter.ofPattern("MM月dd日 EEEE")
            val todayStr = LocalDate.now().format(dateFormatter)
            views.setTextViewText(R.id.widget_date, todayStr)

            if (todayShift != null) {
                val config = SHIFT_CONFIGS[todayShift.shift]
                views.setTextViewText(R.id.widget_shift_name, config?.shiftName ?: todayShift.shift)
                views.setTextViewText(R.id.widget_team, repo.getSelectedTeam())

                val color = if (android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                    android.content.res.Configuration.UI_MODE_NIGHT_YES) {
                    config?.colorDark ?: 0xFFFFFFFF
                } else {
                    config?.colorLight ?: 0xFF000000
                }
                views.setTextColor(R.id.widget_shift_name, color.toInt())
            } else {
                views.setTextViewText(R.id.widget_shift_name, "未导入")
                views.setTextViewText(R.id.widget_team, "请导入排班表")
            }

            if (tomorrowShift != null) {
                val tConfig = SHIFT_CONFIGS[tomorrowShift.shift]
                views.setTextViewText(
                    R.id.widget_tomorrow,
                    "明天: ${tConfig?.shiftName ?: tomorrowShift.shift}"
                )
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun triggerUpdate(context: Context) {
            val intent = Intent(context, ShiftWidgetProvider::class.java).apply {
                action = ACTION_UPDATE_WIDGET
            }
            context.sendBroadcast(intent)
        }
    }
}

package com.shifthelper.app.data

import android.content.Context
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader

object AssetScheduleLoader {

    fun loadDefaultSchedule(context: Context): ScheduleData? {
        return try {
            // 优先加载完整版排班数据
            val fileName = if (context.assets.list("")?.contains("schedule_2026_full.json") == true) {
                "schedule_2026_full.json"
            } else {
                "schedule_2026.json"
            }
            val json = context.assets.open(fileName).use { stream ->
                BufferedReader(InputStreamReader(stream)).readText()
            }
            Gson().fromJson(json, ScheduleData::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

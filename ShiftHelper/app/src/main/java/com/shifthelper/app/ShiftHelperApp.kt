package com.shifthelper.app

import android.app.Application
import com.shifthelper.app.data.AssetScheduleLoader
import com.shifthelper.app.data.ScheduleRepository
import com.shifthelper.app.alarm.AlarmScheduler

class ShiftHelperApp : Application() {
    lateinit var repository: ScheduleRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = ScheduleRepository(this)

        // 首次启动加载默认排班数据
        if (repository.getScheduleData() == null) {
            AssetScheduleLoader.loadDefaultSchedule(this)?.let {
                repository.saveScheduleData(it)
            }
        }

        AlarmScheduler.initialize(this)
    }
}

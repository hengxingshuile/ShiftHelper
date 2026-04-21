package com.shifthelper.app.widget

import android.app.Service
import android.content.Intent
import android.os.IBinder

class ShiftWidgetService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}

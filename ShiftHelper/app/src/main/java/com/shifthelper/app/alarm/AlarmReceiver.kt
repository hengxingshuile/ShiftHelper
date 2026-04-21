package com.shifthelper.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shifthelper.app.AlarmActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val shiftCode = intent.getStringExtra("shift_code") ?: return
        val shiftName = intent.getStringExtra("shift_name") ?: return
        val date = intent.getStringExtra("date") ?: return

        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("shift_code", shiftCode)
            putExtra("shift_name", shiftName)
            putExtra("date", date)
        }
        context.startActivity(alarmIntent)
    }
}

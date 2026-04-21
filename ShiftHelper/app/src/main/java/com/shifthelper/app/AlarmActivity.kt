package com.shifthelper.app

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifthelper.app.data.SHIFT_CONFIGS
import com.shifthelper.app.ui.theme.ShiftHelperTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class AlarmActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 保持屏幕亮起
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        val shiftCode = intent.getStringExtra("shift_code") ?: ""
        val shiftName = intent.getStringExtra("shift_name") ?: ""
        val date = intent.getStringExtra("date") ?: ""

        // 播放闹钟铃声
        playAlarmSound()
        vibrate()

        setContent {
            ShiftHelperTheme(darkTheme = true) {
                AlarmScreen(
                    shiftCode = shiftCode,
                    shiftName = shiftName,
                    date = date,
                    onDismiss = { dismissAlarm() },
                    onSnooze = { snoozeAlarm() }
                )
            }
        }
    }

    private fun playAlarmSound() {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@AlarmActivity, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun vibrate() {
        vibrator = getSystemService(VIBRATOR_SERVICE) as? Vibrator
        vibrator?.let {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                it.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 500, 500, 500),
                        0
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(longArrayOf(0, 500, 500, 500), 0)
            }
        }
    }

    private fun dismissAlarm() {
        stopAlarm()
        finish()
    }

    private fun snoozeAlarm() {
        stopAlarm()
        // 10分钟后再次响铃
        finish()
    }

    private fun stopAlarm() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
    }
}

@Composable
fun AlarmScreen(
    shiftCode: String,
    shiftName: String,
    date: String,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    val config = SHIFT_CONFIGS[shiftCode]
    val color = when (shiftCode) {
        "白" -> Color(0xFFFF9500)
        "中" -> Color(0xFF34C759)
        "夜" -> Color(0xFF5856D6)
        "学" -> Color(0xFF007AFF)
        else -> Color(0xFF8E8E93)
    }

    val dateStr = try {
        val localDate = LocalDate.parse(date)
        localDate.format(DateTimeFormatter.ofPattern("MM月dd日 EEEE", Locale.CHINA))
    } catch (e: Exception) {
        date
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dateStr,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(80.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = shiftName,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "该上班了",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(64.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = color
                )
            ) {
                Text("关闭闹钟", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onSnooze,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White.copy(alpha = 0.7f)
                )
            ) {
                Text("稍后提醒 (10分钟)", fontSize = 16.sp)
            }
        }
    }
}

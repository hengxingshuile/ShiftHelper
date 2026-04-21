package com.shifthelper.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifthelper.app.data.SHIFT_CONFIGS
import com.shifthelper.app.ui.theme.ShiftMiddleColor
import com.shifthelper.app.ui.theme.ShiftNightColor
import com.shifthelper.app.ui.theme.ShiftRestColor
import com.shifthelper.app.ui.theme.ShiftStudyColor
import com.shifthelper.app.ui.theme.ShiftWhiteColor

@Composable
fun ShiftBadge(
    shiftCode: String,
    modifier: Modifier = Modifier,
    showText: Boolean = true
) {
    val config = SHIFT_CONFIGS[shiftCode]
    val color = when (shiftCode) {
        "白" -> ShiftWhiteColor
        "中" -> ShiftMiddleColor
        "夜" -> ShiftNightColor
        "学" -> ShiftStudyColor
        else -> ShiftRestColor
    }

    val backgroundColor = if (MaterialTheme.colorScheme.background.hashCode() == Color.Black.hashCode()) {
        color.copy(alpha = 0.2f)
    } else {
        color.copy(alpha = 0.12f)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        if (showText) {
            Text(
                text = config?.shiftName ?: shiftCode,
                color = color,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        } else {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

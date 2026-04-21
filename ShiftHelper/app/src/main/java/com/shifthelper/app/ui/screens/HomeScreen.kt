package com.shifthelper.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifthelper.app.data.DayShift
import com.shifthelper.app.data.ScheduleRepository
import com.shifthelper.app.data.SHIFT_CONFIGS
import com.shifthelper.app.ui.components.SafariCard
import com.shifthelper.app.ui.components.ShiftBadge
import com.shifthelper.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: ScheduleRepository,
    onNavigateToCalendar: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToImport: () -> Unit = {}
) {
    val todayShift = remember { mutableStateOf(repository.getTodayShift()) }
    val upcomingShifts = remember { mutableStateOf(repository.getUpcomingShifts(7)) }
    val selectedTeam = remember { mutableStateOf(repository.getSelectedTeam()) }

    val today = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("MM月dd日 EEEE", Locale.CHINA)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "倒班助手",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    IconButton(onClick = onNavigateToCalendar) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "日历"
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "设置"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                TodayShiftCard(todayShift.value, selectedTeam.value, today.format(dateFormatter))
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                QuickActionsRow(
                    onCalendarClick = onNavigateToCalendar,
                    onSettingsClick = onNavigateToSettings,
                    onImportClick = onNavigateToImport
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "未来7天",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(upcomingShifts.value) { dayShift ->
                UpcomingShiftItem(dayShift)
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun TodayShiftCard(shift: DayShift?, team: String, dateStr: String) {
    SafariCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dateStr,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = team,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (shift != null) {
                val config = SHIFT_CONFIGS[shift.shift]
                val color = when (shift.shift) {
                    "白" -> ShiftWhiteColor
                    "中" -> ShiftMiddleColor
                    "夜" -> ShiftNightColor
                    "学" -> ShiftStudyColor
                    else -> ShiftRestColor
                }

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = config?.shiftName ?: shift.shift,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (config != null && config.alarmHour >= 0) {
                    val alarmTimeStr = String.format("%02d:%02d", config.alarmHour, config.alarmMinute)
                    Text(
                        text = "闹钟 $alarmTimeStr",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = "未导入排班",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun QuickActionsRow(
    onCalendarClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onImportClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickActionButton(
            icon = Icons.Default.CalendarMonth,
            label = "日历",
            onClick = onCalendarClick
        )
        QuickActionButton(
            icon = Icons.Default.Notifications,
            label = "闹钟",
            onClick = onSettingsClick
        )
        QuickActionButton(
            icon = Icons.Default.Settings,
            label = "设置",
            onClick = onSettingsClick
        )
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FilledTonalIconButton(
            onClick = onClick,
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun UpcomingShiftItem(dayShift: DayShift) {
    val date = LocalDate.parse(dayShift.date)
    val isToday = date == LocalDate.now()
    val dayFormatter = DateTimeFormatter.ofPattern("MM/dd E", Locale.CHINA)
    val config = SHIFT_CONFIGS[dayShift.shift]

    SafariCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = date.format(dayFormatter),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                )
                if (isToday) {
                    Text(
                        text = "今天",
                        style = MaterialTheme.typography.labelSmall,
                        color = SafariBlue
                    )
                }
            }

            ShiftBadge(shiftCode = dayShift.shift)
        }
    }
}

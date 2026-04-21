package com.shifthelper.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.shifthelper.app.alarm.AlarmScheduler
import com.shifthelper.app.calendar.CalendarSyncManager
import com.shifthelper.app.data.ScheduleRepository
import com.shifthelper.app.data.SHIFT_CONFIGS
import com.shifthelper.app.ui.components.SafariCard
import com.shifthelper.app.ui.theme.SafariBlue
import com.shifthelper.app.widget.ShiftWidgetProvider
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: ScheduleRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTeam by remember { mutableStateOf(repository.getSelectedTeam()) }
    var alarmsEnabled by remember { mutableStateOf(repository.isAlarmsEnabled()) }
    var calendarSynced by remember { mutableStateOf(repository.isCalendarSynced()) }

    val teams = listOf("一值", "二值", "三值", "四值", "五值")

    // 权限请求
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && alarmsEnabled) {
            AlarmScheduler.scheduleAllAlarms(context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 班组选择
            SafariCard {
                Text(
                    text = "选择班组",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                teams.forEach { team ->
                    TeamSelectItem(
                        team = team,
                        selected = team == selectedTeam,
                        onClick = {
                            selectedTeam = team
                            repository.setSelectedTeam(team)
                            if (alarmsEnabled) {
                                AlarmScheduler.scheduleAllAlarms(context)
                            }
                            ShiftWidgetProvider.triggerUpdate(context)
                            scope.launch {
                                snackbarHostState.showSnackbar("已切换到 $team")
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 闹钟设置
            SafariCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "班次闹钟",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "根据排班自动设置闹钟",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = alarmsEnabled,
                        onCheckedChange = { enabled ->
                            alarmsEnabled = enabled
                            repository.setAlarmsEnabled(enabled)
                            if (enabled) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    if (ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.POST_NOTIFICATIONS
                                        ) != PackageManager.PERMISSION_GRANTED
                                    ) {
                                        notificationPermissionLauncher.launch(
                                            Manifest.permission.POST_NOTIFICATIONS
                                        )
                                    } else {
                                        AlarmScheduler.scheduleAllAlarms(context)
                                    }
                                } else {
                                    AlarmScheduler.scheduleAllAlarms(context)
                                }
                                scope.launch {
                                    snackbarHostState.showSnackbar("已开启班次闹钟")
                                }
                            } else {
                                AlarmScheduler.cancelAllAlarms(context)
                                scope.launch {
                                    snackbarHostState.showSnackbar("已关闭班次闹钟")
                                }
                            }
                        }
                    )
                }

                if (alarmsEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))
                    SHIFT_CONFIGS.values.filter { it.alarmHour >= 0 }.forEach { config ->
                        AlarmTimeItem(config.shiftName, config.alarmHour, config.alarmMinute)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 日历同步
            SafariCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "同步到系统日历",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "将排班导入手机日历",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (calendarSynced) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "已同步",
                            tint = SafariBlue
                        )
                    } else {
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        val manager = CalendarSyncManager(context)
                                        val teamShifts = repository.getAllShiftsForTeam(selectedTeam)
                                        val count = manager.syncShiftsToCalendar(teamShifts)
                                        calendarSynced = true
                                        repository.setCalendarSynced(true)
                                        snackbarHostState.showSnackbar("已同步 $count 个班次到日历")
                                    } catch (e: SecurityException) {
                                        snackbarHostState.showSnackbar("需要日历权限，请在设置中开启")
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("同步")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun TeamSelectItem(team: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = team,
            style = MaterialTheme.typography.bodyLarge
        )
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "已选择",
                tint = SafariBlue
            )
        }
    }
}

@Composable
fun AlarmTimeItem(shiftName: String, hour: Int, minute: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = shiftName,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = String.format("%02d:%02d", hour, minute),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

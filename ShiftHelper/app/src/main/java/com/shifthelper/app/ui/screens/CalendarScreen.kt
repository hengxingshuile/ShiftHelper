package com.shifthelper.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifthelper.app.data.ScheduleRepository
import com.shifthelper.app.data.SHIFT_CONFIGS
import com.shifthelper.app.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    repository: ScheduleRepository,
    onNavigateBack: () -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val selectedTeam = remember { mutableStateOf(repository.getSelectedTeam()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("排班日历") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 月份导航
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "上月")
                }
                Text(
                    text = currentMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月", Locale.CHINA)),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "下月")
                }
            }

            // 星期标题
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                val weekDays = listOf("日", "一", "二", "三", "四", "五", "六")
                weekDays.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 日历网格
            CalendarGrid(
                yearMonth = currentMonth,
                repository = repository,
                selectedTeam = selectedTeam.value
            )

            // 图例
            Spacer(modifier = Modifier.height(16.dp))
            ShiftLegend()
        }
    }
}

@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    repository: ScheduleRepository,
    selectedTeam: String
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 周日=0

    val days = mutableListOf<CalendarDay>()

    // 空白占位
    repeat(firstDayOfWeek) {
        days.add(CalendarDay.Empty)
    }

    // 实际日期
    for (day in 1..daysInMonth) {
        val date = yearMonth.atDay(day)
        val shift = repository.getShiftForDate(date)
        days.add(CalendarDay.Date(date, shift?.shift))
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        userScrollEnabled = false
    ) {
        items(days) { day ->
            CalendarDayCell(day)
        }
    }
}

sealed class CalendarDay {
    object Empty : CalendarDay()
    data class Date(val date: LocalDate, val shiftCode: String?) : CalendarDay()
}

@Composable
fun CalendarDayCell(day: CalendarDay) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        when (day) {
            is CalendarDay.Empty -> {}
            is CalendarDay.Date -> {
                val isToday = day.date == LocalDate.now()
                val config = day.shiftCode?.let { SHIFT_CONFIGS[it] }
                val color = when (day.shiftCode) {
                    "白" -> ShiftWhiteColor
                    "中" -> ShiftMiddleColor
                    "夜" -> ShiftNightColor
                    "学" -> ShiftStudyColor
                    "休" -> ShiftRestColor
                    else -> Color.Transparent
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (isToday) SafariBlue.copy(alpha = 0.15f)
                                else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.date.dayOfMonth.toString(),
                            fontSize = 14.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (isToday) SafariBlue else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (config != null) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShiftLegend() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val items = listOf(
            "白" to ShiftWhiteColor,
            "中" to ShiftMiddleColor,
            "夜" to ShiftNightColor,
            "学" to ShiftStudyColor,
            "休" to ShiftRestColor
        )
        items.forEach { (code, color) ->
            val config = SHIFT_CONFIGS[code]
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = config?.shiftName ?: code,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

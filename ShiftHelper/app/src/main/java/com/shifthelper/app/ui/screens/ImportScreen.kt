package com.shifthelper.app.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.shifthelper.app.data.ScheduleData
import com.shifthelper.app.data.ScheduleRepository
import com.shifthelper.app.ui.components.SafariCard
import com.shifthelper.app.ui.theme.SafariBlue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    repository: ScheduleRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var importStatus by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    val json = readTextFromUri(context, it)
                    val data = Gson().fromJson(json, ScheduleData::class.java)
                    if (data != null && data.schedules.isNotEmpty()) {
                        repository.saveScheduleData(data)
                        importStatus = "导入成功！共 ${data.schedules.values.first().size} 天排班数据"
                        snackbarHostState.showSnackbar("排班表导入成功")
                    } else {
                        importStatus = "导入失败：数据格式不正确"
                        snackbarHostState.showSnackbar("导入失败：数据格式不正确")
                    }
                } catch (e: Exception) {
                    importStatus = "导入失败：${e.message}"
                    snackbarHostState.showSnackbar("导入失败：${e.message}")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("导入排班表") },
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

            SafariCard {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = "上传",
                        modifier = Modifier.size(48.dp),
                        tint = SafariBlue
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "导入排班文件",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "支持 JSON 格式排班表文件",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { filePickerLauncher.launch("application/json") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("选择文件")
                    }
                }
            }

            if (importStatus.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                SafariCard {
                    Text(
                        text = importStatus,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (importStatus.contains("成功")) SafariBlue else MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            SafariCard {
                Text(
                    text = "文件格式说明",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = """
JSON 格式示例：
{
  "year": 2026,
  "shift_names": {
    "白": "白班",
    "中": "中班",
    "夜": "夜班",
    "学": "学习班",
    "休": "休息"
  },
  "schedules": {
    "三值": [
      {"date": "2026-01-01", "shift": "休"},
      {"date": "2026-01-02", "shift": "白"}
    ]
  }
}
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private fun readTextFromUri(context: Context, uri: Uri): String {
    return context.contentResolver.openInputStream(uri)?.use { stream ->
        stream.bufferedReader().use { it.readText() }
    } ?: throw IllegalArgumentException("无法读取文件")
}

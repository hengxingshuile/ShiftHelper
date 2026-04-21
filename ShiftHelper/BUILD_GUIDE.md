# 倒班助手 APP - 构建与打包指南

## 项目概述

- **名称**: 倒班助手
- **包名**: `com.shifthelper.app`
- **技术栈**: Kotlin + Jetpack Compose + Material3
- **最低SDK**: 26 (Android 8.0)
- **目标SDK**: 35 (Android 15)

## 功能清单

- [x] 导入排班表（JSON格式）
- [x] 选择班组（一值~五值）
- [x] 今日班次展示
- [x] 月历视图（带班次标记）
- [x] 系统日历同步
- [x] 自动闹钟（白班07:10 / 中班14:50 / 夜班23:00 / 学习班08:55）
- [x] 桌面小组件
- [x] Safari风格UI
- [x] 深色模式支持

## 云端打包APK步骤

### 方案一：GitHub Actions 自动打包（推荐）

1. **创建GitHub仓库**
   ```bash
   git init
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin https://github.com/你的用户名/ShiftHelper.git
   git push -u origin main
   ```

2. **创建 `.github/workflows/build.yml`**
   ```yaml
   name: Build APK

   on:
     push:
       branches: [ main ]
     pull_request:
       branches: [ main ]
     workflow_dispatch:

   jobs:
     build:
       runs-on: ubuntu-latest
       steps:
       - uses: actions/checkout@v4

       - name: Set up JDK 17
         uses: actions/setup-java@v4
         with:
           java-version: '17'
           distribution: 'temurin'

       - name: Grant execute permission for gradlew
         run: chmod +x gradlew

       - name: Build with Gradle
         run: ./gradlew assembleRelease

       - name: Upload APK
         uses: actions/upload-artifact@v4
         with:
           name: app-release
           path: app/build/outputs/apk/release/*.apk
   ```

3. **触发构建**
   - 推送代码后自动触发
   - 或进入 GitHub 仓库 → Actions → Build APK → Run workflow

4. **下载APK**
   - 构建完成后在 Actions 页面下载 `app-release` artifact

### 方案二：使用免费CI服务

#### Firebase App Distribution / App Center
- 上传代码到GitHub后，可连接 Microsoft App Center 自动构建

#### 在线APK构建平台
- [Appetize.io](https://appetize.io) - 在线预览
- [APKOnline](https://www.apkonlines.com) - 在线Android模拟器

### 方案三：本地构建（如果你有Android Studio）

```bash
# 1. 打开 Android Studio
# 2. File → Open → 选择 ShiftHelper 文件夹
# 3. 等待Gradle同步完成
# 4. Build → Generate Signed Bundle/APK
# 5. 选择 APK → Next
# 6. 创建或选择密钥库 → Finish
# 7. APK 生成在 app/build/outputs/apk/release/
```

## 排班数据格式

APP首次启动会自动加载 `assets/schedule_2026.json`。你也可以通过"导入"功能加载自定义JSON文件。

```json
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
```

## ColorOS 16 适配说明

- 已申请 `SCHEDULE_EXACT_ALARM` 和 `USE_EXACT_ALARM` 权限
- 已处理 Android 14+ 的精确闹钟权限检查
- 开机自启广播确保闹钟不丢失
- 通知权限在设置页面动态申请

## 权限清单

| 权限 | 用途 |
|------|------|
| READ_CALENDAR | 读取系统日历 |
| WRITE_CALENDAR | 写入排班到日历 |
| SCHEDULE_EXACT_ALARM | 设置精确闹钟 |
| USE_EXACT_ALARM | 使用精确闹钟 (Android 14+) |
| RECEIVE_BOOT_COMPLETED | 开机重新调度闹钟 |
| POST_NOTIFICATIONS | 发送闹钟通知 (Android 13+) |
| VIBRATE | 闹钟震动 |
| WAKE_LOCK | 唤醒屏幕 |

## 项目结构

```
ShiftHelper/
├── app/
│   ├── src/main/
│   │   ├── java/com/shifthelper/app/
│   │   │   ├── MainActivity.kt          # 主Activity
│   │   │   ├── AlarmActivity.kt         # 闹钟响铃页面
│   │   │   ├── ShiftHelperApp.kt        # Application类
│   │   │   ├── data/
│   │   │   │   ├── ScheduleModels.kt    # 数据模型
│   │   │   │   ├── ScheduleRepository.kt # 数据仓库
│   │   │   │   └── AssetScheduleLoader.kt # 资产加载
│   │   │   ├── alarm/
│   │   │   │   ├── AlarmScheduler.kt    # 闹钟调度
│   │   │   │   ├── AlarmReceiver.kt     # 闹钟接收器
│   │   │   │   └── BootReceiver.kt      # 开机广播
│   │   │   ├── calendar/
│   │   │   │   └── CalendarSyncManager.kt # 日历同步
│   │   │   ├── widget/
│   │   │   │   ├── ShiftWidgetProvider.kt # 小组件
│   │   │   │   └── ShiftWidgetService.kt
│   │   │   ├── ui/
│   │   │   │   ├── theme/               # 主题
│   │   │   │   ├── components/          # 组件
│   │   │   │   └── screens/             # 页面
│   │   │   │       ├── HomeScreen.kt
│   │   │   │       ├── CalendarScreen.kt
│   │   │   │       ├── SettingsScreen.kt
│   │   │   │       └── ImportScreen.kt
│   │   ├── res/                         # 资源文件
│   │   └── assets/schedule_2026.json    # 默认排班数据
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/
```

## 注意事项

1. **首次安装后**: 进入设置选择你的班组（默认三值）
2. **闹钟权限**: Android 14+ 需要手动在系统设置中授予"设置闹钟和提醒"权限
3. **日历权限**: 同步日历时需要授权日历读写权限
4. **电池优化**: 建议在系统设置中将本APP加入电池优化白名单，防止闹钟被系统杀掉
5. **ColorOS**: 在 设置 → 应用管理 → 自启动管理 中允许本APP自启动

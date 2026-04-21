# 倒班助手 APK 打包指南（最简单版）

## 方案一：GitHub Actions 全自动打包（推荐，不用装任何东西）

### 第1步：注册GitHub账号
- 打开 https://github.com
- 用邮箱注册，很简单

### 第2步：创建仓库并上传代码
1. 登录GitHub，点击右上角 `+` → `New repository`
2. 仓库名填 `ShiftHelper`，选 `Public`（公开）
3. 点击 `Create repository`
4. 在页面里找到 `uploading an existing file` 链接，点进去
5. 把 `ShiftHelper` 文件夹里的所有文件和文件夹拖进去
   - 注意：是拖 `ShiftHelper` 里面的内容，不是拖文件夹本身
6. 点 `Commit changes`

### 第3步：等自动构建
1. 上传完成后，点上方 `Actions` 标签
2. 会看到 `Build APK` 工作流在运行（黄色圆圈）
3. 等几分钟，变成绿色勾就成功了

### 第4步：下载APK
1. 点左侧 `Build APK` → 最新的那次运行
2. 页面下方 `Artifacts` 里有 `ShiftHelper-Release`
3. 点下载，解压后就是 `app-release.apk`
4. 发到手机上安装即可

> 以后代码有更新，重新上传文件，Actions会自动重新打包

---

## 方案二：找朋友帮忙打包

如果你实在不想折腾GitHub，可以：
1. 把 `ShiftHelper` 文件夹压缩成zip
2. 发给会Android开发的朋友
3. 让他用Android Studio打开，Build → Generate Signed APK
4. 把生成的APK发回给你

---

## 方案三：在线构建平台

### 使用 AppCircle 或 Codemagic（有免费额度）
1. 注册账号
2. 连接GitHub仓库
3. 配置构建流程
4. 自动出APK

---

## 安装到手机

1. 把 `app-release.apk` 发到手机上（微信、QQ、数据线都行）
2. 在手机上点击安装
3. 如果提示"未知来源"，去设置里允许安装未知应用
4. ColorOS用户：设置 → 密码与安全 → 系统安全 → 外部来源应用 → 允许文件管理器

---

## 首次使用

1. 打开APP，默认加载了2026年全年排班
2. 点右上角设置 → 选择你的班组（默认三值）
3. 开启"班次闹钟"
4. （可选）开启"同步到系统日历"
5. 长按桌面 → 小组件 → 找到"倒班助手"添加到桌面

搞定！

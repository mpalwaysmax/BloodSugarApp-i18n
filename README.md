# BloodSugar — 血糖管家

> 奶奶有糖尿病，每天在手抄本上记录血糖值，但本子越来越厚，找某天的记录很困难，每次看诊都要翻半天。我帮她做了这个血糖管家 app，记录更方便，还能一键生成 PDF 报告给医生看。

一个轻量级 Android 血糖记录应用，专为老年人设计。Kotlin + Jetpack Compose + Material 3。

## 功能

- **快速记录**：6 个餐段（早餐前/后、午餐前/后、晚餐前/后），时间自动推断
- **统计摘要**：按餐段分组显示平均值、最高、最低、记录数
- **PDF 导出**：一键生成血糖报告，支持微信/蓝牙/打印分享给医生
- **趋势图**：Canvas 自绘折线图，支持月/年切换，横向滚动
- **时间分组**：本月 / 今年 / 更早，可折叠，空分组隐藏
- **排序**：最新优先 / 最旧优先切换
- **中英双语**：跟随系统语言自动切换
- **颜色编码**：偏低红、正常绿、偏高橙，WCAG AA 无障碍标准
- **数据安全**：Room 数据库 + Migration 框架，覆盖安装不丢数据
- **隐私**：完全离线，不收集任何数据

## Screenshots

*Coming soon*

## Tech Stack

| Component | Version | Notes |
|-----------|---------|-------|
| AGP | 7.4.2 | Compatible with JDK 11 |
| Kotlin | 1.8.22 | Compose Compiler 1.4.8 |
| JDK | 11 | Eclipse Adoptium |
| Compose BOM | 2023.06.01 | |
| Room | 2.5.2 | Using kapt |

## Architecture

- **MVVM** without Repository layer (ViewModel → DAO direct)
- **Jetpack Compose** for all UI
- **Material 3** design system
- **Room** for local database
- **StateFlow** for reactive state management

## Building

```bash
# Set JAVA_HOME (adjust path for your system)
export JAVA_HOME="/path/to/jdk-11"

# Build debug APK
./gradlew assembleDebug

# Output: app/build/outputs/apk/debug/app-debug.apk
```

## Installation

1. Download the APK from releases or build from source
2. Enable "Install from unknown sources" on your Android device
3. Install the APK

## Internationalization

The app supports Chinese (default) and English. To add a new language:

1. Create `app/src/main/res/values-<locale>/strings.xml`
2. Copy all entries from `app/src/main/res/values/strings.xml`
3. Translate the values

## Data Safety

- **No `fallbackToDestructiveMigration()`** — updates will never delete your data
- Over-the-air APK updates preserve all existing records
- Database schema changes use Room's Migration framework

## Project Structure

```
app/src/main/java/com/bloodsugar/
├── MainActivity.kt          # 入口
├── data/
│   ├── AppDatabase.kt       # Room 数据库
│   ├── Record.kt            # 实体：id, value, segment, note, timestamp
│   ├── RecordDao.kt         # DAO：Flow 查询 + 统计聚合
│   └── SegmentStats.kt      # 统计数据类
├── ui/
│   ├── MainScreen.kt        # 主界面：标题栏 + 统计卡片 + 记录列表
│   ├── MainViewModel.kt     # 状态管理
│   ├── RecordSheet.kt       # 新建/编辑弹窗
│   ├── ChartOverlay.kt      # 趋势图：Canvas 自绘
│   ├── StatsSummaryCard.kt  # 统计摘要卡片（可折叠）
│   ├── PdfExporter.kt       # PDF 报告生成（零依赖）
│   └── theme/               # 颜色、字体、主题
└── util/
    ├── GlucoseValidator.kt  # 血糖值校验（1.0-33.3 mmol/L）
    └── MealSegment.kt       # 6 个餐段枚举 + 时间推断
```

## License

MIT License — see [LICENSE](LICENSE) for details.

## Privacy

This app is fully offline. No data is collected, transmitted, or shared. All blood sugar records are stored locally on your device only.

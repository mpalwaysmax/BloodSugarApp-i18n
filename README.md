# BloodSugar Tracker

[中文版](README_zh.md)

> My grandmother has diabetes and used to track her blood sugar in a handwritten notebook. As the pages piled up, finding old records became impossible — every doctor's visit meant flipping through stacks of paper. I built this app to make tracking effortless and sharing with doctors instant.

A lightweight, offline-first Android blood sugar recording app designed for elderly users. Built with Kotlin, Jetpack Compose, and Material 3.

## Features

- **Quick Recording** — 6 meal segments (before/after breakfast, lunch, dinner) with automatic time-based inference
- **Statistics Summary** — Collapsible card showing average, max, min glucose per meal segment
- **PDF Export** — One-tap report generation with stats + full record table, share via Bluetooth, email, or print
- **Trend Chart** — Canvas-drawn line chart with month/year toggle, horizontal scroll, and normal-range indicator
- **Time Grouping** — Records grouped by This Month / This Year / Earlier with collapsible headers
- **Bilingual** — Chinese and English UI, follows system language automatically
- **Accessibility** — Large fonts for elderly users, WCAG AA color contrast for glucose status indicators
- **Data Safety** — Room database with migration framework — APK updates never delete your data
- **Privacy** — Fully offline. Zero data collection. All records stay on your device.

## Screenshots

| Main Screen | Record Dialog | Statistics | PDF Export |
|:-----------:|:------------:|:----------:|:----------:|
| *Coming soon* | *Coming soon* | *Coming soon* | *Coming soon* |

## Tech Stack

| Component | Version | Notes |
|-----------|---------|-------|
| AGP | 7.4.2 | Compatible with JDK 11 |
| Kotlin | 1.8.22 | Compose Compiler 1.4.8 |
| JDK | 11 | Eclipse Adoptium |
| Compose BOM | 2023.06.01 | |
| Room | 2.5.2 | Using kapt |

**Zero third-party dependencies** — PDF generation uses Android's native `PdfDocument` API. Charts are drawn with Compose `Canvas`. No charting libraries, no PDF libraries.

## Architecture

```
app/src/main/java/com/bloodsugar/
├── MainActivity.kt          # Entry point
├── data/
│   ├── AppDatabase.kt       # Room database with migration framework
│   ├── Record.kt            # Entity: id, value, segment, note, timestamp
│   ├── RecordDao.kt         # DAO: Flow queries + SQL aggregation
│   └── SegmentStats.kt      # Stats data class
├── ui/
│   ├── MainScreen.kt        # Main UI: header + stats card + grouped list
│   ├── MainViewModel.kt     # State management (MVVM)
│   ├── RecordSheet.kt       # Record entry/edit dialog
│   ├── ChartOverlay.kt      # Trend chart (Canvas-drawn)
│   ├── StatsSummaryCard.kt  # Collapsible statistics card
│   ├── PdfExporter.kt       # PDF report generation (zero dependencies)
│   └── theme/               # Colors, typography, Material 3 theme
└── util/
    ├── GlucoseValidator.kt  # Input validation (1.0–33.3 mmol/L)
    └── MealSegment.kt       # 6 meal segments + time-based inference
```

- **MVVM** — ViewModel talks directly to DAO (no Repository layer, no DI)
- **Jetpack Compose** — 100% declarative UI
- **Material 3** — Design system with light/dark theme
- **StateFlow** — Reactive state management

## Building

```bash
# Set JAVA_HOME (adjust path for your system)
export JAVA_HOME="/path/to/jdk-11"

# Build debug APK
./gradlew assembleDebug

# Output: app/build/outputs/apk/debug/app-debug.apk
```

## Installation

1. Download the APK from [Releases](../../releases) or build from source
2. Enable "Install from unknown sources" on your Android device
3. Install the APK

## Adding a New Language

1. Create `app/src/main/res/values-<locale>/strings.xml`
2. Copy all entries from `app/src/main/res/values/strings.xml` (Chinese) or `values-en/strings.xml` (English)
3. Translate the values

## Data Safety

- **No `fallbackToDestructiveMigration()`** — updates never destroy your data
- APK overlay installs preserve all existing records
- Schema changes use Room's `Migration` framework with SQL scripts

## Contributing

Contributions are welcome! Feel free to open issues or submit pull requests.

## License

[MIT License](LICENSE) — free to use, modify, and distribute.

## Why This Exists

Diabetes management requires consistent tracking, but existing apps are often bloated, ad-supported, or require cloud accounts. This app is:

- **Dead simple** — designed for people who find smartphones confusing
- **Fully offline** — no account, no cloud, no tracking
- **Zero cost** — no ads, no subscriptions, no paywalls
- **Shareable** — one-tap PDF export for doctor visits

Built with care for those who need it most.

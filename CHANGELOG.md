# Changelog

All notable changes to this project will be documented in this file.

## [1.1.0] - 2026-05-31

### Added
- HbA1c estimation from recent glucose records (Nathan formula)
- % in range (4.4-7.8 mmol/L) display in statistics card
- Medication tracking with CRUD (name, dosage, note, timestamp)
- Medication delete confirmation dialog
- Medication sheet redesigned to match Record Blood Sugar style

### Fixed
- Medication dialog title now shows "Edit Medication" when editing
- Medication timestamps displayed in list
- Scrollable medication dialog content

## [1.0.0] - 2026-05-31

### Added
- Blood sugar recording with 6 meal segments (before/after breakfast, lunch, dinner)
- Automatic meal segment inference based on time of day
- Statistics summary card with HbA1c estimate and % in range (collapsible)
- Medication tracking (CRUD with name, dosage, note, timestamp)
- One-tap PDF report generation with system share (Bluetooth, email, print)
- Copy summary to clipboard for quick sharing via messaging apps
- mmol/L ↔ mg/dL unit toggle (persists across restarts)
- Trend chart with smooth bezier curves, gradient fill, month/year toggle
- Time-based grouping (This Month / This Year / Earlier) with collapsible headers
- Sort by newest / oldest
- Delete with confirmation dialog
- Chinese and English bilingual support (follows system language)
- Large fonts and high-contrast colors for elderly accessibility (WCAG AA)
- Color-coded glucose values: low (red), normal (green), high (orange)
- Input validation (1.0–33.3 mmol/L)
- Room database with migration framework (v1→v2, data preserved across updates)
- FileProvider for secure PDF sharing
- Zero third-party dependencies
- 22 unit tests (GlucoseValidator + MealSegment + GlucoseUnit)
- GitHub Actions CI (build + test on push/PR)
- Issue and PR templates for community contributions

### Technical
- Kotlin + Jetpack Compose + Material 3
- MVVM architecture (ViewModel → DAO direct)
- Canvas-drawn trend chart with bezier interpolation
- Native PdfDocument API (no PDF library)
- StateFlow for reactive state management
- JUnit 4 unit tests
- Nathan formula for HbA1c estimation

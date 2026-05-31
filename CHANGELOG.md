# Changelog

All notable changes to this project will be documented in this file.

## [1.0.0] - 2026-05-31

### Added
- Blood sugar recording with 6 meal segments (before/after breakfast, lunch, dinner)
- Automatic meal segment inference based on time of day
- Statistics summary card (average, max, min per meal segment, collapsible)
- One-tap PDF report generation with system share (Bluetooth, email, print)
- Copy summary to clipboard for quick sharing via messaging apps
- Trend chart with month/year toggle, horizontal scroll, and normal-range indicator
- Time-based grouping (This Month / This Year / Earlier) with collapsible headers
- Sort by newest / oldest
- Delete with confirmation dialog
- Chinese and English bilingual support (follows system language)
- Large fonts and high-contrast colors for elderly accessibility (WCAG AA)
- Color-coded glucose values: low (red), normal (green), high (orange)
- Input validation (1.0–33.3 mmol/L)
- Room database with migration framework (data preserved across updates)
- FileProvider for secure PDF sharing
- Zero third-party dependencies
- 15 unit tests (GlucoseValidator + MealSegment)
- GitHub Actions CI (build + test on push/PR)
- Issue and PR templates for community contributions

### Technical
- Kotlin + Jetpack Compose + Material 3
- MVVM architecture (ViewModel → DAO direct)
- Canvas-drawn trend chart (no charting library)
- Native PdfDocument API (no PDF library)
- StateFlow for reactive state management
- JUnit 4 unit tests

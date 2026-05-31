# BloodSugar Tracker — AI Development Notes

This project was developed with AI assistance (Claude by Anthropic).

## Architecture Decisions

- **MVVM without Repository** — ViewModel talks directly to DAO. Intentional simplicity for a small app.
- **Zero third-party dependencies** — PDF generation uses native `PdfDocument`, charts use Compose `Canvas`.
- **Room with kapt** — chosen for stability with AGP 7.4.2. Do not upgrade to KSP without testing.
- **Compose BOM 2023.06.01** — pinned for AGP 7.4 compatibility. Do not upgrade to 2024+.

## Key Patterns

- All UI strings via `stringResource()` — never hardcoded
- Glucose values validated by `GlucoseValidator` (1.0–33.3 mmol/L)
- Meal segments auto-inferred from time of day via `MealSegment.inferSegment()`
- Database migrations use `ALL_MIGRATIONS` list — never `fallbackToDestructiveMigration()`
- HbA1c estimated via Nathan formula in `HbA1cEstimator`
- Medications stored in separate Room table with `MedicationDao`

## Building

```bash
export JAVA_HOME="C:/Program Files/Eclipse Adoptium/jdk-11.0.28.6-hotspot"
cd D:/Claude/BloodSugarApp-i18n && ./gradlew assembleDebug
```

## Testing

- Install via `adb install -r app-debug.apk`
- Emulator: `ANDROID_AVD_HOME="D:/Android/avd" emulator -avd BloodSugarTest -no-audio -gpu swiftshader_indirect`

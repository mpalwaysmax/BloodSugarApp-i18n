# Contributing to BloodSugar Tracker

Thanks for your interest in contributing!

## Getting Started

1. Fork the repository
2. Clone your fork
3. Create a feature branch: `git checkout -b feature/your-feature`
4. Make your changes
5. Test on an Android device or emulator
6. Commit and push
7. Open a Pull Request

## Development Setup

```bash
# Set JAVA_HOME to JDK 11
export JAVA_HOME="/path/to/jdk-11"

# Build
./gradlew assembleDebug

# APK output: app/build/outputs/apk/debug/app-debug.apk
```

## Code Guidelines

- **No `!!`** — use `?.let` or `?: default` instead
- **All UI strings** must use `stringResource()` — no hardcoded text
- **Bilingual** — add entries to both `values/strings.xml` (Chinese) and `values-en/strings.xml` (English)
- **Touch targets** ≥ 48dp for accessibility
- **Colors** use `MaterialTheme.colorScheme` — glucose status colors use `GlucoseNormal`/`GlucoseHigh`/`GlucoseLow`

## Workflow Best Practices

1. **Build before commit** — `./gradlew assembleDebug` must pass before any commit
2. **Test before push** — `./gradlew testDebugUnitTest` must pass
3. **Screenshots** — use `uiautomator dump` to find element coordinates, never hardcode tap positions
4. **New features** — design the interaction flow first (wireframe or description), then implement
5. **Commit messages** — use conventional commits: `feat:`, `fix:`, `docs:`, `test:`, `chore:`

## Adding a New Language

1. Create `app/src/main/res/values-<locale>/strings.xml`
2. Copy all entries from `values/strings.xml`
3. Translate

## Reporting Bugs

Open an issue using the Bug Report template.

## Feature Requests

Open an issue using the Feature Request template.

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

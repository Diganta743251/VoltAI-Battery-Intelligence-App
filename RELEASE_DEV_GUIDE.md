# VoltAI — Release-Ready Developer Guide

## 1. Release Strategy & Versioning
- **Semantic versioning**: MAJOR.MINOR.PATCH
- **Branching**:
  - master/main (protected)
  - develop (integration)
  - feature/xxx
  - release/x.y.z for RC
- **Tag releases**: `git tag -a v1.2.0 -m "Release 1.2.0"`

## 2. Build & Signing
- **Gradle + Kotlin + Compose**: centralize versions
- **Release signing**:
```groovy
signingConfigs {
  release {
    storeFile = file("../keystore/voltai.jks")
    storePassword = System.getenv("KEYSTORE_PASSWORD")
    keyAlias = System.getenv("KEY_ALIAS")
    keyPassword = System.getenv("KEY_PASSWORD")
  }
}
buildTypes {
  release {
    isMinifyEnabled = true
    signingConfig = signingConfigs["release"]
  }
}
```
- **Proguard rules**:
```
-keepclassmembers class com.voltai.** { *; }
-keep class com.voltai.domain.model.** { *; }
-keep class org.tensorflow.lite.** { *; }
-keep class dagger.hilt.** { *; }
```
- **Test release build**: `./gradlew assembleRelease`

## 3. CI/CD & Testing
- Build debug & release
- Run core & features tests
- Generate Jacoco coverage
- Optional AAB artifact for Play Store
- Secrets & signing handled via GitHub Secrets

Sample workflow snippet:
```yaml
name: Android CI
on:
  push:
    branches: [ develop, main ]
jobs:
  build:
    strategy:
      matrix:
        build-type: [debug, release]
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: 'temurin', java-version: 17 }
      - uses: gradle/gradle-build-action@v2
      - name: Build & Test
        run: ./gradlew :core:build :features:build :core:test :features:test
      - name: Jacoco Coverage
        run: ./gradlew :core:jacocoTestReport :core:jacocoTestCoverageVerification
```

## 4. Data Storage & Privacy
- Battery logs local, encrypted (EncryptedFile/EncryptedSharedPreferences)
- Optional cloud backup: user-initiated only
- Privacy opt-in for anonymous telemetry (default OFF)
- Permissions: FOREGROUND_SERVICE, battery APIs; avoid unnecessary background permissions

## 5. Security Best Practices
- Use HTTPS for APIs
- Keep API keys out of repo
- Hilt DI scoping; avoid leaking mutable singletons

## 6. UX & Accessibility
- LiveCurrentCard: blue=charging (negative), green=discharging (positive)
- Summary rows Min/Avg/Max color-coded
- High-contrast themes, large fonts, TalkBack labels
- Localization using string resources
- Optional TensorFlow Lite guarded by feature flag

## 7. Widgets & Wear OS
- Home widgets: display battery % and mini-graph
- Quick Settings Tiles: lightweight service updates
- Wear OS companion: sync battery % and status via DataLayer

## 8. Performance & Battery Impact
- Minimize wake locks; use WorkManager or ForegroundService carefully
- Limit sample frequency: default 1 sample/5s for UI
- Profile CPU/memory and detect leaks (LeakCanary)
- Coroutines off main thread

## 9. Release Checklist
- `./gradlew clean build` → all modules
- Assemble release AAB
- Unit tests passing
- Jacoco coverage ≥ threshold
- Lint / ktlint / detekt checks
- Manual QA: 2+ API levels, foreground service, battery logs
- Privacy policy & Play Store listing ready
- Stage rollout: 1% → 10% → 100%

## 10. Hotfix & Rollback
- Rollback: use tags or new patch release
- Use feature flags to disable problematic features

## 11. Developer Docs
- README: setup, build, test commands
- Architecture: module responsibilities, data flow
- API & Model mapping: Entity ↔ Domain
- Current sign convention: negative = charging, positive = discharging

## 12. Roadmap & Milestones
- M1: Stabilization (CI, tests, signing)
- M2: Persisted history + Trends UI
- M3: AI recommendations MVP
- M4: Widgets + Quick Tiles + Wear OS
- M5: Polish, accessibility, localization, Play Store release
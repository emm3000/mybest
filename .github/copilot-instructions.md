# Project Guidelines

## Build and Test

- Use JDK 17 when running Gradle tasks.
- Run Gradle from the repository root.
- Prefer focused verification first: `./gradlew :app:detekt`, `./gradlew :app:testDebugUnitTest`, `./gradlew :app:compileDebugAndroidTestKotlin`, and `./gradlew :app:jacocoTestReport`.
- Use `./gradlew :app:assembleQa` for a fast validation APK. Use `:app:assembleRelease` only when release-specific behavior matters.
- Treat Detekt as a hard quality gate. `config/detekt/detekt.yml` is configured with warnings as errors and zero tolerated issues.

## Architecture

- This is a single-module Android app under `app/` using Jetpack Compose, Room, WorkManager, and Koin.
- Keep layer boundaries intact: UI in `features/**` and shared UI helpers, domain contracts and use cases in `domain/**`, persistence and platform integrations in `data/**`, and dependency wiring in `di/AppModule.kt`.
- Keep business logic in use cases, repositories, and viewmodels. Do not move domain rules into composables.
- Link to existing docs instead of duplicating them. See `docs/ARCHITECTURE.md` for the layer model and `docs/FEATURES.md` for feature behavior.

## Conventions

- Follow Kotlin official style and preserve the trailing-comma settings in `.editorconfig`.
- Feature presentation code follows an MVI-like pattern with `State`, `Intent`, and `Effect` types plus a `ViewModel` exposing `StateFlow` and `SharedFlow`. Match existing feature structure when extending screens.
- Compose screens typically obtain dependencies through Koin (`koinViewModel()` or injected collaborators). Register new bindings in `app/src/main/kotlin/com/emm/mybest/di/AppModule.kt`.
- Keep dependency versions in `gradle/libs.versions.toml` instead of hardcoding versions in Gradle files.
- Room schema output is committed under `app/schemas/`; keep it updated when database structure changes.

## Environment Notes

- `keystore.properties` is optional for local development, but qa and release signing read from it through `buildSrc/src/main/kotlin/AppSigning.kt`. If it is missing or incomplete, builds fall back to the debug signing config.
- Coverage reports and exclusions are customized in `app/build.gradle.kts` and `buildSrc/src/main/kotlin/JacocoConfig.kt`. Do not treat excluded UI and generated classes as coverage regressions by default.

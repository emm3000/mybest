# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Verification

Use JDK 17 and run Gradle from the repo root.

- Detekt (hard quality gate — `config/detekt/detekt.yml` runs with `warningsAsErrors: true` and `maxIssues: 0`): `./gradlew :app:detekt`
- Unit tests: `./gradlew :app:testDebugUnitTest`
- Single unit test: `./gradlew :app:testDebugUnitTest --tests "com.emm.mybest.SomeClassTest.someMethod"`
- Instrumented test compile check (no device needed): `./gradlew :app:compileDebugAndroidTestKotlin`
- Coverage report: `./gradlew :app:jacocoTestReport` (Kover is also configured; see `app/build.gradle.kts`)
- Fast validation APK: `./gradlew :app:assembleQa` — `qa` is a `release`-initialized build type with minification disabled, used for daily validation. Only use `:app:assembleRelease` when release-specific (R8/shrink) behavior matters.

Detekt autocorrect is enabled in `app/build.gradle.kts`, so a Detekt run may modify files.

## Signing

`keystore.properties` at the repo root is optional. Release and QA signing is wired through `buildSrc/src/main/kotlin/AppSigning.kt` (`loadReleaseSigningProperties()` / `hasRequiredReleaseSigningKeys()`). When the file is missing or incomplete, both `qa` and `release` fall back to the debug signing config — no manual edits needed for local builds.

## Coverage exclusions

JaCoCo and Kover share an exclusion set defined in `buildSrc/src/main/kotlin/JacocoConfig.kt` (`appJacocoExcludes`) plus the Kover `excludes` block in `app/build.gradle.kts`. UI previews, DI, navigation glue, and generated classes (`BuildConfig`, `R`, `R$*`, `*Preview*`, `*.di.*`, `*.core.navigation.*`) are excluded by design — do not treat these as regressions.

## Architecture

Single-module Android app under `app/`. Compose + Room + WorkManager + Koin. Package root: `com.emm.mybest` under `app/src/main/kotlin/` (note: Kotlin source set is `kotlin/`, not `java/`).

Layer boundaries (keep intact):

- `features/<name>/` — UI (Compose) and per-feature ViewModels. MVI-like: each feature exposes `State`, `Intent`, and `Effect`; ViewModels expose `StateFlow` for state and `SharedFlow` for one-shot effects.
- `domain/` — `models/`, `repository/` contracts, `usecase/`, `validation/`, `reminder/`, `media/`. Business rules live here, not in composables.
- `data/` — Room entities/DAOs, repository implementations, `mappers/`, `reminder/` (workers).
- `di/AppModule.kt` — single Koin module wiring everything. Composables get dependencies via `koinViewModel()` / Koin injection; register new bindings here.
- `core/` — cross-cutting `datetime/` and `navigation/`.
- `ui/components/` and `ui/theme/` — shared UI helpers.

Feature areas: `home`, `habit`, `photo`, `weight`, `history`, `timeline`, `insights`, `settings`. See `docs/ARCHITECTURE.md` and `docs/FEATURES.md` for the layer model and feature behavior — link to these instead of duplicating their content.

## Conventions

- Kotlin official style; `.editorconfig` enables trailing commas — preserve them.
- Keep dependency versions in `gradle/libs.versions.toml`; do not hardcode versions in Gradle files.
- Room schema output is committed under `app/schemas/` — update it whenever DB structure changes (KSP arg `room.schemaLocation` is wired in `app/build.gradle.kts`).
- Opt-ins habilitados a nivel de proyecto en `kotlin.compilerOptions` (`app/build.gradle.kts`): `ExperimentalMaterial3Api`, `ExperimentalFoundationApi`, `ExperimentalCoroutinesApi`. No añadas `@OptIn` para estos marcadores en código.
- minSdk 30, compile/target SDK 36, JVM target 17.

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Purpose

`buildSrc` is a `kotlin-dsl` Gradle project (see `build.gradle.kts`) consumed by `:app`'s `build.gradle.kts`. Anything declared here becomes available to root and module build scripts without `apply` — `loadReleaseSigningProperties()` is called as a `Project` extension and `appJacocoExcludes` is referenced directly in `app/build.gradle.kts`.

Touching files here triggers a Gradle configuration recompile and invalidates the build cache for every consuming module, so keep the surface small. Plugin versions belong in `gradle/libs.versions.toml`, not here.

## Files

- `src/main/kotlin/AppSigning.kt` — `loadReleaseSigningProperties(fileName)` reads `keystore.properties` from the **root project** (not `buildSrc/`) and returns `null` on missing/unreadable file (logged as a warning, not an error). `Properties.hasRequiredReleaseSigningKeys()` validates the four required keys (`keyAlias`, `keyPassword`, `storeFile`, `storePassword`). Both `qa` and `release` build types in `app/build.gradle.kts` rely on the null-return path to fall back to the debug signing config — preserve that contract (return `null` / `false`, do not throw) so local builds without a keystore keep working.
- `src/main/kotlin/JacocoConfig.kt` — `appJacocoExcludes` is the canonical exclusion list for both JaCoCo (`jacocoTestReport` in `app/build.gradle.kts`) and is mirrored by the Kover `excludes` block. When adding new UI-only / generated / DI packages that shouldn't count toward coverage, extend this list rather than tweaking the report task in `app/`.

## Conventions

- Files in `src/main/kotlin/` use the **default package** (no `package` declaration) so consumers can reference them unqualified. Match that when adding new helpers.
- Detekt does not run against `buildSrc`; still follow the repo's Kotlin style and trailing-comma settings from `.editorconfig`.

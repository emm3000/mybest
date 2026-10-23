---
paths:
  - "app/src/test/**"
  - "app/src/androidTest/**"
---

# Testing rules

## Gate

Before every commit and PR, all three must be green:

`./gradlew :app:detekt :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin`

Unit tests alone are not enough. *Why: they miss Detekt violations and androidTest reference rot.* A pre-commit hook runs Detekt; never bypass it with `--no-verify`. Autocorrect may rewrite imports and trailing commas; that is expected.

## Writing tests

- Behavior changes (use case, ViewModel rule, derived state) are written test-first: failing test, then code. Load `mattpocock-skills:tdd`.
- Any flow that loops on the clock must exit independent of the clock: break when the clock did not advance. Never write `while (true) { delay(); emit(clock.todayIn(...)) }` without it. *Why: under `runTest` with a fake clock it hangs and crashes the JaCoCo agent with `JPLISAgent ASSERTION FAILED`.*
- Use the injected clock and `runTest`; never real time or `Thread.sleep`.
- Instrumented tests live in `app/src/androidTest/kotlin/`, never `java/`.

## Coverage

JaCoCo and Kover share the exclusion set in `buildSrc/src/main/kotlin/JacocoConfig.kt` plus the Kover `excludes` block. Previews, DI, navigation glue, `BuildConfig`, `R` and generated classes are excluded by design; their absence is not a regression.

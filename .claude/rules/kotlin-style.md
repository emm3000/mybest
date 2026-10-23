---
paths:
  - "app/src/**/kotlin/**"
---

# Kotlin style rules

Kotlin official style. Detekt (`config/detekt/detekt.yml`) is the hard gate: `warningsAsErrors: true`, `maxIssues: 0`, autocorrect on.

## Comments

- Zero comments. No `//`, no banners, no commented-out code. Rename or extract until the code explains itself.
- Allowed: KDoc on public APIs, and `TODO` with a ticket reference.

## Functions

- At most 20 lines. At most 3 parameters; beyond that, introduce a `data class` or sealed type.
- One job per function. Prefer guard clauses over nested `if`.
- No nested functions: never declare `fun` inside another function or lambda. Precompute a collection, inline, extract a private method, or as a last resort a private top-level function.

## Types and files

- One public top-level `class` / `interface` / `object` / `enum` / `sealed` type per `.kt` file, named after the file. A `State`, `Intent` and `Effect` each get their own file.
- Allowed in the same file: private or internal helpers used only by that file's type, and nested `companion object` / `object`. Test files may keep private fixtures.
- Moving a file: `git mv`, then confirm no duplicate class remains. *Why: copy-without-delete left duplicate classes.*

## Forbidden

- `!!`, `lateinit var` (except tests), `GlobalScope`, `runBlocking` in production code.
- Magic numbers: extract named constants. Hardcoded user-visible strings: use `stringResource()`.
- Dependencies instantiated inside a class.

## Naming

- Booleans start with `is`, `has`, `should`, `can`. Constants are `UPPER_SNAKE_CASE`. Classes are nouns, functions are verbs.
- Names reveal intent; no `data`, `temp`, `manager`, `helper`, `util`.

## Formatting and opt-ins

- `.editorconfig` enables trailing commas; keep them.
- `ExperimentalMaterial3Api`, `ExperimentalFoundationApi`, `ExperimentalCoroutinesApi` are opted in project-wide in `app/build.gradle.kts`. Do not add `@OptIn` for them.
- Versions live only in `gradle/libs.versions.toml`.

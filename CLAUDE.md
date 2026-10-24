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
- minSdk 30, compileSdk 37, targetSdk 36, JVM target 17.

## Rules

Path-scoped rules live in `.claude/rules/` and load when matching files are touched. Peer sessions run in worktrees and do not see Claude's per-project memory, so every rule they must follow is versioned here.

| File | Covers |
|---|---|
| `.claude/rules/architecture.md` | Layers, `ForbiddenImport`, MVI contract, Koin wiring |
| `.claude/rules/kotlin-style.md` | Comments, function size, one type per file, forbidden constructs |
| `.claude/rules/room.md` | Schema files, versioning, destructive migrations |
| `.claude/rules/testing.md` | The three-task gate, clock-independent flows, coverage exclusions |
| `.claude/rules/ui.md` | Container/presentational, Atelier Dark theme, previews |

## Commits

History on `main` is linear and every commit is atomic and reviewable by a human. Applies to the main session and to every peer.

- Conventional commits. Never `Co-Authored-By` or other AI attribution; a `PreToolUse` hook in `.claude/settings.json` blocks it.
- One logical change per commit. Feature code, refactors, formatting and dependency bumps go in separate commits, even when done in the same session.
- Every commit passes `./gradlew :app:detekt :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin` on its own, so `git bisect` works.
- No commit that adds nothing for a reader: no `wip`, no `fix review`, no `address comments`, no merge commits.
- A defect caught before merge is folded into the commit it belongs to: `git commit --fixup=<sha>`, then `GIT_SEQUENCE_EDITOR=true git rebase --autosquash origin/main`, then `git push --force-with-lease` to the own branch. Re-run the gate on the rewritten commits.
- Update a branch with `git rebase origin/main`, never `git merge main`.
- Only rewrite history that is not on `main`. A defect already on `main` gets its own `fix:` commit.

## Agent workflow

Tickets are GitHub issues labeled `ready-for-agent` in `emm3000/mybest`; one peer session works one ticket in its own worktree and opens one PR with `Closes #N`. CI (`.github/workflows/ci.yml`) is the gate and merges are rebase-only. This supersedes the earlier local fast-forward merge habit for peer work.

- Playbook: `docs/agents/multi-session.md`. Read before dispatching.
- `/wave <issues>` boots peers (max 3) and dispatches (`.claude/skills/wave/SKILL.md`).
- `/checks` runs `:app:detekt :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin` and reports per task.
- `/agents-review` reviews the pending diff against `.claude/rules/`.
- Issue tracker: `docs/agents/issue-tracker.md`; labels: `docs/agents/triage-labels.md`. Dispatch history: `docs/agents/dispatch-log.md`.
- Subagents: `pr-reviewer` (read-only MERGE / FIX FIRST) and `ticket-writer` in `.claude/agents/`.

## Agent skills

### Issue tracker

Issues live in GitHub Issues for `emm3000/mybest`, driven with the `gh` CLI. See `docs/agents/issue-tracker.md`.

### Triage labels

The five canonical labels, each string equal to its role: `needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`. See `docs/agents/triage-labels.md`.

### Domain docs

Single-context: `GLOSSARY.md` and `docs/adr/` at the repo root, created lazily by `/domain-modeling`. See `docs/agents/domain.md`.

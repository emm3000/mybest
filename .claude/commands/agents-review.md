---
description: Self-review pending changes against the repo rules
allowed-tools: Bash(git status:*) Bash(git diff:*) Read Grep
disable-model-invocation: true
---

Review the pending changes (staged and unstaged) against `CLAUDE.md` and `.claude/rules/`. Use `git status`, `git diff` and `git diff --cached`.

## Checklist

1. **Layers** (`architecture.md`)
   - `domain/` importing `android.*`, `androidx.*`, Room, WorkManager or Compose?
   - Any import of `com.emm.mybest.data.*` outside `data/`, `di/` and tests?
   - A ViewModel touching a DAO or Room directly?
   - New binding missing from `di/AppModule.kt`?
2. **MVI**
   - `State` (all `val`), `Intent`, `Effect` each in its own file, one `onIntent` entry point, effects not stored in state?
3. **Kotlin style** (`kotlin-style.md`)
   - Any `//` comment that is not KDoc or a ticketed `TODO`?
   - Functions over 20 lines or over 3 parameters? Nested `fun`?
   - More than one public top-level type in a file?
   - `!!`, `lateinit`, `GlobalScope`, `runBlocking`, magic numbers, hardcoded strings?
   - Missing trailing commas, or a redundant `@OptIn` for a project-level opt-in?
4. **UI** (`ui.md`)
   - Logic inside a composable? ViewModel passed into a presentational composable?
   - Hardcoded colors or dimensions instead of Atelier tokens? Raw Material3 where a `ui/components` equivalent exists?
   - Missing `@Preview` on a new screen-level composable?
5. **Room** (`room.md`)
   - Entity or DAO change without a regenerated file under `app/schemas/` and a version bump?
6. **Tests** (`testing.md`)
   - New behavior without a test? A flow loop without a clock-independent exit?
   - Files under `src/**/java/` instead of `kotlin/`?
7. **Hygiene**
   - Sensitive files in the diff (`keystore.properties`, `local.properties`, `key/`)?
   - Commit message not conventional, or carrying `Co-Authored-By`?

## Output

For each violation: `file:line` + rule + suggestion on a single line. If everything is clean, reply **"clean — ready to commit"**. Do not edit files in this turn.

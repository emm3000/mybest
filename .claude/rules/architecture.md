---
paths:
  - "app/src/**/kotlin/**"
---

# Architecture rules

Single module `app`, package root `com.emm.mybest`, source set `kotlin/` (never `java/`). Layer model: `docs/ARCHITECTURE.md`.

## Layers

| Layer | Holds | May import |
|---|---|---|
| `features/<name>/` | Compose screens, per-feature ViewModel, `State` / `Intent` / `Effect` | `domain`, `core`, `ui` |
| `domain/` | models, repository contracts, use cases, validation, reminder and media contracts | Kotlin stdlib, `core` |
| `data/` | Room entities and DAOs, repository implementations, mappers, reminder workers | `domain`, `core` |
| `di/AppModule.kt` | the single Koin module | everything |
| `core/` | `datetime/`, `navigation/` | nothing above it |
| `ui/components/`, `ui/theme/` | shared UI | `core` |

- `domain/` is pure Kotlin: no `android.*`, `androidx.*`, Room, WorkManager, Compose. If it needs to reach outward, declare an interface in `domain/` and implement it in `data/`.
- Only `data/`, `di/` and tests may import `com.emm.mybest.data.*`. Detekt `ForbiddenImport` enforces it. Put cross-cutting constants and types in `core/`, never in `data/`. *Why: a shared constant placed in `data/reminder/` broke the gate.*
- ViewModels talk to use cases or repository contracts, never to DAOs or Room directly.

## MVI per feature

- One `State` (immutable `data class`, all `val`), one `Intent` sealed type, one `Effect` sealed type.
- The ViewModel exposes `StateFlow<State>` and `SharedFlow<Effect>`, plus a single `onIntent(intent)` entry point.
- One-shot events (navigation, snackbars) are effects, never stored in state.
- Time-dependent code receives a `Clock` through the constructor (default `Clock.System`); never call `Clock.System.now()` inside logic, so tests can fake time.

## Dependency injection

- Every new binding is registered in `di/AppModule.kt`. Composables obtain ViewModels through `koinViewModel()`.
- Dependencies arrive by constructor. Never instantiate a collaborator inside a class (`= MyService()`).

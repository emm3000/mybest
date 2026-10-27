# MyBest - Architecture Overview

## 1. App Style
- Android app with Jetpack Compose UI.
- Clean architecture with separated layers: UI, domain, data.
- MVI-like flow in features: State + Intent + Effect.

## 2. Layer Boundaries
- UI (`features/*` + `ui/components`): rendering and user events.
- Domain (`domain/*`): models, use cases, contracts.
- Data (`data/*`): Room DAOs/entities, repository implementations, mappers, reminder workers, notification capture service.
- DI (`di/AppModule.kt`): the single Koin module; ViewModels via `koinViewModel()`.

## 3. Core Runtime Flows
- ViewModel receives Intent.
- ViewModel updates StateFlow for UI state.
- ViewModel emits one-shot effects via SharedFlow.
- UseCase orchestrates domain rules and repository calls.
- Repository persists/reads through Room or platform services.

## 4. Main Modules
Feature scope and acceptance live in [`PRD.md`](PRD.md); per-feature behavior in [`FEATURES.md`](FEATURES.md). Each module is `features/<name>/presentation/`; most have a use-case package under `domain/usecase/<name>/`, while `settings` and backup use cases sit directly in `domain/usecase/`.

| Module | Role |
|---|---|
| `home` | Home tab: routes to the daily checks, pre-outing, check-in or weekly summary (`ResolveHomeDestinationUseCase`) |
| `onboarding` | First-run setup flow, gated at the app root |
| `preouting` | Limits, Pre-outing checklist, planned Outing or Won night |
| `checkin` | Next-day Check-in and unplanned Outing |
| `debts` | Debts and Installments |
| `goal` | Purchase goal, contributions, Not spent (estimated) |
| `weight` | Weigh-in and trend |
| `photo` | Progress photos (trunk, face), viewer, comparison |
| `summary` | Weekly summary |
| `history` | Calendar and day detail |
| `capture` | Notification capture settings and captured list |
| `settings` | Setup, reminders, backup and restore |

Navigation is Navigation 3 (`core/navigation/`): typed `Screen` keys, four top-level tabs (Home, Summary, History, Settings) and pushed screens on top. `core/` also holds `datetime/` (app day, 05:00 cutoff, ADR 0003), `money/`, `flow/`, `coroutines/`, `notification/` and `permission/`.

## 5. Persistence
Room database `my_best_db`, version 10 (`AppDatabase.SCHEMA_VERSION`). Schema JSON per version is committed under `app/schemas/`. Tables (the entities of `data/AppDatabase.kt`):

| Table                   | Description                                                   |
|-------------------------|---------------------------------------------------------------|
| `daily_weight`          | Weigh-in records                                              |
| `progress_photo`        | Progress photos with `PhotoType` (TRUNK / FACE)               |
| `user_setup`            | The single Setup row                                          |
| `daily_check`           | Daily checks per app day and type                             |
| `outing`                | Planned and unplanned Outings with Limits and Check-in data   |
| `won_night`             | Won nights                                                    |
| `no_outing_answer`      | "I stayed in" answers to the unplanned-outing question        |
| `debt`                  | Debts                                                         |
| `debt_payment`          | Installments marked paid                                      |
| `purchase_goal`         | The Purchase goal                                             |
| `goal_contribution`     | Real contributions to the Purchase goal                       |
| `capture_app`           | Apps selected for notification capture                        |
| `captured_notification` | Raw captured notifications (ADR 0002)                         |

Migration rule (PRD section 12, `.claude/rules/room.md`):
- Version 10 is the v1 baseline (`AppDatabase.V1_SCHEMA_VERSION`). Only databases below it are recreated; the tables of the dropped plans went that way (ADR 0004).
- From the baseline on, every structural change bumps `SCHEMA_VERSION`, adds an explicit `Migration` to `ALL_MIGRATIONS` in `data/RoomMigrations.kt`, ships the regenerated schema JSON and a `MigrationTestHelper` test in `AppDatabaseMigrationsTest`. No destructive fallback.
- Data lives only on the device (ADR 0001), so a wipe is unrecoverable. Backup export and restore touch the database file and must keep working after any schema change.

## 6. Quality Rules
- Prefer early return for validation and guard clauses.
- Keep conditional depth low and avoid callback chains in composables.
- Keep business logic in use cases and viewmodels, not in UI widgets.

## 7. Visual Design
The design language is Atelier Dark: `AtelierTheme` in `ui/theme/Theme.kt`, a single dark color scheme with tokens in `AtelierColors.kt`, `AtelierFonts.kt`, `Shape.kt` and `Type.kt`. Contributor rules are in `.claude/rules/ui.md`:
- Use theme tokens and shared components in `ui/components/` and `ui/components/atelier/` (`MicroLabel`, `DisplayNumber`, `Hairline`) before raw Material3.
- No hardcoded colors, dimensions or user-visible strings in feature screens.
- Container composables hold the ViewModel; presentational composables take state and callbacks only.

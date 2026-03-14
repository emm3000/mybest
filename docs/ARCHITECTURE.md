# MyBest - Architecture Overview

## 1. App Style
- Android app with Jetpack Compose UI.
- Clean architecture with separated layers: UI, domain, data.
- MVI-like flow in features: State + Intent + Effect.

## 2. Layer Boundaries
- UI (`features/*` + `ui/components`): rendering and user events.
- Domain (`domain/*`): models, use cases, contracts.
- Data (`data/*`): Room DAOs/entities, repository implementations, workers.
- DI (`di/AppModule.kt`): dependency wiring.

## 3. Core Runtime Flows
- ViewModel receives Intent.
- ViewModel updates StateFlow for UI state.
- ViewModel emits one-shot effects via SharedFlow.
- UseCase orchestrates domain rules and repository calls.
- Repository persists/reads through Room or platform services.

## 4. Main Modules
- Habits: create/update/toggle daily habits.
- Photos: add photos, classify by type, compare before/after.
- Weight: register and read progress.
- History/Timeline/Insights: aggregate and visualize progress.
- Settings: reminders and backup/restore flows.

## 5. Quality Rules
- Prefer early return for validation and guard clauses.
- Keep conditional depth low and avoid callback chains in composables.
- Keep business logic in use cases and viewmodels, not in UI widgets.

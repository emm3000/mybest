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
- Home: daily compliance toggles (4 meals + 1 exercise), plan display, primary CTAs.
- Diet Plan: weekly meal plan editor (7 days × 4 meal slots).
- Exercise Plan: weekly exercise routine editor (7 days × 1 routine).
- Photos: add progress photos (TRUNK/FACE), timeline, before/after comparison.
- Weight: register daily weight values.
- History/Timeline/Insights: aggregate and visualize progress.
- Settings: weight reminder, diet/exercise plan access, backup/restore flows.

## 5. Persistence
Room database v4 (`my_best_db`). Active tables:

| Table                   | Description                                      |
|-------------------------|--------------------------------------------------|
| `daily_weight`          | One row per day with weight value                |
| `progress_photo`        | Progress photos with `PhotoType` (TRUNK / FACE)  |
| `meal_plan_entries`     | Weekly recurring meal plan (day × meal slot)     |
| `exercise_plan_entries` | Weekly recurring exercise routines (day × routine) |
| `meal_compliance`       | Daily meal toggle state per `MealType`           |
| `exercise_compliance`   | Daily exercise toggle state                      |

Migrations:
- `1 → 2`: AutoMigration (Room).
- `2 → 3`: Manual — removes habit tables (`habits`, `habit_records`, `daily_habit`) and strips habit FK columns from `progress_photo` and `daily_weight`.
- `3 → 4`: Manual — unifies legacy `ABDOMEN`/`BODY` photo types into `TRUNK` and removes meal-photo entries.

## 6. Quality Rules
- Prefer early return for validation and guard clauses.
- Keep conditional depth low and avoid callback chains in composables.
- Keep business logic in use cases and viewmodels, not in UI widgets.

## 7. Visual Design
The UI follows the Starlink Mono design system (see `docs/components/DESIGN_SYSTEM.md`).
Key constraints for contributors:
- Dark-first: do not branch on `isSystemInDarkTheme()`; the theme is forced dark.
- Use `MaterialTheme.colorScheme.*` and `MaterialTheme.typography.*` tokens; never hardcode hex colors or sp/dp values for color or text styling.
- Section labels and chips render `string.uppercase()` at the call site with `StarlinkTextStyles.sectionLabel` / `.chipLabel`. Do not uppercase resource strings.
- Hero metrics (weight, deltas, counts) use `displayLarge` / `displayMedium` / `displaySmall` with an uppercase label above.

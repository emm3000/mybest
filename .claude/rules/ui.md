---
paths:
  - "app/src/main/kotlin/com/emm/mybest/ui/**"
  - "app/src/main/kotlin/com/emm/mybest/features/**"
---

# UI rules

## Structure

- Container / presentational split: a container composable obtains the ViewModel (`koinViewModel()`), collects `State`, collects `Effect`, and passes plain values and lambdas down. Presentational composables take state and callbacks only, never a ViewModel.
- No business logic in composables or activities. Derivations, formatting rules and validation live in use cases or the ViewModel.
- State hoisting: state goes up, events come down as lambdas (`onClick: () -> Unit`). Every composable that draws takes `modifier: Modifier = Modifier`.
- Use `remember` / `rememberSaveable` where state must survive recomposition; never build heavy objects during composition.

## Theme and components

- The design language is Atelier Dark: `AtelierTheme` (`ui/theme/Theme.kt`), tokens in `AtelierColors.kt`, `AtelierFonts.kt`, `Shape.kt`, `Type.kt`.
- Use theme tokens and `ui/components/` (and `ui/components/atelier/` primitives such as `MicroLabel`, `DisplayNumber`, `Hairline`) before raw Material3. Hardcoded colors, dimensions and strings are not allowed in feature screens.
- A component used by two or more features moves to `ui/components/`. Changing a shared component or a theme token affects every screen: visually check each affected screen.

## Previews

Every public screen-level composable has a `@Preview` (excluded from coverage by design).

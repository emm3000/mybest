# Custom Time Picker

## Summary

`ReminderTimePickerDialog` is now a custom Compose component that replaces the Material 3 `TimePicker`.

Main goals:

- Keep the existing app contract unchanged.
- Match the app's shadcn-inspired visual system.
- Provide predictable centered snapping while scrolling.
- Stay reusable for future time-based inputs.

Source files:

- `app/src/main/kotlin/com/emm/mybest/ui/components/ReminderTimePickerDialog.kt`
- `app/src/androidTest/java/com/emm/mybest/ui/components/ReminderTimePickerDialogTest.kt`

## Why It Was Rebuilt

The previous implementation wrapped `androidx.compose.material3.TimePicker`, which did not fit the app's custom component direction.

The app already uses custom primitives such as:

- `HButton`
- `HInput`
- `HSelect`
- `HAlertDialog`

This picker was rebuilt to follow the same visual language:

- neutral shadcn-like surface and border tokens
- compact rounded shapes
- external labels
- explicit selected state
- custom action footer

## Public API

The external contract was preserved to avoid touching feature state or domain logic.

```kotlin
@Composable
fun ReminderTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit,
)
```

Internally, the implementation delegates to `HTimePickerDialog`.

## How It Was Built

### 1. Dialog shell

The component uses `BasicAlertDialog` plus a custom `Surface` container instead of Material's time picker UI.

Key styling choices:

- `MaterialTheme.shapes.extraLarge` for the dialog shell
- `outlineVariant` for the border
- `surface` and `surfaceContainerLow/High` for layered surfaces
- `HButton` for actions

### 2. Header

The header has two responsibilities:

- explain the interaction model
- show the currently selected time in a large preview block

This helps the user verify the active value immediately while scrolling.

### 3. Picker columns

Two independent vertical columns are rendered:

- `Hora`: `00..23`
- `Minutos`: `00..59`

Each row is a custom clickable cell with:

- centered text
- stronger typography for the selected row
- semantic selected state
- content descriptions for UI testing and accessibility

### 4. Scroll and snapping

The first version used manual spacer items plus custom settling logic. That produced visible misalignment after scroll.

The final version uses Compose Foundation snapping:

- `rememberLazyListState(...)`
- `rememberSnapFlingBehavior(...)`
- `SnapPosition.Center`
- vertical `contentPadding` to align first and last items with the center band

Selected value detection is derived from the item whose center is closest to the viewport center.

This approach is more robust than manually guessing the final index from `firstVisibleItemIndex` and `firstVisibleItemScrollOffset`.

### 5. State model

The picker keeps local transient state only:

- `selectedHour`
- `selectedMinute`

This state is initialized from the incoming props and only commits changes through `onConfirm`.

That keeps the picker UI isolated from `ViewModel` logic and avoids unnecessary churn in the feature layer.

## What Was Used

Main Compose/Foundation APIs:

- `BasicAlertDialog`
- `LazyColumn`
- `rememberLazyListState`
- `rememberSnapFlingBehavior`
- `SnapPosition.Center`
- `snapshotFlow`
- `rememberSaveable`
- `animateColorAsState`

Theme/system dependencies reused from the app:

- `MaterialTheme.colorScheme`
- `MaterialTheme.shapes`
- `MaterialTheme.typography`
- `HButton`
- existing shadcn-oriented theme mapping in `ui/theme`

## What Was Validated

### Build validation

The component was validated with:

- `./gradlew :app:compileDebugKotlin`
- `./gradlew :app:compileDebugAndroidTestKotlin`
- `./gradlew :app:testDebugUnitTest`

### Device validation

Instrumented Compose tests were executed on an Android emulator:

- `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.emm.mybest.ui.components.ReminderTimePickerDialogTest`

### UI test coverage

Current tests verify:

- initial selected time is rendered correctly
- hour and minute can be changed
- confirmation returns the selected values
- cancel triggers dismiss

### Manual behavior checked during iteration

The implementation was specifically adjusted to fix:

- selected row stopping off-center after scroll
- mismatch between highlighted band and actual selected item
- inconsistent settling when tapping a row versus flinging the list

## Integration Notes

No feature-facing contract was changed.

Current usages remain in:

- `AddHabitScreen`
- `ReminderSettingsScreen`

Because `ReminderTimePickerDialog` kept the same signature, `ViewModel`, intent, and persistence logic did not require changes.

## Future Improvements

Potential next steps:

- Add optional `12h / AM-PM` mode.
- Add haptic feedback when the centered value changes.
- Add disabled ranges or minute steps such as `5` or `15`.
- Extract an inline non-dialog version for forms or bottom sheets.
- Add gradient masks at the top and bottom of the column for stronger depth cues.
- Add screenshot/regression coverage for light and dark mode.
- Expose a dedicated state holder if future screens need controlled mode.

## Tradeoffs

Current tradeoffs are intentional:

- Minutes use all `00..59` values, which is flexible but denser than stepped intervals.
- Selection emphasis is visual plus snapping, not a full wheel picker implementation.
- The component is optimized for reminder selection, not yet for every possible time-input workflow.

## Recommendation

If this component keeps growing, promote it from a single screen utility to a documented design-system primitive:

- `HTimePickerDialog`
- optional `HTimePickerColumn`
- optional inline `HTimePickerField`

That would make future schedule/reminder features cheaper to build and visually consistent by default.

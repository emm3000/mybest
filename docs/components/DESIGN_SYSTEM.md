# MyBest — Design System: Atelier Dark

> Dark-only, editorial. Bone text on near-black, serif display numbers, mono micro labels, hairline rules, square corners, one sage accent for "done".

Rules for writing UI against this system live in [`.claude/rules/ui.md`](../../.claude/rules/ui.md). Layer boundaries live in [`ARCHITECTURE.md`](../ARCHITECTURE.md).

---

## 1. Principles

- **Dark-only**: `AtelierTheme` (`ui/theme/Theme.kt`) always applies one `darkColorScheme` (`atelierScheme`). There is no light scheme.
- **Bone on ink**: text is bone (`AtelierInk`) on `AtelierBackground`; hierarchy comes from bone opacity steps, not from extra hues.
- **One accent**: sage (`AtelierDone`) is reserved for the "done" state. `AtelierWarm` is a single soft hint.
- **Hairlines, no shadows**: dividers are 1 dp rules at 8% bone (`Hairline`, `AtelierHairline`).
- **Hard edges**: every Material 3 shape radius is 0 dp (`AtelierShapes`).
- **Editorial type**: serif (Instrument Serif) for numbers and headlines, sans (Geist) for copy, mono (Geist Mono) for tracked uppercase micro labels.

---

## 2. Color tokens

Source of truth: the color file in `app/src/main/kotlin/com/emm/mybest/ui/theme/`. Material 3 role mapping: `atelierScheme` in `Theme.kt`.

| Token | Value | Use |
|---|---|---|
| `AtelierInk` | `0xFFEEEAE2` | Primary text (bone) |
| `AtelierInkSecondary` | `0x94EEEAE2` | Secondary text, 58% bone |
| `AtelierInkTertiary` | `0x57EEEAE2` | Tertiary text, inactive icon, 34% bone |
| `AtelierInkMuted` | `0x29EEEAE2` | Muted edge, disabled, 16% bone |
| `AtelierHairline` | `0x14EEEAE2` | Hairline rule, 8% bone |
| `AtelierBackground` | `0xFF0A0A0C` | Page background |
| `AtelierBackgroundSheet` | `0xFF101013` | Sheet surface, one step above the page |
| `AtelierBackgroundElevated` | `0xFF15151A` | Input fills, popovers |
| `AtelierDone` | `0xFFC8E6A8` | Sage accent, "done" state |
| `AtelierDoneDim` | `0x33C8E6A8` | Dim sage fill, 20% sage |
| `AtelierWarm` | `0xFFE8C190` | Warm hint, single recommendation tag |

`AtelierError` (`0xFFE85D5D`) is private to `Theme.kt`; it only feeds the `error` roles.

### Material 3 roles (`atelierScheme`)

| Role | Token |
|---|---|
| `background`, `surface`, `surfaceContainerLowest` | `AtelierBackground` |
| `onBackground`, `onSurface` | `AtelierInk` |
| `surfaceContainer`, `surfaceContainerLow` | `AtelierBackgroundSheet` |
| `surfaceVariant`, `surfaceContainerHigh`, `surfaceContainerHighest` | `AtelierBackgroundElevated` |
| `onSurfaceVariant`, `secondary` | `AtelierInkSecondary` |
| `primary`, `inversePrimary` | `AtelierDone` |
| `onPrimary`, `onSecondary`, `onTertiary`, `inverseOnSurface` | `AtelierBackground` |
| `primaryContainer` | `AtelierDoneDim` |
| `onPrimaryContainer`, `onSecondaryContainer`, `onTertiaryContainer`, `inverseSurface` | `AtelierInk` |
| `secondaryContainer`, `tertiaryContainer`, `errorContainer` | `AtelierBackgroundElevated` |
| `tertiary` | `AtelierWarm` |
| `error`, `onErrorContainer` | `AtelierError` |
| `onError` | `AtelierInk` |
| `outline` | `AtelierInkTertiary` |
| `outlineVariant` | `AtelierHairline` |
| `scrim` | `Color.Black` |

---

## 3. Typography

Fonts: the font file in `ui/theme/` defines three families.

| Family | Font | Weights |
|---|---|---|
| `AtelierSerifFamily` | Instrument Serif | Regular, Italic |
| `AtelierSansFamily` | Geist | Regular, Medium, SemiBold, Bold |
| `AtelierMonoFamily` | Geist Mono | Regular, Medium |

Scale: `AtelierTypography` in `Type.kt`, passed to `MaterialTheme` by `AtelierTheme`. The role-to-family/style table is in the KDoc at the top of that file; read it there instead of a copy here. In short: `display*` and `headline*` are serif, `title*`, `body*` and `labelLarge` are sans, `labelMedium` and `labelSmall` are mono.

---

## 4. Shapes

`AtelierShapes` (`Shape.kt`) sets `extraSmall`, `small`, `medium`, `large` and `extraLarge` to `RoundedCornerShape(0.dp)`. No component draws a rounded surface.

---

## 5. Components

Shared composables live in `app/src/main/kotlin/com/emm/mybest/ui/components/`; Atelier primitives live in `ui/components/atelier/`.

### `ui/components/`

| Composable | File | Purpose |
|---|---|---|
| `AtelierAppBar` | `AtelierAppBar.kt` | Top app bar |
| `HButton` | `Button.kt` | Button with `ButtonVariant` (`Default`, `Destructive`, `Outline`, `Secondary`, `Ghost`, `Link`), loading state and optional leading icon; a second overload exists in the same file |
| `HIconButton` | `IconButton.kt` | Icon-only button |
| `HInput` | `Input.kt` | Text field |
| `HSwitch` | `Switch.kt` | Toggle |
| `HAlertDialog` | `Dialog.kt` | Confirm/cancel dialog; `isDangerous` switches the confirm button to the destructive color |
| `HBottomSheet` | `BottomSheet.kt` | Modal bottom sheet |
| `HSnackbarHost`, `HSnackbar` | `Snackbar.kt` | Snackbar host and snackbar |
| `HBottomNavigationBar` | `HBottomNavigationBar.kt` | Four-tab bar (Home, Summary, History, Settings). All four labels share one font size: it starts at `labelSmall` and shrinks in 0.25 sp steps, down to 6 sp, until every label fits its cell on one line |
| `ReminderTimePickerDialog` | `ReminderTimePickerDialog.kt` | Reminder time picker, see [`TIME_PICKER.md`](TIME_PICKER.md) |

### `ui/components/atelier/`

| Composable | File | Purpose |
|---|---|---|
| `MicroLabel` | `MicroLabel.kt` | Geist Mono, all caps, 0.18 em tracking. Callers pass already-uppercase strings. `MicroLabelStyle` (`MicroLabelStyle.kt`) sets `tone`, `size` (default 10 sp) and `isSingleLine` (default `false`; when `true` the label stays on one line) |
| `DisplayNumber` | `DisplayNumber.kt` | Instrument Serif numeric display with tabular digits. `DisplayNumberStyle` (`DisplayNumberStyle.kt`) sets `fontSize`, `italic` and `color` |
| `Hairline` | `Hairline.kt` | 1 dp rule at 8% bone, with optional horizontal `inset` |
| `CompletionCheck` | `CompletionCheck.kt` | Done/not-done check box: sage fill with a tick when done, 1 dp hairline outline when not |
| `AtelierCaret` | `AtelierCaret.kt` | Decorative chevron, tertiary tint, no click handler |

`MicroLabelTone` (`MicroLabelTone.kt`) has four values:

| Tone | Color |
|---|---|
| `Default` | Secondary bone (58%) |
| `Dim` | Tertiary bone (34%) |
| `Done` | Sage, completion context only |
| `Warm` | Warm hint |

`PhotoTypeLabel.kt` holds `photoTypeLabel(type)`, a function that returns the label for a `PhotoType`. `FaceSilhouette.kt` and `TrunkSilhouette.kt` hold the photo silhouette drawings.

---

## 6. Usage rules

See [`.claude/rules/ui.md`](../../.claude/rules/ui.md): container/presentational split, state hoisting, theme tokens and shared components before raw Material 3, previews. A component used by two or more features moves to `ui/components/`.

---

## 7. Splash and system bars

Defined in `app/src/main/res/values/themes.xml` and `values-night/themes.xml`; colors in `values/colors.xml`.

- `Theme.MyBest`: window background is the `app_window_background` color (black); status bar and navigation bar are `#000000`; light status and navigation bar icons are off (`windowLightStatusBar` and `windowLightNavigationBar` are `false`). The base theme is `android:Theme.Material.Light.NoActionBar` in `values/` and `android:Theme.Material.NoActionBar` in `values-night/`.
- `Theme.MyBest.Starting`: splash theme (`Theme.SplashScreen.IconBackground`). Background is `splash_screen_background` (black); the animated icon is `ic_progress_foreground`; after the splash the app switches to `Theme.MyBest`.

# MyBest — Design System: Starlink Mono

> Dark-first, high-contrast, monochrome with a single cyan accent. Inspired by the visual language of Starlink's mobile app.

---

## 1. Principios

- **Dark-first**: la app fuerza tema oscuro siempre. `MyBestTheme` ignora `isSystemInDarkTheme()` y aplica únicamente `darkScheme`.
- **Mono**: blanco sobre negro como base (`#FAFAFA` sobre `#000000`); un único acento cian (`#00A3FF`) para énfasis.
- **Hairlines, no shadows**: bordes `1dp` con `outlineVariant` (`#1F1F1F`) en lugar de elevaciones (`shadowElevation = 0.dp` en todos los componentes).
- **Hard edges**: radios `2–10dp`. Sin `CircleShape` en superficies, sin "pill" cards.
- **Uppercase tracked labels**: secciones, chips y etiquetas de nav se renderizan en mayúsculas en el call site via `.uppercase()` con `StarlinkTextStyles.sectionLabel` / `.chipLabel`.
- **Métricas hero grandes**: `displayLarge` (56sp) / `displayMedium` (40sp) / `displaySmall` (32sp) sobre etiquetas pequeñas tracked (`labelSmall` a 1.8sp).

---

## 2. Color tokens

Fuente de verdad: `app/src/main/kotlin/com/emm/mybest/ui/theme/Color.kt` (bloque "Starlink Mono palette") y `Theme.kt` (`darkScheme`).

### Paleta Starlink Mono

| Token Kotlin         | Valor hex   | Descripción                                      |
|----------------------|-------------|--------------------------------------------------|
| `starlinkBlack`      | `#000000`   | Background puro                                  |
| `starlinkSurface`    | `#0A0A0A`   | Surface elevada nivel 1                          |
| `starlinkSurfaceHigh`| `#141414`   | Surface elevada nivel 2                          |
| `starlinkBorder`     | `#1F1F1F`   | Hairline divider (`outlineVariant`)              |
| `starlinkOutline`    | `#2A2A2A`   | Borde de input/card (`outline`)                  |
| `starlinkMuted`      | `#8A8A8A`   | Texto secundario (`onSurfaceVariant`)            |
| `starlinkOnSurface`  | `#FAFAFA`   | Texto primario sobre negro (`onSurface`)         |
| `starlinkAccent`     | `#00A3FF`   | Acento cian Starlink (`tertiary`)                |
| `starlinkAccentDim`  | `#0077B6`   | Acento atenuado / hover (`tertiaryContainer`)    |
| `starlinkError`      | `#FF3B30`   | Error iOS red — alto contraste sobre negro puro  |

### Mapeo a Material 3 dark scheme

| Rol M3                    | Token Starlink         | Uso principal                                       |
|---------------------------|------------------------|-----------------------------------------------------|
| `primary`                 | `starlinkOnSurface`    | Botones primarios (texto blanco sobre fondo negro)  |
| `onPrimary`               | `starlinkBlack`        | Texto sobre botones primarios                       |
| `primaryContainer`        | `starlinkSurfaceHigh`  | Contenedor de acción principal                      |
| `secondary`               | `starlinkMuted`        | Botones secundarios                                 |
| `onSecondary`             | `starlinkBlack`        | Texto sobre secundario                              |
| `secondaryContainer`      | `starlinkSurface`      | Contenedor secundario                               |
| `onSecondaryContainer`    | `starlinkOnSurface`    | Texto sobre contenedor secundario                   |
| `tertiary`                | `starlinkAccent`       | Acento cian: métricas positivas, dot success, CTA   |
| `tertiaryContainer`       | `starlinkAccentDim`    | Contenedor de acento                                |
| `error`                   | `starlinkError`        | Error; barra de `HAlert.Destructive`; dot destructive en `HBadge` |
| `background`              | `starlinkBlack`        | Fondo de pantalla                                   |
| `surface`                 | `starlinkBlack`        | Superficie de componentes                           |
| `onSurface`               | `starlinkOnSurface`    | Texto/iconos sobre surface                          |
| `surfaceVariant`          | `starlinkSurface`      | Variante de superficie                              |
| `onSurfaceVariant`        | `starlinkMuted`        | Texto secundario / labels de chips                  |
| `outline`                 | `starlinkOutline`      | Borde de `HInput` en reposo                         |
| `outlineVariant`          | `starlinkBorder`       | Hairline de `HSeparator`, `HCard`, chips, alerts    |
| `surfaceContainerLowest`  | `starlinkBlack`        | —                                                   |
| `surfaceContainerLow`     | `starlinkSurface`      | —                                                   |
| `surfaceContainer`        | `starlinkSurface`      | —                                                   |
| `surfaceContainerHigh`    | `starlinkSurfaceHigh`  | —                                                   |
| `surfaceContainerHighest` | `starlinkOutline`      | Fondo de `HCard.Filled`                             |

> **Nota**: La `lightScheme` existe en `Theme.kt` pero está anotada con `@Suppress("UnusedPrivateProperty")` y no se aplica en runtime. No se usa en ningún path de ejecución.

---

## 3. Typography

Fuente base: **Geist** (Regular / Medium / SemiBold / Bold), cargada como `AppFontFamily`.
Fuente de verdad: `app/src/main/kotlin/com/emm/mybest/ui/theme/Type.kt` y `TextStyles.kt`.

### Escala Material 3

| Token M3          | Tamaño   | Peso       | Line height | Letter spacing | Uso en MyBest                                        |
|-------------------|----------|------------|-------------|----------------|------------------------------------------------------|
| `displayLarge`    | 56sp     | Bold       | 64sp        | −1.5sp         | Métricas hero: peso actual, delta grande             |
| `displayMedium`   | 40sp     | Bold       | 48sp        | −1.0sp         | Métricas secundarias: ratio de cumplimiento, delta   |
| `displaySmall`    | 32sp     | SemiBold   | 40sp        | −0.5sp         | Métricas auxiliares; contador de paginación          |
| `headlineLarge`   | 32sp     | ExtraBold  | 40sp        | −0.5sp         | Reservado                                            |
| `headlineMedium`  | 28sp     | Bold       | 36sp        | −0.25sp        | Reservado                                            |
| `headlineSmall`   | 24sp     | SemiBold   | 32sp        | 0sp            | Título de tarjeta en CardDetail                      |
| `titleLarge`      | 20sp     | SemiBold   | 28sp        | 0sp            | TopAppBar, sección principal                         |
| `titleMedium`     | 16sp     | Medium     | 24sp        | 0sp            | Subtítulos de sección                                |
| `titleSmall`      | 14sp     | Medium     | 20sp        | 0sp            | Labels de sección (SectionCard)                      |
| `bodyLarge`       | 16sp     | Normal     | 26sp        | 0sp            | Cuerpo principal de flashcard                        |
| `bodyMedium`      | 14sp     | Normal     | 22sp        | 0sp            | Textos secundarios / `onSurfaceVariant`              |
| `bodySmall`       | 12sp     | Normal     | 18sp        | 0sp            | Helper / supporting text                             |
| `labelLarge`      | 14sp     | Medium     | 20sp        | 0sp            | Texto de botones                                     |
| `labelMedium`     | 12sp     | Medium     | 16sp        | 1.2sp          | Valor numérico en `HStatChip`; badges; chips         |
| `labelSmall`      | 10sp     | Medium     | 14sp        | 1.5sp          | Micro-labels; fechas de review                       |

### Estilos Starlink adicionales (`StarlinkTextStyles`)

| Propiedad                   | Base            | Letter spacing final | Uso                                              |
|-----------------------------|-----------------|----------------------|--------------------------------------------------|
| `StarlinkTextStyles.sectionLabel` | `labelSmall`  | 1.8sp          | Etiquetas de sección uppercase (encima de métricas hero) |
| `StarlinkTextStyles.chipLabel`    | `labelMedium` | 1.4sp          | Etiqueta uppercase en chips y nav items          |

**Extensión `uppercaseTracked()`**: añade `+0.4sp` al `letterSpacing` existente de cualquier `TextStyle`. No requiere `@Composable`; es Kotlin puro.

> **Regla de call site**: la transformación a mayúsculas se realiza siempre en el call site con `.uppercase()`. Los `strings.xml` permanecen en minúsculas para preservar el casing correcto en datos localizados dinámicos (números, unidades).

---

## 4. Shapes & Elevation

Fuente de verdad: `app/src/main/kotlin/com/emm/mybest/ui/theme/Shape.kt`.

### Radios

| Token M3        | Radio  | Usos típicos                                  |
|-----------------|--------|-----------------------------------------------|
| `extraSmall`    | 2dp    | Elementos mínimos                             |
| `small`         | 4dp    | Chips pequeños                                |
| `medium`        | 6dp    | Filtros, inputs                               |
| `large`         | 8dp    | Cards, alerts                                 |
| `extraLarge`    | 10dp   | Modales, bottom sheets                        |


### Regla de elevación

**`shadowElevation = 0.dp` en todos los componentes.** El depth visual se obtiene exclusivamente con hairlines `1dp` (`outlineVariant` = `#1F1F1F`). No hay tonal elevation, ni `tonalElevation` efectivo. El `HCard.Elevated` tenía `1dp` antes del rebrand; la Fase 5 (`7387d38`) lo redujo a `0dp`.

---

## 5. Componentes

Todos los componentes viven en `app/src/main/kotlin/com/emm/mybest/ui/components/`.

### HBadge (`Badge.kt`)
Etiqueta de estado pequeña. Variantes: `Default`, `Secondary`, `Destructive`, `Outline`, `Success`.
- Superficie: `color = cs.surface`, `contentColor = cs.onSurface`.
- Borde hairline: `BorderStroke(1.dp, cs.outlineVariant)`.
- Dot indicator circular (`CircleShape`, 6dp): `onSurface` para Default/Secondary/Outline, `cs.tertiary` (cian) para Success, `cs.error` (iOS red) para Destructive.
- Label: `StarlinkTextStyles.chipLabel` en uppercase.

### HStatChip (`StatChip.kt`)
Chip de métrica con label superior y valor numérico. Variantes: `Neutral`, `Primary`, `Secondary`, `Tertiary`, `Success`, `Destructive`.
- Borde `1dp outlineVariant`; forma `CircleShape` (versión compacta) o `RoundedCornerShape`.
- Label uppercase con `labelLarge`; valor con color según variante: `cs.tertiary` para Tertiary/Success, `cs.error` para Destructive, `cs.onSurface` para el resto.
- El label se renderiza con `.uppercase()` en el call site.

### HFilterChip (`FilterChip.kt`)
Chip de filtro seleccionable. Estado: `selected: Boolean`.
- **Seleccionado**: `containerColor = cs.onSurface`, `contentColor = cs.surface`, sin borde explícito.
- **No seleccionado**: `containerColor = Color.Transparent`, `contentColor = cs.onSurface`, `BorderStroke(1.dp, cs.outlineVariant)`.
- Label uppercase via `.uppercase()` en call site; `StarlinkTextStyles.chipLabel`.
- `FILTER_CHIP_DISABLED_ALPHA = 0.5f`.

### HAlert (`Alert.kt`)
Callout informativo con barra lateral de color. Variantes: `Default`, `Destructive`, `Warning`, `Success`.
- Superficie `cs.surface`, borde `1dp outlineVariant`.
- Barra de 2dp en el extremo izquierdo cuyo color indica la variante:
  - Default → `cs.onSurfaceVariant` (gris)
  - Destructive → `cs.error` (iOS red)
  - Warning → `cs.onSurface` (blanco; mono, sin amarillo)
  - Success → `cs.tertiary` (cian)
- Icono opcional que hereda el `barColor`.

### HCard (`Card.kt`)
Card con slots opcionales: `HCardHeader`, `HCardContent`, `HCardFooter`. Variantes: `Elevated`, `Filled`, `Outlined`.
- `Elevated`: `surface` + `0dp shadow` + `1dp outlineVariant`.
- `Filled`: `surfaceContainerHighest` (`starlinkOutline` = `#2A2A2A`) + sin borde.
- `Outlined`: `surface` + `1dp outlineVariant`.
- `cornerRadius` default `8dp` (igual a `Shapes.large`).

### HButton (`Button.kt`)
Botón de acción. Variantes: `Default`, `Destructive`, `Outline`, `Secondary`, `Ghost`, `Link`.
- `shadowElevation = 0.dp` en todas las variantes.
- `Outline`: `Color.Transparent` + `1dp outlineVariant`.
- Sin `uppercaseTracked`; el texto de botón usa `labelLarge` (0sp letter spacing).

### HTopBar (`TopBar.kt`)
Barra superior de pantalla.
- `shadowElevation = 0.dp`.
- Título renderizado con `.uppercase()`, `titleSmall` + `letterSpacing = 1.2.sp`.
- `HSeparator()` al fondo del componente como hairline de separación.

### HBottomNavigationBar (`HBottomNavigationBar.kt`)
Barra de navegación inferior.
- `HSeparator(color = cs.outlineVariant)` encima de la barra en lugar de elevación.
- Labels del nav item: `stringResource(id = item.labelResId).uppercase()` con `StarlinkTextStyles.chipLabel`.
- Indicador de ítem activo: dot circular (`CircleShape`, `cs.onSurface`) debajo del icono, no un pill.

### HSeparator (`Separator.kt`)
Divisor horizontal. `HorizontalDivider` de `1dp` en `cs.outlineVariant` por defecto. Usado en `HTopBar` y `HBottomNavigationBar`.

### HInput (`Input.kt`)
Campo de texto basado en `BasicTextField`.
- Borde en reposo: `outlineVariant` (`#1F1F1F`).
- Borde con foco: `outline` (`#2A2A2A`) animado con `tween`.
- Borde con error: `cs.error`.
- Estilo de texto: `bodyMedium`; placeholder: `onSurfaceVariant` al 60% alfa.

### HProgressRing (`ProgressRing.kt`)
Anillo de progreso circular dibujado en `Canvas`.
- Track: `cs.tertiary.copy(alpha = 0.16f)` (`starlinkAccent` al 16%).
- Arco de progreso: `cs.tertiary` (`starlinkAccent`, cian `#00A3FF`).
- `StrokeCap.Round`; `strokeWidth` default `8dp`.
- Animación de `300ms` en la fracción de progreso.

---

## 6. Reglas de uso

### Cuándo usar el acento cian (`starlinkAccent` / `cs.tertiary`)
- Máximo **1 elemento por pantalla** con acento cian.
- Reservado a: métricas de progreso positivo (compliance ratio, completados), dot de estado "Success" en `HBadge`, barra de `HAlert.Success`.
- No usar en texto de cuerpo, labels de sección ni decoración.

### Cuándo NO usar uppercase
- Números, unidades de medida (kg, lbs, %).
- Contenido localizado dinámico (nombres de hábitos, títulos de usuario).
- Strings en `strings.xml`: no añadir `textAllCaps` ni mayúsculas en recursos. La transformación es siempre `.uppercase()` en el call site de Compose.

### Status sin color
- Success y Warning se expresan con dots / iconos en mono (`cs.onSurface` o `cs.onSurfaceVariant`).
- `HAlert.Warning` usa `cs.onSurface` como barColor (blanco/gris), no amarillo.
- Solo `AlertVariant.Destructive` y `BadgeVariant.Destructive` usan color semántico: `cs.error` = `#FF3B30`.

### Nunca hardcodear
- No usar hex literales para colores. Siempre `MaterialTheme.colorScheme.*`.
- No hardcodear `sp` o `dp` para tamaños de tipografía. Siempre `MaterialTheme.typography.*`.
- No ramificar en `isSystemInDarkTheme()`; `MyBestTheme` fuerza dark.

---

## 7. Splash & System Bars

Fuente de verdad: `app/src/main/res/values/themes.xml`, `values-night/themes.xml`, `values/colors.xml`.

| Elemento                        | Valor                          |
|---------------------------------|--------------------------------|
| Splash background               | `@color/splash_screen_background` → `#000000` |
| Splash animated icon            | `@drawable/ic_progress_foreground` (línea blanca `#FAFAFA`) |
| Splash icon background          | `@color/ic_progress_background` → `#000000` |
| Window background               | `@color/app_window_background` → `#000000` |
| `statusBarColor`                | `#000000`                      |
| `navigationBarColor`            | `#000000`                      |
| `windowLightStatusBar`          | `false` (iconos claros)        |
| `windowLightNavigationBar`      | `false` (iconos claros)        |

El parent del tema de producción es `android.Theme.Material.Light.NoActionBar` (light) con overrides de barras; el tema noche usa `android.Theme.Material.NoActionBar` (dark). Ambos producen el mismo resultado visual: barras negras con iconos blancos.

Los vectores de drawables del splash (`ic_progress_foreground`) cambiaron de `#FFF8EE`/`#E8B269` (amber/warm) a `#FAFAFA` (mono blanco) en la Fase 6 (`27016a3`). Los launcher icons se mantuvieron sin cambios.

---

## 8. Decisiones tomadas

### Por qué iOS red (`#FF3B30`) en vez de coral (`#F87171`)
El shadcnDarkDestructive original era `#F87171` (Tailwind red-400, coral-pink). Sobre fondo `#000000` puro, ese tono coral se percibe como "muted" y casi playful. `#FF3B30` es el rojo de sistema iOS, diseñado específicamente para alta legibilidad sobre fondos oscuros y puros. Documentado en `Color.kt` y en el commit `f7131b9`.

### Por qué tertiary cian en vez de success green
En una paleta mono, introducir un verde funcional crearía un segundo color semántico además del error rojo. El cian `#00A3FF` cubre el rol de "énfasis positivo" sin añadir más cromáticas. Los tokens `shadcnDarkSuccess` (verde) permanecen en `Color.kt` como referencia del diseño anterior pero no están mapeados al `darkScheme` activo. Documentado en el comentario del bloque Starlink Mono en `Color.kt`.

### Por qué uppercase en call site y no en `strings.xml`
Aplicar `.uppercase()` en el call site de Compose permite que los mismos strings se reutilicen en contextos donde el uppercase no aplica (números, unidades, contenido dinámico del usuario). Si la casing estuviera en el recurso, los datos localizados podrían resultar incorrectos. Documentado en `TextStyles.kt` y en el commit `f36d68b`.

### Por qué `shadowElevation = 0.dp` en todos los componentes
El depth visual en un fondo negro puro no se transmite bien con sombras (producen un halo oscuro imperceptible). Los hairlines `1dp outlineVariant` son más legibles y corresponden al lenguaje visual de Starlink. Documentado en el commit `7387d38`.

### Por qué el nav indicator es un dot y no un pill
El pill de Android estándar introduce un color de fondo que rompe la paleta mono. Un dot circular `cs.onSurface` (blanco) debajo del icono activo preserva la austeridad del sistema sin perder señal visual de selección. Documentado en el commit `6bb8df0`.

### Por qué la light scheme se retiene pero no se aplica
`MyBestTheme` acepta `darkTheme: Boolean` para no romper la API de llamada, pero ignora su valor. La `lightScheme` se conserva como referencia arquitectónica del mapeo shadcn→M3, útil si en el futuro se decide soportar tema claro. Documentado en `Theme.kt`.

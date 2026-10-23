# MyBest — Feature Map

## 1. Home (Diario)
- Día actual + 5 toggles de cumplimiento (4 comidas + 1 ejercicio).
- Plan del día visible (descripción de cada comida + rutina) tomado del plan semanal.
- Hero metric: ratio cumplido (ej. 3/5).
- CTAs: registrar peso, tomar foto, editar plan de dieta, editar plan de ejercicio.

## 2. Plan de Dieta (semanal)
- 7 días × 4 slots de comida (`BREAKFAST`, `LUNCH`, `DINNER`, `SNACK`).
- Editor: tap en una comida → bottom sheet con descripción editable.
- Plantilla recurrente (no por semana específica).

## 3. Plan de Ejercicio (semanal)
- 7 días × 1 rutina por día (texto libre).
- Editor: tap en un día → bottom sheet con la rutina editable.

## 4. Peso
- Registro diario.
- Visible en Home como CTA "registrar peso".
- Recordatorio matinal configurable (ver §7).

## 5. Fotos de progreso
- Dos tipos: `TRUNK` (tronco) y `FACE` (cara).
- Capture desde cámara o galería.
- Timeline y comparación antes/después por tipo.

## 6. Insights
- Peso: delta total, peso inicial vs actual, gráfica.
- Contador de fotos.
- Una recomendación derivada (`ADJUST_WEIGHT_PLAN`, `ADD_PROGRESS_PHOTO`, `KEEP_ROUTINE`).

## 7. Recordatorio semanal de pesaje
- Configurable desde Settings con "Día de pesaje" y "Hora de pesaje" (`Setup.weighInDay`, `Setup.weighInReminderTime`; sin default forzado — hasta que el usuario elija ambos, no se agenda nada).
- Notificación local "Hoy te toca pesarte" cada 7 días, el día y la hora elegidos, con CTA → AddWeight.
- `SyncRemindersUseCase` agenda o cancela el trabajo al cambiar el setup y al abrir la app; borrar el día o la hora lo cancela.

## 8. Historial y Timeline
- Calendario con días que tienen registros (peso, fotos).
- Timeline de fotos de progreso por tipo.

## 9. Settings
- Recordatorios: antes de salir, check-in de la mañana, día y hora de pesaje, foto de progreso cada 4 semanas y cuotas por vencer. Las cuotas por vencer aún no agendan trabajo.
- Acceso al plan de dieta y al plan de ejercicio.
- Backup export/import (SAF).

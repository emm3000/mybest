# MyBest - Feature Map

What the code does today, one section per scope item of the v1 PRD. Rules and acceptance criteria live in [`PRD.md`](PRD.md); decisions live in [`adr/`](adr/). Terms follow [`../GLOSSARY.md`](../GLOSSARY.md). Layers and persistence: [`ARCHITECTURE.md`](ARCHITECTURE.md).

Paths below are relative to `app/src/main/kotlin/com/emm/mybest/`.

## Navigation and Home

- The root gate (`core/navigation/AppRoot.kt`) shows onboarding (`features/onboarding/`) until it is completed, then the app. Onboarding is not a PRD scope item; it walks through the welcome, daily checks, outings, money and weight steps.
- Four bottom tabs: Home, Summary, History, Settings (`ui/components/HBottomNavigationBar.kt`). Other screens are pushed on top (`core/navigation/Screen.kt`).
- The Home tab is routed by `domain/usecase/home/ResolveHomeDestinationUseCase.kt` to one of four destinations: the daily checks, the pre-outing screen, the check-in or the weekly summary. Home also offers shortcuts to the pre-outing screen, the check-in and the weekly summary (`features/home/`).
- Reminder notifications and launch actions open their target screen through `core/navigation/LaunchActionRule.kt`.

## 1. Setup

PRD: [section 1](PRD.md#1-setup). ADR: [0001](adr/0001-all-data-stays-on-device.md).

- One `Setup` model (`domain/models/Setup.kt`) held in `user_setup`; every field is unset until the user chooses it.
- Edited from Settings (`features/settings/`): target weight, monthly outing limit, typical outing cost, weekly targets, reminder times, weigh-in day, photo reminder switch. Debts, the purchase goal and notification capture open their own screens from Settings rows.

## 2. Daily checks

PRD: [section 2](PRD.md#2-daily-checks). ADR: [0003](adr/0003-app-day-ends-at-5am.md).

- Four checks (`domain/models/DailyCheckType.kt`): gym, diet, English, alcohol. Alcohol is inverted: done means the user drank.
- Shown and marked from Home with the week against the weekly targets (`features/home/`, `domain/usecase/dailycheck/`). Records are keyed by app day.

## 3. Pre-outing screen

PRD: [section 3](PRD.md#3-pre-outing-screen). ADR: [0003](adr/0003-app-day-ends-at-5am.md).

- `features/preouting/`: comparison rows (outings this month, nearest installment, purchase goal left, outings against the monthly limit), then the decision.
- Going out sets the Limits and the Pre-outing checklist and records a planned Outing (`PlanOutingUseCase`). Staying in records a Won night (`RecordWonNightUseCase`), allowed only on Friday and Saturday app days (`domain/models/Tonight.kt`).
- Estimated outing cost: `GetEstimatedOutingCostUseCase` in `domain/usecase/preouting/`.
- Reminders on Friday and Saturday at the chosen time (`data/reminder/PreOutingReminderSchedulerImpl.kt`).

## 4. Next-day check-in

PRD: [section 4](PRD.md#4-next-day-check-in). ADR: [0003](adr/0003-app-day-ends-at-5am.md).

- `features/checkin/`: for a pending Outing it records total spent, limits kept or exceeded and mood from 1 to 5. On Saturday and Sunday app days with no Outing and no stayed-in answer it asks whether the user went out, which records an unplanned Outing, or stayed in (`domain/usecase/checkin/ObserveCheckInTaskUseCase.kt`).
- A check-in reminder fires at the user-chosen time (`data/reminder/CheckInReminderSchedulerImpl.kt`).

## 5. Relapse and return

PRD: [section 5](PRD.md#5-relapse-and-return). ADR: [0005](adr/0005-return-time-instead-of-streaks.md).

- Relapse reasons are broke a limit, over the monthly limit, unplanned (`domain/models/RelapseReason.kt`).
- `domain/usecase/relapse/` computes relapses and Return time; they surface in the weekly summary and History. No resetting counter exists.

## 6. Money

PRD: [section 6](PRD.md#6-money).

- Debts (`features/debts/`, `domain/usecase/debts/`): add, edit, mark the Installment paid; one reminder per Debt a few days before the payment day (`domain/usecase/reminder/GetInstallmentRemindersUseCase.kt`).
- Purchase goal (`features/goal/`, `domain/usecase/goal/`): one item, progress from user-entered contributions only.
- Not spent (estimated): shown on the purchase goal screen, fed by Won nights (`domain/usecase/preouting/ObserveNotSpentThisMonthUseCase.kt`).

## 7. Body

PRD: [section 7](PRD.md#7-body).

- Weigh-in (`features/weight/`): weekly record, target weight and trend (`domain/usecase/weight/`). Weekly reminder on the chosen day and time (`data/reminder/WeightReminderSchedulerImpl.kt`).
- Progress photos (`features/photo/`): trunk and face, camera capture, viewer, replace from the viewer (same type, date and place in the timeline; `ReplacePhotoUseCase`), timeline and before/after comparison. Optional photo reminder, off by default, published by the weigh-in reminder work when the latest photo is 28 days old or more.

## 8. Weekly summary

PRD: [section 8](PRD.md#8-weekly-summary).

- `features/summary/` with `domain/usecase/summary/`: one screen for a Monday to Sunday app week with sections for outings, relapses and Return time, checks against targets, weigh-in trend and money. Reachable from the Summary tab, and opened by Home on Sunday evening.

## 9. Calendar

PRD: [section 9](PRD.md#9-calendar).

- `features/history/` (History tab): month pager with marks for checks, outings, relapses, won nights, weigh-ins and photos and a day detail (`domain/usecase/history/`).
- Bitácora: a "CALENDARIO" / "BITÁCORA" switch under the month hero picks the view, and the month pager and swipe work in both. The Bitácora lists one month newest app day first, grouped by app day: outings with their check-in or relapse, won nights, stayed-in answers, returns, weigh-ins, photos, paid installments (on the due date of their month) and purchase-goal contributions. Daily checks are left out. `GetBitacoraUseCase` merges the existing repositories, so there is no event table. A row opens that app day's detail.

## 10. Notification capture

PRD: [section 10](PRD.md#10-notification-capture-groundwork-for-v11). ADR: [0002](adr/0002-capture-notifications-now-parse-later.md), [0001](adr/0001-all-data-stays-on-device.md).

- `features/capture/` with `data/capture/NotificationCaptureService.kt`: pick apps from the installed ones, view captured notifications, delete all, turn capture off. Nothing is parsed.
- Group summaries are not stored, and a repost with the same notification key, title and text as the latest stored row of that key is not stored again. A changed title or text is stored as a new row; stored rows are never modified. Rows stored before schema 11 have no key.

## 11. Backup

PRD: [section 11](PRD.md#11-backup). ADR: [0001](adr/0001-all-data-stays-on-device.md).

- Export and restore of the database file from Settings (`domain/usecase/ExportDatabaseBackupUseCase.kt`, `domain/usecase/RestoreDatabaseBackupUseCase.kt`, `data/BackupRepositoryImpl.kt`). A backup from another schema version is refused; a successful restore restarts the app; a restore that fails after closing the database keeps the current data and also restarts the app.

## 12. Data safety after v1

PRD: [section 12](PRD.md#12-data-safety-after-v1). ADR: [0004](adr/0004-drop-diet-and-exercise-plans.md).

- Migration policy is in [`ARCHITECTURE.md`](ARCHITECTURE.md#5-persistence) and `.claude/rules/room.md`.

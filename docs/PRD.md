# MyBest v1 — Win the weekend

Status: accepted (2026-10-23). Supersedes the diet/exercise scope in `docs/FEATURES.md`; that file is rewritten when v1 ships.

## Problem

The owner works a regular week but goes out to a night club on Friday or Saturday. The night follows a chain: going out → drinking → spending with no control (Yape, Plin, credit cards) → next-day regret about money and debts → diet and gym lost for the following days → "start again on Monday".

The strongest felt pain is **money regret**: what could have been bought or paid instead of one night. Body goals (weight, gym), English study for a better-paid job, and alcohol control all depend on breaking the same chain.

## Goal

Break the weekend chain using money as the motivator, and make returning after a relapse fast instead of punishing it. Gym, diet, English and alcohol are tracked as one-tap daily checks.

## Users

One user, the owner. Single device, Android, Spanish-speaking (Peru), currency soles (PEN).

## Principles

- Logging any item takes at most 5 seconds; the next-day check-in at most ~10 seconds.
- No punishing streaks. Measure time to return after a relapse.
- No forced defaults: notification times and the weigh-in day are unset until the user chooses them; nothing is scheduled before that.
- All data stays on the device (ADR 0001).
- The day ends at 05:00, not midnight (ADR 0003).

## Scope v1

### 1. Setup

- The user can enter, in any order and edit later: debts, a purchase goal, a target weight, a monthly outing limit, a typical outing cost, weekly targets for each daily check, the pre-outing reminder time, and the weigh-in day.
- Acceptance: every field above is editable from Settings; leaving any field empty never crashes a screen that reads it, and that screen shows a call to set it.

### 2. Daily checks

- Four yes/no checks per day: gym, diet, English, alcohol ("did you drink today?").
- Each check has an editable weekly target (e.g. gym 4/7). The week shows done vs target.
- Acceptance: one tap toggles a check; a check made at 02:00 belongs to the previous day.

### 3. Pre-outing screen

- Shown as the home screen on Friday and Saturday evenings, and reachable any day. A notification opens it at the user-chosen time on Friday and Saturday.
- Shows, before the decision: money spent on outings this month, the nearest debt installment and what an average night represents of it ("a night ≈ 23% of your installment on the 15th"), what is left for the purchase goal, and outings used vs the monthly limit (warning when the next outing would exceed it).
- **Going out**: the user sets three limits — money, drinks, return time — and completes a two-item checklist: credit cards off and out of the phone wallet; outing account loaded with the money limit. An outing is recorded as planned.
- **Staying in**: the night is recorded as a won night and adds the estimated outing cost to the "not spent this month (estimated)" counter.
- Estimated outing cost: the typical cost declared in setup until three outings are recorded, then the average of recorded outings.
- Acceptance: the comparison numbers recompute from stored data; a won night never changes debt balances or purchase-goal progress.

### 4. Next-day check-in

- The morning after a planned outing, the app opens on the check-in; a mid-morning notification points to it.
- On Saturday and Sunday mornings with no recorded outing, the check-in asks "did you go out last night?"; yes records an **unplanned outing**.
- Records: total spent, whether each limit was kept and by how much it was exceeded, and mood 1–5.
- Acceptance: completing the check-in takes at most four inputs for a planned outing; skipping it leaves the outing marked as pending, never deleted.

### 5. Relapse and return

- A **relapse** is an outing that broke at least one limit, exceeded the monthly outing limit, or was unplanned.
- A **return** is the first day after a relapse with diet done and no alcohol.
- The app shows the days from the latest relapse to its return, and the history of that number.
- Acceptance: no screen shows a streak counter that resets to zero.

### 6. Money

- **Debts**: each with name, balance, monthly installment and payment day. One tap marks this month's installment paid and lowers the balance. A reminder fires a few days before each payment day.
- **Purchase goal**: one item with a price; progress moves only with real contributions entered by the user.
- **Not spent (estimated)**: a separate monthly counter fed by won nights, always labeled as an estimate.
- Acceptance: estimated amounts are never added to any real balance or goal.

### 7. Body

- Weekly weigh-in on the user-chosen day, with a target weight and trend.
- Progress photos (trunk, face) keep their current capture and before/after comparison; an optional reminder every 4 weeks on the weigh-in day, off by default.

### 8. Weekly summary

- Sunday evening: outings and spend, limits kept, relapses and return time, checks vs weekly targets, weight trend, debt and goal progress. One screen.

### 9. Calendar

- Rebuilt month view: days marked with outings (relapse flagged), checks and weigh-ins. Tapping a day shows its detail.

### 10. Notification capture (groundwork for v1.1)

- The user picks which apps to capture (e.g. Yape, Plin, bank apps) from the installed apps. For those apps only, the notification title, text and timestamp are stored locally, unmodified. The user can view, delete all, and turn capture off. Nothing is parsed in v1 (ADR 0002).

### 11. Backup

- The existing export/import keeps working with the new schema.

### 12. Data safety after v1

- Once the v1 schema is complete, the destructive migration fallback is removed: from then on every schema change ships an explicit Room migration that preserves user data, tested with a migration test.

## Removed

The weekly diet plan (7 days × 4 meals), the weekly exercise plan, the five daily compliance toggles, and the Insights tab (replaced by the weekly summary and the calendar). Their tables are dropped with a destructive migration (ADR 0004).

## Out of scope

- Parsing captured notifications into expenses (v1.1).
- English lessons or content; only the daily study check.
- Detailed meal, workout or per-payment logging.
- Any backend, account, sync or cloud.
- Currencies other than PEN.

## Success after 6 months (owner-defined, set in-app)

Target weight reached or trending toward it; debts lower; outings within the monthly limit; weekly check targets met most weeks. After one month of data, the owner decides whether limits work or outings pause for a while.

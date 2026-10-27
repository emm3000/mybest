# MyBest v1 — QA test plan

Status: draft for the first QA cycle. Build under test: the `origin/main` SHA the orchestrator pins at dispatch (all peers test the same SHA).

## 1. Scope and references

Read first: [`docs/PRD.md`](../PRD.md) (the contract, cited below as §n), [`docs/adr/`](../adr/) (0001 on-device data, 0002 raw capture, 0003 05:00 app day, 0005 return time instead of streaks), [`docs/agents/multi-session.md`](../agents/multi-session.md) ("Isolation: emulators" and "Visual check"), [`CLAUDE.md`](../../CLAUDE.md). `docs/FEATURES.md` and `docs/ARCHITECTURE.md` §4–5 still describe the removed diet/exercise app; ignore them for expected behavior.

**In scope:** PRD §1–§12 as built: setup and onboarding, daily checks, pre-outing, check-in, relapse and return time, debts and installments, purchase goal, "not spent (estimated)", weigh-in and photos, weekly summary, history calendar, notification capture, backup export/import, reminders, and the cross-cutting behavior in §4.4.

**Out of scope** (PRD "Out of scope"): parsing captured notifications into expenses; English lessons or content; detailed meal, workout or per-payment logging; any backend, account, sync or cloud; currencies other than PEN. Also out: code fixes (testers never change code), release/R8 builds, devices other than the emulator.

**Where the PRD and the code disagree,** the case is written against the PRD and marked `FLAG`. A FLAG case that fails is filed as a bug and also listed in §9 for the owner.

## 2. Strategy

| Phase | Who | What | Entry | Exit |
|---|---|---|---|---|
| 0 Smoke + tooling | 1 peer, `emulator-5554` | Cases P0-01..P0-20: install, navigation, and proof of every must-verify tool in §3. Records the chosen clock method and the job IDs. | Pinned SHA builds; `Medium_Phone_5554` boots. | Every P0 case PASS or FAIL with a fallback chosen; §3.9 tooling table filled in the Phase 0 report; any S1 bug filed. Orchestrator approves the clock method before Phase 1. |
| 1 Suites | 3 peers in parallel | Suite A on 5554, B on 5556, C on 5558 (§4). | Phase 0 exit met; same SHA in all worktrees; each peer has the Phase 0 report. | Every case PASS, FAIL (with issue) or BLOCKED (with reason); no open S1 or S2 bug that the orchestrator has not triaged; per-suite report (§8) delivered. |
| 2 Retest + regression | 1 peer (or 3 if many fixes) | Retest each fixed issue's case on the new SHA, then the regression smoke R-01..R-14 (§4.5), including upgrade-install over the Phase 1 build. | Fix PRs merged; new SHA pinned. | All retested cases PASS; R-01..R-14 PASS; no open S1/S2. |

Order inside a suite: P0 cases first, then P1, then the rest. A tester who hits an S1 crash keeps going with the other cases and reports it at once.

## 3. Environment and tooling

### 3.1 Device and install

- One emulator per peer, booted per multi-session.md: `emulator -avd Medium_Phone_<port> -port <port> -no-snapshot-save -timezone America/Lima &` (`-timezone` is a standard emulator flag; Lima is UTC-5 with no DST, so no DST edge cases). Wait for `adb -s <serial> shell getprop sys.boot_completed` = `1`.
- In every command below `S=<serial>` (e.g. `emulator-5556`) and `PKG=com.emm.mybest`.
- Install: `ANDROID_SERIAL=$S ./gradlew :app:installDebug` from the peer's worktree. Never `installQa`.
- Fresh state at the start of every suite and wherever a case says "fresh": `adb -s $S shell pm clear $PKG`, then grant notifications (§3.6) and re-grant the listener if the suite needs it (§3.7). After `pm clear` the onboarding shows again.
- Launch: `adb -s $S shell am start -n $PKG/.MainActivity`. `monkey -p $PKG -c android.intent.category.LAUNCHER 1` exits with rc 251 and does not start the app on this image (Phase 0).
- uiautomator dump occasionally fails ("null root node" or a UiAutomation connect timeout): delete `/sdcard/ui.xml` and retry. The timeout writes a FATAL EXCEPTION from the uiautomator process into `logcat -b crash`; it is not an app crash. Compose switches dump as `View` with `checked="false"` whatever their state, so judge a switch from the screenshot.
- Fast seeding: besides the UI import (§6.2), replace the database directly: `adb -s $S push f.db /data/local/tmp/x.db`, `am force-stop $PKG`, `adb -s $S shell run-as $PKG sh -c 'cp /data/local/tmp/x.db databases/my_best_db && rm -f databases/my_best_db-wal databases/my_best_db-shm'`, relaunch (Phase 0).
- Navigate only with `adb -s $S shell uiautomator dump /sdcard/ui.xml && adb -s $S pull /sdcard/ui.xml` and tap the center of the node's `bounds`. Type with `adb -s $S shell input text '80,5'` (a space is `%s`).
- Screenshot: `adb -s $S exec-out screencap -p > <ID>-<sha>.png` (sha = `git rev-parse --short HEAD` of the build).
- Build SHA in every report and file name.

### 3.2 How the app reads time (verified in code)

- `di/AppModule.kt` binds `AppDayClock(Clock.System, TimeZone.currentSystemDefault())` and `ReminderDelays(Clock.System, TimeZone.currentSystemDefault())` as Koin singletons. The device wall clock is the only time source; there is no debug override.
- `core/datetime/AppDayStart.kt`: the app day starts at 05:00. `MorningAfterCutoff.kt`: "morning" is 05:00–12:00. `ResolveHomeDestinationUseCase`: evening is 18:00–05:00.
- The time zone is captured once per process. `AppDayClock.observeToday()` and `observeLocalDateTime()` wait with coroutine `delay`, which does not react to a wall-clock jump. Several ViewModels read "today" once at creation (`PhotosViewModel`, `HistoryViewModel`).
- **Rule: after every clock or time-zone change, run `adb -s $S shell am force-stop $PKG` and relaunch.** Only cases that test live rollover (A-001, C-007) skip this and wait in real time.

### 3.3 Clock presets

All local time, America/Lima. "Clock Kn" in a precondition means: apply the Phase 0 clock method, confirm with `adb -s $S shell date`, force-stop, relaunch.

| ID | Local time | App day | Why |
|---|---|---|---|
| K1 | Wed 2026-11-04 20:00 | Wed | weekday evening |
| K2 | Fri 2026-11-06 17:58 | Fri | 2 min before evening |
| K3 | Fri 2026-11-06 20:00 | Fri | Friday evening |
| K4 | Sat 2026-11-07 02:00 | Fri | night, still Friday |
| K5 | Sat 2026-11-07 04:58 | Fri | 2 min before rollover |
| K6 | Sat 2026-11-07 09:00 | Sat | Saturday morning |
| K7 | Sat 2026-11-07 12:30 | Sat | past morning |
| K8 | Sat 2026-11-07 20:00 | Sat | Saturday evening |
| K9 | Sun 2026-11-08 09:00 | Sun | Sunday morning |
| K10 | Sun 2026-11-08 19:00 | Sun | Sunday evening |
| K11 | Mon 2026-11-09 03:00 | Sun | still Sunday |
| K12 | Mon 2026-11-09 09:00 | Mon | Monday morning |
| K13 | Tue 2026-11-10 10:00 | Tue | mid-month weekday |
| K14 | Fri 2026-11-27 10:00 | Fri | 3 days before Nov 30 |
| K15 | Mon 2026-11-30 10:00 | Mon | month end |
| K16 | Tue 2026-12-01 10:00 | Tue | new month |
| K17 | Wed 2026-12-30 10:00 | Wed | year end, 3 days before Jan 2 |
| K18 | Thu 2027-02-25 10:00 | Thu | 3 days before Feb 28 (2027 not leap) |
| K19 | Fri 2026-10-30 20:00 | Fri | previous month's Friday |

### 3.4 Controlling the clock — options

The image is `google_apis_playstore` API 36: user build, no `adb root`, so `adb shell date` cannot set the time. Phase 0 (build `e2315235`, 2026-10-26) kept **M1**; use it in every suite.

| Method | Commands | Status |
|---|---|---|
| M1 shell alarm | `adb -s $S shell settings put global auto_time 0` then `adb -s $S shell cmd alarm set-time <epoch_ms>` (epoch ms from `date -j -f "%Y-%m-%d %H:%M:%S %z" "2026-11-06 20:00:00 -0500" +%s` x1000 on the macOS host) | **works, kept** (Phase 0). Force-stop and relaunch after every set |
| M2 time detector | `adb -s $S shell cmd time_detector suggest_manual_time --elapsed_realtime <ms> --unix_epoch_time <ms>` (auto time off first) | **fails**: SecurityException, uid 2000 lacks `SUGGEST_MANUAL_TIME_AND_ZONE` |
| M3 Settings UI | `adb -s $S shell am start -a android.settings.DATE_SETTINGS`, auto time off, then the date and time pickers, driven by uiautomator | works; slow. Fallback for M1 |
| M4 rooted image | AVD on `system-images;android-36;google_apis;arm64-v8a` (non-Play), `adb -s $S root`, `adb -s $S shell date 110620002026.00` (toybox `MMDDhhmmCCYY.ss`) | standard on userdebug images; needs a new image download and an AVD outside the `Medium_Phone_<port>` set, so the orchestrator must approve |
| M5 debug clock override | A debug-only Koin binding that replaces `Clock.System` with an offset clock set from adb (for example `adb shell setprop debug.mybest.now <epoch_ms>` read on start) | **code change: recommendation only** (§9 Q1). Not available to testers. |

Time zone: use the boot flag. To change it on a running device: `adb -s $S shell cmd alarm set-timezone America/Lima` (works; `persist.sys.timezone` and `date` follow), or the Settings UI. `cmd time_zone_detector suggest_manual_time_zone` fails with the same SecurityException as M2. Then force-stop.

Reset after a suite: `adb -s $S shell settings put global auto_time 1`.

### 3.5 Triggering WorkManager reminders

Facts (verified in code): every reminder is a unique `PeriodicWorkRequest` (`data/reminder/*SchedulerImpl.kt`) with an initial delay computed from "now" when it is scheduled; work names `pre_outing_reminder_friday`, `pre_outing_reminder_saturday`, `check_in_reminder`, `weight_reminder`, `debt_reminder`. Scheduling runs at app start (`SyncRemindersUseCase` from `MyBestApp`) and whenever a reminder field changes. An unfinished work item tagged with the same time and zone is **kept**, so after a clock jump the old delay survives until the time is changed. Each worker re-checks its condition at run time (notifications preference on, outing pending, installment due, photo due).

| Method | How | Status |
|---|---|---|
| W1 schedule then wait | Set the clock (K-preset) a few minutes before the target, force-stop, relaunch; in Settings set the reminder to a time 2–3 min ahead (pick a time different from the current value, or Borrar first, so it re-enqueues); keep the screen on; wait up to 15 min | standard; slow; WorkManager/JobScheduler may defer a few minutes |
| W2 advance clock and force the job | After scheduling, move the wall clock past the target (do not force-stop), find the job: `adb -s $S shell dumpsys jobscheduler $PKG` (jobs show as `androidx.work.systemjobscheduler:u0a<uid>/<jobId>`), then `adb -s $S shell cmd jobscheduler run -f -n androidx.work.systemjobscheduler $PKG <jobId>`. The `-n` namespace is required: without it the command answers "Could not find job" | **works, kept** (Phase 0): the worker runs after a wall-clock jump without force-stop |
| W3 diagnostics | `adb -s $S shell am broadcast -a androidx.work.diagnostics.REQUEST_DIAGNOSTICS -p $PKG`, then `adb -s $S logcat -d -s WM-DiagnosticsWrkr` lists enqueued work with tags `reminder-time:<HH:MM>@<zone>` | **works** (Phase 0) |
| W4 deep link only | `adb -s $S shell am start -n $PKG/.MainActivity -a <ACTION>` with `com.emm.mybest.ACTION_PRE_OUTING`, `ACTION_CHECK_IN`, `ACTION_DEBTS`, `ACTION_ADD_WEIGHT`, `ACTION_ADD_PHOTO`, `ACTION_WEEKLY_SUMMARY` | verified-in-code (`MainActivity` is exported; `AppNavigation.HandleIntentAction`). Tests routing, not the worker |

Check posted notifications: `adb -s $S shell dumpsys notification --noredact` (search for `pkg=com.emm.mybest`), open the shade with `adb -s $S shell cmd statusbar expand-notifications`, tap via uiautomator. Channel IDs (verified in code): `weight_reminder`, `pre_outing_reminder`, `check_in_reminder`, `photo_reminder`, `debt_reminder`.

### 3.6 POST_NOTIFICATIONS

`MainActivity` asks for it on first launch (API 33+, verified in code). Grant without the dialog: `adb -s $S shell pm grant $PKG android.permission.POST_NOTIFICATIONS`; revoke with `pm revoke` (standard-adb). `pm clear` resets the grant (Phase 0); grant again after every clear. There is no in-app notifications switch; the stored preference defaults to on (`UserPreferencesRepositoryImpl`).

### 3.7 Notification listener (capture)

Component (verified in manifest): `com.emm.mybest/com.emm.mybest.data.capture.NotificationCaptureService`, declared `exported="false"` with `BIND_NOTIFICATION_LISTENER_SERVICE`.

- Grant: `adb -s $S shell cmd notification allow_listener com.emm.mybest/com.emm.mybest.data.capture.NotificationCaptureService`; revoke: `disallow_listener` with the same component (standard-adb). The system binds the non-exported service and Ajustes lists the apps (Phase 0). `pm clear` drops the grant; grant again after every clear.
- UI path: Capture screen > "Abrir ajustes" opens the system notification access list.

### 3.8 Posting notifications to capture

The capture list shows only apps with a launcher activity (`InstalledAppsRepositoryImpl`), so notifications must come from a launcher app.

| Method | How | Status |
|---|---|---|
| N1 SMS | `adb -s $S emu sms send 5551234 "Yapeaste S/ 12.50 a José"` → Messages posts a notification | **works** (Phase 0). Open Messages once first (`am start -a android.intent.action.MAIN -c android.intent.category.APP_MESSAGING`, "Use Messages without an account"); the stored row title is `555-1234` |
| N2 shell post | `adb -s $S shell cmd notification post -S bigtext -t "Plin" qa1 "Recibiste S/ 30.00"` posts as `com.android.shell` | standard-adb; Shell has no launcher entry, so use it only for the "not selected, not captured" case |
| N3 MyBest itself | Select "MyBest" in the capture list and trigger a MyBest reminder (§3.5) | verified-in-code (MyBest has a launcher entry) |
| N4 Clock timer | `adb -s $S shell am start -a android.intent.action.SET_TIMER --ei android.intent.extra.alarm.LENGTH 5 --ez android.intent.extra.alarm.SKIP_UI true` | **works** (Phase 0): Clock posts an ongoing "Firing" notification after 5 s; dismiss with `am force-stop com.google.android.deskclock` |

Read stored rows without the UI (debug build is debuggable): `adb -s $S shell run-as $PKG ls databases/` and copy out with `adb -s $S exec-out run-as $PKG cat databases/my_best_db > db` plus the `-wal` and `-shm` files, then `sqlite3 db "SELECT title, text FROM captured_notification"` on the host. Works (P0-15); always copy the `-wal`, the main file alone is nearly empty.

### 3.9 Other device controls (standard-adb)

| Need | Command |
|---|---|
| Rotation | `adb -s $S shell settings put system accelerometer_rotation 0`, then `settings put system user_rotation 1` (landscape) and `0` (portrait) |
| Process death | Put the app in background (`adb -s $S shell input keyevent KEYCODE_HOME`), `adb -s $S shell am kill $PKG`, relaunch from Recents (`input keyevent KEYCODE_APP_SWITCH`, tap the card) |
| Font scale | `adb -s $S shell settings put system font_scale 1.3` (or `2.0`); reset to `1.0` |
| System dark/light | `adb -s $S shell cmd uimode night yes` / `no` |
| 3-button nav | `adb -s $S shell cmd overlay enable com.android.internal.systemui.navbar.threebutton` (gesture: `...navbar.gestural`) |
| Back | `adb -s $S shell input keyevent KEYCODE_BACK` |
| Double tap | Two taps in one shell call: `adb -s $S shell "input tap X Y; input tap X Y"` |
| Permissions list | `adb -s $S shell dumpsys package $PKG` (section `requested permissions`); or `aapt2 dump permissions app/build/outputs/apk/debug/app-debug.apk` |
| TalkBack | `adb -s $S shell settings put secure enabled_accessibility_services com.google.android.marvin.talkback/com.google.android.marvin.talkback.TalkBackService` (installed at `/product/app/talkback`, Phase 0); fallback: check `content-desc`/`text` in the uiautomator dump |
| Push/pull files | `adb -s $S push f /sdcard/Download/`; make it visible to the picker with `adb -s $S shell content call --uri content://media --method scan_volume --arg external_primary` (works, Phase 0) |

Phase 0 report table to fill (one row per must-verify item): item, method kept, exact command, result, fallback.

## 4. Suites

Format: one row per case. **P** priority (P0 blocks release, P1 major, P2 normal, P3 minor). **Type**: F functional, E edge, N negative, R regression, X cross-cutting. **Pre**: clock preset (§3.3) and seed (§6). **Ev**: screenshot to take, saved as `<ID>-<sha>.png`; "—" none. Spanish UI copy is quoted exactly as in `res/values/strings.xml`. Amounts render as `S/ 1,234.50` (`MoneyFormatting.kt`).

Common seed **S0** (UI, any clock): fresh install, POST_NOTIFICATIONS granted, onboarding skipped with "Omitir".

### 4.0 Phase 0 smoke (one peer, emulator-5554)

| ID | Title | P | Type | Pre | Steps | Expected | Ev |
|---|---|---|---|---|---|---|---|
| P0-01 | Install and first launch | P0 | F | emulator booted, TZ Lima | 1) `installDebug` 2) `pm clear` 3) launch | Notification permission dialog, then onboarding "1 / 5" "Gana el fin de semana" | P0-01 |
| P0-02 | Skip onboarding and tabs | P0 | F | P0-01 | 1) Tap "Omitir" 2) Tap each bottom tab "Diario", "Resumen", "Historia", "Ajustes" | Home "Un día a la vez." then each tab opens without crash | P0-02 |
| P0-03 | uiautomator navigation | P0 | F | S0 | Dump, find "Ajustes" node, tap its bounds center | Settings opens; dump shows "METAS Y LÍMITES" | — |
| P0-04 | Clock method | P0 | F | S0 | Try M1, then M2, M3 (§3.4) to reach K3; `adb shell date`; force-stop; relaunch | `date` shows Fri 6 Nov 2026 20:00 -05; Home shows the pre-outing headline "¿Sales esta noche?" (the pre-outing screen has no date label). Record the method | P0-04 |
| P0-05 | Clock back to a weekday | P0 | F | P0-04 | Apply K13 | Home shows the daily checks ("HOY · 0 DE 4") | — |
| P0-06 | Time zone is Lima | P1 | F | — | `adb shell getprop persist.sys.timezone` | `America/Lima` | — |
| P0-07 | POST_NOTIFICATIONS grant/revoke | P0 | F | S0 | `pm grant`, `dumpsys package` check, `pm revoke`, check, grant again; then `pm clear` and check | Grant state toggles; record whether `pm clear` reset it | — |
| P0-08 | Deep links | P0 | F | S0 | Run W4 for each of the six actions (force-stop between) | Opens: Antes de salir, Deudas, check-in ("Nada pendiente"), Resumen de la semana, Pesaje, Fotos | P0-08 |
| P0-09 | WorkManager listing | P1 | F | S0, Clock K13; check-in reminder set to 10:05 | Run W3 | Logcat lists `check_in_reminder` with tag `reminder-time:10:05@America/Lima` | — |
| P0-10 | Reminder fires (W2) | P0 | F | Seed S-A1 (planned outing on Fri 6 Nov, pending), Clock K6 − 10 min (08:50); check-in reminder 09:00 | Advance clock to 09:01, `cmd jobscheduler run -f` the job | Notification "¿Cómo te fue anoche?" / "Completa tu check-in." Record W2 result; if it fails, prove W1 | P0-10 |
| P0-11 | Notification tap routes | P0 | F | P0-10 | Expand shade, tap the notification | App opens on the check-in form for "VIE · 6 NOV" | — |
| P0-12 | Listener grant | P0 | F | S0 | `allow_listener` (§3.7); open Ajustes > "CAPTURA DE NOTIFICACIONES" | The "ACCESO A NOTIFICACIONES" block is gone; "APPS A CAPTURAR" lists apps. Record whether `pm clear` drops the grant | P0-12 |
| P0-13 | SMS capture | P0 | F | P0-12; Messages opened once | Select Messages; `emu sms send` (N1) | Entry under "NOTIFICACIONES GUARDADAS" with the SMS text. If N1 fails, prove N3 or N4 | P0-13 |
| P0-14 | Shell post | P2 | F | P0-13 | N2 | Notification visible in shade; not stored in MyBest | — |
| P0-15 | run-as DB read | P1 | F | P0-13 | §3.8 commands; host `sqlite3` query | Query returns the captured row | — |
| P0-16 | Backup export to Downloads | P0 | F | S0 | Ajustes > BACKUP "Exportar" > save `mybest-backup.db` in Downloads; `adb pull /sdcard/Download/mybest-backup.db` | Snackbar "Backup exportado correctamente"; file starts with `SQLite format 3` | — |
| P0-17 | Fixture import path | P0 | F | P0-16 | Build FX-A3 (§6.2), push, media scan, "+ Importar", pick it; force-stop; relaunch | Snackbar "Backup restaurado. Reinicia la app para aplicar."; after relaunch the fixture data shows (pre-outing hero "S/ 120.00"). Record whether direct `run-as` copy also works | P0-17 |
| P0-18 | Rotation, process death, font scale | P1 | F | S0 | Run each §3.9 command once on Home | Rotates; relaunch after `am kill` works; text grows at 2.0; reset all | — |
| P0-19 | Camera available | P1 | F | S0 | Fotos > take photo, allow permission | System camera opens and returns; photo appears | — |
| P0-20 | Assets branch publish | P1 | F | screenshots from P0-01..P0-17 | Follow "Visual check" with branch `assets/qa-smoke` | Branch pushed; one raw URL opens | — |

### 4.1 Suite A — weekend loop (emulator-5554)

Data owned: outings, won nights, check-ins, relapses, weekly summary. Base seed **S-A0** (UI): S0, then Ajustes: "COSTO TÍPICO DE UNA SALIDA" 100, "LÍMITE DE SALIDAS AL MES" 4, GYM 4, DIETA 5, INGLÉS 3, ALCOHOL 2; Deudas: "Banco", saldo 2000, cuota 400, día 15; Meta de compra: "Zapatillas", 450.

| ID | Title | P | Type | Pre | Steps | Expected | Ev |
|---|---|---|---|---|---|---|---|
| A-001 | Evening starts live at 18:00 | P1 | F | S-A0, Clock K2, app on Home | Wait on Home up to 4 min real time (no relaunch) | Home switches from the daily checks to the pre-outing at 18:00 (§3) | A-001 |
| A-002 | Friday evening routes Home | P0 | F | S-A0, K3 | Launch | Home shows "¿Sales esta noche?", "Me quedo" and "Salgo", plus "Ver mis checks de hoy" at the bottom (§3) | A-002 |
| A-003 | Pre-outing reachable on a weekday | P1 | F | S-A0, K1 | 1) Home shows daily checks 2) Tap shortcut "Antes de salir" | Pre-outing opens; only "Salgo" shown, no "Me quedo" (won nights only Fri/Sat); tapping "Salgo" opens the form headed "SALIDA · MIÉ · 4 NOV" (§3). The pre-outing screen itself has no date label | A-003 |
| A-004 | 02:00 still belongs to Friday | P1 | E | S-A0, K4, Friday undecided | Launch | Home shows the pre-outing; tapping "Salgo" opens the form headed "SALIDA · VIE · 6 NOV", not Saturday (ADR 0003) | A-004 |
| A-005 | Dismiss routed screen | P2 | F | A-002 | 1) Tap "Ver mis checks de hoy" 2) Go to "Historia" and back to "Diario" | Daily checks shown both times; pre-outing not forced back while still undecided | — |
| A-006 | Sunday evening routes to summary | P1 | F | S-A0 | 1) K10 launch 2) K11 launch 3) K12 launch | K10 and K11: Home shows "Resumen de la semana"; K12: daily checks (§8, ADR 0003) | A-006 |
| A-007 | Morning after a planned outing | P0 | F | S-A1, K6 | Launch | Home opens the check-in form "VIE · 6 NOV · CHECK-IN", "¿Cómo te fue anoche?" (§4) | A-007 |
| A-008 | Pending outside the morning | P1 | F | S-A1, K7 | 1) Launch 2) Tap shortcut "Check-in pendiente" | Home shows daily checks with the "Check-in pendiente" shortcut; tap opens the form (§4) | — |
| A-009 | Pre-outing with nothing set | P1 | F | S0, K3 | 1) Open pre-outing 2) Tap each prompt, back each time | No amount under the headline; prompts "Toca para fijar tu costo típico de salida en Ajustes", "Toca para agregar tus deudas", "Toca para crear tu meta de compra", "Toca para fijar tu límite de salidas en Ajustes"; they open Ajustes, Deudas, Meta de compra, Ajustes; "Gastado en salidas este mes" and "No gastado este mes (estimado)" read S/ 0.00; no crash (§1, §3) | A-009 |
| A-010 | Comparison numbers | P0 | F | S-A0, K3 | Open pre-outing | Hero "S/ 100.00" + "tu costo típico de una salida"; "Una noche ≈ 25% de tu cuota de Banco del 15"; "Te falta S/ 450.00 para Zapatillas"; "Salidas este mes: 0 de 4"; no limit warning (§3) | A-010 |
| A-011 | Over-limit warning | P1 | F | S-A0 with limit 1; one outing on Wed 4 Nov (go out at K1); K3 | Open pre-outing | "Salidas este mes: 1 de 1" and "La próxima salida pasa tu límite del mes" (§3) | A-011 |
| A-012 | Numbers recompute from stored data | P1 | F | A-010 | 1) Ajustes: typical cost 200 2) Back to pre-outing 3) Deudas: cuota 800 4) Back | Hero S/ 200.00 and 50%; then 25% (200 of 800); no relaunch needed (§3 acceptance) | — |
| A-013 | Spent this month counts check-ins only | P2 | F | FX-AW restored, K13, then "Salgo" and confirm an outing today (pending) | Open pre-outing via the "Antes de salir" shortcut | "Gastado en salidas este mes" S/ 240.00 (the two checked-in November outings; the pending one adds 0); "Salidas este mes: 3 de 4" counts it (§3) | — |
| A-014 | Going-out form gating | P0 | F | S-A0, K3 | 1) "Salgo" 2) Fill money 100, drinks 3, return 02:00 3) Toggle each checklist item on and off | Header "SALIDA · VIE · 6 NOV"; "Confirmar salida" enabled only with all three limits and both items "Tarjetas de crédito apagadas…" and "Cuenta de salida cargada con S/ 100.00" checked (§3) | A-014 |
| A-015 | Money limit validation | P2 | N | A-014 form open | Type money `0`, `abc`, `-5`, `1,234` in turn | Confirm stays disabled; checklist line reads "Cuenta de salida cargada con el límite de dinero"; `80,5` shows "Cuenta de salida cargada con S/ 80.50" | — |
| A-016 | Drinks field keeps digits only | P3 | E | A-014 form open | Type `2.5`, then `-3` | Field shows `25`, then `3`. FLAG: a decimal silently becomes 25; report if confusing | — |
| A-017 | Confirm going out | P0 | F | A-014 complete | Tap "Confirmar salida" | "Esta noche sales, con topes" with Dinero S/ 100.00, Tragos 3, Hora de regreso 02:00; Home (relaunch) shows daily checks; Historia marks Fri 6 Nov with an outing (§3) | A-017 |
| A-018 | Double tap Confirm | P1 | X | A-014 complete | Double tap "Confirmar salida" (§3.9) | One outing; no crash; no "Esta noche ya tiene una decisión" loop. Code has no submit guard and a second insert throws: watch `adb logcat -b crash` | A-018 |
| A-019 | Back closes the form | P2 | X | A-014 partly filled | System back | Form closes, numbers screen shows, nothing recorded; re-open "Salgo" starts empty | — |
| A-020 | Rotation keeps the form | P2 | X | A-014 filled, time picker closed | Rotate to landscape and back | Form, values and checks kept; Confirm reachable by scroll in landscape | A-020 |
| A-021 | Process death with form open | P2 | X | A-014 filled | §3.9 process death | Relaunch shows the pre-outing without crash; no outing recorded | — |
| A-022 | Keyboard on the form | P2 | X | A-014 | Focus money, then drinks | Focused field stays above the keyboard; Confirm reachable by scroll while the keyboard is up | A-022 |
| A-023 | Stay in on Friday | P0 | F | S-A0, K3 | Tap "Me quedo" | "Esta noche te quedas" + "+ S/ 100.00 a No gastado este mes (estimado)"; "No gastado este mes (estimado)" S/ 100.00; Historia Fri 6 Nov "Noche ganada" "S/ 100.00 (estimado)" (§3) | A-023 |
| A-024 | Won night never touches real money | P0 | F | Before A-023 note Deudas saldo and Meta "Aportado"/% ; then A-023 | Re-read Deudas, Meta de compra, Resumen | Debt balance S/ 2,000.00 unchanged; goal Aportado S/ 0.00 and 0% unchanged; Resumen "No gastado (estimado)" S/ 100.00 separate (§3, §6) | A-024 |
| A-025 | Won night without an estimate | P2 | E | S0 (no typical cost), K3 | "Me quedo" | "Sumada a No gastado este mes (estimado)"; counter S/ 0.00; Historia "Sin estimado"; no crash | — |
| A-026 | Estimate frozen at decision time | P2 | E | A-023 | Ajustes typical cost 150; reopen pre-outing | Counter still S/ 100.00; hero now S/ 150.00 | — |
| A-027 | Two won nights add up | P2 | F | A-023, then K8 "Me quedo" | Open pre-outing | "No gastado este mes (estimado)" S/ 200.00 | — |
| A-028 | Average after three check-ins | P1 | F | FX-A3, K3 | Open pre-outing | Hero "S/ 120.00" + "lo que te cuesta una noche, en promedio" (§3) | A-028 |
| A-029 | FLAG average after three recorded outings | P1 | F | FX-A2P (3 outings, 2 checked in at 90 and 150), K3 | Open pre-outing | Per PRD §3 ("three outings are recorded") the average caption shows. Code switches only after 3 check-ins and keeps "tu costo típico de una salida" S/ 100.00. File if so; see §9 Q2 | A-029 |
| A-030 | Unplanned spend in the average | P2 | F | FX-A3U (planned 90, 120, unplanned 300), K3 | Open pre-outing | Hero S/ 170.00 (all recorded outings with a spend) | — |
| A-031 | Average rounds half-up | P3 | E | FX-A3 with spends 10.01, 10.01, 10.00 | Open pre-outing | Hero S/ 10.01 | — |
| A-032 | Check-in form gating | P0 | F | S-A1, K6 | Fill in turn: spent, drinks, return time, mood | Hints "tope S/ 100.00", "tope 3", "tope 02:00"; "Guardar" enabled only after all four (§4: at most four inputs) | A-032 |
| A-033 | All limits kept | P0 | F | A-032: 80, 2, 01:30, mood 4 | "Guardar" | "Anotado" and "Cumpliste" for each limit; "Listo" returns to Home daily checks (§4) | A-033 |
| A-034 | Limits exceeded | P0 | F | S-A1, K6: 150, 5, 02:45, mood 2 | "Guardar" | "Te pasaste por S/ 50.00", "Te pasaste por 2 tragos", "Te pasaste por 45 min"; Historia Fri marked as relapse "Rompiste un tope" (§4, §5) | A-034 |
| A-035 | Return time across midnight | P1 | E | FX-A2PEND with the 2026-11-04 row's `limit_return_minutes` = 1410 (23:30), K6 | Check in Wed 4 Nov with return 00:15; then Fri 6 Nov (limit 02:00) with return 07:00; other inputs within limits | "Te pasaste por 45 min"; then "Te pasaste por 300 min" (a 05:00–12:00 return counts as the next morning) | — |
| A-036 | FLAG return after noon | P2 | E | S-A1 (limit 02:00), K7 | Check-in with return 12:30 | A return the next day at 12:30 is past a 02:00 limit; code treats 12:00–05:00 as the same evening and shows "Cumpliste". File if so (§9 Q5) | — |
| A-037 | Spend validation | P2 | N | A-032 form | Spent `abc`, `-5`, `1,234`, then `0` | Save disabled for the first three; `0` allowed and evaluates as kept | — |
| A-038 | Skip leaves it pending | P0 | F | S-A1, K6 | 1) "Ahora no" 2) Check Home, Resumen, Historia Fri 3) K12 relaunch | Form closes; outing kept: shortcut "Check-in pendiente", Resumen "Check-ins pendientes" 1, day detail "Check-in pendiente"; next morning Home opens the check-in again (§4 acceptance) | A-038 |
| A-039 | Oldest pending first | P2 | E | FX-A2PEND (pending outings Wed 4 Nov and Fri 6 Nov), K6 | Open check-in; save it; reopen | First form is "MIÉ · 4 NOV", then "VIE · 6 NOV" | — |
| A-040 | Rotation on the check-in | P2 | X | A-032 partly filled, time picker open | Rotate | Values kept; picker still open or cleanly closed; no crash | — |
| A-041 | Double tap Save | P3 | X | A-032 complete | Double tap "Guardar" | One "Anotado"; no error snackbar | — |
| A-042 | Unplanned question, answer No | P1 | F | S-A0, Friday with no record, K6 | 1) Launch 2) "No" 3) Relaunch | "¿Saliste anoche?" + "No registraste ninguna salida esa noche."; "No" closes; not asked again; daily checks (§4) | A-042 |
| A-043 | Unplanned outing, answer Yes | P0 | F | as A-042 | 1) "Sí" 2) Spent 200, mood 2 3) "Guardar" | Form has only spend (hint "salida no planeada") and mood; "Anotado"; Historia Fri "No planeada" with "Salida no planeada" relapse (§4, §5) | A-043 |
| A-044 | Yes replaces a won night | P1 | F | A-023 (Fri won night), K6 | Question appears; "Sí"; save spend 100 | Fri now shows an outing, not "Noche ganada"; "No gastado este mes (estimado)" drops by S/ 100.00 | — |
| A-045 | Sunday asks about Saturday, weekdays never | P1 | F | S-A0 | 1) K9 with Saturday unrecorded 2) K12 with Sunday unrecorded | K9 asks about "SÁB · 7 NOV"; K12 shows daily checks, no question (§4) | — |
| A-046 | Over monthly limit is a relapse | P1 | F | S-A0 limit 1; two kept planned outings in November (Wed 4, Fri 6) | Historia: tap each day | Fri detail shows "Recaída" "Pasaste tu límite de salidas del mes"; Wed shows no relapse (§5) | A-046 |
| A-047 | Return in one day | P0 | F | A-034 relapse on Fri; at K6 Home: "Dieta" on, alcohol "NO"; then K10 | Open Resumen | "RECAÍDAS Y REGRESO": "Regresaste en 1 día"; history row "Recaída del VIE 6 NOV" "1 día" (§5) | A-047 |
| A-048 | Open relapse, later return | P1 | F | A-034; Sat: Dieta on, alcohol "SÍ"; check at K9 Resumen; then Sun: Dieta on, alcohol unanswered; K10 | Open Resumen at K9 and K10 | K9: "Aún sin regreso · 2 días"; K10: "Regresaste en 2 días" (unanswered alcohol counts as no alcohol) (§5) | — |
| A-049 | No resetting streak anywhere | P1 | R | A-047 data | Visit Diario, Resumen, Historia, pre-outing, check-in result | No streak or "días seguidos" counter; only return time and weekly counts (§5 acceptance, ADR 0005) | — |
| A-050 | Weekly summary content | P0 | F | FX-AW, K10 | Open Resumen | SALIDAS: Salidas 2, Gasto S/ 240.00, Check-ins pendientes 0; LÍMITES "Cumpliste 1 de 2"; RECAÍDAS Y REGRESO "Regresaste en 1 día"; CHECKS Gym "2 de 4", Dieta "2 de 5", Inglés "0 de 3", Alcohol "1 · máx. 2"; PESO "Último peso" 79.9 kg, "Cambio" −0.5 kg, "Para tu meta" 4.9 kg; DINERO "Deuda total" S/ 1,600.00, "Cuotas pagadas este mes" 1, "Meta: Zapatillas" "S/ 100.00 · 22%", "No gastado (estimado)" S/ 100.00 (§8) | A-050 |
| A-051 | Empty summary | P1 | F | S0, K10 | Open Resumen; tap each prompt | "Sin check-ins todavía", "Sin recaídas esta semana", prompts "Toca para fijar tus metas semanales en Ajustes", "Toca para registrar tu peso y ver la tendencia", "Toca para agregar tus deudas…", "Toca para crear tu meta de compra…" open the right screens (§1) | A-051 |
| A-052 | Week navigation | P2 | F | FX-AW, K12 | "Semana anterior", then "Semana siguiente" | Previous week shows the FX-AW week with "Deuda total hoy"; "Semana siguiente" disabled on the current week | — |
| A-053 | Pre-outing reminder on Friday | P1 | F | S-A0, pre-outing reminder 20:05, clock K3 | W2 (or W1) | "¿Sales hoy?" / "Mira los números antes de decidir."; tap opens "Antes de salir" (§3) | A-053 |
| A-054 | Pre-outing reminder only Fri/Sat | P2 | F | S-A0, K1, reminder 20:05 | W3 | Only `pre_outing_reminder_friday` and `_saturday` enqueued; no weekday work. Optional W2 on Wed: no notification | — |
| A-055 | Check-in reminder only with a pending outing | P1 | F | S-A1, check-in reminder 09:05, K6 | W2; then save the check-in; next day K12 W2 again | Day 1: "¿Cómo te fue anoche?" → tap opens the form; day 2: no notification | — |
| A-056 | Deep link while on another screen | P2 | X | S-A0, app on Historia | W4 `ACTION_PRE_OUTING`; then back | Pre-outing opens; back returns to Historia | — |
| A-057 | Recents relaunch does not repeat the deep link | P3 | X | A-056 | Back out to launcher; reopen from Recents | Opens Home, not the pre-outing (`LaunchActionRule`) | — |
| A-058 | Font scale 2.0 | P2 | X | S-A0, K3, font 2.0 | Open pre-outing, going-out form, check-in form | All text visible, no clipped or overlapping labels; every button reachable by scroll | A-058 |
| A-059 | Accessibility labels | P3 | X | S-A0, K6 check-in | uiautomator dump of mood selector and decision buttons | Mood options read "Ánimo 1 de 5"…"Ánimo 5 de 5"; buttons have text. Selection and switch state are not exposed in the dump (§3.1); judge them from the screenshot | — |

### 4.2 Suite B — money and setup (emulator-5556)

Data owned: onboarding, setup, debts, payments, purchase goal, contributions, backup. Base seed **S-B0** (UI): S0. Clock K13 unless stated.

| ID | Title | P | Type | Pre | Steps | Expected | Ev |
|---|---|---|---|---|---|---|---|
| B-001 | First run | P0 | F | fresh, POST_NOTIFICATIONS not granted | Launch | Permission dialog; onboarding "1 / 5" "Gana el fin de semana" with three lines; "Omitir" and "Siguiente" | B-001 |
| B-002 | Deny notifications | P2 | N | B-001 | Deny; rotate | App continues; FLAG if the dialog reappears after rotation (the request flag lives in the Activity) | — |
| B-003 | Step through onboarding | P1 | F | B-001 | "Siguiente" x4, back once, "Siguiente", "Empezar" | Titles "Checks de la semana", "Topes de salida", "Deudas y meta de compra", "Peso y recordatorios"; back returns one step; "Empezar" opens Home; relaunch skips onboarding | B-003 |
| B-004 | Skip onboarding | P1 | F | fresh | "Omitir" on step 1; relaunch | Home; no onboarding after relaunch | — |
| B-005 | Set values inside onboarding | P1 | F | fresh | Step 2: GYM 4; step 3: typical cost 100; finish | Ajustes shows GYM "4 días por semana", cost "S/ 100.00" (§1) | — |
| B-006 | Debts from the money step | P2 | F | fresh, step 4 | Tap DEUDAS row; add a debt; back | Deudas opens inside onboarding; back returns to step 4, not out of the app | — |
| B-007 | Editor sheet survives rotation | P2 | X | fresh, step 2 | Open GYM, type 5, rotate; repeat on the Ajustes screen | Sheet stays open with 5 in both places. Known risk: in onboarding `DismissEditorsOnLeave` closes it on recreation | B-007 |
| B-008 | Process death in onboarding | P3 | X | fresh, step 3, a value saved | §3.9 process death | No crash; onboarding still shown (any step); saved value kept | — |
| B-009 | Replay the intro | P2 | F | S-B0 with values | Ajustes "INTRODUCCIÓN" "Volver a ver la introducción"; walk to the end | Onboarding from "1 / 5"; steps show existing values; "Empezar" back to Home; data intact | — |
| B-010 | Deep link during onboarding | P3 | E | fresh, onboarding shown | W4 `ACTION_DEBTS`; then "Omitir" | Onboarding stays; after finishing, no crash (opening Deudas or Home both acceptable; note which) | — |
| B-011 | Nothing set by default | P1 | F | S-B0 | Open Ajustes | Every METAS Y LÍMITES row and every RECORDATORIOS row "Sin definir"; photo switch off; DEUDAS and META rows show their empty values; CAPTURA "Desactivada" (§1, principles) | B-011 |
| B-012 | Empty fields never crash | P0 | F | S-B0, K3 | Visit Diario, pre-outing, Resumen, Historia, Pesaje, Deudas, Meta de compra, Captura | Each opens; each missing input shows its call to set (texts as in A-009, A-051, C-011, "Agrega tu primera deuda", "Define tu meta de compra") (§1 acceptance) | — |
| B-013 | Target weight input | P1 | N | S-B0 | PESO OBJETIVO: `72,5`; then `0`, `-3`, `abc`, empty; "Borrar" | "72.5 kg"; "Peso fuera de rango. Acepta 20–500 kg." for 0; "Escribe un número válido." for -3, abc and empty; Borrar → "Sin definir" | — |
| B-014 | Target weight range | P3 | E | S-B0 | Enter `1e3`, then `9999` | `1e3`: "Escribe un número válido."; `9999`: "Peso fuera de rango. Acepta 20–500 kg."; the row keeps its old value in both cases (weigh-in range 20–500). File as S4 if either is saved | — |
| B-015 | Monthly outing limit input | P2 | N | S-B0 | `0`, `-1`, `2.5`, `99999999999` | 0 saved; "El valor no puede ser negativo."; "Escribe un número válido." twice | — |
| B-016 | Typical cost input | P2 | N | S-B0 | `120,5`, `120.505`, `1,234.50`, `-10`, `0` | "S/ 120.50"; invalid number for the next two; `-10` shows an error (code shows "Escribe un número válido.", the negative message is unreachable: S4 copy note); `0` saves "S/ 0.00" | — |
| B-017 | Weekly targets 0..7 | P2 | N | S-B0 | GYM `7`, `8`, `-1`, empty | 7 saved; "Elige entre 0 y 7 días." for 8 and -1; invalid number for empty; alcohol row label "ALCOHOL · MÁX. DÍAS POR SEMANA" | — |
| B-018 | Clearing a value in use | P2 | E | S-B0 cost 100, K3, won night recorded | Borrar typical cost; open pre-outing | Prompt for typical cost returns; counter keeps S/ 100.00; no crash | — |
| B-019 | Double tap Guardar in Ajustes | P3 | X | S-B0 editor open | Double tap "Guardar" | One save; sheet closes; one "Guardado" snackbar | — |
| B-020 | Font scale 2.0 in Ajustes and Deudas | P2 | X | S-B0 with one debt, font 2.0 | Scroll both screens | Labels and values readable; "Borrar", "EDITAR", "MARCAR CUOTA PAGADA" reachable | B-020 |
| B-021 | Empty debts | P2 | F | S-B0 | Open Deudas | "Agrega tu primera deuda" and "Agregar deuda" | — |
| B-022 | Create a debt | P0 | F | S-B0, K13 | "Agregar deuda": Banco, 2000, 400, 15; "Guardar" | Row "Banco" "S/ 400.00 · día 15 · saldo S/ 2,000.00"; Ajustes "1 deuda · S/ 2,000.00"; Diario "PRÓXIMA CUOTA · 15 NOV" (§6) | B-022 |
| B-023 | Debt validation | P1 | N | Deudas form | Save with: name spaces; balance empty; balance `-5`; cuota `0`; día `0`, `32`, `abc` | "Escribe un nombre"; "Escribe el saldo"; "El saldo no puede ser negativo"; "La cuota debe ser mayor que cero"; "El día de pago va de 1 a 31"; several errors show together; nothing saved. Balance 0 and cuota > saldo are accepted | — |
| B-024 | Edit a debt | P1 | F | B-022 | "EDITAR": cuota 500 | Title "EDITAR DEUDA", fields prefilled 2000 / 400 / 15; row shows S/ 500.00 | — |
| B-025 | Delete a debt | P1 | F | B-022 | "ELIMINAR": cancel, then confirm | "¿Eliminar deuda?" "Se borrará Banco con sus pagos registrados."; cancel keeps; "Eliminar" removes; Ajustes row back to empty | — |
| B-026 | Double tap Guardar on a new debt | P1 | X | Deudas form filled | Double tap "Guardar" | Exactly one "Banco" row. Code has no submit guard: two rows = S2 bug | B-026 |
| B-027 | Mark the installment paid | P0 | F | B-022, K13 | "MARCAR CUOTA PAGADA" | Saldo S/ 1,600.00; row "CUOTA PAGADA"/"PAGADO", mark button gone; Resumen "Cuotas pagadas este mes" 1 (§6) | B-027 |
| B-028 | No double payment | P1 | X | B-022 | Double tap "MARCAR CUOTA PAGADA"; relaunch | Saldo S/ 1,600.00 (one deduction) | — |
| B-029 | Last partial installment | P1 | E | debt saldo 250, cuota 400 | Mark paid | Saldo S/ 0.00, never negative | — |
| B-030 | Paid-off debt | P3 | E | B-029 | Next month (K16): look at the row and tap mark paid | Nothing changes. FLAG: the mark button still shows for a S/ 0.00 balance | — |
| B-031 | New month resets paid | P1 | F | B-027, then K16 | Open Deudas | "MARCAR CUOTA PAGADA" back; saldo still S/ 1,600.00 | — |
| B-032 | FLAG payment booked to the wrong month | P1 | F | Clock K15 (Mon 30 Nov): debt "Tarjeta", saldo 1000, cuota 200, día 2 | 1) Diario shows "PRÓXIMA CUOTA · 2 DIC"; Deudas shows "MARCAR CUOTA PAGADA" 2) Tap it (saldo S/ 800.00) 3) Clock K16 (Tue 1 Dec), open Deudas and Diario | Per PRD §6 the tap paid the installment shown as next (2 Dec): on 1 Dec the row reads "CUOTA PAGADA" and Diario shows "2 ENE". Code books the payment to November, so on 1 Dec the row asks again, Diario still shows "2 DIC", and a second tap drops the saldo to S/ 600.00. File if so (§9 Q3) | B-032 |
| B-033 | Payment day 31 in a 30-day month | P1 | E | debt día 31, K13 | Diario and pre-outing | "PRÓXIMA CUOTA · 30 NOV"; pre-outing "…de tu cuota de <name> del 30" | — |
| B-034 | Payment day 29–31 in February | P2 | E | debts día 29, 30, 31, K18 | Diario, pre-outing | Next date "28 FEB" for all three | — |
| B-035 | Nearest of several debts | P1 | F | debts día 5 and día 20, K13 | Diario; mark día-20 paid; Diario | First "20 NOV"; then "5 DIC" | — |
| B-036 | FLAG paid-off debt still "nearest" | P2 | E | B-029 debt (saldo 0) with día 12 plus another día 25, K13 | Diario and pre-outing | Expected: the S/ 0.00 debt is not the next installment (the reminder already skips it). Code picks it. File if so | — |
| B-037 | Year rollover | P3 | E | debt día 2, K17 | Diario | "PRÓXIMA CUOTA · 2 ENE" | — |
| B-038 | Installment reminder 3 days before | P0 | F | debt "Banco" 2000/400 día 30; reminder "CUOTAS POR VENCER" 10:05; K14 | W2 | "Cuota por vencer" / "Tu cuota de Banco (S/ 400.00) vence el 30"; tap opens Deudas (§6) | B-038 |
| B-039 | Reminder skips paid, zero and off-days | P1 | N | B-038 setup | a) mark paid then W2 at K14; b) saldo 0; c) W2 at Nov 26 and Nov 28 | No notification in a, b, c | — |
| B-040 | Reminder uses the smaller balance | P3 | E | saldo 250, cuota 400, día 30, K14 | W2 | Text shows "(S/ 250.00)" | — |
| B-041 | February reminder | P2 | E | debt día 31, K18 | W2 | "…vence el 28" | — |
| B-042 | No reminder until a time is chosen | P1 | F | S-B0 with one debt | W3 | No `debt_reminder` work enqueued (principles: no forced defaults) | — |
| B-043 | Goal empty and validation | P1 | N | S-B0 | Meta de compra: save empty; name only; price `0`, `abc` | "Define tu meta de compra"; "Escribe qué quieres comprar"; "El precio debe ser mayor que cero" | — |
| B-044 | Save a goal | P0 | F | S-B0 | "Zapatillas", 450, "Guardar meta" | "META · ZAPATILLAS", 0%, "Precio S/ 450.00", "Aportado S/ 0.00", "Te falta S/ 450.00", "Aún no hay aportes"; Ajustes "Zapatillas · S/ 450.00" (§6) | B-044 |
| B-045 | Double tap "Guardar meta" | P2 | X | form filled | Double tap | One goal; no error snackbar | — |
| B-046 | Add a contribution | P0 | F | B-044, K13 | "Agregar aporte" 100, "Guardar" | 22%, "Aportado S/ 100.00", "Te falta S/ 350.00"; row date "Martes 10 noviembre", S/ 100.00 (§6). Note any unaccented "Sabado"/"Miercoles" as S4 | B-046 |
| B-047 | Contribution validation | P2 | N | B-044 | Amounts `0`, `-5`, `abc`, empty | "El aporte debe ser mayor que cero"; nothing added | — |
| B-048 | Double tap contribution save | P1 | X | contribution 100 typed | Double tap "Guardar" | One contribution. Code has no guard: two rows = S2 bug (money) | B-048 |
| B-049 | Over-funded goal | P2 | E | B-044 | Contributions 300 + 200 | 100%, "Te falta S/ 0.00" | — |
| B-050 | Delete a contribution | P2 | F | B-046 | Row "ELIMINAR" | Totals recompute; FLAG S4 if no confirmation is wanted | — |
| B-051 | Remove the goal | P2 | F | B-046 | "QUITAR META", cancel, then "Quitar" | "¿Quitar la meta?" "Se borrará Zapatillas con todos sus aportes."; define form returns | — |
| B-052 | FLAG edit the goal later | P2 | F | B-046 | Try to change the price to 500 keeping contributions | PRD §1 says the goal is editable later. The only path is "QUITAR META", which deletes contributions. File as S3 if no edit exists | — |
| B-053 | Not spent never feeds the goal | P0 | F | B-046, K3, "Me quedo" with typical cost 100 | Open Meta de compra | "NO GASTADO ESTE MES (ESTIMADO)" S/ 100.00 + "Es un estimado, no dinero real. No suma a tu meta."; Aportado still S/ 100.00, 22% (§6 acceptance) | B-053 |
| B-054 | Not spent resets by month | P2 | E | won night at K19 | K16 (or any November day) open Meta de compra | Counter S/ 0.00 in November | — |
| B-055 | Export | P0 | F | S-B0 with debt, goal, setup | BACKUP "Exportar" → Downloads | "Backup exportado correctamente"; pulled file > 0 bytes, header `SQLite format 3` (§11) | — |
| B-056 | Export, clear, import | P0 | F | B-055 file | `pm clear`; "Omitir"; "+ Importar" pick file; force-stop; relaunch | "Backup restaurado. Reinicia la app para aplicar."; setup, debts, payments, goal, contributions all back (§11) | B-056 |
| B-057 | Use the app right after restore | P1 | E | B-056 before relaunch | Without restarting: Diario toggle a check, open Deudas | No crash (the live DB was closed by restore). Crash = S1 | — |
| B-058 | Other-version backup | P1 | N | FX-V9 and FX-HASH (§6.2) | Import each | "Este backup es de otra versión de la app y no se puede restaurar"; current data unchanged (§11) | — |
| B-059 | Not a database | P2 | N | a `.txt` file in Downloads | Import it | Error snackbar; data unchanged. FLAG S4: message is the raw English "Invalid SQLite backup file" | — |
| B-060 | Restore fails after the DB is closed | P1 | E | — | Make `Files.move` fail after `database.close()` (`BackupFileReplacement.kt`) | Expected: error shown, live data intact, app usable. Cannot be induced on the emulator without a code hook: mark BLOCKED and say so (§9 Q8) | — |
| B-061 | Huge amounts | P2 | E | S-B0 | Balance `9999999999999999` (16 digits); then 17 digits; typical cost 16 digits, open pre-outing at K3 | 16 digits saved and formatted with commas; 17 digits rejected; pre-outing percent is sane or hidden, no crash (percent math can overflow) | — |
| B-062 | No INTERNET permission | P1 | X | installed debug APK | `dumpsys package` and `aapt2 dump permissions` | Only CAMERA and POST_NOTIFICATIONS (plus library-internal ones); no `android.permission.INTERNET` (ADR 0001) | — |
| B-063 | Dark theme only | P2 | X | S-B0 | `cmd uimode night no`; launch; open Ajustes, Deudas, a sheet | App stays Atelier Dark; no white flash on launch; status bar icons legible | B-063 |
| B-064 | Deep link to Deudas from a cold start | P2 | X | B-022, app force-stopped | W4 `ACTION_DEBTS`; back | Deudas opens; back goes to Diario | — |
| B-065 | Keyboard on the debt sheet | P2 | X | Deudas form | Focus each field | Field and "Guardar" stay visible above the keyboard | B-065 |
| B-066 | Process death with the debt sheet open | P3 | X | form half filled | §3.9 process death | No crash; no partial debt saved | — |

### 4.3 Suite C — body, records and system (emulator-5558)

Data owned: daily checks, weights, photos, history, capture, reminder settings. Base seed **S-C0**: S0; Ajustes: PESO OBJETIVO 75, GYM 4, DIETA 5, INGLÉS 3, ALCOHOL 2.

| ID | Title | P | Type | Pre | Steps | Expected | Ev |
|---|---|---|---|---|---|---|---|
| C-001 | Fresh Diario | P1 | F | S0, K13 | Open Diario | "HOY · 0 DE 4"; Gym, Dieta, Inglés unchecked; "¿Tomaste hoy?" with "NO"/"SÍ" none selected; "ESTA SEMANA" rows "FIJAR META EN AJUSTES" open Ajustes (§1, §2) | C-001 |
| C-002 | One tap toggles | P0 | F | S-C0, K13 | Tap Gym; tap Gym again | "HOY · 1 DE 4" then "HOY · 0 DE 4"; week Gym "1/4" then "0/4" (§2) | C-002 |
| C-003 | Alcohol answer | P1 | F | S-C0, K13 | "NO", then "SÍ" | "NO" adds 1 to "HOY · x DE 4"; "SÍ" removes it; week "Alcohol" "1 · MÁX 2" after SÍ | — |
| C-004 | Weekly targets over days | P1 | F | S-C0 | 1) Clock K12: Gym on, alcohol "SÍ" 2) Clock K13: Gym on, alcohol "SÍ" 3) Clock Wed 2026-11-11 10:00: alcohol "SÍ"; open Diario | "ESTA SEMANA": Gym "2/4"; Alcohol "3 · MÁX 2 · 1 DE MÁS" in the broken tone (§2) | C-004 |
| C-005 | 02:00 counts for the previous day | P0 | E | S-C0, Clock Tue 2026-11-10 02:00 | Tap Dieta; then K13; Historia tap Mon 9 and Tue 10 | Mon 9 detail shows Dieta "Sí"; Tue 10 Dieta unchecked (§2, ADR 0003) | C-005 |
| C-006 | 04:58 vs 05:00 | P1 | E | S-C0, K5 | Tap Inglés at 04:58; set Sat 05:00; relaunch | Inglés belongs to Fri 6; Sat shows "HOY · 0 DE 4" | — |
| C-007 | Live rollover at 05:00 | P2 | E | S-C0, K5, Diario open | Wait 3 min real time | Diario resets to the new day without relaunch | — |
| C-008 | Week runs Monday to Sunday | P2 | E | S-C0 | Gym at K11 (Mon 03:00) and at K12 | K11 tap counts in week Nov 2–8; K12 tap starts the new week "1/4" | — |
| C-009 | Fast repeated toggles | P3 | X | S-C0 | Tap Gym 5 times fast | Final state checked (odd count); History day matches | — |
| C-010 | Check labels for accessibility | P3 | X | S-C0 | uiautomator dump of the checks | Each check exposes its label text and a checkable state; NO/SÍ are selectable | — |
| C-011 | Empty Pesaje | P1 | F | S0 | Diario "REGISTRAR" | "¿Cuánto pesas hoy?", "Define tu peso objetivo", "Define tu día de pesaje" (both open Ajustes); save disabled while empty (§1) | C-011 |
| C-012 | Weight validation | P1 | N | S-C0 | Enter `19.99`, `500.01`, `72.`, `72,456`, `abc`, then `20`, `500` | "Peso fuera de rango. Acepta 20–500 kg." for the first two; "Ingresa un peso valido. Ejemplo: 72.4 o 72,4" for the next three; save disabled; 20 and 500 accepted | — |
| C-013 | Save and same-day replace | P0 | F | S-C0, K13 | Save `80,4`; save `80.0` again same day | Diario shows 80.0; Historia day has one weight 80.0 (§7) | C-013 |
| C-014 | Trend and target | P1 | F | C-013 on Tue; Wed 2026-11-11 save 79.5 | Open Pesaje and Resumen | "−0.5 kg desde el último pesaje", "Te faltan 4.5 kg", "Meta: 75.0 kg"; Resumen PESO "Último peso" 79.5, "Cambio" "−0.5 kg", "Para tu meta" 4.5 kg (§7, §8) | C-014 |
| C-015 | Below target and upward | P3 | E | weights 74.0 then 74.6 | Pesaje | "+0.6 kg desde el último pesaje"; no "Te faltan" line | — |
| C-016 | Weigh-in at 02:00 | P2 | E | S-C0, Clock Wed 2026-11-11 02:00 | Save 79.0 | Historia puts it on Tue 10 Nov (replacing any Tue entry), nothing on Wed 11 | — |
| C-017 | Weigh-in day shown | P2 | F | S-C0 | Ajustes "DÍA DE PESAJE" Domingo; open Pesaje | "Tu día de pesaje: domingo" | — |
| C-018 | Empty photos | P2 | F | S0 | Diario "FOTO" | Tabs "Tronco"/"Cara"; empty placeholder; compare not available | — |
| C-019 | Take a trunk photo | P0 | F | S0, K13 | Camera button, allow permission, shoot, accept | Photo in Tronco timeline; Diario photo caption "Tronco · hoy" (§7) | C-019 |
| C-020 | Camera permission denied | P2 | N | fresh | Deny once; tap again and deny "don't ask again" | "Se necesita acceso a la cámara para tomar fotos."; then "Habilita el permiso de cámara en Ajustes." with "Ajustes" opening app info | — |
| C-021 | Cancel the camera | P3 | N | S0 | Open camera, back | No photo added | — |
| C-022 | Face photo | P1 | F | S0 | Tab "Cara", take a photo | Saved under Cara, not Tronco | — |
| C-023 | Compare two photos | P1 | F | Tronco photos on K12 and Clock Sat 2026-11-21 10:00, weights near both dates | "COMPARAR ↗" | Before "ANTES" / after "AHORA" with "12 DÍAS · −x KG" (or "— DÍAS · — KG" without weights) (§7) | C-023 |
| C-024 | Process death during camera | P2 | X | S0, tab Cara | Open camera; `am kill $PKG` from another shell; shoot; accept | Photo saved as Cara. Risk: the selected tab resets and it saves as Tronco | — |
| C-025 | Rotation on Fotos | P3 | X | C-022 | Rotate | Selected tab and timeline kept | — |
| C-026 | Delete a photo from History | P2 | F | C-019 | Historia > day > "Eliminar foto" > confirm | Photo gone from day detail and Fotos timeline | — |
| C-027 | Calendar header and legend | P1 | F | FX-C, K13 | Open Historia | Month title, "HOY" chip on 10, legend "PESO REGISTRADO", "FOTO", "CHECKS HECHOS", "SALIDA", "RECAÍDA", "NOCHE GANADA"; "SALIDAS 3 · NOCHES GANADAS 1"; "2 PESAJES · 0 FOTOS" (§9) | C-027 |
| C-028 | Day marks | P0 | F | FX-C, K13 | Inspect the grid | Days with checks, weights, the outing, the relapse outing (flagged differently) and the won night carry their marks; unmarked days plain (§9) | C-028 |
| C-029 | Day detail content | P1 | F | FX-C, K13 | Tap Fri 6 Nov, Sat 7 Nov, Mon 9 Nov, Tue 3 Nov | Fri 6: "Salida" "Planeada", "Gasto" S/ 150.00, Dinero exceeded, "Ánimo" "2 de 5", "Recaída" "Rompiste un tope". Sat 7: "Noche ganada" "S/ 100.00 (estimado)", checks Dieta "Sí", alcohol "No". Mon 9: "No planeada", "Salida no planeada". Tue 3: Gym and Dieta "Sí", others "—" (§9) | C-029 |
| C-030 | Empty day | P3 | F | FX-C | Tap a day with nothing | "Sin actividad" | — |
| C-031 | Month navigation | P2 | F | FX-C | Go back 2 months and forward 2 past today | No crash; past months show their data; future months empty | — |
| C-032 | Delete a weight from History | P2 | F | C-013 | Day detail "Eliminar peso" | Mark gone; Diario weight shows the previous value or "—" | — |
| C-033 | Rotation with day detail open | P2 | X | C-029 | Rotate | Detail stays open on the same day | — |
| C-034 | Calendar at font scale 2.0 | P2 | X | FX-C, font 2.0 | Open Historia | Day numbers and marks legible; no overlap that hides a mark | C-034 |
| C-035 | Capture without access | P1 | F | S0, listener not granted | Ajustes "CAPTURA DE NOTIFICACIONES" | Privacy text; "ACCESO A NOTIFICACIONES" + "Abrir ajustes" opens the system list containing MyBest (§10) | C-035 |
| C-036 | Grant access and list apps | P1 | F | C-035 | Grant (§3.7); back to the app | Access block gone on resume; "APPS A CAPTURAR" alphabetical launcher apps incl. Messages and MyBest | — |
| C-037 | Capture from a selected app | P0 | F | C-036 | Select Messages; N1 SMS | Ajustes "Activa · 1 app"; entry "MESSAGES" (app label) with title, text and date/time under "NOTIFICACIONES GUARDADAS" (§10) | C-037 |
| C-038 | Stored unmodified | P1 | F | C-037 | N1 with `"Yapeaste S/ 12.50 a José — ¡gracias! 😀"` and a 300-char text; read DB (§3.8) | `title` and `text` columns equal the posted strings byte for byte (ADR 0002) | — |
| C-039 | Unselected apps ignored | P1 | N | C-037 | N2 shell post; deselect Messages and send SMS | Neither stored | — |
| C-040 | Delete all | P1 | F | entries exist | "BORRAR TODO": cancel, then "Borrar" | "¿Borrar todo?" "Se borrarán todas las notificaciones guardadas. No se puede deshacer."; cancel keeps; Borrar → "Aún no hay notificaciones guardadas" (§10) | — |
| C-041 | Turn capture off | P1 | F | entries exist, Messages selected | "DESACTIVAR CAPTURA"; send SMS | All apps unselected; Ajustes "Desactivada"; new SMS not stored; old entries still listed (§10) | — |
| C-042 | Access revoked | P2 | N | C-037 | `disallow_listener`; resume the screen; send SMS | Access block returns; nothing stored; no crash | — |
| C-043 | Capture after process death | P2 | X | C-037 | `am kill $PKG` (app in background); send SMS | Captured (system rebinds the listener) | — |
| C-044 | Reminder rows and picker | P1 | F | S0 | Set each RECORDATORIOS time; cancel the picker once; "Borrar" one | Values "HH:MM"; cancel keeps the old value; Borrar → "Sin definir"; "DÍA DE PESAJE" lists Lunes..Domingo | C-044 |
| C-045 | Nothing scheduled by default | P1 | F | S0 | W3 | No reminder work enqueued (principles) | — |
| C-046 | Weigh-in reminder needs day and time | P1 | F | S0 | Only HORA DE PESAJE 10:05: W3; then DÍA DE PESAJE Martes, clock K13 near 10:05: W2 | No `weight_reminder` with time only; with both, "Hoy te toca pesarte" / "Toca para registrar tu pesaje"; tap opens Pesaje (§7) | C-046 |
| C-047 | Photo reminder off by default, then on | P1 | F | C-046 setup, no photos | W2 with switch off; switch "FOTO DE PROGRESO CADA 4 SEMANAS" on; W2 next Tuesday | Off: only the weigh-in notification. On: also "Toca tu foto de progreso" / "Tómala hoy junto a tu pesaje."; tap opens Fotos (§7) | — |
| C-048 | Photo every 28 days | P2 | E | photo dated Tue 2026-11-10; switch on; weigh-in Martes | W2 on Tue Dec 1 (21 d) and Tue Dec 8 (28 d) | Dec 1: no photo notification; Dec 8: photo notification | — |
| C-049 | Picker survives rotation | P3 | X | S0 | Open a reminder picker; rotate | Picker still open or cleanly closed; no crash | — |
| C-050 | Notifications permission revoked | P2 | X | C-046 setup | `pm revoke` POST_NOTIFICATIONS; W2 | No notification; no crash | — |
| C-051 | Changing a reminder time replaces it | P3 | F | C-046 | Change HORA DE PESAJE to 11:00; W3 | One `weight_reminder` with tag `reminder-time:TUESDAY 11:00@America/Lima` | — |
| C-052 | Reinstall over existing data | P1 | X | S-C0 with weights and checks | `installDebug` again (no clear) | All data kept; no onboarding | — |
| C-053 | Edge-to-edge and nav modes | P2 | X | S-C0 | Gesture nav then 3-button nav: Diario, Historia, Pesaje with keyboard | No content under the status or navigation bar; bottom tabs above the system bar; keyboard does not cover the save button | C-053 |
| C-054 | Process death on a tab | P3 | X | S-C0 on Historia | §3.9 process death | Relaunch without crash; data shown | — |
| C-055 | Restore without photo files | P2 | E | B-style export with photos, then `pm clear`, import | Open Fotos, Historia | No crash; missing images show a placeholder (backup holds only the DB) | — |

### 4.4 Cross-cutting coverage map

Each concern has at least one case in a suite: rotation (A-020, A-040, B-007, C-025, C-033, C-049), process death (A-021, B-008, B-066, C-024, C-043, C-054), first run and empty states (A-009, A-051, B-001, B-011, B-012, C-001, C-011, C-018), validation (A-015, A-037, B-013..B-017, B-023, B-043, B-047, B-061, C-012), double taps (A-018, A-041, B-019, B-026, B-028, B-045, B-048, C-009), back and deep links (A-019, A-056, A-057, B-010, B-064, P0-08, P0-11), font scale (A-058, B-020, C-034), insets and IME (A-022, B-065, C-053), dark theme (B-063), accessibility (A-059, C-010), no INTERNET (B-062), upgrade and reinstall (C-052, R-13).

### 4.5 Phase 2 regression smoke

Run on the fixed SHA after retesting each fixed issue's own case.

| ID | Runs | Note |
|---|---|---|
| R-01 | P0-01, P0-02 | install, onboarding, tabs |
| R-02 | A-002, A-007 | Home routing |
| R-03 | A-010, A-017, A-023, A-024 | pre-outing decisions |
| R-04 | A-033, A-034, A-038 | check-in and pending |
| R-05 | A-043, A-047 | unplanned, return time |
| R-06 | A-050 | weekly summary |
| R-07 | B-022, B-027, B-038 | debts and reminder |
| R-08 | B-044, B-046, B-053 | goal and not spent |
| R-09 | B-055, B-056 | backup round trip |
| R-10 | C-002, C-005, C-013 | checks, app day, weigh-in |
| R-11 | C-019, C-028 | photo, calendar |
| R-12 | C-037 | capture |
| R-13 | Upgrade install | On a device holding the Phase 1 build with S-A0 + FX-AW data, `installDebug` the fixed SHA without clearing: all data kept. If `AppDatabase.SCHEMA_VERSION` changed, `RoomMigrations.ALL_MIGRATIONS` must contain the step; data loss = S1 (§12) |
| R-14 | B-062 | no INTERNET |

## 5. Test case format

Every case above has: ID, title, priority, type, preconditions (clock preset and seed), steps as user actions, the expected result as on-screen facts with the PRD section, and evidence. A tester adds nothing to the case; deviations go in the result notes.

Results per case: **PASS** (all expected facts seen), **FAIL** (any expected fact differs; file a bug), **BLOCKED** (a precondition or tool is not available; give the reason), **SKIP** (orchestrator said so).

## 6. Seed data recipes

### 6.1 UI recipes

| Seed | Steps |
|---|---|
| S0 | `pm clear`; grant POST_NOTIFICATIONS; launch; "Omitir" |
| S-A0 | S0 + values in §4.1 header, entered in Ajustes, Deudas and Meta de compra |
| S-A1 | S-A0; Clock K3; pre-outing "Salgo": 100, 3, 02:00, both checklist items, "Confirmar salida" → planned outing on Fri 6 Nov, pending |
| S-B0 | S0 |
| S-C0 | S0 + §4.3 header values |
| Multi-day UI data | Repeat the action at successive clock presets, force-stopping between them. One outing or won night per app day (unique index on `outing.app_day`) |

### 6.2 Fixtures through backup import

A fixture is an exported backup edited with `sqlite3` on the macOS host (`/usr/bin/sqlite3`). It keeps the live schema (version 10, Room identity hash `6bd62271070669e8ddd8de9e9a02bffc` from `app/schemas/.../10.json`). Proven in P0-17.

1. On S0, Ajustes > BACKUP "Exportar" to Downloads as `base.db`; `adb -s $S pull /sdcard/Download/base.db <scratchpad>/base.db`.
2. `cp base.db fx.db && sqlite3 fx.db < fx.sql`.
3. `adb -s $S push fx.db /sdcard/Download/`; media scan (§3.9).
4. "+ Importar", pick `fx.db`; force-stop; relaunch.

Column facts (from the v10 schema): dates `YYYY-MM-DD`; money in cents; times in minutes since midnight (02:00 = 120); `outing.kind` `PLANNED`/`UNPLANNED`; a pending outing has `spent_cents` and `mood` NULL; `daily_check.type` `GYM`/`DIET`/`ENGLISH`/`ALCOHOL`, `is_done` 1/0 (ALCOHOL 1 = drank); `user_setup.weigh_in_day` ISO number (Mon = 1); `debt_payment.month` `YYYY-MM`.

Common setup block (`setup.sql`):

```sql
INSERT OR REPLACE INTO user_setup (id, target_weight_kg, monthly_outing_limit, typical_outing_cost_cents,
  gym_weekly_target, diet_weekly_target, english_weekly_target, alcohol_weekly_target,
  pre_outing_reminder_minutes, check_in_reminder_minutes, weigh_in_day, weigh_in_reminder_minutes,
  is_photo_reminder_enabled, debt_reminder_minutes)
VALUES (1, 75.0, 4, 10000, 4, 5, 3, 2, NULL, NULL, NULL, NULL, 0, NULL);
INSERT INTO debt (name, balance_cents, installment_cents, payment_day) VALUES ('Banco', 200000, 40000, 15);
INSERT OR REPLACE INTO purchase_goal (id, name, price_cents) VALUES (1, 'Zapatillas', 45000);
```

| Fixture | Content (after `setup.sql`) |
|---|---|
| FX-A3 | `INSERT INTO outing (app_day, kind, limit_money_cents, limit_drinks, limit_return_minutes, spent_cents, drinks, return_minutes, mood) VALUES ('2026-10-09','PLANNED',10000,3,120,9000,2,90,4), ('2026-10-16','PLANNED',10000,3,120,12000,4,150,2), ('2026-10-23','PLANNED',10000,3,120,15000,3,100,3);` |
| FX-A2P | FX-A3 with the 10-16 row's `spent_cents, drinks, return_minutes, mood` = NULL |
| FX-A3U | FX-A3 with the 10-23 row replaced by `('2026-10-23','UNPLANNED',NULL,NULL,NULL,30000,NULL,NULL,2)` and the 10-16 spend 12000 kept, 10-09 9000 → average (9000+12000+30000)/3 = S/ 170.00 |
| FX-A2PEND | outings (column list as FX-A3) `('2026-11-04','PLANNED',10000,3,120,NULL,NULL,NULL,NULL), ('2026-11-06','PLANNED',10000,3,120,NULL,NULL,NULL,NULL)` |
| FX-AW | Week Nov 2–8, column lists as FX-A3: outings `('2026-11-04','PLANNED',10000,3,120,9000,2,90,4)` (kept), `('2026-11-06','PLANNED',10000,3,120,15000,3,100,2)` (money broken); `INSERT INTO won_night VALUES ('2026-11-07', 10000);`; daily checks: `INSERT INTO daily_check VALUES ('2026-11-02','GYM',1),('2026-11-03','GYM',1),('2026-11-03','DIET',1),('2026-11-07','DIET',1),('2026-11-07','ALCOHOL',0),('2026-11-06','ALCOHOL',1);`; weights: `INSERT INTO daily_weight (id, date, weight, note, created_at, updated_at) VALUES ('w1','2026-11-02',80.4,NULL,0,0),('w2','2026-11-08',79.9,NULL,0,0);`; payment `INSERT INTO debt_payment VALUES (1,'2026-11',40000);` plus `UPDATE debt SET balance_cents = 160000 WHERE id = 1;`; contribution `INSERT INTO goal_contribution (app_day, amount_cents) VALUES ('2026-11-05', 10000);` |
| FX-C | FX-AW plus `INSERT INTO outing (app_day, kind, limit_money_cents, limit_drinks, limit_return_minutes, spent_cents, drinks, return_minutes, mood) VALUES ('2026-11-09','UNPLANNED',NULL,NULL,NULL,20000,NULL,NULL,3);` |
| FX-V9 | `base.db` then `PRAGMA user_version = 9;` |
| FX-HASH | `base.db` then `UPDATE room_master_table SET identity_hash = 'qa-other-hash' WHERE id = 42;` |

Photos cannot be seeded by fixture (rows point at files in app storage); take them through the UI.

## 7. Bug reporting

One GitHub issue per bug in `emm3000/mybest`. Search first: `gh issue list --repo emm3000/mybest --state open --search "<keywords>"`; if it exists, comment with the new case ID instead of filing.

```sh
gh issue create --repo emm3000/mybest --label needs-triage \
  --title "[QA <case ID>] <observed symptom, short>" --body-file <scratchpad>/bug.md
```

Body template:

```markdown
**Case:** A-018 · **Severity:** S1 · **Suite:** A
**Build:** <short SHA> · **Device:** emulator-5554, Medium_Phone_5554, API 36 google_apis_playstore, TZ America/Lima
**Clock:** K3 (method M1) · **Seed:** S-A1

### Steps
1. …

### Expected
… (PRD §3)

### Actual
…

### Evidence
![A-018](https://raw.githubusercontent.com/emm3000/mybest/assets/qa-a/A-018-<sha>.png)
Crash log: `adb -s emulator-5554 logcat -d -b crash` excerpt.

### Notes
FLAG (PRD vs code) if applicable.
```

Severity:

| Sev | Meaning |
|---|---|
| S1 | Crash, ANR, data loss or corruption, wrong money total or balance |
| S2 | A PRD acceptance criterion fails with no workaround |
| S3 | Functional defect with a workaround, or wrong/misleading copy |
| S4 | Cosmetic: layout, spacing, accents, English text in the Spanish UI |

Screenshots: branch `assets/qa-<suite>` (`assets/qa-smoke`, `assets/qa-a`, `assets/qa-b`, `assets/qa-c`, `assets/qa-retest`), built with the orphan-worktree steps of multi-session.md "Visual check", file names `<case ID>-<sha>.png`, new screenshots as new commits, never force-pushed. Link them with `raw.githubusercontent.com` URLs.

Testers do not fix code, commit to `main`, open PRs, or change labels beyond `needs-triage`. The orchestrator triages.

## 8. Reporting back

Each peer sends the orchestrator one message at the end of its suite:

```text
Suite <A|B|C> · build <sha> · emulator-<port> · clock method <M1..M4>
PASS <n> · FAIL <n> · BLOCKED <n> · SKIP <n> (of <total>)
Bugs: #<n> (S1, A-018), #<n> (S3, A-029) …
Blocked: A-053 (W2 and W1 both failed: …), B-060 (needs code hook)
FLAGs confirmed: A-029, B-032 …
Assets: assets/qa-<suite>
```

Results matrix (one per suite, kept in the peer's scratchpad and pasted into the report when asked):

| ID | Result | Issue | Evidence | Notes |
|---|---|---|---|---|
| A-001 | PASS | — | A-001-<sha>.png | |
| A-002 | FAIL | #123 | A-002-<sha>.png | Home stayed on checks |
| A-003 | BLOCKED | — | — | clock method failed |

Phase 0 additionally returns the tooling table from §3.9.

## 9. Risks and open questions for the owner

1. **Debug clock override (M5).** Every time-dependent case depends on changing the device clock. A debug-only offset clock bound in Koin would make Suite A and C runs fast and repeatable. Acceptable as a small debug-only change?
2. **Estimated cost rule.** PRD §3: typical cost "until three outings are recorded". `GetEstimatedOutingCostUseCase` waits for three outings *with a check-in* and averages their spend (unplanned included). Which is intended? (A-029, A-030)
3. **Which month a "mark paid" books.** `MarkInstallmentPaidUseCase` books the current app-day month, while Home, the pre-outing and the reminder may already show next month's installment. Paying early or late can double-deduct (B-032).
4. **Paid-off debts as "next installment".** `ObserveNearestInstallmentUseCase` does not skip a S/ 0.00 balance; the reminder does (B-036, B-030).
5. **Return at or after 12:00.** `Limits.evaluate` treats 12:00–05:00 as the same evening, so a next-day 13:00 return keeps a 02:00 limit (A-036).
6. **No double-tap guard** on "Confirmar salida" (a second insert throws in `OutingRepositoryImpl.insert`, likely a crash), new-debt "Guardar" and contribution "Guardar" (A-018, B-026, B-048). The goal save is an upsert, so its double tap is harmless (B-045).
7. **Onboarding editor sheet closes on rotation** (`DismissEditorsOnLeave` runs on recreation) (B-007).
8. **Restore after `database.close()`.** If `Files.move` fails, the DB is already closed and the app keeps running on it; nothing restarts the app after a successful restore either (B-057, B-060). Needs a unit/instrumented test with an injectable move; testers cannot force it.
9. **Weekly summary has no notification.** Only Home routing opens it on Sunday evening; `ACTION_WEEKLY_SUMMARY` exists but no worker posts it. Is a Sunday notification expected under §8?
10. **No in-app notifications switch**; the preference defaults to on and can only be changed through system settings.
11. **Purchase goal cannot be edited**, only removed with its contributions (B-052, PRD §1).
12. **Backup scope:** the DB only; photos and preferences (onboarding flag) are not included (C-055).
13. **Listener declared `exported="false"`**; if the system cannot bind it, capture never works (P0-12).
14. **Copy:** raw English/exception messages in snackbars ("Invalid SQLite backup file", "Error al guardar: <exception>"), unaccented weekday names ("sabado", "miercoles") in contribution dates (B-046, B-059).
15. **Target weight range** is now 20–500 kg like the weigh-in (B-014). A target already stored out of range blocks saving other setup fields until it is fixed or cleared.

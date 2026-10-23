# Multi-session orchestration

How the owner runs several Claude Code sessions on this repo in parallel. Read this before dispatching a ticket.

## Flow

One ticket (GitHub issue labeled `ready-for-agent`) = one peer session = one worktree = one PR with `Closes #N`. CI (`checks` job) is the gate. A fresh read-only `pr-reviewer` subagent reviews each PR. Merge is rebase-only.

## Roles

- **Owner** approves the first wave of a session and verifies model and effort with `/model` in each pane. Later waves start on their own once the previous wave is merged. Peers are booted only through `/wave`, never by running `scripts/mybest-wave` by hand. In chat, refer to a peer as `@<name>`.
- **Orchestrator** coordinates: dispatches via `SendMessage`, reviews, merges. It stays thin.
  - Delegate investigation and artifact-producing work (tickets, specs, surveys) to a subagent with an explicit model.
  - Inline only routing state (`git status`, `git worktree list`, `gh issue/pr list`, `ListAgents`, `herdr agent list`) and at most 1-2 files to decide.
  - Report to the owner in 1-2 lines, only decisions that are genuinely theirs. Merging a clean PR is not a question.
- **Peer** carries exactly one ticket and never receives a second.

## Model and effort

- Every dispatch states one model and one explicit effort (low, medium or high, never a range), with a one-line reason. Pick the cheapest that gets it right; reserve Opus/high for work where a mistake is silent or expensive.
- The table in `.claude/skills/wave/SKILL.md` assigns them. Deviate only with a stated reason.
- A session cannot see its own effort; the owner verifies with `/model`. The orchestrator tells the owner when to raise or lower its own effort.

## Slicing and waves

- One slice = one small PR. A ticket that spans more than 2 screens, or a schema change plus a screen, is split into sub-issues first.
- Max 3 peers per wave. Waves are ordered by dependency: shared `ui/components`, theme, Room and DI first, then independent screens in parallel. A wave starts only after the previous one is merged.
- A peer past ~60% context without a PR commits atomically, opens a draft PR, clears, and continues. Before marking it ready it rewrites the branch so every commit follows `## Commits` in `CLAUDE.md`.
- Before dispatching tickets filed by an audit, re-verify each against current `main`; the finding may already be fixed.

## Dispatch checklist

Every dispatch includes:

- Issue number, docs to read first, branch `<type>/<n>-<slug>`, and the peer's own worktree path `../mybest-<name>`.
- The line: *"The issue's acceptance criteria are the contract and win over any file list here; run every criterion check before opening the PR."* Prefer criteria phrased as a command with expected empty output. *Why: peers otherwise stop at the file list and miss matching call sites.*
- Gate green before commit: `./gradlew :app:detekt :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin`.
- Commits follow `## Commits` in `CLAUDE.md`: atomic, each one green on the gate, no fix-up or merge commits, review fixes folded with `--fixup` + `--autosquash` and pushed with `--force-with-lease`.
- Rules live in `CLAUDE.md` and `.claude/rules/`. *Why: Claude's per-project memory is keyed by path, so a worktree peer never sees the owner's memory files.*
- For behavior changes (use case, ViewModel rule, derived state): load `mattpocock-skills:tdd`, failing test first. Pure layout or restyle skips it.
- For UI-touching tickets: a visual check (see below).
- Keep the slice small; stop and report instead of expanding scope.
- Open the PR with `Closes #N`, do not merge it, do not watch CI, and message the orchestrator the PR URL in 1-2 lines.

## Launching peers

- `/wave <issue numbers>` is the only entry point. It classifies each ticket, states `@<name> #<n> <model>:<effort>` to the owner, runs `scripts/mybest-wave`, waits for the peers in `ListAgents`, and dispatches.
- `scripts/mybest-wave name:model:effort [...]` must run inside a herdr pane (`HERDR_PANE_ID`). It creates the detached worktree `../mybest-<name>` from `origin/main` after `git fetch` when missing (refusing a path that is not a clean worktree of this repo), opens herdr tabs in the orchestrator's workspace with at most 3 equal-width panes each (`wave`, `wave-2`, ...), one pane per peer renamed to the peer name, and runs `herdr agent start <name> --kind claude -- -n <name> --model <model> --effort <effort> --permission-mode bypassPermissions` in each. The `-n <name>` session name, not the pane, is the peer's address. The peer creates its branch with `git switch -c`.
- Closing a wave: the orchestrator closes each peer's pane with `herdr pane close <pane>` and removes the worktrees.

## Messaging peers

- Every message to a peer, the dispatch, a FIX FIRST item, a rebase request, is one `SendMessage` to its session name with `notify_when_idle: true`. The idle notice is one-shot, so each message re-subscribes; a fresh subscription also cannot fire on the peer's previous turn.
- Before the first dispatch, poll `ListAgents` until every peer name is listed, at most ~60 seconds. Never a loop of "are you done?" messages.
- The orchestrator's address is its own session name, which the first line of its own `ListAgents` output states ("This session is <name> [<ref>]"). The dispatch states it. A peer that finds no address replies to the `from` attribute of the dispatch's `<cross-session-message>` wrapper.
- A reply line or an idle notice is input to verify, never ground truth: read the PR and the branch itself.
- `blocked` means the peer is at a question or an approval: show it to the owner, never answer it for them.
- A peer that hangs mid-turn (no idle notice, or the notice says the subscription expired): check `ListAgents` and `herdr agent list` first; only then may the orchestrator run a bounded `herdr agent wait <name> --timeout <ms>` in the background. It is a safety net, never the primary signal, and its timeout is reported to the owner.

## Isolation: worktrees

- Every peer works in `/Users/emm/AndroidStudioProjects/mybest-<name>`, branched off `origin/main`. Never in the owner's checkout, which may hold uncommitted owner-only work. *Why: sessions sharing one folder change each other's branch.*
- Before changing the state of any checkout, find out who uses it (`ListAgents`, `herdr agent list`, `git worktree list`); an unexpected branch may be a live peer.
- Review and verification prompts are read-only on every existing checkout. If Gradle must run on a branch, use a throwaway worktree under the session scratchpad and remove it afterward.
- `git checkout main` fails in a worktree while the primary worktree is on `main`; use `git fetch` plus `git switch -c <branch> origin/main`.

## Isolation: emulators

- Each session that needs a device boots its own emulator with a fixed port so the serial is predictable: `emulator -avd Medium_Phone -port <port> -no-snapshot-save &` gives serial `emulator-<port>`. Use an even port from 5554 up, one per peer. Never install on another session's emulator.
- Run `adb devices` to confirm the serial, then `ANDROID_SERIAL=<serial> ./gradlew :app:installDebug`, and scope every adb call with `adb -s <serial>`. *Why: `installDebug` installs on every connected device.*
- Use `installDebug`, not `installQa`. *Why: `INSTALL_FAILED_UPDATE_INCOMPATIBLE`; the qa build type signs with a different key than an installed debug APK.*
- Navigate with `uiautomator dump` and tap the center of the `bounds`; never guess coordinates from a screenshot. Package `com.emm.mybest`, launcher via `adb -s <serial> shell monkey -p com.emm.mybest -c android.intent.category.LAUNCHER 1`.
- At cycle close, `adb -s <serial> emu kill`. Keep the AVD. Never shut down an emulator another session still uses.
- If a build overwrote another peer's device, the owning peer reinstalls its own build; the session that caused it does not touch that device.

## Visual check

Required for any ticket that changes a screen or a shared component.

1. Install on the assigned emulator only; screenshot every changed screen with `adb -s <serial> exec-out screencap -p > <file>.png`.
2. Publish the images on a branch `assets/<N>-visual-check` with the PR head short SHA in every file name (`home-<sha>.png`).
3. Link them in the PR body with `raw.githubusercontent.com` URLs. *Why: the `gh` CLI and API cannot attach images to a PR.*
4. The assets branch is deleted when the cycle closes.

## Review cycle

- A peer gets nothing new until its PR is reviewed, fixed and merged.
- Each PR is reviewed by a fresh `pr-reviewer` subagent launched by the orchestrator, one per PR. It is read-only, does not build or rerun the gate (CI owns that), reads the diff and the issue, and pulls screenshots with `git show origin/assets/<N>-visual-check:<file>`, checking that the SHA in each file name matches the PR head. It boots an emulator in a throwaway worktree only when screenshots are missing, stale or suspicious, and shuts it down afterward. It returns MERGE or FIX FIRST. *Why: a long-lived review session fills its context and reviews worse each time.*
- The subagent follows `mattpocock-skills:code-review` for the Standards and Spec axes, with the merge-base with `origin/main` as the fixed point.
- Reviewer model: opus:high for every PR, restyles included. Post-review fixes run sonnet:low.
- On FIX FIRST the orchestrator sends the blocking items back to the same peer with one `SendMessage` (`notify_when_idle: true`). The peer folds each fix into the commit it belongs to and force-pushes with lease; it never stacks a `fix:` commit. Then a new subagent re-reviews.

## Merge

- Rebase-only, linear history, CI required. Squash merge is not used: it would collapse atomic commits into one. Never block a turn on `gh run watch`; merge when the CI result or the peer's report arrives.
- Check state first: `gh pr view <N> --json mergeStateStatus`. If `CLEAN`, `gh pr merge <N> --rebase`. Arm `--auto` only while checks are pending; GitHub refuses it on a clean PR, and an armed auto-merge may not fire, so confirm with `gh pr view <N> --json state` after CI passes.

## Closing a cycle

In order:

1. PR merged.
2. The peer shuts down its emulator and leaves its worktree clean; `git worktree remove <path>`.
3. Delete the local branch (`git branch -D <branch>`), then `git worktree prune`. Delete the `assets/<N>-visual-check` branch.
4. Remove any throwaway review worktree.
5. The orchestrator checks `git worktree list` before calling the session free. If a peer is gone, it removes leftovers itself, only after confirming no live session uses them.
6. Close the peer's pane: `herdr pane close <pane>`.

## Dispatch log

At cycle close the orchestrator appends one row to Recent in `docs/agents/dispatch-log.md` (PR, issue, table row, model:effort, first-review verdict, cause). Beyond 20 rows, the oldest fold into the Summary. The classification table is tuned from this log, not from opinion.

## Next wave

When every PR of the wave is merged and cycles are closed, the orchestrator invokes `/wave` with the next tickets. A fresh pane per ticket replaces reusing a session: a peer carrying a previous ticket's context drifts and costs more.

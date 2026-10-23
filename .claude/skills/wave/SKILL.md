---
name: wave
description: "Boot one herdr pane per GitHub ticket and dispatch each ticket to its own peer Claude Code session (max 3 peers per wave). Trigger: /wave, start a wave, open peers, dispatch wave; also levantar wave, abrir peers, lanzar sesiones."
argument-hint: <issue numbers>
allowed-tools: Bash(gh:*) Bash(scripts/mybest-wave:*) Bash(herdr:*) Bash(git worktree:*) ListAgents SendMessage Read
license: Apache-2.0
metadata:
  author: "emm3000"
  version: "1.0"
---

## Activation Contract

Run when the owner invokes `/wave` with one or more issue numbers, or when the orchestrator starts the next wave after the previous wave is fully merged. Each number becomes one peer session and one dispatch. Stop and report if there are more than 3 numbers, or if any number is not an open `ready-for-agent` issue.

## Hard Rules

- Read `docs/agents/multi-session.md` first. Its dispatch checklist and isolation rules bind every dispatch.
- Never queue two tickets on one peer. Never dispatch while a PR from the same wave is unmerged.
- One explicit model and one explicit effort per ticket, stated to the owner before booting.
- Never touch the owner's checkout. Peers get their own worktree from `scripts/mybest-wave`.
- The orchestrator must itself run inside herdr: the script refuses without `HERDR_PANE_ID`. herdr only opens the panes; every message to a peer, the dispatch and each follow-up, is a `SendMessage` to its session name with `notify_when_idle: true` (`docs/agents/multi-session.md`, Messaging peers).
- Two tickets in one wave must not touch the same shared component, theme token, DAO or `AppModule.kt` section.

## Decision Gates

Rows are ordered by blast radius. A ticket that matches several rows takes the highest-numbered row that matches.

| Row | Work | Model:effort | Extra instruction |
|---|---|---|---|
| 1 | Docs, strings, renames; every criterion is a command with empty output | sonnet:low | none: the gate and the criteria fail loudly |
| 2 | One screen, layout or restyle only | sonnet:medium | visual check |
| 3 | ViewModel or use-case behavior | sonnet:high | load `mattpocock-skills:tdd`, red-green-refactor |
| 4 | Shared `ui/components` or theme change | sonnet:high | visual check of every affected screen |
| 5 | Room schema or migration, DI wiring, WorkManager or reminders, backup import/export | opus:high | visual check if a screen changes |

Sonnet writes the code on rows 1-4. Row 5 is the only Opus implementation row: its failures (data-dropping migrations, missing Koin bindings, workers that never run) compile and pass CI, so no gate catches them.

Other roles: every PR review opus:high (the reviewer is the only judgment check, and a review costs far less than an implementation), post-review fixes sonnet:low, ticket-writer opus:high.

Every row is a bet until `docs/agents/dispatch-log.md` says otherwise. When a row shows two or more first-review FIX FIRST verdicts with cause `judgment`, raise it one step and note why here.

- Row 3 moved from sonnet:medium to sonnet:high (2026-10-23): PRs #44, #45 and #48 each got a first-review FIX FIRST with cause `judgment` (ignored use-case result, unguarded save/delete, view logic left in composables).

## Execution Steps

1. For each issue run `gh issue view <n> --json title,labels,body`. Confirm the `ready-for-agent` label and derive a short lowercase pane name from the title (one word, no digits).
2. Classify each ticket with the table. Deviate only with a one-line reason in the plan. Tell the owner one line per ticket, `@<name> #<n> <model>:<effort>`, before booting anything.
3. Run `scripts/mybest-wave <name>:<model>:<effort> ...` once with every ticket. It creates `../mybest-<name>` detached at `origin/main` (after `git fetch`), opens herdr tabs of at most 3 equal-width panes, one pane per peer renamed to the peer name, and starts `claude -n <name>` in each.
4. Poll `ListAgents` until every peer name is listed, at most 60 seconds. A name still missing after that: check `herdr agent list` and report to the owner instead of dispatching to it.
5. Send each peer one dispatch with `SendMessage` (`to: <name>`, `notify_when_idle: true`), naming the orchestrator's own address for the reply, built from the playbook checklist: issue, docs to read, branch `<type>/<n>-<slug>`, its worktree `../mybest-<name>`, the acceptance-criteria line, the gate, TDD or visual check per the table, `Closes #<n>`, no merge, reply with the PR URL.
6. Report to the owner in one or two lines: peers booted, tickets dispatched.

## Output Contract

Return the list `@<name> #<n> <model>:<effort>` and nothing else until a peer reports back.

## References

- `docs/agents/multi-session.md` — dispatch checklist, isolation, review cycle.
- `scripts/mybest-wave` — worktrees plus named herdr panes plus `claude -n <name>`.

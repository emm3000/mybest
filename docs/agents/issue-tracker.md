# Issue tracker: GitHub

Issues and specs for this repo (`emm3000/mybest`) live as GitHub issues. Use the `gh` CLI; inside a clone it infers the repo from `git remote -v`.

## Commands

- Create: `gh issue create --title "..." --body "..." --label ready-for-agent` (heredoc for multi-line bodies).
- Read: `gh issue view <number> --comments`.
- List open tickets: `gh issue list --state open --label ready-for-agent --json number,title,labels --jq '[.[] | {number, title, labels: [.labels[].name]}]'`.
- Comment: `gh issue comment <number> --body "..."`.
- Labels: `gh issue edit <number> --add-label "..."` / `--remove-label "..."`.
- Close: `gh issue close <number> --comment "..."`.
- PRs: `gh pr view <n>`, `gh pr diff <n>`, `gh pr checks <n>`, `gh pr merge <n> --rebase`.

GitHub shares one number space across issues and PRs: resolve a bare `#42` with `gh pr view 42`, falling back to `gh issue view 42`.

When a skill says "publish to the issue tracker", create a GitHub issue. When it says "fetch the relevant ticket", run `gh issue view <number> --comments`.

## Labels

| Label | Meaning |
|---|---|
| `needs-triage` | Maintainer needs to evaluate this issue |
| `needs-info` | Waiting on reporter for more information |
| `ready-for-agent` | Fully specified; a peer session can take it. `/wave` accepts only these |
| `ready-for-human` | Requires human implementation |
| `wontfix` | Will not be actioned |

The labels must exist in the repo before first use (`gh label list`; create with `gh label create <name>`). External PRs are not a triage surface.

# Dispatch log

The table in `.claude/skills/wave/SKILL.md` is tuned from this log. A table row with two or more `judgment` FIX FIRST verdicts moves one step up. A row that stays MERGE across many PRs may move one step down.

The log has a fixed size. The Summary keeps the totals per table row forever. Recent keeps only the last 20 PRs. At cycle close the orchestrator appends the new PR to Recent. When Recent passes 20 rows, it adds the oldest rows to the Summary counts and deletes them.

Cause values: `checklist` (a recurring item from the dispatch checklist, the model was fine), `judgment` (a wrong decision the model made), `spec` (the issue was wrong or thin).

## Summary

Totals of rows already folded out of Recent.

| Row | Model:effort | PRs | MERGE | FIX FIRST checklist | FIX FIRST judgment | FIX FIRST spec |
|---|---|---|---|---|---|---|
| 1 | sonnet:low | 1 | 1 | 0 | 0 | 0 |
| 2 | sonnet:medium | 2 | 2 | 0 | 0 | 0 |
| 3 | sonnet:medium | 7 | 3 | 1 | 3 | 0 |
| 3 | sonnet:high | 15 | 8 | 5 | 2 | 0 |
| 4 | sonnet:high | 2 | 2 | 0 | 0 | 0 |
| 5 | opus:high | 13 | 9 | 0 | 4 | 0 |

## Recent

Last 20 PRs, oldest first. Model:effort is what actually ran, which may differ from the table row.

| PR | Issue | Row | Model:effort | First review | Cause |
|---|---|---|---|---|---|
| #90 | #27 | 5 | opus:high | MERGE | - |
| #92 | #52 | 5 | opus:high | MERGE | - |
| #94 | #37 | 5 | opus:high | MERGE | - |
| #95 | #91 | 5 | opus:high | FIX FIRST | checklist |
| #96 | #93 | 5 | opus:high | MERGE | - |
| #97 | #30 | 5 | opus:high | MERGE | - |
| #98 | #78 | 5 | opus:high | MERGE | - |
| #99 | #47 | 4 | sonnet:high | FIX FIRST | judgment |
| #107 | #103 | 1 | sonnet:low | MERGE | - |
| #108 | #101 | 5 | opus:high | MERGE | - |
| #109 | #102 | 5 | opus:high | MERGE | - |
| #110 | #106 | 2 | sonnet:medium | MERGE | - |
| #111 | #105 | 3 | sonnet:high | MERGE | - |
| #112 | #104 | 3 | sonnet:high | MERGE | - |
| #140 | #139 | 3 | sonnet:high | MERGE | - |
| #141 | #138 | 4 | sonnet:high | FIX FIRST | judgment + spec |
| #142 | #125 | 5 | opus:high | MERGE | - |
| #146 | #143 | 3 | sonnet:high | MERGE | - |
| #149 | #144 | 2 | sonnet:medium | FIX FIRST | checklist |
| #145 | #118 | 3 | sonnet:high | FIX FIRST | judgment |

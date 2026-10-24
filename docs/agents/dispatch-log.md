# Dispatch log

The table in `.claude/skills/wave/SKILL.md` is tuned from this log. A table row with two or more `judgment` FIX FIRST verdicts moves one step up. A row that stays MERGE across many PRs may move one step down.

The log has a fixed size. The Summary keeps the totals per table row forever. Recent keeps only the last 20 PRs. At cycle close the orchestrator appends the new PR to Recent. When Recent passes 20 rows, it adds the oldest rows to the Summary counts and deletes them.

Cause values: `checklist` (a recurring item from the dispatch checklist, the model was fine), `judgment` (a wrong decision the model made), `spec` (the issue was wrong or thin).

## Summary

Totals of rows already folded out of Recent.

| Row | Model:effort | PRs | MERGE | FIX FIRST checklist | FIX FIRST judgment | FIX FIRST spec |
|---|---|---|---|---|---|---|
| 1 | sonnet:low | 0 | 0 | 0 | 0 | 0 |
| 2 | sonnet:medium | 0 | 0 | 0 | 0 | 0 |
| 3 | sonnet:medium | 7 | 3 | 1 | 3 | 0 |
| 3 | sonnet:high | 9 | 4 | 4 | 1 | 0 |
| 4 | sonnet:high | 0 | 0 | 0 | 0 | 0 |
| 5 | opus:high | 9 | 7 | 0 | 2 | 0 |

## Recent

Last 20 PRs, oldest first. Model:effort is what actually ran, which may differ from the table row.

| PR | Issue | Row | Model:effort | First review | Cause |
|---|---|---|---|---|---|
| #65 | #62 | 3 | sonnet:high | MERGE | - |
| #66 | #21 | 5 | opus:high | FIX FIRST | judgment |
| #67 | #26 | 3 | sonnet:high | FIX FIRST | judgment |
| #73 | #71 | 3 | sonnet:high | MERGE | - |
| #74 | #69 | 5 | opus:high | FIX FIRST | judgment |
| #75 | #72 | 3 | sonnet:high | FIX FIRST | checklist |
| #76 | #68 | 5 | opus:high | MERGE | - |
| #80 | #79 | 3 | sonnet:high | MERGE | - |
| #81 | #77 | 5 | opus:high | MERGE | - |
| #82 | #70 | 3 | sonnet:high | MERGE | - |
| #85 | #63 | 1 | sonnet:low | MERGE | - |
| #86 | #84 | 2 | sonnet:medium | MERGE | - |
| #87 | #83 | 4 | sonnet:high | MERGE | - |
| #88 | #29 | 4 | sonnet:high | MERGE | - |
| #89 | #64 | 2 | sonnet:medium | MERGE | - |
| #90 | #27 | 5 | opus:high | MERGE | - |
| #92 | #52 | 5 | opus:high | MERGE | - |
| #94 | #37 | 5 | opus:high | MERGE | - |
| #95 | #91 | 5 | opus:high | FIX FIRST | checklist |
| #96 | #93 | 5 | opus:high | MERGE | - |

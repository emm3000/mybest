# Dispatch log

The table in `.claude/skills/wave/SKILL.md` is tuned from this log. A table row with two or more `judgment` FIX FIRST verdicts moves one step up. A row that stays MERGE across many PRs may move one step down.

The log has a fixed size. The Summary keeps the totals per table row forever. Recent keeps only the last 20 PRs. At cycle close the orchestrator appends the new PR to Recent. When Recent passes 20 rows, it adds the oldest rows to the Summary counts and deletes them.

Cause values: `checklist` (a recurring item from the dispatch checklist, the model was fine), `judgment` (a wrong decision the model made), `spec` (the issue was wrong or thin).

## Summary

Totals of rows already folded out of Recent.

| Row | Model:effort | PRs | MERGE | FIX FIRST checklist | FIX FIRST judgment | FIX FIRST spec |
|---|---|---|---|---|---|---|
| 1 | sonnet:low | 4 | 4 | 0 | 0 | 0 |
| 1 | sonnet:medium | 1 | 0 | 1 | 0 | 0 |
| 2 | sonnet:medium | 10 | 8 | 2 | 0 | 0 |
| 3 | sonnet:medium | 7 | 3 | 1 | 3 | 0 |
| 3 | sonnet:high | 38 | 30 | 5 | 3 | 0 |
| 4 | sonnet:high | 4 | 2 | 0 | 2 | 1 |
| 4 | opus:medium | 1 | 1 | 0 | 0 | 0 |
| 5 | opus:high | 27 | 22 | 1 | 4 | 0 |

## Recent

Last 20 PRs, oldest first. Model:effort is what actually ran, which may differ from the table row.

| PR | Issue | Row | Model:effort | First review | Cause |
|---|---|---|---|---|---|
| #208 | #198 | 3 | sonnet:high | MERGE | - |
| #209 | #201 | 5 | opus:high | FIX FIRST | judgment |
| #210 | #202 | 3 | sonnet:high | MERGE | - |
| #211 | #199 | 3 | sonnet:high | MERGE | - |
| #212 | #203 | 3 | sonnet:high | MERGE | - |
| #213 | #204 | 3 | sonnet:high | MERGE | - |
| #215 | #205 | 4 | opus:medium | MERGE | - |
| #216 | #214 | 4 | opus:medium | FIX FIRST | judgment |
| #218 | #217 | 4 | opus:medium | MERGE | - |
| #220 | #219 | 3 | sonnet:high | MERGE | - |
| #222 | #221 | 5 | opus:high | MERGE | - |
| #226 | #223 | 3 | sonnet:high | MERGE | - |
| #227 | #225 | 1 | sonnet:low | MERGE | - |
| #228 | #224 | 3 | sonnet:high | MERGE | - |
| #235 | #229 | 3 | sonnet:high | MERGE | - |
| #236 | #231 | 4 | opus:medium | MERGE | - |
| #237 | #230 | 4 | opus:medium | FIX FIRST | judgment |
| #239 | #232 | 1 | sonnet:low | MERGE | - |
| #240 | #234 | 1 | sonnet:low | MERGE | - |
| #241 | #233 | 1 | sonnet:low | MERGE | - |

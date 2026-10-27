# Dispatch log

The table in `.claude/skills/wave/SKILL.md` is tuned from this log. A table row with two or more `judgment` FIX FIRST verdicts moves one step up. A row that stays MERGE across many PRs may move one step down.

The log has a fixed size. The Summary keeps the totals per table row forever. Recent keeps only the last 20 PRs. At cycle close the orchestrator appends the new PR to Recent. When Recent passes 20 rows, it adds the oldest rows to the Summary counts and deletes them.

Cause values: `checklist` (a recurring item from the dispatch checklist, the model was fine), `judgment` (a wrong decision the model made), `spec` (the issue was wrong or thin).

## Summary

Totals of rows already folded out of Recent.

| Row | Model:effort | PRs | MERGE | FIX FIRST checklist | FIX FIRST judgment | FIX FIRST spec |
|---|---|---|---|---|---|---|
| 1 | sonnet:low | 2 | 2 | 0 | 0 | 0 |
| 1 | sonnet:medium | 1 | 0 | 1 | 0 | 0 |
| 2 | sonnet:medium | 9 | 7 | 2 | 0 | 0 |
| 3 | sonnet:medium | 7 | 3 | 1 | 3 | 0 |
| 3 | sonnet:high | 33 | 25 | 5 | 3 | 0 |
| 4 | sonnet:high | 4 | 2 | 0 | 2 | 1 |
| 4 | opus:medium | 1 | 1 | 0 | 0 | 0 |
| 5 | opus:high | 24 | 19 | 1 | 4 | 0 |

## Recent

Last 20 PRs, oldest first. Model:effort is what actually ran, which may differ from the table row.

| PR | Issue | Row | Model:effort | First review | Cause |
|---|---|---|---|---|---|
| #178 | #42 | 5 | opus:high | MERGE | - |
| #182 | #179 | 3 | sonnet:high | MERGE | - |
| #183 | #180 | 5 | opus:high | MERGE | - |
| #184 | #181 | 3 | sonnet:high | MERGE | - |
| #191 | #189 | 2 | sonnet:medium | MERGE | - |
| #192 | #186 | 5 | opus:high | MERGE | - |
| #193 | #187 | 3 | sonnet:high | MERGE | - |
| #194 | #190 | 3 | sonnet:high | MERGE | - |
| #195 | #188 | 3 | sonnet:high | MERGE | - |
| #197 | #196 | 1 | sonnet:low | MERGE | - |
| #207 | #200 | 1 | sonnet:low | MERGE | - |
| #208 | #198 | 3 | sonnet:high | MERGE | - |
| #209 | #201 | 5 | opus:high | FIX FIRST | judgment |
| #210 | #202 | 3 | sonnet:high | MERGE | - |
| #211 | #199 | 3 | sonnet:high | MERGE | - |
| #212 | #203 | 3 | sonnet:high | MERGE | - |
| #213 | #204 | 3 | sonnet:high | MERGE | - |
| #215 | #205 | 4 | opus:medium | MERGE | - |
| #216 | #214 | 4 | opus:medium | FIX FIRST | judgment |
| #218 | #217 | 4 | opus:medium | MERGE | - |

# Dispatch log

The table in `.claude/skills/wave/SKILL.md` is tuned from this log. A table row with two or more `judgment` FIX FIRST verdicts moves one step up. A row that stays MERGE across many PRs may move one step down.

The log has a fixed size. The Summary keeps the totals per table row forever. Recent keeps only the last 20 PRs. At cycle close the orchestrator appends the new PR to Recent. When Recent passes 20 rows, it adds the oldest rows to the Summary counts and deletes them.

Cause values: `checklist` (a recurring item from the dispatch checklist, the model was fine), `judgment` (a wrong decision the model made), `spec` (the issue was wrong or thin).

## Summary

Totals of rows already folded out of Recent.

| Row | Model:effort | PRs | MERGE | FIX FIRST checklist | FIX FIRST judgment | FIX FIRST spec |
|---|---|---|---|---|---|---|
| 1 | sonnet:low | 2 | 2 | 0 | 0 | 0 |
| 2 | sonnet:medium | 4 | 3 | 1 | 0 | 0 |
| 3 | sonnet:medium | 7 | 3 | 1 | 3 | 0 |
| 3 | sonnet:high | 23 | 15 | 5 | 3 | 0 |
| 4 | sonnet:high | 4 | 2 | 0 | 2 | 1 |
| 5 | opus:high | 24 | 19 | 1 | 4 | 0 |

## Recent

Last 20 PRs, oldest first. Model:effort is what actually ran, which may differ from the table row.

| PR | Issue | Row | Model:effort | First review | Cause |
|---|---|---|---|---|---|
| #154 | #130 | 3 | sonnet:high | MERGE | - |
| #157 | #135 | 3 | sonnet:high | MERGE | - |
| #158 | #119 | 3 | sonnet:high | MERGE | - |
| #159 | #155 | 3 | sonnet:high | MERGE | - |
| #162 | #160 | 3 | sonnet:high | MERGE | - |
| #163 | #120 | 4 | opus:medium | MERGE | - |
| #164 | #113 | 3 | sonnet:high | MERGE | - |
| #165 | #136 | 3 | sonnet:high | MERGE | - |
| #167 | #161 | 3 | sonnet:high | MERGE | - |
| #166 | #132 | 2 | sonnet:medium | FIX FIRST | checklist |
| #168 | #137 | 2 | sonnet:medium | MERGE | - |
| #169 | #114 | 2 | sonnet:medium | MERGE | - |
| #170 | #156 | 2 | sonnet:medium | MERGE | - |
| #172 | #28 | 1 | sonnet:medium | FIX FIRST | checklist |
| #173 | #171 | 2 | sonnet:medium | MERGE | - |
| #175 | #174 | 3 | sonnet:high | MERGE | - |
| #177 | #176 | 3 | sonnet:high | MERGE | - |
| #178 | #42 | 5 | opus:high | MERGE | - |
| #182 | #179 | 3 | sonnet:high | MERGE | - |
| #183 | #180 | 5 | opus:high | MERGE | - |

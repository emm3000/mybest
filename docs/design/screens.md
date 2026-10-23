# v1 screen designs

Reference designs for the v1 screens live in the claude.ai canvas **MyBest v1 Screens**:
https://claude.ai/artifact/XzEMj9XM8MewPJKe2FWtK7 (private to the owner; read it with the Artifact tool, `action: "read"`, `path: "project/<File>.dc.html"`).

Boards are 390×844 and use the Atelier Dark tokens already in `ui/theme/`. Build against the board, not a pixel copy: spacing, type roles and colors must map to existing theme tokens. Copy is Spanish (Peru); numbers on the boards are sample data.

| Board | Screen | Ticket |
|---|---|---|
| `Main.dc.html` | Hoy: 4 daily checks, week vs targets, next installment, bottom nav | #11, #26 |
| `PreOuting.dc.html` | ¿Sales esta noche?: typical outing cost vs installment and goal, "Me quedo" primary | #14, #15 |
| `Limits.dc.html` | Outing limits (money, drinks, return hour) and the two-item checklist | #15 |
| `CheckIn.dc.html` | Next-day check-in: spent, drinks, return hour vs limits, mood 1–5 | #12 |
| `Money.dc.html` | Total debt, installments with PAGUÉ, purchase goal, not spent (estimated) | #16, #17 |
| `Summary.dc.html` | Weekly summary led by return time | #23 |
| `Calendar.dc.html` | Month grid with check, outing and relapse marks, selected day detail | #24 |

## Rules the boards follow

- Bottom nav: four text tabs, `HOY · PLATA · CALENDARIO · RESUMEN`, mono uppercase. Settings opens from the top bar.
- `done` (sage) marks kept targets and the safe choice. `error` marks only broken limits and relapses, and always comes with text ("S/ 60 de más", "recaída"), never color alone.
- Large numbers use the serif italic display role; labels use the mono micro-label role.
- Square corners, hairline dividers, no shadows or cards.
- On the pre-outing screen staying in is the primary action; going out is secondary and needs both checklist items before it enables.
- Screens with no board (settings #9 #10 #25, weigh-in #18) reuse these patterns: hairline rows, mono labels, serif numbers.

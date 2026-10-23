# Drop the diet and exercise plans

The weekly diet plan (7 days × 4 meals), the weekly exercise plan and the five daily toggles are removed: detailed planning added friction without addressing the weekend chain, and one-tap daily checks replace them. Their tables are dropped with a destructive migration, acceptable because the app has a single user and no data worth preserving while v1 is built. After v1 the fallback is removed and every schema change needs an explicit migration (PRD section 12).

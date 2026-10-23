---
description: Run the MyBest gate (detekt, unit tests, androidTest compile) and report per task
allowed-tools: Bash(./gradlew:*) Read
disable-model-invocation: true
---

Run the standard gate for this repo:

`./gradlew :app:detekt :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin --continue`

Detekt autocorrect may modify files; mention it if `git status` shows changes afterwards.

Report one line per task: `:app:detekt`, `:app:testDebugUnitTest`, `:app:compileDebugAndroidTestKotlin`, each PASS or FAIL. For every failure include `file:line` and the rule or test name.

Do NOT fix anything in this turn. End with one line: **"ready to commit"** if all three pass, or a short list of what to fix next.

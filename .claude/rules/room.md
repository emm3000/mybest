---
paths:
  - "app/src/main/kotlin/com/emm/mybest/data/**"
  - "app/schemas/**"
  - "app/src/androidTest/**"
---

# Room rules

- Schema JSON is committed under `app/schemas/` (KSP arg `room.schemaLocation`). Any entity, DAO query shape or version change ships the regenerated schema file in the same PR.
- Bump the database version on every structural change.
- The app is pre-release: destructive migration is acceptable, so do not write a hand migration unless the ticket asks for one. *Why: owner decision; no installed user base to preserve.*
- Entities and DAOs stay in `data/`; domain models are mapped through `data/mappers/`. Domain never sees an entity.
- Backup export and import touch the database file: any schema change needs a check that `ExportDatabaseBackupUseCase` and `RestoreDatabaseBackupUseCase` still work.
- Verify with `./gradlew :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin`; the schema diff must be visible in the PR.

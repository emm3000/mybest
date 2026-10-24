---
paths:
  - "app/src/main/kotlin/com/emm/mybest/data/**"
  - "app/schemas/**"
  - "app/src/androidTest/**"
---

# Room rules

- Schema JSON is committed under `app/schemas/` (KSP arg `room.schemaLocation`). Any entity, DAO query shape or version change ships the regenerated schema file in the same PR.
- Every structural change bumps `AppDatabase.SCHEMA_VERSION`, adds a `Migration` to `ALL_MIGRATIONS` (`data/RoomMigrations.kt`) and adds a `MigrationTestHelper` test in `AppDatabaseMigrationsTest` that seeds rows at the previous version, calls `runMigrationsAndValidate` and proves the rows survive. No destructive fallback. *Why: data lives only on the device (ADR 0001), so a wipe is unrecoverable; `RoomMigrationsChainTest` fails when a version has no migration.*
- Only versions below `AppDatabase.V1_SCHEMA_VERSION` are recreated, through `withAppMigrations()`. Never add a version to that range. Every builder that opens a database file, in production or in tests, goes through `withAppMigrations()`; in-memory test databases (`Room.inMemoryDatabaseBuilder`) are exempt because they never migrate.
- Entities and DAOs stay in `data/`; domain models are mapped through `data/mappers/`. Domain never sees an entity.
- Backup export and import touch the database file: any schema change needs a check that `ExportDatabaseBackupUseCase` and `RestoreDatabaseBackupUseCase` still work.
- Verify with `./gradlew :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin`; the schema diff must be visible in the PR.

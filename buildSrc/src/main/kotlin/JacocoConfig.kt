val appJacocoExcludes = listOf(
    "**/R.class",
    "**/R$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*Test*.*",
    "**/*Preview*.*",
    // Generated Room classes and DAO declarations are validated by integration tests.
    "**/*Dao_Impl*.*",
    "**/*Database_Impl*.*",
    "**/data/entities/*Dao*.*",
    // UI rendering classes are excluded from unit-test coverage baseline.
    "**/*Screen*.*",
    "**/ui/components/**",
    "**/ui/theme/**",
    "**/core/navigation/**",
    "**/MainActivity*.*",
)

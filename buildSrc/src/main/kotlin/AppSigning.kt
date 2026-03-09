import org.gradle.api.Project
import java.util.Properties

private val releaseSigningKeys = listOf("keyAlias", "keyPassword", "storeFile", "storePassword")

fun Project.loadReleaseSigningProperties(fileName: String = "keystore.properties"): Properties? {
    val propertiesFile = rootProject.file(fileName)
    if (!propertiesFile.isFile || !propertiesFile.canRead()) return null

    return runCatching {
        Properties().apply {
            propertiesFile.inputStream().use(::load)
        }
    }.onFailure { error ->
        logger.warn("Skipping release signing config: failed to read $fileName", error)
    }.getOrNull()
}

fun Properties.hasRequiredReleaseSigningKeys(): Boolean {
    return releaseSigningKeys.all { key -> getProperty(key).isNullOrBlank().not() }
}

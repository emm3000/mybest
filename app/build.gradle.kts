import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kover)
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("jacoco")
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin.serialization)
}

detekt {
    toolVersion = libs.versions.detekt.get()
    config.setFrom(file("${project.rootDir}/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    allRules = false
    autoCorrect = true
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "*.BuildConfig",
                    "*.R",
                    "*.R$*",
                    "*Preview*",
                    "*.di.*",
                    "*.core.navigation.*",
                )
            }
        }
    }
}

jacoco {
    toolVersion = "0.8.12"
}

val releaseSigningProperties = rootProject.loadReleaseSigningProperties()
val hasCompleteReleaseSigning = releaseSigningProperties?.hasRequiredReleaseSigningKeys() == true

android {
    namespace = "com.emm.mybest"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.emm.mybest"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasCompleteReleaseSigning) {
            create("config") {
                keyAlias = releaseSigningProperties?.getProperty("keyAlias")
                keyPassword = releaseSigningProperties?.getProperty("keyPassword")
                storeFile = file(releaseSigningProperties?.getProperty("storeFile").orEmpty())
                storePassword = releaseSigningProperties?.getProperty("storePassword")
            }
        }
    }

    buildTypes {
        create("qa") {
            initWith(getByName("release"))
            // Fast validation build for day-to-day development.
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.findByName("config") ?: signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.findByName("config") ?: signingConfigs.getByName("debug")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=androidx.compose.material3.ExperimentalMaterial3Api")
    }
}

tasks.withType<Test>().configureEach {
    extensions.configure(JacocoTaskExtension::class.java) {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    val javaClasses = fileTree("${layout.buildDirectory.get().asFile}/intermediates/javac/debug/compileDebugJavaWithJavac/classes") {
        exclude(appJacocoExcludes)
    }
    val kotlinClasses = fileTree("${layout.buildDirectory.get().asFile}/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes") {
        exclude(appJacocoExcludes)
    }

    classDirectories.setFrom(files(javaClasses, kotlinClasses))
    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    executionData.setFrom(
        fileTree(layout.buildDirectory) {
            include("jacoco/testDebugUnitTest.exec")
            include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
        }
    )
}

dependencies {
    detektPlugins(libs.detekt.formatting)
    detektPlugins(libs.detekt.compose.rules)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.material.icons.extended)
    
    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    
    // Coil
    implementation(libs.coil.compose)
    
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.exifinterface)
    ksp(libs.androidx.room.compiler)

    // DataStore
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Stats (Vico)
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m3)
    
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(platform(libs.firebase.bom))
}

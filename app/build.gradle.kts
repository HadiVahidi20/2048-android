plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

android {
    namespace = "com.hadify.NumberMerge2048"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.hadify.NumberMerge2048"
        minSdk = 24
        targetSdk = 36
        versionCode = 8
        versionName = "1.0.5"
        ndk {
            debugSymbolLevel = "SYMBOL_TABLE"
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    val keystorePropsFile = rootProject.file("keystore.properties")
    val keystoreProps = Properties()
    if (keystorePropsFile.exists()) {
        keystorePropsFile.inputStream().use { keystoreProps.load(it) }
    }

    val localPropsFile = rootProject.file("local.properties")
    val localProps = Properties()
    if (localPropsFile.exists()) {
        localPropsFile.inputStream().use { localProps.load(it) }
    }

    val testAdMobAppId = "ca-app-pub-3940256099942544~3347511713"
    val releaseAdMobAppId =
        (project.findProperty("ADMOB_APP_ID_RELEASE") as String?)
            ?: localProps.getProperty("ADMOB_APP_ID_RELEASE")
            ?: System.getenv("ADMOB_APP_ID_RELEASE")

    val isReleaseTaskRequested = gradle.startParameter.taskNames.any {
        it.contains("Release", ignoreCase = true)
    }
    if (isReleaseTaskRequested && releaseAdMobAppId.isNullOrBlank()) {
        throw GradleException(
            "Missing AdMob App ID for release. Set ADMOB_APP_ID_RELEASE in local.properties, " +
                "gradle.properties, or environment variables."
        )
    }

    signingConfigs {
        if (keystorePropsFile.exists()) {
            create("release") {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            manifestPlaceholders["admobAppId"] = testAdMobAppId
            // UMP consent debug geography (debug builds only, emulators/test devices):
            // "EEA" = GDPR message (EEA/UK), "US_STATE" = US state privacy message, "" = real location.
            buildConfigField("String", "UMP_DEBUG_GEOGRAPHY", "\"EEA\"")
        }
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            manifestPlaceholders["admobAppId"] = releaseAdMobAppId ?: testAdMobAppId
            // Never force a consent debug geography in release builds.
            buildConfigField("String", "UMP_DEBUG_GEOGRAPHY", "\"\"")
            if (keystorePropsFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    lint {
        // PropertyEscape only flags the local, gitignored local.properties SDK path, not shipped code.
        disable += "PropertyEscape"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")

    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("com.google.android.gms:play-services-ads:25.5.0")
    implementation("com.google.android.ump:user-messaging-platform:4.0.0")

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

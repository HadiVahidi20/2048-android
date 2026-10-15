plugins {
    // AGP 8.13: supports compileSdk 36 and its lint understands Kotlin 2.2/2.3 metadata.
    id("com.android.application") version "8.13.2" apply false
    // Kotlin 2.2+ is required by play-services-ads 25.x (its API jar ships Kotlin 2.3 metadata).
    id("org.jetbrains.kotlin.android") version "2.2.10" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10" apply false
}

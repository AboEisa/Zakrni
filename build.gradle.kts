// Root build.gradle.kts (Project level)
plugins {
    kotlin("android") version "1.9.22" apply false
    id("com.google.devtools.ksp") version "1.9.22-1.0.16" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.8.0" apply false
    id("com.android.application") version "8.4.0" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
}



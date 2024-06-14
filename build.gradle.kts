buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        val nav_version = "2.7.7"
        val kotlin_version = "1.6.10"
        classpath("com.android.tools.build:gradle:7.0.4")
        classpath("com.google.gms:google-services:4.4.2")

        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:$nav_version")
        classpath("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")

        classpath("org.jetbrains.kotlin:kotlin-serialization:$kotlin_version")
    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1" apply false
}

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.example.glmaclient.persistentcloudanchor"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.glmaclient.persistentcloudanchor"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")

    implementation("androidx.appcompat:appcompat:1.6.1")

    implementation("com.google.android.material:material:1.12.0")

    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation ("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation ("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation ("androidx.legacy:legacy-support-v4:1.0.0")
    androidTestImplementation ("androidx.navigation:navigation-testing:2.7.7")

    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("com.google.firebase:firebase-firestore:25.0.0")
    implementation("com.google.firebase:firebase-storage:21.0.0")
    implementation("com.google.firebase:firebase-database:21.0.0")

    implementation ("com.github.bumptech.glide:glide:4.12.0")

    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation ("com.google.android.gms:play-services-auth:21.2.0")

    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.1")

    implementation("com.google.ar:core:1.43.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")


    implementation("de.javagl:obj:0.4.0")
    implementation("com.google.guava:guava:27.0.1-android")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
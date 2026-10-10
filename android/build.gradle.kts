plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.andikrue.tauri.a3lmessaging"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

val configuredA3lAar = providers
    .gradleProperty("A3L_MESSAGING_AAR")
    .orElse(providers.environmentVariable("A3L_MESSAGING_AAR"))

val a3lAar = if (configuredA3lAar.isPresent) {
    file(configuredA3lAar.get())
} else {
    file("libs/A3LMessaging-1.1.1.aar")
}

if (!a3lAar.exists()) {
    throw GradleException(
        "A3L Messaging SDK not found at ${a3lAar}. " +
        "Set A3L_MESSAGING_AAR to Amazon's A3LMessaging-1.1.1.aar " +
        "or place it in android/libs for local plugin development."
    )
}

dependencies {
    implementation(project(":tauri-android"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation(files(a3lAar))
    // Amazon's A3L 1.1.1 setup documentation requires this dependency for Android/FCM.
    implementation("com.google.firebase:firebase-messaging:23.0.0")
}

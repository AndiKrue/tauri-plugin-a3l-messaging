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

val a3lAar = file("libs/A3LMessaging-1.1.1.aar")
if (!a3lAar.exists()) {
    logger.warn(
        "A3L Messaging SDK not found at ${a3lAar}. " +
        "Download A3L Messaging 1.1.1 from Amazon before building Android/Fire OS."
    )
}

dependencies {
    implementation(project(":tauri-android"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation(files(a3lAar))
    // Amazon's A3L 1.1.1 setup documentation requires this dependency for Android/FCM.
    implementation("com.google.firebase:firebase-messaging:23.0.0")
}

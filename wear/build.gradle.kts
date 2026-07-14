import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}
val mqttBrokerUrl = localProperties.getProperty("mqtt.broker.url") ?: "ssl://e91065e599624a25b6b87e3a35173a65.s1.eu.hivemq.cloud:8883"
val mqttUsername = localProperties.getProperty("mqtt.username") ?: "abraham"
val mqttPassword = localProperties.getProperty("mqtt.password") ?: "linux123"

android {
    namespace = "mx.utng.cala.wear"
    compileSdk = 35 

    defaultConfig {
        applicationId = "mx.utng.cala.smarthealthmonitor"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        
        buildConfigField("String", "MQTT_BROKER_URL", "\"$mqttBrokerUrl\"")
        buildConfigField("String", "MQTT_USERNAME", "\"$mqttUsername\"")
        buildConfigField("String", "MQTT_PASSWORD", "\"$mqttPassword\"")
        
        val neonHost = localProperties.getProperty("NEON_HOST") ?: ""
        buildConfigField("String", "NEON_HOST", "\"$neonHost\"")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.ui)

    // Wear OS specific
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.androidx.wear.compose.material)
    implementation(libs.androidx.wear.compose.material3)
    implementation(libs.androidx.wear.compose.navigation)
    debugImplementation(libs.androidx.wear.compose.ui.tooling)
    implementation(libs.androidx.wear.tooling.preview)
    
    // Horologist
    implementation(libs.horologist.compose.layout)
    implementation(libs.horologist.compose.material)

    // Wearable Data Layer
    implementation(libs.play.services.wearable)

    // Health Services API
    implementation("androidx.health:health-services-client:1.1.0-alpha03")

    // Coroutines y Guava
    implementation("com.google.guava:guava:33.0.0-android")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Jetpack WatchFace API
    implementation("androidx.wear.watchface:watchface:1.2.1")
    implementation("androidx.wear.watchface:watchface-guava:1.2.1")
    implementation("androidx.wear.watchface:watchface-complications-rendering:1.2.1")
    implementation("androidx.wear.watchface:watchface-style:1.2.1")

    // Eclipse Paho MQTT para Android
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    implementation("org.eclipse.paho:org.eclipse.paho.android.service:1.1.1")
    
    // Kotlinx Serialization para JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Retrofit + OkHttp para llamadas a Neon HTTP API
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
}

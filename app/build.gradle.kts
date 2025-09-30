plugins {
    id("com.android.application")
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
}

android {
    namespace = "es.sanchoo.capitulojusto"
    compileSdk = 34

    defaultConfig {
        applicationId = "es.sanchoo.capitulojusto"
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
}

dependencies {
    implementation("androidx.fragment:fragment-ktx:1.8.4")
    implementation("androidx.activity:activity-ktx:1.9.1")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
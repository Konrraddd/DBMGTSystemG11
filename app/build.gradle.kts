plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlinAndroid)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.eventpass2"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.eventpass2"
        minSdk = 24
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Firebase BOM — manages all versions automatically
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
}
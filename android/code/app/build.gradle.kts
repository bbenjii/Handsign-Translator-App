plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.handsign_translator_app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.handsign_translator_app"
        minSdk = 24
        targetSdk = 35
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
//    implementation 'org.tensorflow:tensorflow-lite:2.9.0'
    implementation("org.tensorflow:tensorflow-lite-select-tf-ops:2.9.0")  // Add this line for TensorFlow Lite Select Ops
    implementation(libs.tensorflow.lite)  // Keep the general TensorFlow Lite implementation
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
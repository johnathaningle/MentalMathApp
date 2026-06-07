plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.mentalmath"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.mentalmath"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        val releaseStoreFile = project.findProperty("RELEASE_STORE_FILE") as? String
        if (!releaseStoreFile.isNullOrEmpty()) {
            create("release") {
                storeFile = file(releaseStoreFile)
                storePassword = project.findProperty("RELEASE_STORE_PASSWORD") as? String ?: ""
                keyAlias = project.findProperty("RELEASE_KEY_ALIAS") as? String ?: ""
                keyPassword = project.findProperty("RELEASE_KEY_PASSWORD") as? String ?: ""
            }
        }
    }

    buildTypes {
        release {
            val releaseStoreFile = project.findProperty("RELEASE_STORE_FILE") as? String
            if (!releaseStoreFile.isNullOrEmpty()) {
                signingConfig = signingConfigs.getByName("release")
            }
            optimization {
                enable = false
            }
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
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
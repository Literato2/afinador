plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.juanito.afinador"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.juanito.afinador"
        minSdk = 34
        targetSdk = 35
        versionCode = 6
        versionName = "1.5"

        
    }

    signingConfigs {
        create("release") {
            // Clave fuera del repo: ~/.gradle/gradle.properties (JUANITO_*).
            val store = providers.gradleProperty("JUANITO_KEYSTORE").orNull
            if (store != null) {
                storeFile = file(store)
                storePassword = providers.gradleProperty("JUANITO_KEY_PASSWORD").get()
                keyAlias = providers.gradleProperty("JUANITO_KEY_ALIAS").get()
                keyPassword = providers.gradleProperty("JUANITO_KEY_PASSWORD").get()
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
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
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.navigation.safeargs)
}

android {
    namespace = "com.navassist.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.navassist.android"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            resValue("string", "app_name", "NavAssist")
            buildConfigField("String", "BASE_URL", "\"http://192.168.31.46:8000/api/v1/\"")
            buildConfigField("String", "WS_BASE_URL", "\"ws://192.168.31.46:8000/ws/\"")
        }
        create("staging") {
            dimension = "environment"
            applicationIdSuffix = ".staging"
            resValue("string", "app_name", "NavAssist")
            buildConfigField("String", "BASE_URL", "\"https://staging-api.navassist.com/api/v1/\"")
            buildConfigField("String", "WS_BASE_URL", "\"wss://staging-api.navassist.com/ws/\"")
        }
        create("production") {
            dimension = "environment"
            resValue("string", "app_name", "NavAssist")
            buildConfigField("String", "BASE_URL", "\"https://api.navassist.com/api/v1/\"")
            buildConfigField("String", "WS_BASE_URL", "\"wss://api.navassist.com/ws/\"")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            isDebuggable = true
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"
        )
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
    arg("room.generateKotlin", "true")
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Catalog Bundles
    implementation(libs.bundles.android.core)
    implementation(libs.bundles.network)
    implementation(libs.bundles.database)
    implementation(libs.bundles.maps)

    // Play Services Auth / SMS Retriever API
    implementation("com.google.android.gms:play-services-auth-api-phone:18.1.0")

    // Lifecycle & ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Navigation Component
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Hilt Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Room KSP Compiler
    ksp(libs.androidx.room.compiler)

    // DataStore Preferences
    implementation(libs.androidx.datastore.preferences)

    // Coil Image Loading
    implementation(libs.coil)
    implementation(libs.coil.network.okhttp)
    implementation(libs.coil.vector)

    // SwipeRefreshLayout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Background Tasks
    implementation(libs.androidx.work.runtime.ktx)

    // Logging
    implementation(libs.timber)

    // Razorpay Payment SDK
    implementation("com.razorpay:checkout:1.6.40")

    // Testing Bundles
    testImplementation(libs.bundles.testing)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
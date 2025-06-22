plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.sslablk.fixmate"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sslablk.fixmate"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    configurations.all {
        resolutionStrategy {
            force ("androidx.annotation:annotation:1.7.1")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    // Core UI & Navigation
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation ("androidx.work:work-runtime:2.9.0")

    // Lifecycle (ViewModel & LiveData for Java)
    implementation("androidx.lifecycle:lifecycle-livedata:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.1")

    // Firebase
    implementation("com.google.firebase:firebase-auth:23.2.1")
    implementation("com.google.firebase:firebase-firestore:25.1.4")

    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    // For pull-to-refresh functionality
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // For loading images from URLs
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")

    // Google Maps SDK
    implementation ("com.google.android.gms:play-services-maps:18.2.0")
    // Location services for getting the user's current location
    implementation ("com.google.android.gms:play-services-location:21.3.0")
    // Places SDK for search functionality
    implementation ("com.google.android.libraries.places:places:3.5.0")

    //FringerPrint
    implementation ("androidx.biometric:biometric:1.1.0")
}
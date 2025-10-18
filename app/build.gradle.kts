plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
}

kotlin {
    jvmToolchain(21)
}

android {
    namespace = "com.cauchymop.goblob"
    compileSdk = 36

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.cauchymop.goblob"
        minSdk = 24
        targetSdk = 36
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {

    api(project(":goblobBase"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.games)
    implementation(libs.play.services.auth)
    implementation(libs.guava)
    implementation(libs.dagger)
    implementation(libs.dagger.android.support)
    implementation(libs.protobuf.java)
    implementation(libs.firebase.core)
    implementation(libs.firebase.analytics)
    implementation(libs.kotlin.stdlib.jdk7)
    implementation(libs.crashlytics)

    kapt(libs.dagger.compiler)
    kapt(libs.dagger.android.processor)

    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.guava.testlib)
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.truth)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

//    implementation 'androidx.multidex:multidex:2.0.1'
//
//    implementation "com.jakewharton:butterknife:${butterknife_version}"
//    implementation 'javax.annotation:javax.annotation-api:1.2'
//    kapt "com.jakewharton:butterknife-compiler:${butterknife_version}"
//    kapt "com.google.dagger:dagger-compiler:${dagger_version}"
}
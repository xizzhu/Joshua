/*
 * Copyright (C) 2019 Xizhi Zhu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}
apply("$rootDir/scripts/coverage.gradle.kts")

android {
    compileOptions {
        sourceCompatibility = Versions.java
        targetCompatibility = Versions.java
    }

    compileSdkVersion(Versions.compileSdk)
    buildToolsVersion(Versions.buildTools)

    defaultConfig {
        applicationId = "me.xizzhu.android.joshua"

        minSdkVersion(Versions.minSdk)
        targetSdkVersion(Versions.targetSdk)

        versionCode = Versions.versionCode
        versionName = Versions.versionName

        resConfigs(Versions.supportedLocales)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
        getByName("debug").java.srcDirs("src/debug/kotlin")
        getByName("release").java.srcDirs("src/release/kotlin")

        getByName("test").java.srcDirs("src/testCommon/kotlin", "src/test/kotlin")
        getByName("androidTest").java.srcDirs("src/testCommon/kotlin", "src/androidTest/kotlin")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFile(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
        getByName("debug") {
            applicationIdSuffix = ".debug"
            isTestCoverageEnabled = true
        }
    }
}

dependencies {
    implementation(Dependencies.Kotlin.stdlib)
    implementation(Dependencies.Kotlin.coroutinesAndroid)

    implementation(Dependencies.AndroidX.annotation)
    implementation(Dependencies.AndroidX.appCompat)
    implementation(Dependencies.AndroidX.coordinatorLayout)
    implementation(Dependencies.AndroidX.drawerLayout)
    implementation(Dependencies.AndroidX.material)
    implementation(Dependencies.AndroidX.recyclerView)
    implementation(Dependencies.AndroidX.viewPager)

    implementation(Dependencies.Dagger.dagger)
    implementation(Dependencies.Dagger.android)
    implementation(Dependencies.Dagger.androidSupport)
    kapt(Dependencies.Dagger.compiler)
    kapt(Dependencies.Dagger.androidProcessor)

    implementation(Dependencies.Retrofit.retrofit)
    implementation(Dependencies.Retrofit.moshiConverter)
    implementation(Dependencies.Retrofit.okhttp3)
    implementation(Dependencies.Retrofit.moshi)
    implementation(Dependencies.Retrofit.okio)

    debugImplementation(Dependencies.Stetho.stetho)

    testImplementation(Dependencies.Kotlin.test)
    testImplementation(Dependencies.Mockito.mockito)

    androidTestImplementation(Dependencies.AndroidX.testRunner)
    androidTestImplementation(Dependencies.AndroidX.testRules)
}

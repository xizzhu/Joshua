/*
 * Copyright (C) 2021 Xizhi Zhu
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

import java.util.Properties

plugins {
    id("com.android.application")
    id("dagger.hilt.android.plugin")
    kotlin("android")
    kotlin("kapt")
}
apply("$rootDir/scripts/coverage.gradle.kts")

android {
    compileOptions {
        sourceCompatibility = Versions.java
        targetCompatibility = Versions.java
    }
    kotlinOptions {
        jvmTarget = Versions.Kotlin.jvmTarget
    }

    buildToolsVersion = Versions.Sdk.buildTools
    compileSdk = Versions.Sdk.compile

    defaultConfig {
        applicationId = Configurations.applicationId

        minSdk = Versions.Sdk.min
        targetSdk = Versions.Sdk.target

        versionCode = Versions.App.code
        versionName = Versions.App.name

        multiDexEnabled = true

        resourceConfigurations.addAll(Configurations.supportedLocales)
    }

    buildFeatures {
        viewBinding = true
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
        getByName("debug").java.srcDirs("src/debug/kotlin")
        getByName("release").java.srcDirs("src/release/kotlin")

        getByName("test").java.srcDirs("src/test/kotlin")
    }

    signingConfigs {
        create("release") {
            val keystorePropertiesFile = File("keystore.properties")
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = Properties()
                keystoreProperties.load(keystorePropertiesFile.inputStream())

                storeFile = File(rootDir, keystoreProperties.getProperty("KEYSTORE_FILE"))
                storePassword = keystoreProperties.getProperty("KEYSTORE_PASSWORD")
                keyAlias = keystoreProperties.getProperty("KEY_ALIAS")
                keyPassword = keystoreProperties.getProperty("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")

            // Temporarily use proguard-android.txt instead of proguard-android-optimize.txt as a workaround of an R8 issue:
            // https://issuetracker.google.com/issues/174167294
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = " debug"
            isTestCoverageEnabled = project.hasProperty("coverage")
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    packagingOptions {
        resources.excludes.addAll(listOf(
                "META-INF/atomicfu.kotlin_module", "META-INF/AL2.0", "META-INF/LGPL2.1", "META-INF/licenses/*",

                // https://github.com/Kotlin/kotlinx.coroutines/tree/master/kotlinx-coroutines-debug#debug-agent-and-android
                "win32-x86/attach_hotspot_windows.dll", "win32-x86-64/attach_hotspot_windows.dll"
        ))
    }
}

tasks.withType(Test::class) {
    maxParallelForks = Runtime.getRuntime().availableProcessors() / 2
}

dependencies {
    implementation(Dependencies.Kotlin.coroutinesAndroid)

    debugImplementation(Dependencies.AndroidX.multidex)
    implementation(Dependencies.AndroidX.activity)
    implementation(Dependencies.AndroidX.annotation)
    implementation(Dependencies.AndroidX.appCompat)
    implementation(Dependencies.AndroidX.core)
    implementation(Dependencies.AndroidX.Lifecycle.common)
    implementation(Dependencies.AndroidX.Lifecycle.runtime)
    implementation(Dependencies.AndroidX.Lifecycle.viewModel)
    implementation(Dependencies.AndroidX.View.constraintLayout)
    implementation(Dependencies.AndroidX.View.coordinatorLayout)
    implementation(Dependencies.AndroidX.View.drawerLayout)
    implementation(Dependencies.AndroidX.View.material)
    implementation(Dependencies.AndroidX.View.recyclerView)
    implementation(Dependencies.AndroidX.View.swipeRefreshLayout)
    implementation(Dependencies.AndroidX.View.viewPager2)

    releaseImplementation(platform(Dependencies.Firebase.bom))
    releaseImplementation(Dependencies.Firebase.analytics)
    releaseImplementation(Dependencies.Firebase.Crashlytics.crashlytics)

    implementation(Dependencies.Hilt.android)
    kapt(Dependencies.Hilt.compiler)

    implementation(Dependencies.Ask.ask)

    implementation(Dependencies.Logger.logger)

    testImplementation(Dependencies.Kotlin.test)
    testImplementation(Dependencies.Kotlin.coroutinesTest)
    testImplementation(Dependencies.AndroidX.Test.core)
    testImplementation(Dependencies.Mockk.mockk)
    testImplementation(Dependencies.Robolectric.robolectric)
}

apply {
    plugin("com.google.gms.google-services")
    plugin("com.google.firebase.crashlytics")
}

/*
 * Copyright (C) 2020 Xizhi Zhu
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
    kotlin("android")
    kotlin("kapt")
}
apply {
    plugin("io.fabric")
}
apply("$rootDir/scripts/coverage.gradle.kts")

android {
    compileOptions {
        sourceCompatibility = Versions.java
        targetCompatibility = Versions.java
    }

    buildToolsVersion(Versions.Sdk.buildTools)
    compileSdkVersion(Versions.Sdk.compile)

    defaultConfig {
        applicationId = Configurations.applicationId

        minSdkVersion(Versions.Sdk.min)
        targetSdkVersion(Versions.Sdk.target)

        versionCode = Versions.App.code
        versionName = Versions.App.name

        resConfigs(Configurations.supportedLocales)

        buildConfigField("long", "BUILD_TIME", System.currentTimeMillis().toString() + "L")

        testInstrumentationRunner = Dependencies.AndroidX.Test.runner
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
        getByName("debug").java.srcDirs("src/debug/kotlin")
        getByName("release").java.srcDirs("src/release/kotlin")

        getByName("test").java.srcDirs("src/testCommon/kotlin", "src/test/kotlin")
        getByName("androidTest").java.srcDirs("src/testCommon/kotlin", "src/androidTest/kotlin")
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

            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        exclude("META-INF/atomicfu.kotlin_module")
    }
}

tasks.withType(Test::class.java) {
    maxParallelForks = Runtime.getRuntime().availableProcessors() / 2
}

dependencies {
    implementation(Dependencies.Kotlin.stdlib)
    implementation(Dependencies.Kotlin.coroutinesAndroid)

    implementation(Dependencies.AndroidX.annotation)
    implementation(Dependencies.AndroidX.appCompat)
    implementation(Dependencies.AndroidX.constraintLayout)
    implementation(Dependencies.AndroidX.coordinatorLayout)
    implementation(Dependencies.AndroidX.drawerLayout)
    implementation(Dependencies.AndroidX.material)
    implementation(Dependencies.AndroidX.recyclerView)
    implementation(Dependencies.AndroidX.viewPager)

    implementation(Dependencies.Firebase.core)
    implementation(Dependencies.Firebase.analytics)
    implementation(Dependencies.Firebase.Crashlytics.crashlytics)

    implementation(Dependencies.Dagger.dagger)
    implementation(Dependencies.Dagger.android)
    implementation(Dependencies.Dagger.androidSupport)
    kapt(Dependencies.Dagger.compiler)
    kapt(Dependencies.Dagger.androidProcessor)

    implementation(Dependencies.Ask.ask)

    implementation(Dependencies.Logger.logger)

    debugImplementation(Dependencies.Stetho.stetho)

    testImplementation(Dependencies.Kotlin.test)
    testImplementation(Dependencies.Kotlin.coroutinesTest)
    testImplementation(Dependencies.Mockito.mockito)

    androidTestImplementation(Dependencies.Kotlin.test)
    androidTestImplementation(Dependencies.Kotlin.coroutinesTest)
    androidTestImplementation(Dependencies.AndroidX.Test.junit)
    androidTestImplementation(Dependencies.AndroidX.Test.rules)
    androidTestImplementation(Dependencies.Mockito.mockito)
    androidTestImplementation(Dependencies.Mockito.android)
}

apply {
    plugin("com.google.gms.google-services")
}

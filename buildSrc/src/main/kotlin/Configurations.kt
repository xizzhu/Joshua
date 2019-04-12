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

import org.gradle.api.JavaVersion

object Versions {
    const val androidGradle = "3.3.2"
    const val coverallsGradle = "2.8.2"

    val java = JavaVersion.VERSION_1_8
    const val kotlin = "1.3.30"
    const val kotlinCoroutines = "1.1.1"

    const val compileSdk = 28
    const val buildTools = "28.0.3"
    const val minSdk = 21
    const val targetSdk = 28

    const val versionCode = 100
    const val versionName = "0.1.0"

    val supportedLocales = listOf("en")

    const val annotation = "1.0.1"
    const val appCompat = "1.0.2"
    const val coordinatorLayout = "1.0.0"
    const val drawerLayout = "1.0.0"
    const val material = "1.0.0"
    const val recyclerView = "1.0.0"
    const val viewPager = "1.0.0"

    const val dagger = "2.22.1"

    const val retrofit = "2.5.0"
    const val okhttp = "3.14.1"
    const val moshi = "1.8.0"
    const val okio = "2.2.2"

    const val stetho = "1.5.1"

    const val testRunner = "1.1.0"
    const val testRules = "1.1.1"
    const val mockito = "2.27.0"
}

object Dependencies {
    object Kotlin {
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
        const val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlinCoroutines}"
        const val test = "org.jetbrains.kotlin:kotlin-test-junit:${Versions.kotlin}"
        const val coroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.kotlinCoroutines}"
    }

    object AndroidX {
        const val annotation = "androidx.annotation:annotation:${Versions.annotation}"
        const val appCompat = "androidx.appcompat:appcompat:${Versions.appCompat}"
        const val coordinatorLayout = "androidx.coordinatorlayout:coordinatorlayout:${Versions.coordinatorLayout}"
        const val drawerLayout = "androidx.drawerlayout:drawerlayout:${Versions.drawerLayout}"
        const val material = "com.google.android.material:material:${Versions.material}"
        const val recyclerView = "androidx.recyclerview:recyclerview:${Versions.recyclerView}"
        const val viewPager = "androidx.viewpager:viewpager:${Versions.viewPager}"

        const val testRunner = "androidx.test.ext:junit:${Versions.testRunner}"
        const val testRules = "androidx.test:rules:${Versions.testRules}"
    }

    object Dagger {
        const val dagger = "com.google.dagger:dagger:${Versions.dagger}"
        const val android = "com.google.dagger:dagger-android:${Versions.dagger}"
        const val androidSupport = "com.google.dagger:dagger-android-support:${Versions.dagger}"
        const val compiler = "com.google.dagger:dagger-compiler:${Versions.dagger}"
        const val androidProcessor = "com.google.dagger:dagger-android-processor:${Versions.dagger}"
    }

    object Retrofit {
        const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
        const val moshiConverter = "com.squareup.retrofit2:converter-moshi:${Versions.retrofit}"
        const val okhttp3 = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
        const val moshi = "com.squareup.moshi:moshi:${Versions.moshi}"
        const val okio = "com.squareup.okio:okio:${Versions.okio}"
    }

    object Stetho {
        const val stetho = "com.facebook.stetho:stetho:${Versions.stetho}"
    }

    object Mockito {
        const val mockito = "org.mockito:mockito-core:${Versions.mockito}"
        const val android = "org.mockito:mockito-android:${Versions.mockito}"
    }
}

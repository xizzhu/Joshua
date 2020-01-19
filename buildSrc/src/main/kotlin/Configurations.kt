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

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.gradle.api.JavaVersion

object Configurations {
    const val applicationId = "me.xizzhu.android.joshua"
    val supportedLocales = listOf("en")
}

object Versions {
    object App {
        const val code = 1302
        val name: String by lazy {
            "${code / 10000}.${(code % 10000) / 100}.${code % 100} " +
                    "(${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))})"
        }
    }

    object Coveralls {
        const val classpath = "2.8.3"
    }

    object Sdk {
        const val classpath = "3.5.3"
        const val buildTools = "29.0.2"
        const val compile = 29
        const val min = 21
        const val target = 29
    }

    val java = JavaVersion.VERSION_1_8

    object Kotlin {
        const val core = "1.3.61"
        const val coroutines = "1.3.3"
    }

    object AndroidX {
        const val annotation = "1.1.0"
        const val appCompat = "1.1.0"
        const val constraintLayout = "1.1.3"
        const val coordinatorLayout = "1.1.0"
        const val drawerLayout = "1.0.0"
        const val material = "1.0.0"
        const val recyclerView = "1.1.0"
        const val viewPager = "1.0.0"

        object Test {
            const val junit = "1.1.1"
            const val rules = "1.2.0"
        }
    }

    object Firebase {
        const val classpath = "4.3.3"
        const val core = "17.2.2"
        const val analytics = "17.2.2"

        object Crashlytics {
            const val classpath = "1.28.1"
            const val crashlytics = "2.10.1"
        }
    }

    const val dagger = "2.25.4"

    const val ask = "0.5.1"

    const val logger = "0.1.5"

    const val stetho = "1.5.1"

    const val mockito = "3.2.4"
}

object Dependencies {
    object Sdk {
        const val classpath = "com.android.tools.build:gradle:${Versions.Sdk.classpath}"
    }

    object Coveralls {
        const val classpath = "org.kt3k.gradle.plugin:coveralls-gradle-plugin:${Versions.Coveralls.classpath}"
    }

    object Kotlin {
        const val classpath = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.Kotlin.core}"
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.Kotlin.core}"
        const val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.Kotlin.coroutines}"
        const val test = "org.jetbrains.kotlin:kotlin-test-junit:${Versions.Kotlin.core}"
        const val coroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.Kotlin.coroutines}"
    }

    object AndroidX {
        const val annotation = "androidx.annotation:annotation:${Versions.AndroidX.annotation}"
        const val appCompat = "androidx.appcompat:appcompat:${Versions.AndroidX.appCompat}"
        const val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.AndroidX.constraintLayout}"
        const val coordinatorLayout = "androidx.coordinatorlayout:coordinatorlayout:${Versions.AndroidX.coordinatorLayout}"
        const val drawerLayout = "androidx.drawerlayout:drawerlayout:${Versions.AndroidX.drawerLayout}"
        const val material = "com.google.android.material:material:${Versions.AndroidX.material}"
        const val recyclerView = "androidx.recyclerview:recyclerview:${Versions.AndroidX.recyclerView}"
        const val viewPager = "androidx.viewpager:viewpager:${Versions.AndroidX.viewPager}"

        object Test {
            const val junit = "androidx.test.ext:junit:${Versions.AndroidX.Test.junit}"
            const val rules = "androidx.test:rules:${Versions.AndroidX.Test.rules}"

            const val runner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    object Firebase {
        const val classpath = "com.google.gms:google-services:${Versions.Firebase.classpath}"
        const val core = "com.google.firebase:firebase-core:${Versions.Firebase.core}"
        const val analytics = "com.google.firebase:firebase-analytics:${Versions.Firebase.analytics}"

        object Crashlytics {
            const val classpath = "io.fabric.tools:gradle:${Versions.Firebase.Crashlytics.classpath}"
            const val crashlytics = "com.crashlytics.sdk.android:crashlytics:${Versions.Firebase.Crashlytics.crashlytics}"
        }
    }

    object Dagger {
        const val dagger = "com.google.dagger:dagger:${Versions.dagger}"
        const val android = "com.google.dagger:dagger-android:${Versions.dagger}"
        const val androidSupport = "com.google.dagger:dagger-android-support:${Versions.dagger}"
        const val compiler = "com.google.dagger:dagger-compiler:${Versions.dagger}"
        const val androidProcessor = "com.google.dagger:dagger-android-processor:${Versions.dagger}"
    }

    object Ask {
        const val ask = "com.github.xizzhu:ask:${Versions.ask}"
    }

    object Logger {
        const val logger = "com.github.xizzhu:Logger:${Versions.logger}"
    }

    object Stetho {
        const val stetho = "com.facebook.stetho:stetho:${Versions.stetho}"
    }

    object Mockito {
        const val mockito = "org.mockito:mockito-core:${Versions.mockito}"
        const val android = "org.mockito:mockito-android:${Versions.mockito}"
    }
}

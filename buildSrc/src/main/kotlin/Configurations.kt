/*
 * Copyright (C) 2023 Xizhi Zhu
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
        const val code = 2002
        val name: String by lazy {
            "${code / 10000}.${(code % 10000) / 100}.${code % 100} " +
                "(${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))})"
        }
    }

    object Sdk {
        const val classpath = "7.3.1"
        const val buildTools = "33.0.1"
        const val compile = 33
        const val min = 19
        const val target = 33
    }

    val java = JavaVersion.VERSION_11

    object Kotlin {
        val jvmTarget = java.toString()
        const val core = "1.8.0"
        const val coroutines = "1.6.4"
        const val kover = "0.6.1"
    }

    object AndroidX {
        const val activity = "1.6.1"
        const val annotation = "1.5.0"
        const val appCompat = "1.5.1"
        const val core = "1.9.0"
        const val fragment = "1.5.5"
        const val lifecycle = "2.5.1"
        const val multidex = "2.0.1"

        object View {
            const val constraintLayout = "2.1.4"
            const val coordinatorLayout = "1.2.0"
            const val drawerLayout = "1.1.1"
            const val recyclerView = "1.2.1"
            const val swipeRefreshLayout = "1.1.0"
            const val viewPager2 = "1.0.0"
        }

        object Test {
            const val core = "1.5.0"
        }
    }

    object Firebase {
        const val classpath = "4.3.10"
        const val bom = "31.1.1"

        object Crashlytics {
            const val classpath = "2.9.2"
        }
    }

    const val ask = "0.5.2"
    const val hilt = "2.44.2"
    const val logger = "0.7.0"
    const val materialComponent = "1.7.0"

    const val mockk = "1.13.3"
    const val robolectric = "4.9.2"
}

object Dependencies {
    object Sdk {
        const val classpath = "com.android.tools.build:gradle:${Versions.Sdk.classpath}"
    }

    object Kotlin {
        const val classpath = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.Kotlin.core}"
        const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.Kotlin.coroutines}"
        const val reflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.Kotlin.core}"

        const val test = "org.jetbrains.kotlin:kotlin-test-junit:${Versions.Kotlin.core}"
        const val coroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.Kotlin.coroutines}"

        object Kover {
            const val plugin = "org.jetbrains.kotlinx.kover"
        }
    }

    object AndroidX {
        const val activity = "androidx.activity:activity-ktx:${Versions.AndroidX.activity}"
        const val annotation = "androidx.annotation:annotation:${Versions.AndroidX.annotation}"
        const val appCompat = "androidx.appcompat:appcompat:${Versions.AndroidX.appCompat}"
        const val core = "androidx.core:core:${Versions.AndroidX.core}"
        const val multidex = "androidx.multidex:multidex:${Versions.AndroidX.multidex}"

        object Fragment {
            const val core = "androidx.fragment:fragment-ktx:${Versions.AndroidX.fragment}"
            const val test = "androidx.fragment:fragment-testing:${Versions.AndroidX.fragment}"
        }

        object Lifecycle {
            const val common = "androidx.lifecycle:lifecycle-common-java8:${Versions.AndroidX.lifecycle}"
            const val runtime = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.AndroidX.lifecycle}"
            const val viewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.AndroidX.lifecycle}"
        }

        object View {
            const val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.AndroidX.View.constraintLayout}"
            const val coordinatorLayout = "androidx.coordinatorlayout:coordinatorlayout:${Versions.AndroidX.View.coordinatorLayout}"
            const val drawerLayout = "androidx.drawerlayout:drawerlayout:${Versions.AndroidX.View.drawerLayout}"
            const val recyclerView = "androidx.recyclerview:recyclerview:${Versions.AndroidX.View.recyclerView}"
            const val swipeRefreshLayout = "androidx.swiperefreshlayout:swiperefreshlayout:${Versions.AndroidX.View.swipeRefreshLayout}"
            const val viewPager2 = "androidx.viewpager2:viewpager2:${Versions.AndroidX.View.viewPager2}"
        }

        object Test {
            const val core = "androidx.test:core:${Versions.AndroidX.Test.core}"
        }
    }

    object Firebase {
        const val classpath = "com.google.gms:google-services:${Versions.Firebase.classpath}"
        const val bom = "com.google.firebase:firebase-bom:${Versions.Firebase.bom}"
        const val analytics = "com.google.firebase:firebase-analytics"

        object Crashlytics {
            const val classpath = "com.google.firebase:firebase-crashlytics-gradle:${Versions.Firebase.Crashlytics.classpath}"
            const val crashlytics = "com.google.firebase:firebase-crashlytics"
        }
    }

    object Hilt {
        const val plugin = "com.google.dagger.hilt.android"
        const val android = "com.google.dagger:hilt-android:${Versions.hilt}"
        const val compiler = "com.google.dagger:hilt-android-compiler:${Versions.hilt}"
        const val test = "com.google.dagger:hilt-android-testing:${Versions.hilt}"
    }

    const val ask = "com.github.xizzhu:ask:${Versions.ask}"
    const val logger = "me.xizzhu:logger:${Versions.logger}"
    const val materialComponent = "com.google.android.material:material:${Versions.materialComponent}"

    const val mockk = "io.mockk:mockk:${Versions.mockk}"
    const val robolectric = "org.robolectric:robolectric:${Versions.robolectric}"
}

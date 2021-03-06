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
        const val code = 1700
        val name: String by lazy {
            "${code / 10000}.${(code % 10000) / 100}.${code % 100} " +
                    "(${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))})"
        }
    }

    object Coveralls {
        const val classpath = "2.8.3"
    }

    object Sdk {
        const val classpath = "4.1.2"
        const val buildTools = "30.0.3"
        const val compile = 30
        const val min = 21
        const val target = 30
    }

    val java = JavaVersion.VERSION_1_8

    object Kotlin {
        const val jvmTarget = "1.8"
        const val core = "1.4.30"
        const val coroutines = "1.4.2"
    }

    object AndroidX {
        const val activity = "1.2.0"
        const val annotation = "1.1.0"
        const val appCompat = "1.2.0"
        const val core = "1.3.2"
        const val lifecycle = "2.3.0"

        object View {
            const val constraintLayout = "2.0.4"
            const val coordinatorLayout = "1.1.0"
            const val drawerLayout = "1.1.1"
            const val material = "1.3.0"
            const val recyclerView = "1.1.0"
            const val swipeRefreshLayout = "1.1.0"
            const val viewPager2 = "1.0.0"
        }

        object Test {
            const val junit = "1.1.2"
            const val rules = "1.3.0"
            const val espresso = "3.3.0"
        }
    }

    object Firebase {
        const val classpath = "4.3.5"
        const val analytics = "18.0.2"

        object Crashlytics {
            const val classpath = "2.5.0"
            const val crashlytics = "17.3.1"
        }

        object Perf {
            const val classpath = "1.3.4"
            const val perf = "19.1.1"
        }
    }

    const val hilt = "2.32-alpha"

    const val ask = "0.5.2"

    const val logger = "0.4.0"

    const val mockito = "3.7.7"
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
        const val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.Kotlin.coroutines}"
        const val test = "org.jetbrains.kotlin:kotlin-test-junit:${Versions.Kotlin.core}"
        const val coroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.Kotlin.coroutines}"
    }

    object AndroidX {
        const val activity = "androidx.activity:activity-ktx:${Versions.AndroidX.activity}"
        const val annotation = "androidx.annotation:annotation:${Versions.AndroidX.annotation}"
        const val appCompat = "androidx.appcompat:appcompat:${Versions.AndroidX.appCompat}"
        const val core = "androidx.core:core:${Versions.AndroidX.core}"

        object Lifecycle {
            const val common = "androidx.lifecycle:lifecycle-common-java8:${Versions.AndroidX.lifecycle}"
            const val runtime = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.AndroidX.lifecycle}"
            const val viewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.AndroidX.lifecycle}"
        }

        object View {
            const val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.AndroidX.View.constraintLayout}"
            const val coordinatorLayout = "androidx.coordinatorlayout:coordinatorlayout:${Versions.AndroidX.View.coordinatorLayout}"
            const val drawerLayout = "androidx.drawerlayout:drawerlayout:${Versions.AndroidX.View.drawerLayout}"
            const val material = "com.google.android.material:material:${Versions.AndroidX.View.material}"
            const val recyclerView = "androidx.recyclerview:recyclerview:${Versions.AndroidX.View.recyclerView}"
            const val swipeRefreshLayout = "androidx.swiperefreshlayout:swiperefreshlayout:${Versions.AndroidX.View.swipeRefreshLayout}"
            const val viewPager2 = "androidx.viewpager2:viewpager2:${Versions.AndroidX.View.viewPager2}"
        }

        object Test {
            const val junit = "androidx.test.ext:junit:${Versions.AndroidX.Test.junit}"
            const val rules = "androidx.test:rules:${Versions.AndroidX.Test.rules}"

            const val runner = "androidx.test.runner.AndroidJUnitRunner"

            object Espresso {
                const val core = "androidx.test.espresso:espresso-core:${Versions.AndroidX.Test.espresso}"
                const val contrib = "androidx.test.espresso:espresso-contrib:${Versions.AndroidX.Test.espresso}"
            }
        }
    }

    object Firebase {
        const val classpath = "com.google.gms:google-services:${Versions.Firebase.classpath}"
        const val analytics = "com.google.firebase:firebase-analytics:${Versions.Firebase.analytics}"

        object Crashlytics {
            const val classpath = "com.google.firebase:firebase-crashlytics-gradle:${Versions.Firebase.Crashlytics.classpath}"
            const val crashlytics = "com.google.firebase:firebase-crashlytics:${Versions.Firebase.Crashlytics.crashlytics}"
        }

        object Perf {
            const val classpath = "com.google.firebase:perf-plugin:${Versions.Firebase.Perf.classpath}"
            const val perf = "com.google.firebase:firebase-perf:${Versions.Firebase.Perf.perf}"
        }
    }

    object Hilt {
        const val classpath = "com.google.dagger:hilt-android-gradle-plugin:${Versions.hilt}"
        const val android = "com.google.dagger:hilt-android:${Versions.hilt}"
        const val compiler = "com.google.dagger:hilt-android-compiler:${Versions.hilt}"
    }

    object Ask {
        const val ask = "com.github.xizzhu:ask:${Versions.ask}"
    }

    object Logger {
        const val logger = "com.github.xizzhu:Logger:${Versions.logger}"
    }

    object Mockito {
        const val mockito = "org.mockito:mockito-core:${Versions.mockito}"
        const val android = "org.mockito:mockito-android:${Versions.mockito}"
    }
}

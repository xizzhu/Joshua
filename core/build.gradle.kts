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
    id("com.android.library")
    kotlin("android")
}

android {
    compileOptions {
        sourceCompatibility = Versions.java
        targetCompatibility = Versions.java
    }

    buildToolsVersion(Versions.Sdk.buildTools)
    compileSdkVersion(Versions.Sdk.compile)

    defaultConfig {
        minSdkVersion(Versions.Sdk.min)
        targetSdkVersion(Versions.Sdk.target)
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
        getByName("test").java.srcDirs("src/test/kotlin")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isShrinkResources = false
        }
        getByName("debug") {
            isTestCoverageEnabled = project.hasProperty("coverage")
        }
    }
}

dependencies {
    implementation(Dependencies.Kotlin.stdlib)
    implementation(Dependencies.Kotlin.coroutinesAndroid)

    implementation(Dependencies.AndroidX.annotation)
    implementation(Dependencies.AndroidX.collection)

    implementation(Dependencies.Logger.logger)

    testImplementation(project(":core-tests"))
    testImplementation(Dependencies.Kotlin.test)
    testImplementation(Dependencies.Kotlin.coroutinesTest)
    testImplementation(Dependencies.Mockito.mockito)
}

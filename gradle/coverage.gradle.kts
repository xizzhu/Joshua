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

apply(plugin = "jacoco")
apply(plugin = "com.github.kt3k.coveralls")

tasks {
    val jacocoMerge by registering(JacocoMerge::class)
    jacocoMerge {
        executionData(fileTree("$buildDir") {
            setIncludes(listOf("jacoco/testDebugUnitTest.exec", "outputs/code_coverage/debugAndroidTest/connected/*.ec"))
        })
        dependsOn("testDebugUnitTest", "connectedDebugAndroidTest")
    }

    val jacocoReport by registering(JacocoReport::class)
    jacocoReport {
        dependsOn(jacocoMerge)

        classDirectories.from(fileTree("${buildDir}/tmp/kotlin-classes/debug"))

        sourceDirectories.from("src/debug/kotlin")
        sourceDirectories.from("src/main/kotlin")
        sourceDirectories.from("src/release/kotlin")

        executionData.setFrom(jacocoMerge.get().destinationFile)

        reports.xml.isEnabled = true
        reports.xml.destination = file("$buildDir/reports/jacoco/test/jacocoTestReport.xml")
        reports.html.isEnabled = true
    }

    getByName("coveralls").dependsOn(jacocoReport)
}

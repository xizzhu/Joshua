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
    val debugCoverageReport by registering(JacocoReport::class)
    debugCoverageReport {
        val dependencies = mutableListOf<String>()
        val kotlinClasses = mutableListOf<ConfigurableFileTree>()
        val coverageSourceDirs = mutableListOf<String>()
        val executionDataDirs = mutableListOf<ConfigurableFileTree>()
        subprojects.forEach { subproject ->
            dependencies.add("${subproject.name}:testDebugUnitTest")
            dependencies.add("${subproject.name}:connectedDebugAndroidTest")

            kotlinClasses.add(fileTree("${subproject.buildDir}/tmp/kotlin-classes/debug"))

            coverageSourceDirs.add("${subproject.name}/src/debug/kotlin")
            coverageSourceDirs.add("${subproject.name}/src/main/kotlin")
            coverageSourceDirs.add("${subproject.name}/src/release/kotlin")

            executionDataDirs.add(fileTree("${subproject.buildDir}") {
                setIncludes(listOf("jacoco/testDebugUnitTest.exec", "outputs/code_coverage/debugAndroidTest/connected/*.ec"))
            })
        }

        dependsOn(dependencies.toTypedArray())

        classDirectories.setFrom(files(kotlinClasses))
        additionalSourceDirs.setFrom(files(coverageSourceDirs))
        sourceDirectories.setFrom(coverageSourceDirs)
        executionData.setFrom(executionDataDirs)

        reports.xml.isEnabled = true
        reports.xml.destination = file("$buildDir/reports/jacoco/test/jacocoTestReport.xml")
        reports.html.isEnabled = true
    }

    getByName("coveralls").dependsOn(debugCoverageReport)
}

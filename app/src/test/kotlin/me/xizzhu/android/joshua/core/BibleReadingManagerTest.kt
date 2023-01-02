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

package me.xizzhu.android.joshua.core

import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.xizzhu.android.joshua.core.repository.BibleReadingRepository
import me.xizzhu.android.joshua.core.repository.TranslationRepository
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import kotlin.test.*

class BibleReadingManagerTest : BaseUnitTest() {
    private lateinit var bibleReadingRepository: BibleReadingRepository
    private lateinit var translationRepository: TranslationRepository

    @BeforeTest
    override fun setup() {
        super.setup()

        bibleReadingRepository = mockk()

        translationRepository = mockk()
        every { translationRepository.downloadedTranslations } returns emptyFlow()
    }

    @Test
    fun `test updateDownloadedTranslations() with no translations downloaded`() = runTest {
        coEvery { bibleReadingRepository.saveCurrentTranslation("") } returns Unit
        coEvery { bibleReadingRepository.clearParallelTranslation() } returns Unit

        val bibleReadingManager = BibleReadingManager(bibleReadingRepository, translationRepository, testScope)
        bibleReadingManager.updateDownloadedTranslations(emptyList())
        coVerifySequence {
            bibleReadingRepository.saveCurrentTranslation("")
            bibleReadingRepository.clearParallelTranslation()
        }
    }

    @Test
    fun `test updateDownloadedTranslations() for first downloaded translation`() = runTest {
        coEvery { bibleReadingRepository.saveCurrentTranslation(MockContents.cuvShortName) } returns Unit
        every { bibleReadingRepository.currentTranslation } returns flowOf("")

        val bibleReadingManager = BibleReadingManager(bibleReadingRepository, translationRepository, testScope)
        bibleReadingManager.updateDownloadedTranslations(listOf(MockContents.cuvTranslationInfo))
        coVerifySequence {
            bibleReadingRepository.currentTranslation
            bibleReadingRepository.saveCurrentTranslation(MockContents.cuvShortName)
            bibleReadingRepository.parallelTranslations
        }
    }

    @Test
    fun `test updateDownloadedTranslations() with translations already downloaded`() = runTest {
        every { bibleReadingRepository.currentTranslation } returns flowOf(MockContents.kjvShortName)

        val bibleReadingManager = BibleReadingManager(bibleReadingRepository, translationRepository, testScope)
        bibleReadingManager.updateDownloadedTranslations(listOf(MockContents.cuvDownloadedTranslationInfo, MockContents.kjvDownloadedTranslationInfo))
        coVerifySequence {
            bibleReadingRepository.currentTranslation
            bibleReadingRepository.parallelTranslations
        }
    }

    @Test
    fun `test updateDownloadedTranslations() with parallel translations`() = runTest {
        every { bibleReadingRepository.currentTranslation } returns flowOf(MockContents.kjvShortName)
        every { bibleReadingRepository.parallelTranslations } returns flowOf(listOf(MockContents.cuvShortName))

        val bibleReadingManager = BibleReadingManager(bibleReadingRepository, translationRepository, testScope)

        // Two translations are downloaded.
        bibleReadingManager.updateDownloadedTranslations(listOf(
                MockContents.cuvDownloadedTranslationInfo, MockContents.kjvDownloadedTranslationInfo
        ))
        // Two more translations are downloaded.
        bibleReadingManager.updateDownloadedTranslations(listOf(
                MockContents.cuvDownloadedTranslationInfo,
                MockContents.kjvDownloadedTranslationInfo,
                MockContents.bbeDownloadedTranslationInfo,
                MockContents.msgDownloadedTranslationInfo
        ))
        // One translation is removed.
        bibleReadingManager.updateDownloadedTranslations(listOf(
                MockContents.cuvDownloadedTranslationInfo, MockContents.kjvDownloadedTranslationInfo, MockContents.bbeDownloadedTranslationInfo
        ))
        // One translation, which is also a parallel, is removed. Should update the parallel.
        bibleReadingManager.updateDownloadedTranslations(listOf(
                MockContents.kjvDownloadedTranslationInfo, MockContents.bbeDownloadedTranslationInfo
        ))
        every { bibleReadingRepository.parallelTranslations } returns flowOf(emptyList())
        // One more translation installed, and current translation removed. Should update the current.
        coEvery { bibleReadingRepository.saveCurrentTranslation(MockContents.bbeShortName) } returns Unit
        bibleReadingManager.updateDownloadedTranslations(listOf(
                MockContents.bbeDownloadedTranslationInfo, MockContents.msgDownloadedTranslationInfo
        ))

        coVerifySequence {
            // Two translations are downloaded.
            bibleReadingRepository.currentTranslation
            bibleReadingRepository.parallelTranslations

            // Two more translations are downloaded.
            bibleReadingRepository.currentTranslation
            bibleReadingRepository.parallelTranslations

            // One translation is removed.
            bibleReadingRepository.currentTranslation
            bibleReadingRepository.parallelTranslations

            // One translation, which is also a parallel, is removed. Should update the parallel.
            bibleReadingRepository.currentTranslation
            bibleReadingRepository.parallelTranslations
            bibleReadingRepository.saveParallelTranslations(emptyList())

            // One more translation installed, and current translation removed. Should update the current.
            bibleReadingRepository.currentTranslation
            bibleReadingRepository.saveCurrentTranslation(MockContents.bbeShortName)
            bibleReadingRepository.parallelTranslations
        }
    }
}

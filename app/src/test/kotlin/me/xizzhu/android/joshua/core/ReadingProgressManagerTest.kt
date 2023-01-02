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

import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import me.xizzhu.android.joshua.core.repository.BibleReadingRepository
import me.xizzhu.android.joshua.core.repository.ReadingProgressRepository
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.utils.elapsedRealtime
import kotlin.test.BeforeTest
import kotlin.test.Test

class ReadingProgressManagerTest : BaseUnitTest() {
    private lateinit var bibleReadingRepository: BibleReadingRepository
    private lateinit var readingProgressRepository: ReadingProgressRepository
    private lateinit var readingProgressManager: ReadingProgressManager

    @BeforeTest
    override fun setup() {
        super.setup()

        bibleReadingRepository = mockk()
        readingProgressRepository = mockk()
        readingProgressManager = ReadingProgressManager(bibleReadingRepository, readingProgressRepository, testScope)
    }

    @Test
    fun `test trackReadingProgress() with nothing to be recorded`() = runTest {
        every { bibleReadingRepository.currentVerseIndex } returns emptyFlow()
        every { bibleReadingRepository.currentTranslation } returns emptyFlow()

        readingProgressManager.startTracking()
        readingProgressManager.stopTracking()

        coVerify(exactly = 0) { readingProgressRepository.trackReadingProgress(any(), any(), any(), any()) }
    }

    @Test
    fun `test trackReadingProgress()`() = runTest {
        every { bibleReadingRepository.currentVerseIndex } returns flowOf(VerseIndex(1, 2, 3))
        every { bibleReadingRepository.currentTranslation } returns flowOf(MockContents.kjvShortName)

        elapsedRealtime = 1L
        readingProgressManager.startTracking()

        elapsedRealtime = ReadingProgressManager.TIME_SPENT_THRESHOLD_IN_MILLIS + 2L
        readingProgressManager.stopTracking()

        coVerifySequence {
            readingProgressRepository.trackReadingProgress(
                    bookIndex = 1,
                    chapterIndex = 2,
                    timeSpentInMills = ReadingProgressManager.TIME_SPENT_THRESHOLD_IN_MILLIS + 1L,
                    timestamp = ReadingProgressManager.TIME_SPENT_THRESHOLD_IN_MILLIS + 2L
            )
        }
    }

    @Test
    fun `test trackReadingProgress() with too little time spent`() = runTest {
        every { bibleReadingRepository.currentVerseIndex } returns flowOf(VerseIndex(1, 2, 3))
        every { bibleReadingRepository.currentTranslation } returns flowOf(MockContents.kjvShortName)

        readingProgressManager.startTracking()
        coVerify(exactly = 0) { readingProgressRepository.trackReadingProgress(any(), any(), any(), any()) }

        elapsedRealtime = ReadingProgressManager.TIME_SPENT_THRESHOLD_IN_MILLIS - 1L

        readingProgressManager.stopTracking()
        coVerify(exactly = 0) { readingProgressRepository.trackReadingProgress(any(), any(), any(), any()) }
    }

    @Test
    fun `test trackReadingProgress() without current translation`() = runTest {
        every { bibleReadingRepository.currentVerseIndex } returns flowOf(VerseIndex(1, 2, 3))
        every { bibleReadingRepository.currentTranslation } returns emptyFlow()

        readingProgressManager.startTracking()
        coVerify(exactly = 0) { readingProgressRepository.trackReadingProgress(any(), any(), any(), any()) }

        elapsedRealtime = ReadingProgressManager.TIME_SPENT_THRESHOLD_IN_MILLIS + 2L

        readingProgressManager.stopTracking()
        coVerify(exactly = 0) { readingProgressRepository.trackReadingProgress(any(), any(), any(), any()) }
    }

    @Test
    fun `test calling startTracking() multiple times`() = runTest {
        every { bibleReadingRepository.currentVerseIndex } returns flowOf(VerseIndex.INVALID)
        every { bibleReadingRepository.currentTranslation } returns flowOf(MockContents.kjvShortName)

        launch(Dispatchers.Unconfined) {
            readingProgressManager.startTracking()
        }
        launch(Dispatchers.Unconfined) {
            readingProgressManager.startTracking()
        }
        readingProgressManager.stopTracking()

        coVerifySequence { bibleReadingRepository.currentVerseIndex }
    }
}

/*
 * Copyright (C) 2021 Xizhi Zhu
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

package me.xizzhu.android.joshua.progress

import android.app.Application
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.ReadingProgress
import me.xizzhu.android.joshua.core.ReadingProgressManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.infra.BaseViewModel
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReadingProgressViewModelTest : BaseUnitTest() {
    private lateinit var readingProgressManager: ReadingProgressManager
    private lateinit var bibleReadingManager: BibleReadingManager
    private lateinit var settingsManager: SettingsManager
    private lateinit var application: Application

    private lateinit var readingProgressViewModel: ReadingProgressViewModel

    @BeforeTest
    override fun setup() {
        super.setup()

        readingProgressManager = mockk()
        bibleReadingManager = mockk()
        settingsManager = mockk()
        application = mockk()

        readingProgressViewModel = ReadingProgressViewModel(bibleReadingManager, readingProgressManager, settingsManager, application)
    }

    @Test
    fun `test loadReadingProgress with exception`() = testDispatcher.runBlockingTest {
        coEvery { readingProgressManager.read() } throws RuntimeException("random exception")

        val job = async { readingProgressViewModel.readingProgress().take(2).toList() }
        readingProgressViewModel.loadReadingProgress()

        val actual = job.await()
        assertEquals(2, actual.size)
        assertTrue(actual[0] is BaseViewModel.ViewData.Loading)
        assertTrue(actual[1] is BaseViewModel.ViewData.Failure)
    }

    @Test
    fun `test loadReadingProgress`() = testDispatcher.runBlockingTest {
        coEvery { bibleReadingManager.currentTranslation() } returns flowOf(MockContents.kjvShortName)
        coEvery { bibleReadingManager.readBookNames(MockContents.kjvShortName) } returns MockContents.kjvBookNames
        coEvery { readingProgressManager.read() } returns ReadingProgress(
                continuousReadingDays = 2,
                lastReadingTimestamp = 12345L,
                chapterReadingStatus = listOf(
                        ReadingProgress.ChapterReadingStatus(0, 0, 1, 2L, 123L),
                        ReadingProgress.ChapterReadingStatus(0, 1, 1, 2L, 123L),
                        ReadingProgress.ChapterReadingStatus(1, 1, 1, 2L, 123L),
                        ReadingProgress.ChapterReadingStatus(62, 0, 1, 2L, 123L),
                )
        )

        val job = async { readingProgressViewModel.readingProgress().take(2).toList() }
        readingProgressViewModel.loadReadingProgress()

        val actual = job.await()
        assertEquals(2, actual.size)
        assertTrue(actual[0] is BaseViewModel.ViewData.Loading)

        val success = actual[1]
        assertTrue(success is BaseViewModel.ViewData.Success)
        assertEquals(67, success.data.items.size)
        assertEquals(2, (success.data.items[0] as ReadingProgressSummaryItem).continuousReadingDays)
        assertEquals(4, (success.data.items[0] as ReadingProgressSummaryItem).chaptersRead)
        assertEquals(1, (success.data.items[0] as ReadingProgressSummaryItem).finishedBooks)
        assertEquals(0, (success.data.items[0] as ReadingProgressSummaryItem).finishedOldTestament)
        assertEquals(1, (success.data.items[0] as ReadingProgressSummaryItem).finishedNewTestament)
        success.data.items.subList(1, success.data.items.size).forEachIndexed { book, item ->
            assertTrue(item is ReadingProgressDetailItem)
            when (book) {
                0 -> {
                    item.chaptersRead.forEachIndexed { chapter, read -> assertEquals(chapter == 0 || chapter == 1, read) }
                    assertEquals(2, item.chaptersReadCount)
                }
                1 -> {
                    item.chaptersRead.forEachIndexed { chapter, read -> assertEquals(chapter == 1, read) }
                    assertEquals(1, item.chaptersReadCount)
                }
                62 -> {
                    item.chaptersRead.forEach { assertTrue(it) }
                    assertEquals(1, item.chaptersReadCount)
                }
                else -> {
                    item.chaptersRead.forEach { assertFalse(it) }
                    assertEquals(0, item.chaptersReadCount)
                }
            }
        }
    }
}

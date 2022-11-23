/*
 * Copyright (C) 2022 Xizhi Zhu
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
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import me.xizzhu.android.joshua.core.*
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
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT)
        application = mockk()

        readingProgressViewModel = ReadingProgressViewModel(bibleReadingManager, readingProgressManager, settingsManager, application)
    }

    @Test
    fun `test loadReadingProgress with exception`() = runTest {
        coEvery { readingProgressManager.read() } throws RuntimeException("random exception")

        val viewActionAsync = async(Dispatchers.Default) { readingProgressViewModel.viewAction().first() }
        delay(100)

        readingProgressViewModel.loadReadingProgress()

        assertTrue(viewActionAsync.await() is ReadingProgressViewModel.ViewAction.ShowLoadReadingProgressFailedError)
        with(readingProgressViewModel.viewState().first()) {
            assertEquals(Settings.DEFAULT, settings)
            assertFalse(loading)
            assertTrue(readingProgressItems.isEmpty())
        }
    }

    @Test
    fun `test loadReadingProgress`() = runTest {
        coEvery { bibleReadingManager.currentTranslation() } returns flowOf(MockContents.kjvShortName)
        coEvery { bibleReadingManager.readBookNames(MockContents.kjvShortName) } returns MockContents.kjvBookNames
        coEvery { readingProgressManager.read() } returns ReadingProgress(
                continuousReadingDays = 2,
                lastReadingTimestamp = 12345L,
                chapterReadingStatus = listOf(
                        ReadingProgress.ChapterReadingStatus(0, 0, 1, 2L, 123L),
                        ReadingProgress.ChapterReadingStatus(0, 1, 1, 2L, 123L),
                        ReadingProgress.ChapterReadingStatus(1, 1, 1, 2L, 123L),
                        ReadingProgress.ChapterReadingStatus(30, 0, 1, 2L, 123L),
                        ReadingProgress.ChapterReadingStatus(62, 0, 1, 2L, 123L),
                )
        )

        val viewStateAsync = async(Dispatchers.Default) {
            readingProgressViewModel.viewState()
                    .drop(1) // drop the initial state
                    .take(2)
                    .toList()
        }
        delay(100) // makes sure the async is up and running

        readingProgressViewModel.loadReadingProgress()

        val viewStates = viewStateAsync.await()

        assertTrue(viewStates[0].loading)
        assertEquals(Settings.DEFAULT, viewStates[0].settings)
        assertTrue(viewStates[0].readingProgressItems.isEmpty())

        assertEquals(Settings.DEFAULT, viewStates[1].settings)
        assertFalse(viewStates[1].loading)
        assertEquals(67, viewStates[1].readingProgressItems.size)
        assertEquals(2, (viewStates[1].readingProgressItems[0] as ReadingProgressSummaryItem).continuousReadingDays)
        assertEquals(5, (viewStates[1].readingProgressItems[0] as ReadingProgressSummaryItem).chaptersRead)
        assertEquals(2, (viewStates[1].readingProgressItems[0] as ReadingProgressSummaryItem).finishedBooks)
        assertEquals(1, (viewStates[1].readingProgressItems[0] as ReadingProgressSummaryItem).finishedOldTestament)
        assertEquals(1, (viewStates[1].readingProgressItems[0] as ReadingProgressSummaryItem).finishedNewTestament)
        viewStates[1].readingProgressItems.subList(1, viewStates[1].readingProgressItems.size).forEachIndexed { book, item ->
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
                30 -> {
                    item.chaptersRead.forEach { assertTrue(it) }
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

    @Test
    fun `test openVerse() with exception`() = runTest {
        coEvery { bibleReadingManager.saveCurrentVerseIndex(VerseIndex(0, 0, 0)) } throws RuntimeException("random exception")

        val viewActionAsync = async(Dispatchers.Default) { readingProgressViewModel.viewAction().first() }
        delay(100)

        readingProgressViewModel.openVerse(VerseIndex(0, 0, 0))

        with(viewActionAsync.await()) {
            assertTrue(this is ReadingProgressViewModel.ViewAction.ShowOpenVerseFailedError)
            assertEquals(VerseIndex(0, 0, 0), verseToOpen)
        }
    }

    @Test
    fun `test openVerse()`() = runTest {
        coEvery { bibleReadingManager.saveCurrentVerseIndex(VerseIndex(0, 0, 0)) } returns Unit

        val viewActionAsync = async(Dispatchers.Default) { readingProgressViewModel.viewAction().first() }
        delay(100)

        readingProgressViewModel.openVerse(VerseIndex(0, 0, 0))

        assertTrue(viewActionAsync.await() is ReadingProgressViewModel.ViewAction.OpenReadingScreen)
    }
}

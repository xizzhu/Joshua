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

package me.xizzhu.android.joshua.progress

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.ReadingProgress
import me.xizzhu.android.joshua.core.ReadingProgressManager
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReadingProgressViewModelTest : BaseUnitTest() {
    private lateinit var readingProgressManager: ReadingProgressManager
    private lateinit var bibleReadingManager: BibleReadingManager
    private lateinit var settingsManager: SettingsManager

    private lateinit var readingProgressViewModel: ReadingProgressViewModel

    @BeforeTest
    override fun setup() {
        super.setup()

        readingProgressManager = mockk()
        bibleReadingManager = mockk()
        settingsManager = mockk<SettingsManager>().apply { every { settings() } returns flowOf(Settings.DEFAULT) }

        readingProgressViewModel = ReadingProgressViewModel(bibleReadingManager, readingProgressManager, settingsManager, testCoroutineDispatcherProvider)
    }

    @Test
    fun `test loadReadingProgress, with exception`() = runTest {
        coEvery { readingProgressManager.read() } throws RuntimeException("random exception")

        readingProgressViewModel.loadReadingProgress()
        assertEquals(
            ReadingProgressViewModel.ViewState(
                loading = false,
                items = emptyList(),
                error = ReadingProgressViewModel.ViewState.Error.ReadingProgressLoadingError
            ),
            readingProgressViewModel.viewState().first()
        )

        readingProgressViewModel.markErrorAsShown(ReadingProgressViewModel.ViewState.Error.VerseOpeningError(VerseIndex(0, 0, 0)))
        assertEquals(
            ReadingProgressViewModel.ViewState(
                loading = false,
                items = emptyList(),
                error = ReadingProgressViewModel.ViewState.Error.ReadingProgressLoadingError
            ),
            readingProgressViewModel.viewState().first()
        )

        readingProgressViewModel.markErrorAsShown(ReadingProgressViewModel.ViewState.Error.ReadingProgressLoadingError)
        assertEquals(
            ReadingProgressViewModel.ViewState(
                loading = false,
                items = emptyList(),
                error = null
            ),
            readingProgressViewModel.viewState().first()
        )
    }

    @Test
    fun `test loadReadingProgress`() = runTest {
        coEvery { bibleReadingManager.currentTranslation() } returns flowOf(MockContents.kjvShortName)
        coEvery { bibleReadingManager.readBookNames(MockContents.kjvShortName) } returns MockContents.kjvBookNames
        coEvery { readingProgressManager.read() } returns ReadingProgress(
            continuousReadingDays = 2,
            lastReadingTimestamp = 12345L,
            chapterReadingStatus = listOf(
                ReadingProgress.ChapterReadingStatus(bookIndex = 0, chapterIndex = 0, readCount = 1, timeSpentInMillis = 2L, lastReadingTimestamp = 123L),
                ReadingProgress.ChapterReadingStatus(bookIndex = 0, chapterIndex = 1, readCount = 1, timeSpentInMillis = 2L, lastReadingTimestamp = 123L),
                ReadingProgress.ChapterReadingStatus(bookIndex = 0, chapterIndex = 2, readCount = 0, timeSpentInMillis = 2L, lastReadingTimestamp = 123L),
                ReadingProgress.ChapterReadingStatus(bookIndex = 1, chapterIndex = 1, readCount = 1, timeSpentInMillis = 2L, lastReadingTimestamp = 123L),
                ReadingProgress.ChapterReadingStatus(bookIndex = 30, chapterIndex = 0, readCount = 1, timeSpentInMillis = 2L, lastReadingTimestamp = 123L),
                ReadingProgress.ChapterReadingStatus(bookIndex = 62, chapterIndex = 0, readCount = 1, timeSpentInMillis = 2L, lastReadingTimestamp = 123L),
            )
        )

        readingProgressViewModel.loadReadingProgress()

        val actual = readingProgressViewModel.viewState().first()
        assertFalse(actual.loading)
        assertEquals(67, actual.items.size)
        assertEquals(
            ReadingProgressItem.Summary(
                settings = Settings.DEFAULT,
                continuousReadingDays = 2,
                chaptersRead = 5,
                finishedBooks = 2,
                finishedOldTestament = 1,
                finishedNewTestament = 1
            ),
            actual.items[0]
        )
        actual.items.subList(1, actual.items.size).forEachIndexed { book, item ->
            assertTrue(item is ReadingProgressItem.Book)
            assertEquals(Settings.DEFAULT, item.settings)
            assertEquals(book, item.bookIndex)
            assertEquals(book == 0, item.expanded)
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
        assertNull(actual.error)

        readingProgressViewModel.expandOrCollapseBook(bookIndex = 1)
        with(readingProgressViewModel.viewState().first().items.subList(1, actual.items.size)) {
            forEachIndexed { book, item ->
                assertTrue(item is ReadingProgressItem.Book)
                assertEquals(book == 0 || book == 1, item.expanded)
            }
        }

        readingProgressViewModel.expandOrCollapseBook(bookIndex = 1)
        with(readingProgressViewModel.viewState().first().items.subList(1, actual.items.size)) {
            forEachIndexed { book, item ->
                assertTrue(item is ReadingProgressItem.Book)
                assertEquals(book == 0, item.expanded)
            }
        }
    }

    @Test
    fun `test expandOrCollapseBook(), with no items`() = runTest {
        readingProgressViewModel.expandOrCollapseBook(bookIndex = 0)

        assertEquals(
            ReadingProgressViewModel.ViewState(
                loading = false,
                items = emptyList(),
                error = null
            ),
            readingProgressViewModel.viewState().first()
        )
    }

    @Test
    fun `test openVerse(), with exception`() = runTest {
        coEvery { bibleReadingManager.saveCurrentVerseIndex(VerseIndex(0, 0, 0)) } throws RuntimeException("random exception")

        readingProgressViewModel.openVerse(VerseIndex(0, 0, 0))

        assertEquals(
            ReadingProgressViewModel.ViewState(
                loading = false,
                items = emptyList(),
                error = ReadingProgressViewModel.ViewState.Error.VerseOpeningError(VerseIndex(0, 0, 0))
            ),
            readingProgressViewModel.viewState().first()
        )
    }

    @Test
    fun `test openVerse()`() = runTest {
        coEvery { bibleReadingManager.saveCurrentVerseIndex(VerseIndex(0, 0, 0)) } returns Unit

        val viewAction = async(Dispatchers.Unconfined) { readingProgressViewModel.viewAction().first() }

        readingProgressViewModel.markErrorAsShown(ReadingProgressViewModel.ViewState.Error.ReadingProgressLoadingError)
        readingProgressViewModel.openVerse(VerseIndex(0, 0, 0))

        assertEquals(
            ReadingProgressViewModel.ViewState(
                loading = false,
                items = emptyList(),
                error = null
            ),
            readingProgressViewModel.viewState().first()
        )
        assertEquals(ReadingProgressViewModel.ViewAction.OpenReadingScreen, viewAction.await())
    }
}

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

package me.xizzhu.android.joshua.annotated

import android.app.Application
import android.content.res.Resources
import android.text.format.DateUtils
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.annotated.bookmarks.BookmarkItem
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.VerseAnnotation
import me.xizzhu.android.joshua.core.VerseAnnotationManager
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.TextItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import me.xizzhu.android.joshua.utils.currentTimeMillis
import java.util.*
import kotlin.test.*

class AnnotatedVersesViewModelTest : BaseUnitTest() {
    private class TestAnnotatedVerse(verseIndex: VerseIndex, timestamp: Long) : VerseAnnotation(verseIndex, timestamp)

    private class TestAnnotatedVersesViewModel(
            bibleReadingManager: BibleReadingManager,
            verseAnnotationManager: VerseAnnotationManager<TestAnnotatedVerse>,
            settingsManager: SettingsManager,
            application: Application
    ) : AnnotatedVersesViewModel<TestAnnotatedVerse>(
            bibleReadingManager, verseAnnotationManager, R.string.text_no_bookmarks, settingsManager, application
    ) {
        override fun buildBaseItem(
                annotatedVerse: TestAnnotatedVerse, bookName: String, bookShortName: String, verseText: String, sortOrder: Int
        ): BaseItem = BookmarkItem(annotatedVerse.verseIndex, bookName, bookShortName, verseText, sortOrder)
    }

    private lateinit var bibleReadingManager: BibleReadingManager
    private lateinit var verseAnnotationManager: VerseAnnotationManager<TestAnnotatedVerse>
    private lateinit var settingsManager: SettingsManager
    private lateinit var resources: Resources
    private lateinit var application: Application
    private lateinit var annotatedVersesViewModel: AnnotatedVersesViewModel<TestAnnotatedVerse>

    @BeforeTest
    override fun setup() {
        super.setup()

        bibleReadingManager = mockk()
        every { bibleReadingManager.currentTranslation() } returns emptyFlow()

        verseAnnotationManager = mockk()
        every { verseAnnotationManager.sortOrder() } returns emptyFlow()

        settingsManager = mockk()
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT)

        resources = mockk()
        every { resources.getStringArray(R.array.text_months) } returns arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12")

        application = mockk()
        every { application.getString(R.string.text_no_bookmarks) } returns "NO ANNOTATED VERSES"
        every { application.resources } returns resources

        annotatedVersesViewModel = TestAnnotatedVersesViewModel(bibleReadingManager, verseAnnotationManager, settingsManager, application)
    }

    @Test
    fun `test loadAnnotatedVerses() from constructor with exception`() = runTest {
        every { bibleReadingManager.currentTranslation() } returns flowOf(MockContents.kjvShortName)
        every { verseAnnotationManager.sortOrder() } returns flowOf(Constants.SORT_BY_BOOK)
        coEvery { verseAnnotationManager.read(Constants.SORT_BY_BOOK) } throws RuntimeException("random exception")

        annotatedVersesViewModel = TestAnnotatedVersesViewModel(bibleReadingManager, verseAnnotationManager, settingsManager, application)
        delay(100)

        with(annotatedVersesViewModel.viewState().first()) {
            assertEquals(Settings.DEFAULT, settings)
            assertFalse(loading)
            assertEquals(Constants.SORT_BY_BOOK, sortOrder)
            assertTrue(annotatedVerseItems.isEmpty())
        }
    }

    @Test
    fun `test loadAnnotatedVerses() from constructor`() = runTest {
        every { bibleReadingManager.currentTranslation() } returns flowOf("", MockContents.kjvShortName, "")
        coEvery { bibleReadingManager.readBookNames(MockContents.kjvShortName) } returns MockContents.kjvBookNames
        coEvery { bibleReadingManager.readBookShortNames(MockContents.kjvShortName) } returns MockContents.kjvBookShortNames
        coEvery { bibleReadingManager.readVerses(MockContents.kjvShortName, emptyList()) } returns emptyMap()
        every { verseAnnotationManager.sortOrder() } returns flowOf(Constants.SORT_BY_BOOK)
        coEvery { verseAnnotationManager.read(Constants.SORT_BY_BOOK) } returns emptyList()

        annotatedVersesViewModel = TestAnnotatedVersesViewModel(bibleReadingManager, verseAnnotationManager, settingsManager, application)
        delay(100)

        with(annotatedVersesViewModel.viewState().first()) {
            assertEquals(Settings.DEFAULT, settings)
            assertFalse(loading)
            assertEquals(Constants.SORT_BY_BOOK, sortOrder)
            assertEquals(1, annotatedVerseItems.size)
            assertEquals("NO ANNOTATED VERSES", (annotatedVerseItems[0] as TextItem).title.toString())
        }
    }

    @Test
    fun `test loadAnnotatedVerses() with exception`() = runTest {
        every { bibleReadingManager.currentTranslation() } throws RuntimeException("random excption")

        annotatedVersesViewModel.loadAnnotatedVerses()
        delay(100)

        with(annotatedVersesViewModel.viewState().first()) {
            assertEquals(Settings.DEFAULT, settings)
            assertFalse(loading)
            assertEquals(Constants.SORT_BY_DATE, sortOrder)
            assertTrue(annotatedVerseItems.isEmpty())
        }
    }

    @Test
    fun `test loadAnnotatedVerses() with empty result`() = runTest {
        every { bibleReadingManager.currentTranslation() } returns flowOf(MockContents.kjvShortName)
        coEvery { bibleReadingManager.readBookNames(MockContents.kjvShortName) } returns MockContents.kjvBookNames
        coEvery { bibleReadingManager.readBookShortNames(MockContents.kjvShortName) } returns MockContents.kjvBookShortNames
        coEvery { bibleReadingManager.readVerses(MockContents.kjvShortName, emptyList()) } returns emptyMap()
        every { verseAnnotationManager.sortOrder() } returns flowOf(Constants.SORT_BY_DATE)
        coEvery { verseAnnotationManager.read(Constants.SORT_BY_DATE) } returns emptyList()

        annotatedVersesViewModel.loadAnnotatedVerses()
        delay(100)

        with(annotatedVersesViewModel.viewState().first()) {
            assertEquals(Settings.DEFAULT, settings)
            assertFalse(loading)
            assertEquals(Constants.SORT_BY_DATE, sortOrder)
            assertEquals(1, annotatedVerseItems.size)
            assertEquals("NO ANNOTATED VERSES", (annotatedVerseItems[0] as TextItem).title.toString())
        }
    }

    @Test
    fun `test loadAnnotatedVerses() sort by book`() = runTest {
        every { bibleReadingManager.currentTranslation() } returns flowOf(MockContents.kjvShortName)
        coEvery { bibleReadingManager.readBookNames(MockContents.kjvShortName) } returns MockContents.kjvBookNames
        coEvery { bibleReadingManager.readBookShortNames(MockContents.kjvShortName) } returns MockContents.kjvBookShortNames
        coEvery {
            bibleReadingManager.readVerses(
                    MockContents.kjvShortName,
                    listOf(
                            VerseIndex(0, 0, 0),
                            VerseIndex(0, 0, 1),
                            VerseIndex(0, 9, 9),
                            VerseIndex(1, 22, 18)
                    )
            )
        } returns mapOf(
                Pair(VerseIndex(0, 0, 0), MockContents.kjvVerses[0]),
                Pair(VerseIndex(0, 0, 1), MockContents.kjvVerses[1]),
                Pair(VerseIndex(0, 9, 9), MockContents.kjvExtraVerses[0]),
                Pair(VerseIndex(1, 22, 18), MockContents.kjvExtraVerses[1])
        )
        every { verseAnnotationManager.sortOrder() } returns flowOf(Constants.SORT_BY_BOOK)
        coEvery { verseAnnotationManager.read(Constants.SORT_BY_BOOK) } returns listOf(
                TestAnnotatedVerse(VerseIndex(0, 0, 0), 12345L),
                TestAnnotatedVerse(VerseIndex(0, 0, 1), 12345L),
                TestAnnotatedVerse(VerseIndex(0, 9, 9), 12345L),
                TestAnnotatedVerse(VerseIndex(1, 22, 18), 12345L)
        )

        annotatedVersesViewModel.loadAnnotatedVerses()
        delay(100)

        with(annotatedVersesViewModel.viewState().first()) {
            assertEquals(Settings.DEFAULT, settings)
            assertFalse(loading)
            assertEquals(6, annotatedVerseItems.size)
            assertEquals("Genesis", (annotatedVerseItems[0] as TitleItem).title.toString())
            assertEquals(VerseIndex(0, 0, 0), (annotatedVerseItems[1] as BookmarkItem).verseIndex)
            assertEquals(VerseIndex(0, 0, 1), (annotatedVerseItems[2] as BookmarkItem).verseIndex)
            assertEquals(VerseIndex(0, 9, 9), (annotatedVerseItems[3] as BookmarkItem).verseIndex)
            assertEquals("Exodus", (annotatedVerseItems[4] as TitleItem).title.toString())
            assertEquals(VerseIndex(1, 22, 18), (annotatedVerseItems[5] as BookmarkItem).verseIndex)
        }
    }

    @Test
    fun `test loadAnnotatedVerses() sort by date`() = runTest {
        annotatedVersesViewModel = spyk(annotatedVersesViewModel)
        every { annotatedVersesViewModel.formatDate(any(), any()) } answers { (it.invocation.args[1] as Long).toString() }
        every { bibleReadingManager.currentTranslation() } returns flowOf(MockContents.kjvShortName)
        coEvery { bibleReadingManager.readBookNames(MockContents.kjvShortName) } returns MockContents.kjvBookNames
        coEvery { bibleReadingManager.readBookShortNames(MockContents.kjvShortName) } returns MockContents.kjvBookShortNames
        coEvery {
            bibleReadingManager.readVerses(
                    MockContents.kjvShortName,
                    listOf(
                            VerseIndex(0, 0, 0),
                            VerseIndex(0, 0, 1),
                            VerseIndex(0, 0, 2),
                            VerseIndex(0, 9, 9),
                            VerseIndex(1, 22, 18)
                    )
            )
        } returns mapOf(
                VerseIndex(0, 0, 0) to MockContents.kjvVerses[0],
                VerseIndex(0, 0, 1) to MockContents.kjvVerses[1],
                VerseIndex(0, 9, 9) to MockContents.kjvExtraVerses[0],
                VerseIndex(1, 22, 18) to MockContents.kjvExtraVerses[1]
        )
        every { verseAnnotationManager.sortOrder() } returns flowOf(Constants.SORT_BY_DATE)
        coEvery { verseAnnotationManager.read(Constants.SORT_BY_DATE) } returns listOf(
                TestAnnotatedVerse(VerseIndex(0, 0, 0), 5L + DateUtils.DAY_IN_MILLIS * 360),
                TestAnnotatedVerse(VerseIndex(0, 0, 1), 4L),
                TestAnnotatedVerse(VerseIndex(0, 0, 2), 3L), // This verse is "not available" when reading from bibleReadingManager.readVerses
                TestAnnotatedVerse(VerseIndex(0, 9, 9), 2L),
                TestAnnotatedVerse(VerseIndex(1, 22, 18), 1L)
        )

        annotatedVersesViewModel.loadAnnotatedVerses()
        delay(100)

        with(annotatedVersesViewModel.viewState().first()) {
            assertEquals(Settings.DEFAULT, settings)
            assertFalse(loading)
            assertEquals(6, annotatedVerseItems.size)
            assertEquals("31104000005", (annotatedVerseItems[0] as TitleItem).title.toString())
            assertEquals(VerseIndex(0, 0, 0), (annotatedVerseItems[1] as BookmarkItem).verseIndex)
            assertEquals("4", (annotatedVerseItems[2] as TitleItem).title.toString())
            assertEquals(VerseIndex(0, 0, 1), (annotatedVerseItems[3] as BookmarkItem).verseIndex)
            assertEquals(VerseIndex(0, 9, 9), (annotatedVerseItems[4] as BookmarkItem).verseIndex)
            assertEquals(VerseIndex(1, 22, 18), (annotatedVerseItems[5] as BookmarkItem).verseIndex)
        }
    }

    @Test
    fun `test formatDate()`() {
        every { application.getString(R.string.text_date_without_year, *anyVararg()) } answers {
            (it.invocation.args[1] as Array<Any>).joinToString(separator = "-")
        }
        every { application.getString(R.string.text_date, *anyVararg()) } answers {
            (it.invocation.args[1] as Array<Any>).joinToString(separator = ".")
        }

        currentTimeMillis = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2022)
            set(Calendar.MONTH, Calendar.FEBRUARY)
            set(Calendar.DATE, 10)
        }.timeInMillis

        val sameYear = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2022)
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DATE, 30)
        }.timeInMillis
        assertEquals("1-30", annotatedVersesViewModel.formatDate(Calendar.getInstance(), sameYear))

        val differentYear = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2021)
            set(Calendar.MONTH, Calendar.FEBRUARY)
            set(Calendar.DATE, 10)
        }.timeInMillis
        assertEquals("2.10.2021", annotatedVersesViewModel.formatDate(Calendar.getInstance(), differentYear))
    }

    @Test
    fun `test saveSortOrder() with exception`() = runTest {
        coEvery { verseAnnotationManager.saveSortOrder(Constants.SORT_BY_BOOK) } throws RuntimeException("random exception")

        val viewActionAsync = async(Dispatchers.Default) { annotatedVersesViewModel.viewAction().first() }
        delay(100)

        annotatedVersesViewModel.saveSortOrder(Constants.SORT_BY_BOOK)

        with(viewActionAsync.await()) {
            assertTrue(this is AnnotatedVersesViewModel.ViewAction.ShowSaveSortOrderFailedError)
            assertEquals(Constants.SORT_BY_BOOK, sortOrderToSave)
        }
    }

    @Test
    fun `test saveSortOrder()`() = runTest {
        coEvery { verseAnnotationManager.saveSortOrder(Constants.SORT_BY_BOOK) } returns Unit

        annotatedVersesViewModel.saveSortOrder(Constants.SORT_BY_BOOK)

        assertEquals(Constants.SORT_BY_BOOK, annotatedVersesViewModel.viewState().first().sortOrder)
    }

    @Test
    fun `test openVerse() with exception`() = runTest {
        coEvery { bibleReadingManager.saveCurrentVerseIndex(VerseIndex(0, 0, 0)) } throws RuntimeException("random exception")

        val viewActionAsync = async(Dispatchers.Default) { annotatedVersesViewModel.viewAction().first() }
        delay(100)

        annotatedVersesViewModel.openVerse(VerseIndex(0, 0, 0))

        with(viewActionAsync.await()) {
            assertTrue(this is AnnotatedVersesViewModel.ViewAction.ShowOpenVerseFailedError)
            assertEquals(VerseIndex(0, 0, 0), verseToOpen)
        }
    }

    @Test
    fun `test openVerse()`() = runTest {
        coEvery { bibleReadingManager.saveCurrentVerseIndex(VerseIndex(0, 0, 0)) } returns Unit

        val viewActionAsync = async(Dispatchers.Default) { annotatedVersesViewModel.viewAction().first() }
        delay(100)

        annotatedVersesViewModel.openVerse(VerseIndex(0, 0, 0))

        assertTrue(viewActionAsync.await() is AnnotatedVersesViewModel.ViewAction.OpenReadingScreen)
    }

    @Test
    fun `test showPreview() with invalid verse index`() = runTest {
        val viewActionAsync = async(Dispatchers.Default) { annotatedVersesViewModel.viewAction().first() }
        delay(100)

        annotatedVersesViewModel.showPreview(VerseIndex.INVALID)

        assertTrue(viewActionAsync.await() is AnnotatedVersesViewModel.ViewAction.ShowOpenPreviewFailedError)
    }

    @Test
    fun `test showPreview()`() = runTest {
        coEvery { bibleReadingManager.currentTranslation() } returns flowOf(MockContents.kjvShortName)
        coEvery {
            bibleReadingManager.readVerses(MockContents.kjvShortName, 0, 0)
        } returns listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[1], MockContents.kjvVerses[2])
        coEvery { bibleReadingManager.readBookShortNames(MockContents.kjvShortName) } returns MockContents.kjvBookShortNames
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT)

        val viewActionAsync = async(Dispatchers.Default) { annotatedVersesViewModel.viewAction().first() }
        delay(100)

        annotatedVersesViewModel.showPreview(VerseIndex(0, 0, 1))

        val actual = viewActionAsync.await()
        assertTrue(actual is AnnotatedVersesViewModel.ViewAction.ShowPreview)
        assertEquals(Settings.DEFAULT, actual.previewViewData.settings)
        assertEquals("Gen., 1", actual.previewViewData.title)
        assertEquals(3, actual.previewViewData.items.size)
        assertEquals(1, actual.previewViewData.currentPosition)
    }
}

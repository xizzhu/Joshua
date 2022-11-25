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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.annotated.bookmarks.BookmarkItem
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.provider.CoroutineDispatcherProvider
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.VerseAnnotation
import me.xizzhu.android.joshua.core.VerseAnnotationManager
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.provider.TimeProvider
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.tests.TestTimeProvider
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.TextItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import java.util.Calendar
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AnnotatedVersesViewModelTest : BaseUnitTest() {
    private class TestAnnotatedVerse(verseIndex: VerseIndex, timestamp: Long) : VerseAnnotation(verseIndex, timestamp)

    private class TestAnnotatedVersesViewModel(
        bibleReadingManager: BibleReadingManager,
        verseAnnotationManager: VerseAnnotationManager<TestAnnotatedVerse>,
        settingsManager: SettingsManager,
        coroutineDispatcherProvider: CoroutineDispatcherProvider,
        timeProvider: TimeProvider,
        application: Application
    ) : AnnotatedVersesViewModel<TestAnnotatedVerse>(
        bibleReadingManager, verseAnnotationManager, R.string.text_no_bookmarks, settingsManager, coroutineDispatcherProvider, timeProvider, application
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

        bibleReadingManager = mockk<BibleReadingManager>().apply { every { currentTranslation() } returns emptyFlow() }
        verseAnnotationManager = mockk<VerseAnnotationManager<TestAnnotatedVerse>>().apply { every { sortOrder() } returns emptyFlow() }
        settingsManager = mockk<SettingsManager>().apply { every { settings() } returns emptyFlow() }
        resources = mockk<Resources>().apply {
            every { getStringArray(R.array.text_months) } returns arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12")
        }
        application = mockk<Application>().apply {
            every { getString(R.string.text_date_without_year, any(), any()) } answers { call ->
                val params = call.invocation.args[1] as Array<Any>
                "${params[0]}-${params[1]}"
            }
            every { getString(R.string.text_date, any(), any(), any()) } answers { call ->
                val params = call.invocation.args[1] as Array<Any>
                "${params[2]}-${params[0]}-${params[1]}"
            }
            every { getString(R.string.text_no_bookmarks) } returns "NO ANNOTATED VERSES"
            every { resources } returns this@AnnotatedVersesViewModelTest.resources
        }

        annotatedVersesViewModel = TestAnnotatedVersesViewModel(
            bibleReadingManager = bibleReadingManager,
            verseAnnotationManager = verseAnnotationManager,
            settingsManager = settingsManager,
            coroutineDispatcherProvider = testCoroutineDispatcherProvider,
            timeProvider = TestTimeProvider(),
            application = application
        )
    }

    @Test
    fun `test loadAnnotatedVerses(), called in constructor, with exception`() = runTest {
        every { bibleReadingManager.currentTranslation() } returns flowOf("", MockContents.kjvShortName)
        every { verseAnnotationManager.sortOrder() } returns flowOf(Constants.SORT_BY_BOOK)
        coEvery { verseAnnotationManager.read(Constants.SORT_BY_BOOK) } throws RuntimeException("random error")

        annotatedVersesViewModel = TestAnnotatedVersesViewModel(
            bibleReadingManager = bibleReadingManager,
            verseAnnotationManager = verseAnnotationManager,
            settingsManager = settingsManager,
            coroutineDispatcherProvider = testCoroutineDispatcherProvider,
            timeProvider = TestTimeProvider(),
            application = application
        )
        assertEquals(
            AnnotatedVersesViewModel.ViewState(
                settings = null,
                loading = false,
                sortOrder = Constants.SORT_BY_BOOK,
                items = emptyList(),
                preview = null,
                error = AnnotatedVersesViewModel.ViewState.Error.AnnotatedVersesLoadingError
            ),
            annotatedVersesViewModel.viewState().first()
        )

        annotatedVersesViewModel.markErrorAsShown(AnnotatedVersesViewModel.ViewState.Error.SortOrderSavingError(Constants.SORT_BY_DATE))
        assertEquals(
            AnnotatedVersesViewModel.ViewState(
                settings = null,
                loading = false,
                sortOrder = Constants.SORT_BY_BOOK,
                items = emptyList(),
                preview = null,
                error = AnnotatedVersesViewModel.ViewState.Error.AnnotatedVersesLoadingError
            ),
            annotatedVersesViewModel.viewState().first()
        )

        annotatedVersesViewModel.markErrorAsShown(AnnotatedVersesViewModel.ViewState.Error.AnnotatedVersesLoadingError)
        assertEquals(
            AnnotatedVersesViewModel.ViewState(
                settings = null,
                loading = false,
                sortOrder = Constants.SORT_BY_BOOK,
                items = emptyList(),
                preview = null,
                error = null
            ),
            annotatedVersesViewModel.viewState().first()
        )
    }

    @Test
    fun `test loadAnnotatedVerses(), called in constructor, with empty result`() = runTest {
        every { bibleReadingManager.currentTranslation() } returns flowOf(MockContents.kjvShortName)
        coEvery { bibleReadingManager.readBookNames(MockContents.kjvShortName) } returns MockContents.kjvBookNames
        coEvery { bibleReadingManager.readBookShortNames(MockContents.kjvShortName) } returns MockContents.kjvBookShortNames
        coEvery { bibleReadingManager.readVerses(MockContents.kjvShortName, emptyList()) } returns emptyMap()
        every { verseAnnotationManager.sortOrder() } returns flowOf(Constants.SORT_BY_DATE)
        coEvery { verseAnnotationManager.read(Constants.SORT_BY_DATE) } returns emptyList()

        annotatedVersesViewModel = TestAnnotatedVersesViewModel(
            bibleReadingManager = bibleReadingManager,
            verseAnnotationManager = verseAnnotationManager,
            settingsManager = settingsManager,
            coroutineDispatcherProvider = testCoroutineDispatcherProvider,
            timeProvider = TestTimeProvider(),
            application = application
        )

        val actual = annotatedVersesViewModel.viewState().first()
        assertNull(actual.settings)
        assertFalse(actual.loading)
        assertEquals(Constants.SORT_BY_DATE, actual.sortOrder)
        assertEquals(1, actual.items.size)
        assertEquals("NO ANNOTATED VERSES", (actual.items[0] as TextItem).title.toString())
        assertNull(actual.preview)
        assertNull(actual.error)
    }

    @Test
    fun `test loadAnnotatedVerses(), with exception`() = runTest {
        every { verseAnnotationManager.sortOrder() } throws RuntimeException("random exception")

        annotatedVersesViewModel.loadAnnotatedVerses()

        assertEquals(
            AnnotatedVersesViewModel.ViewState(
                settings = null,
                loading = false,
                sortOrder = Constants.SORT_BY_DATE,
                items = emptyList(),
                preview = null,
                error = AnnotatedVersesViewModel.ViewState.Error.AnnotatedVersesLoadingError
            ),
            annotatedVersesViewModel.viewState().first()
        )
    }

    @Test
    fun `test loadAnnotatedVerses(), sort by book`() = runTest {
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
            Pair(VerseIndex(0, 0, 0), MockContents.kjvVerses[0]),
            Pair(VerseIndex(0, 0, 1), MockContents.kjvVerses[1]),
            Pair(VerseIndex(0, 9, 9), MockContents.kjvExtraVerses[0]),
            Pair(VerseIndex(1, 22, 18), MockContents.kjvExtraVerses[1])
        )
        every { verseAnnotationManager.sortOrder() } returns flowOf(Constants.SORT_BY_BOOK)
        coEvery { verseAnnotationManager.read(Constants.SORT_BY_BOOK) } returns listOf(
            TestAnnotatedVerse(VerseIndex(0, 0, 0), 1L),
            TestAnnotatedVerse(VerseIndex(0, 0, 1), 1L),
            TestAnnotatedVerse(VerseIndex(0, 0, 2), 1L),
            TestAnnotatedVerse(VerseIndex(0, 9, 9), 1L),
            TestAnnotatedVerse(VerseIndex(1, 22, 18), 1L)
        )

        annotatedVersesViewModel.loadAnnotatedVerses()

        val actual = annotatedVersesViewModel.viewState().first()
        assertNull(actual.settings)
        assertFalse(actual.loading)
        assertEquals(Constants.SORT_BY_BOOK, actual.sortOrder)
        assertEquals(6, actual.items.size)
        assertEquals(VerseIndex(0, 0, 0), (actual.items[1] as BookmarkItem).verseIndex)
        assertEquals(VerseIndex(0, 0, 1), (actual.items[2] as BookmarkItem).verseIndex)
        assertEquals(VerseIndex(0, 9, 9), (actual.items[3] as BookmarkItem).verseIndex)
        assertEquals("Exodus", (actual.items[4] as TitleItem).title.toString())
        assertEquals(VerseIndex(1, 22, 18), (actual.items[5] as BookmarkItem).verseIndex)
        assertNull(actual.preview)
        assertNull(actual.error)
    }

    @Test
    fun `test loadAnnotatedVerses(), called in constructor, sort by date`() = runTest {
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
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT)
        every { verseAnnotationManager.sortOrder() } returns flowOf(Constants.SORT_BY_DATE)
        coEvery { verseAnnotationManager.read(Constants.SORT_BY_DATE) } returns listOf(
            TestAnnotatedVerse(VerseIndex(0, 0, 0), 4L + DateUtils.DAY_IN_MILLIS * 367), // 1971-1-3
            TestAnnotatedVerse(VerseIndex(0, 0, 1), 3L + DateUtils.DAY_IN_MILLIS * 2), // 1970-1-3
            TestAnnotatedVerse(VerseIndex(0, 9, 9), 2L), // 1970-1-2
            TestAnnotatedVerse(VerseIndex(1, 22, 18), 1L)
        )

        annotatedVersesViewModel = TestAnnotatedVersesViewModel(
            bibleReadingManager = bibleReadingManager,
            verseAnnotationManager = verseAnnotationManager,
            settingsManager = settingsManager,
            coroutineDispatcherProvider = testCoroutineDispatcherProvider,
            timeProvider = TestTimeProvider(
                currentTimeMillis = DateUtils.DAY_IN_MILLIS * 400 // year 1971
            ),
            application = application
        )

        val actual = annotatedVersesViewModel.viewState().first()
        assertEquals(Settings.DEFAULT, actual.settings)
        assertFalse(actual.loading)
        assertEquals(Constants.SORT_BY_DATE, actual.sortOrder)
        assertEquals(7, actual.items.size)
        assertEquals("1-3", (actual.items[0] as TitleItem).title.toString())
        assertEquals(VerseIndex(0, 0, 0), (actual.items[1] as BookmarkItem).verseIndex)
        assertEquals("1970-1-3", (actual.items[2] as TitleItem).title.toString())
        assertEquals(VerseIndex(0, 0, 1), (actual.items[3] as BookmarkItem).verseIndex)
        assertEquals("1970-1-1", (actual.items[4] as TitleItem).title.toString())
        assertEquals(VerseIndex(0, 9, 9), (actual.items[5] as BookmarkItem).verseIndex)
        assertEquals(VerseIndex(1, 22, 18), (actual.items[6] as BookmarkItem).verseIndex)
        assertNull(actual.preview)
        assertNull(actual.error)
    }

    @Test
    fun `test saveSortOrder() with exception`() = runTest {
        coEvery { verseAnnotationManager.saveSortOrder(Constants.SORT_BY_DATE) } throws RuntimeException("random exception")

        annotatedVersesViewModel.saveSortOrder(Constants.SORT_BY_DATE)

        assertEquals(
            AnnotatedVersesViewModel.ViewState(
                settings = null,
                loading = false,
                sortOrder = Constants.DEFAULT_SORT_ORDER,
                items = emptyList(),
                preview = null,
                error = AnnotatedVersesViewModel.ViewState.Error.SortOrderSavingError(Constants.SORT_BY_DATE)
            ),
            annotatedVersesViewModel.viewState().first()
        )
    }

    @Test
    fun `test saveSortOrder()`() = runTest {
        coEvery { verseAnnotationManager.saveSortOrder(Constants.SORT_BY_DATE) } returns Unit

        annotatedVersesViewModel.markErrorAsShown(AnnotatedVersesViewModel.ViewState.Error.AnnotatedVersesLoadingError)
        annotatedVersesViewModel.saveSortOrder(Constants.SORT_BY_DATE)

        assertEquals(
            AnnotatedVersesViewModel.ViewState(
                settings = null,
                loading = false,
                sortOrder = Constants.DEFAULT_SORT_ORDER, // The sort order will be updated inside the flow in init{} block, so not changed here.
                items = emptyList(),
                preview = null,
                error = null
            ),
            annotatedVersesViewModel.viewState().first()
        )
    }

    @Test
    fun `test openVerse() with exception`() = runTest {
        coEvery { bibleReadingManager.saveCurrentVerseIndex(VerseIndex(0, 0, 0)) } throws RuntimeException("random exception")

        annotatedVersesViewModel.openVerse(VerseIndex(0, 0, 0))

        assertEquals(
            AnnotatedVersesViewModel.ViewState(
                settings = null,
                loading = false,
                sortOrder = Constants.DEFAULT_SORT_ORDER,
                items = emptyList(),
                preview = null,
                error = AnnotatedVersesViewModel.ViewState.Error.VerseOpeningError(VerseIndex(0, 0, 0))
            ),
            annotatedVersesViewModel.viewState().first()
        )
    }

    @Test
    fun `test openVerse()`() = runTest {
        coEvery { bibleReadingManager.saveCurrentVerseIndex(VerseIndex(0, 0, 0)) } returns Unit

        val viewAction = async(Dispatchers.Unconfined) { annotatedVersesViewModel.viewAction().first() }

        annotatedVersesViewModel.markErrorAsShown(AnnotatedVersesViewModel.ViewState.Error.AnnotatedVersesLoadingError)
        annotatedVersesViewModel.openVerse(VerseIndex(0, 0, 0))

        assertEquals(
            AnnotatedVersesViewModel.ViewState(
                settings = null,
                loading = false,
                sortOrder = Constants.DEFAULT_SORT_ORDER,
                items = emptyList(),
                preview = null,
                error = null
            ),
            annotatedVersesViewModel.viewState().first()
        )
        assertEquals(AnnotatedVersesViewModel.ViewAction.OpenReadingScreen, viewAction.await())
    }

    @Test
    fun `test loadPreview() with invalid verse index`() = runTest {
        annotatedVersesViewModel.loadPreview(VerseIndex.INVALID)

        assertEquals(
            AnnotatedVersesViewModel.ViewState(
                settings = null,
                loading = false,
                sortOrder = Constants.DEFAULT_SORT_ORDER,
                items = emptyList(),
                preview = null,
                error = AnnotatedVersesViewModel.ViewState.Error.PreviewLoadingError(VerseIndex.INVALID)
            ),
            annotatedVersesViewModel.viewState().first()
        )
    }

    @Test
    fun `test loadPreview()`() = runTest {
        coEvery { bibleReadingManager.currentTranslation() } returns flowOf(MockContents.kjvShortName)
        coEvery {
            bibleReadingManager.readVerses(MockContents.kjvShortName, 0, 0)
        } returns listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[1], MockContents.kjvVerses[2])
        coEvery { bibleReadingManager.readBookShortNames(MockContents.kjvShortName) } returns MockContents.kjvBookShortNames
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT)

        annotatedVersesViewModel.markErrorAsShown(AnnotatedVersesViewModel.ViewState.Error.AnnotatedVersesLoadingError)
        annotatedVersesViewModel.loadPreview(VerseIndex(0, 0, 1))

        val actual = annotatedVersesViewModel.viewState().first()
        assertNull(actual.settings)
        assertFalse(actual.loading)
        assertTrue(actual.items.isEmpty())
        assertEquals(Settings.DEFAULT, actual.preview?.settings)
        assertEquals("Gen., 1", actual.preview?.title)
        assertEquals(3, actual.preview?.items?.size)
        assertEquals(1, actual.preview?.currentPosition)
        assertNull(actual.error)

        annotatedVersesViewModel.markPreviewAsClosed()
        assertEquals(
            AnnotatedVersesViewModel.ViewState(
                settings = null,
                loading = false,
                sortOrder = Constants.DEFAULT_SORT_ORDER,
                items = emptyList(),
                preview = null,
                error = null
            ),
            annotatedVersesViewModel.viewState().first()
        )
    }
}

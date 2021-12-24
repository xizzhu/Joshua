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

package me.xizzhu.android.joshua.annotated

import android.app.Application
import android.content.res.Resources
import android.text.format.DateUtils
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.annotated.bookmarks.BookmarkItem
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.VerseAnnotation
import me.xizzhu.android.joshua.core.VerseAnnotationManager
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.BaseViewModel
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.TextItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import me.xizzhu.android.joshua.utils.currentTimeMillis
import java.util.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

        resources = mockk()
        every { resources.getStringArray(R.array.text_months) } returns arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12")

        application = mockk()
        every { application.getString(R.string.text_no_bookmarks) } returns "NO ANNOTATED VERSES"
        every { application.resources } returns resources

        annotatedVersesViewModel = TestAnnotatedVersesViewModel(bibleReadingManager, verseAnnotationManager, settingsManager, application)
    }

    @Test
    fun `test loadAnnotatedVerses() from constructor`() = runTest {
        every { bibleReadingManager.currentTranslation() } returns flowOf(MockContents.kjvShortName)
        coEvery { bibleReadingManager.readBookNames(MockContents.kjvShortName) } returns MockContents.kjvBookNames
        coEvery { bibleReadingManager.readBookShortNames(MockContents.kjvShortName) } returns MockContents.kjvBookShortNames
        coEvery { bibleReadingManager.readVerses(MockContents.kjvShortName, emptyList()) } returns emptyMap()
        every { verseAnnotationManager.sortOrder() } returns flowOf(Constants.SORT_BY_BOOK)
        coEvery { verseAnnotationManager.read(Constants.SORT_BY_BOOK) } returns emptyList()

        val job = async { annotatedVersesViewModel.annotatedVerses().first { it is BaseViewModel.ViewData.Success } }
        annotatedVersesViewModel = TestAnnotatedVersesViewModel(bibleReadingManager, verseAnnotationManager, settingsManager, application)

        val actual = job.await()
        assertTrue(actual is BaseViewModel.ViewData.Success)
        assertEquals(1, actual.data.items.size)
        assertEquals("NO ANNOTATED VERSES", (actual.data.items[0] as TextItem).title.toString())
    }

    @Test
    fun `test loadAnnotatedVerses() with empty result`() = runTest {
        every { bibleReadingManager.currentTranslation() } returns flowOf(MockContents.kjvShortName)
        coEvery { bibleReadingManager.readBookNames(MockContents.kjvShortName) } returns MockContents.kjvBookNames
        coEvery { bibleReadingManager.readBookShortNames(MockContents.kjvShortName) } returns MockContents.kjvBookShortNames
        coEvery { bibleReadingManager.readVerses(MockContents.kjvShortName, emptyList()) } returns emptyMap()
        every { verseAnnotationManager.sortOrder() } returns flowOf(Constants.SORT_BY_BOOK)
        coEvery { verseAnnotationManager.read(Constants.SORT_BY_BOOK) } returns emptyList()

        val job = async { annotatedVersesViewModel.annotatedVerses().first { it is BaseViewModel.ViewData.Success } }
        annotatedVersesViewModel.loadAnnotatedVerses()

        val actual = job.await()
        assertTrue(actual is BaseViewModel.ViewData.Success)
        assertEquals(1, actual.data.items.size)
        assertEquals("NO ANNOTATED VERSES", (actual.data.items[0] as TextItem).title.toString())
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

        val job = async { annotatedVersesViewModel.annotatedVerses().first { it is BaseViewModel.ViewData.Success } }
        annotatedVersesViewModel.loadAnnotatedVerses()

        val actual = job.await()
        assertTrue(actual is BaseViewModel.ViewData.Success)
        assertEquals(6, actual.data.items.size)
        assertEquals("Genesis", (actual.data.items[0] as TitleItem).title.toString())
        assertEquals(VerseIndex(0, 0, 0), (actual.data.items[1] as BookmarkItem).verseIndex)
        assertEquals(VerseIndex(0, 0, 1), (actual.data.items[2] as BookmarkItem).verseIndex)
        assertEquals(VerseIndex(0, 9, 9), (actual.data.items[3] as BookmarkItem).verseIndex)
        assertEquals("Exodus", (actual.data.items[4] as TitleItem).title.toString())
        assertEquals(VerseIndex(1, 22, 18), (actual.data.items[5] as BookmarkItem).verseIndex)
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
        every { verseAnnotationManager.sortOrder() } returns flowOf(Constants.SORT_BY_DATE)
        coEvery { verseAnnotationManager.read(Constants.SORT_BY_DATE) } returns listOf(
                TestAnnotatedVerse(VerseIndex(0, 0, 0), 4L + DateUtils.DAY_IN_MILLIS * 360),
                TestAnnotatedVerse(VerseIndex(0, 0, 1), 3L),
                TestAnnotatedVerse(VerseIndex(0, 9, 9), 2L),
                TestAnnotatedVerse(VerseIndex(1, 22, 18), 1L)
        )

        val job = async { annotatedVersesViewModel.annotatedVerses().first { it is BaseViewModel.ViewData.Success } }
        annotatedVersesViewModel.loadAnnotatedVerses()

        val actual = job.await()
        assertTrue(actual is BaseViewModel.ViewData.Success)
        assertEquals(6, actual.data.items.size)
        assertEquals("31104000004", (actual.data.items[0] as TitleItem).title.toString())
        assertEquals(VerseIndex(0, 0, 0), (actual.data.items[1] as BookmarkItem).verseIndex)
        assertEquals("3", (actual.data.items[2] as TitleItem).title.toString())
        assertEquals(VerseIndex(0, 0, 1), (actual.data.items[3] as BookmarkItem).verseIndex)
        assertEquals(VerseIndex(0, 9, 9), (actual.data.items[4] as BookmarkItem).verseIndex)
        assertEquals(VerseIndex(1, 22, 18), (actual.data.items[5] as BookmarkItem).verseIndex)
    }
}

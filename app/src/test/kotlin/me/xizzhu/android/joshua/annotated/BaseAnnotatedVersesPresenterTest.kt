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

package me.xizzhu.android.joshua.annotated

import android.content.res.Resources
import android.view.View
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.annotated.bookmarks.list.BookmarkItem
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.infra.arch.toViewData
import me.xizzhu.android.joshua.infra.arch.viewData
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.CommonRecyclerView
import me.xizzhu.android.joshua.ui.recyclerview.TextItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BaseAnnotatedVersesPresenterTest : BaseUnitTest() {
    private data class TestVerseAnnotation(override val verseIndex: VerseIndex, override val timestamp: Long) : VerseAnnotation(verseIndex, timestamp)

    private class TestAnnotatedVersePresenter(activity: BaseAnnotatedVersesActivity<TestVerseAnnotation>,
                                              navigator: Navigator,
                                              interactor: AnnotatedVersesInteractor<TestVerseAnnotation>,
                                              dispatcher: CoroutineDispatcher)
        : BaseAnnotatedVersesPresenter<TestVerseAnnotation, AnnotatedVersesInteractor<TestVerseAnnotation>>(activity, navigator, R.string.text_no_bookmark, interactor, dispatcher) {
        override fun TestVerseAnnotation.toBaseItem(bookName: String, bookShortName: String, verseText: String, sortOrder: Int): BaseItem =
                BookmarkItem(verseIndex, bookName, bookShortName, verseText, sortOrder, ::openVerse)
    }

    @Mock
    private lateinit var resources: Resources
    @Mock
    private lateinit var activity: BaseAnnotatedVersesActivity<TestVerseAnnotation>
    @Mock
    private lateinit var navigator: Navigator
    @Mock
    private lateinit var interactor: AnnotatedVersesInteractor<TestVerseAnnotation>
    @Mock
    private lateinit var annotatedVerseListView: CommonRecyclerView

    private lateinit var annotatedVersesViewHolder: AnnotatedVersesViewHolder
    private lateinit var baseAnnotatedVersesPresenter: TestAnnotatedVersePresenter

    @BeforeTest
    override fun setup() {
        super.setup()

        `when`(resources.getString(anyInt(), anyString(), anyInt())).thenReturn("")
        `when`(resources.getString(anyInt(), anyString(), anyInt(), anyInt())).thenReturn("")
        `when`(resources.getStringArray(anyInt())).thenReturn(Array(12) { "" })
        `when`(activity.resources).thenReturn(resources)

        `when`(interactor.settings()).thenReturn(emptyFlow())
        `when`(interactor.sortOrder()).thenReturn(emptyFlow())
        `when`(interactor.currentTranslation()).thenReturn(emptyFlow())

        annotatedVersesViewHolder = AnnotatedVersesViewHolder(annotatedVerseListView)
        baseAnnotatedVersesPresenter = TestAnnotatedVersePresenter(activity, navigator, interactor, testDispatcher)
        baseAnnotatedVersesPresenter.create(annotatedVersesViewHolder)
    }

    @AfterTest
    override fun tearDown() {
        baseAnnotatedVersesPresenter.destroy()
        super.tearDown()
    }

    @Test
    fun testObserveSettings() = testDispatcher.runBlockingTest {
        val settings = Settings(false, true, 1, true, true)
        `when`(interactor.settings()).thenReturn(flowOf(ViewData.loading(), ViewData.success(settings), ViewData.error()))

        baseAnnotatedVersesPresenter.create(annotatedVersesViewHolder)
        verify(annotatedVerseListView, times(1)).setSettings(settings)
        verify(annotatedVerseListView, never()).setSettings(Settings.DEFAULT)

        baseAnnotatedVersesPresenter.destroy()
    }

    @Test
    fun testLoadWithEmptyVerseAnnotation() = testDispatcher.runBlockingTest {
        val title = "random title"
        `when`(activity.getString(anyInt())).thenReturn(title)
        `when`(interactor.sortOrder()).thenReturn(flowOf(Constants.SORT_BY_DATE).toViewData())
        `when`(interactor.currentTranslation()).thenReturn(flowOf(MockContents.kjvShortName).toViewData())
        `when`(interactor.verseAnnotations(anyInt())).thenReturn(viewData { emptyList<TestVerseAnnotation>() })

        baseAnnotatedVersesPresenter.create(annotatedVersesViewHolder)

        with(inOrder(interactor, annotatedVerseListView)) {
            verify(interactor, times(1)).updateLoadingState(ViewData.loading())
            verify(annotatedVerseListView, times(1)).visibility = View.GONE
            verify(annotatedVerseListView, times(1)).setItems(listOf(TextItem(title)))
            verify(annotatedVerseListView, times(1)).fadeIn()
            verify(interactor, times(1)).updateLoadingState(ViewData.success(null))
        }

        baseAnnotatedVersesPresenter.destroy()
    }

    @Test
    fun testLoadWithException() = testDispatcher.runBlockingTest {
        val exception = RuntimeException("random exception")
        `when`(interactor.verseAnnotations(anyInt())).thenThrow(exception)
        `when`(interactor.sortOrder()).thenReturn(flowOf(Constants.SORT_BY_DATE).toViewData())
        `when`(interactor.currentTranslation()).thenReturn(flowOf(MockContents.kjvShortName).toViewData())

        baseAnnotatedVersesPresenter.create(annotatedVersesViewHolder)

        with(inOrder(interactor, annotatedVerseListView)) {
            verify(interactor, times(1)).updateLoadingState(ViewData.loading())
            verify(annotatedVerseListView, times(1)).visibility = View.GONE
            verify(interactor, times(1)).updateLoadingState(ViewData.error(exception = exception))
        }
        verify(annotatedVerseListView, never()).setItems(any())
        verify(annotatedVerseListView, never()).fadeIn()
        verify(interactor, never()).updateLoadingState(ViewData.success(null))

        baseAnnotatedVersesPresenter.destroy()
    }

    @Test
    fun testToBaseItemsByDate() {
        val expected = listOf(
                TitleItem("", false),
                BookmarkItem(VerseIndex(0, 0, 4), MockContents.kjvBookNames[0], MockContents.kjvBookShortNames[0], MockContents.kjvVerses[4].text.text, Constants.SORT_BY_DATE, baseAnnotatedVersesPresenter::openVerse),
                TitleItem("", false),
                BookmarkItem(VerseIndex(0, 0, 1), MockContents.kjvBookNames[0], MockContents.kjvBookShortNames[0], MockContents.kjvVerses[1].text.text, Constants.SORT_BY_DATE, baseAnnotatedVersesPresenter::openVerse),
                BookmarkItem(VerseIndex(0, 0, 3), MockContents.kjvBookNames[0], MockContents.kjvBookShortNames[0], MockContents.kjvVerses[3].text.text, Constants.SORT_BY_DATE, baseAnnotatedVersesPresenter::openVerse),
                TitleItem("", false),
                BookmarkItem(VerseIndex(0, 0, 2), MockContents.kjvBookNames[0], MockContents.kjvBookShortNames[0], MockContents.kjvVerses[2].text.text, Constants.SORT_BY_DATE, baseAnnotatedVersesPresenter::openVerse)
        )
        val actual = with(baseAnnotatedVersesPresenter) {
            listOf(
                    TestVerseAnnotation(VerseIndex(0, 0, 4), 2L * 365L * 24L * 3600L * 1000L),
                    TestVerseAnnotation(VerseIndex(0, 0, 1), 36L * 3600L * 1000L),
                    TestVerseAnnotation(VerseIndex(0, 0, 3), 36L * 3600L * 1000L - 1000L),
                    TestVerseAnnotation(VerseIndex(0, 0, 2), 0L)
            ).toBaseItemsByDate(MockContents.kjvBookNames, MockContents.kjvBookShortNames, mapOf(
                    Pair(VerseIndex(0, 0, 4), MockContents.kjvVerses[4]),
                    Pair(VerseIndex(0, 0, 1), MockContents.kjvVerses[1]),
                    Pair(VerseIndex(0, 0, 3), MockContents.kjvVerses[3]),
                    Pair(VerseIndex(0, 0, 2), MockContents.kjvVerses[2])
            ))
        }
        assertEquals(expected, actual)
    }

    @Test
    fun testToBaseItemsByBook() {
        val expected = listOf(
                TitleItem(MockContents.kjvBookNames[0], false),
                BookmarkItem(VerseIndex(0, 0, 2), MockContents.kjvBookNames[0], MockContents.kjvBookShortNames[0], MockContents.kjvVerses[2].text.text, Constants.SORT_BY_BOOK, baseAnnotatedVersesPresenter::openVerse),
                BookmarkItem(VerseIndex(0, 0, 4), MockContents.kjvBookNames[0], MockContents.kjvBookShortNames[0], MockContents.kjvVerses[4].text.text, Constants.SORT_BY_BOOK, baseAnnotatedVersesPresenter::openVerse)
        )
        val actual = with(baseAnnotatedVersesPresenter) {
            listOf(
                    TestVerseAnnotation(VerseIndex(0, 0, 2), 0L),
                    TestVerseAnnotation(VerseIndex(0, 0, 4), 2L * 365L * 24L * 3600L * 1000L)
            ).toBaseItemsByBook(MockContents.kjvBookNames, MockContents.kjvBookShortNames, mapOf(
                    Pair(VerseIndex(0, 0, 2), MockContents.kjvVerses[2]),
                    Pair(VerseIndex(0, 0, 4), MockContents.kjvVerses[4])
            ))
        }
        assertEquals(expected, actual)
    }
}

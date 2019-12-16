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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.annotated.bookmarks.list.BookmarkItem
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.infra.arch.viewData
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
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

        annotatedVersesViewHolder = AnnotatedVersesViewHolder(annotatedVerseListView)
        baseAnnotatedVersesPresenter = TestAnnotatedVersePresenter(activity, navigator, interactor, testDispatcher)
        baseAnnotatedVersesPresenter.bind(annotatedVersesViewHolder)
    }

    @AfterTest
    override fun tearDown() {
        baseAnnotatedVersesPresenter.unbind()
        super.tearDown()
    }

    @Test
    fun testObserveSettings() = testDispatcher.runBlockingTest {
        val settings = Settings(false, true, 1, true, true)
        `when`(interactor.settings()).thenReturn(flowOf(ViewData.loading(), ViewData.success(settings), ViewData.error()))

        baseAnnotatedVersesPresenter.start()
        verify(annotatedVerseListView, times(1)).setSettings(settings)
        verify(annotatedVerseListView, never()).setSettings(Settings.DEFAULT)

        baseAnnotatedVersesPresenter.stop()
    }

    @Test
    fun testPrepareItems() = testDispatcher.runBlockingTest {
        val title = "random title"
        `when`(interactor.verseAnnotations(anyInt())).thenReturn(viewData { emptyList<TestVerseAnnotation>() })
        `when`(activity.getString(anyInt())).thenReturn(title)

        assertEquals(listOf(TextItem(title)), baseAnnotatedVersesPresenter.prepareItems(Constants.SORT_BY_DATE))
        assertEquals(listOf(TextItem(title)), baseAnnotatedVersesPresenter.prepareItems(Constants.SORT_BY_BOOK))
    }

    @Test
    fun testToBaseItemsByDate() = testDispatcher.runBlockingTest {
        val bookNames = MockContents.kjvBookNames
        val bookShortNames = MockContents.kjvBookShortNames
        `when`(interactor.bookNames()).thenReturn(viewData { bookNames })
        `when`(interactor.bookShortNames()).thenReturn(viewData { bookShortNames })
        `when`(interactor.verse(any())).then { invocation ->
            return@then viewData { MockContents.kjvVerses[(invocation.arguments[0] as VerseIndex).verseIndex] }
        }

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
            ).toBaseItemsByDate()
        }
        assertEquals(expected, actual)
    }

    @Test
    fun testToBaseItemsByBook() = testDispatcher.runBlockingTest {
        val bookNames = MockContents.kjvBookNames
        val bookShortNames = MockContents.kjvBookShortNames
        `when`(interactor.bookNames()).thenReturn(viewData { bookNames })
        `when`(interactor.bookShortNames()).thenReturn(viewData { bookShortNames })
        `when`(interactor.verse(any())).then { invocation ->
            return@then viewData { MockContents.kjvVerses[(invocation.arguments[0] as VerseIndex).verseIndex] }
        }

        val expected = listOf(
                TitleItem(MockContents.kjvBookNames[0], false),
                BookmarkItem(VerseIndex(0, 0, 2), MockContents.kjvBookNames[0], MockContents.kjvBookShortNames[0], MockContents.kjvVerses[2].text.text, Constants.SORT_BY_BOOK, baseAnnotatedVersesPresenter::openVerse),
                BookmarkItem(VerseIndex(0, 0, 4), MockContents.kjvBookNames[0], MockContents.kjvBookShortNames[0], MockContents.kjvVerses[4].text.text, Constants.SORT_BY_BOOK, baseAnnotatedVersesPresenter::openVerse)
        )
        val actual = with(baseAnnotatedVersesPresenter) {
            listOf(
                    TestVerseAnnotation(VerseIndex(0, 0, 2), 0L),
                    TestVerseAnnotation(VerseIndex(0, 0, 4), 2L * 365L * 24L * 3600L * 1000L)
            ).toBaseItemsByBook()
        }
        assertEquals(expected, actual)
    }
}

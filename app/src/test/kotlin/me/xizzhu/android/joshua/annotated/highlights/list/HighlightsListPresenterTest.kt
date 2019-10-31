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

package me.xizzhu.android.joshua.annotated.highlights.list

import android.content.res.Resources
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.annotated.highlights.HighlightsActivity
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class HighlightsListPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var resources: Resources
    @Mock
    private lateinit var highlightsActivity: HighlightsActivity
    @Mock
    private lateinit var navigator: Navigator
    @Mock
    private lateinit var highlightsListInteractor: HighlightsListInteractor

    private lateinit var highlightsListPresenter: HighlightsListPresenter

    @BeforeTest
    override fun setup() {
        super.setup()

        `when`(resources.getString(anyInt(), anyString(), anyInt())).thenReturn("")
        `when`(resources.getString(anyInt(), anyString(), anyInt(), anyInt())).thenReturn("")
        `when`(resources.getStringArray(anyInt())).thenReturn(Array(12) { "" })
        `when`(highlightsActivity.resources).thenReturn(resources)

        highlightsListPresenter = HighlightsListPresenter(highlightsActivity, navigator, highlightsListInteractor, testDispatcher)
    }

    @Test
    fun testToBaseItemsByDate() = testDispatcher.runBlockingTest {
        val bookNames = MockContents.kjvBookNames
        val bookShortNames = MockContents.kjvBookShortNames
        `when`(highlightsListInteractor.bookNames()).thenReturn(bookNames)
        `when`(highlightsListInteractor.bookShortNames()).thenReturn(bookShortNames)
        `when`(highlightsListInteractor.verse(any())).then { invocation ->
            return@then MockContents.kjvVerses[(invocation.arguments[0] as VerseIndex).verseIndex]
        }

        val expected = listOf(
                TitleItem("", false),
                HighlightItem(VerseIndex(0, 0, 4), MockContents.kjvBookNames[0], MockContents.kjvBookShortNames[0], MockContents.kjvVerses[4].text.text, Highlight.COLOR_PURPLE, Constants.SORT_BY_DATE, highlightsListPresenter::openVerse),
                TitleItem("", false),
                HighlightItem(VerseIndex(0, 0, 1), MockContents.kjvBookNames[0], MockContents.kjvBookShortNames[0], MockContents.kjvVerses[1].text.text, Highlight.COLOR_BLUE, Constants.SORT_BY_DATE, highlightsListPresenter::openVerse),
                HighlightItem(VerseIndex(0, 0, 3), MockContents.kjvBookNames[0], MockContents.kjvBookShortNames[0], MockContents.kjvVerses[3].text.text, Highlight.COLOR_YELLOW, Constants.SORT_BY_DATE, highlightsListPresenter::openVerse),
                TitleItem("", false),
                HighlightItem(VerseIndex(0, 0, 2), MockContents.kjvBookNames[0], MockContents.kjvBookShortNames[0], MockContents.kjvVerses[2].text.text, Highlight.COLOR_PINK, Constants.SORT_BY_DATE, highlightsListPresenter::openVerse)
        )
        val actual = with(highlightsListPresenter) {
            listOf(
                    Highlight(VerseIndex(0, 0, 4), Highlight.COLOR_PURPLE, 2L * 365L * 24L * 3600L * 1000L),
                    Highlight(VerseIndex(0, 0, 1), Highlight.COLOR_BLUE, 36L * 3600L * 1000L),
                    Highlight(VerseIndex(0, 0, 3), Highlight.COLOR_YELLOW, 36L * 3600L * 1000L - 1000L),
                    Highlight(VerseIndex(0, 0, 2), Highlight.COLOR_PINK, 0L)
            ).toBaseItemsByDate()
        }
        assertEquals(expected, actual)
    }

    @Test
    fun testToBaseItemsByBook() = testDispatcher.runBlockingTest {
        val bookNames = MockContents.kjvBookNames
        val bookShortNames = MockContents.kjvBookShortNames
        `when`(highlightsListInteractor.bookNames()).thenReturn(bookNames)
        `when`(highlightsListInteractor.bookShortNames()).thenReturn(bookShortNames)
        `when`(highlightsListInteractor.verse(any())).then { invocation ->
            return@then MockContents.kjvVerses[(invocation.arguments[0] as VerseIndex).verseIndex]
        }

        val expected = listOf(
                TitleItem(MockContents.kjvBookNames[0], false),
                HighlightItem(VerseIndex(0, 0, 2), MockContents.kjvBookNames[0], MockContents.kjvBookShortNames[0], MockContents.kjvVerses[2].text.text, Highlight.COLOR_PINK, Constants.SORT_BY_BOOK, highlightsListPresenter::openVerse),
                HighlightItem(VerseIndex(0, 0, 4), MockContents.kjvBookNames[0], MockContents.kjvBookShortNames[0], MockContents.kjvVerses[4].text.text, Highlight.COLOR_PURPLE, Constants.SORT_BY_BOOK, highlightsListPresenter::openVerse)
        )
        val actual = with(highlightsListPresenter) {
            listOf(
                    Highlight(VerseIndex(0, 0, 2), Highlight.COLOR_PINK, 0L),
                    Highlight(VerseIndex(0, 0, 4), Highlight.COLOR_PURPLE, 2L * 365L * 24L * 3600L * 1000L)
            ).toBaseItemsByBook()
        }
        assertEquals(expected, actual)
    }
}

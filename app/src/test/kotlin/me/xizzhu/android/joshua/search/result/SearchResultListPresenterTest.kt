/*
 * Copyright (C) 2020 Xizhi Zhu
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

package me.xizzhu.android.joshua.search.result

import android.view.View
import android.widget.ProgressBar
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.search.SearchActivity
import me.xizzhu.android.joshua.search.SearchRequest
import me.xizzhu.android.joshua.search.SearchResult
import me.xizzhu.android.joshua.search.SearchViewModel
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.recyclerview.CommonRecyclerView
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SearchResultListPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var lifecycle: Lifecycle

    @Mock
    private lateinit var navigator: Navigator

    @Mock
    private lateinit var searchViewModel: SearchViewModel

    @Mock
    private lateinit var searchActivity: SearchActivity

    @Mock
    private lateinit var loadingSpinner: ProgressBar

    @Mock
    private lateinit var searchResultListView: CommonRecyclerView

    private lateinit var searchResultViewHolder: SearchResultViewHolder
    private lateinit var searchResultListPresenter: SearchResultListPresenter

    @BeforeTest
    override fun setup() {
        super.setup()

        `when`(searchActivity.lifecycle).thenReturn(lifecycle)
        `when`(searchActivity.getString(R.string.title_bookmarks)).thenReturn("Bookmarks")
        `when`(searchActivity.getString(R.string.title_highlights)).thenReturn("Highlights")
        `when`(searchActivity.getString(R.string.title_notes)).thenReturn("Notes")
        `when`(searchViewModel.settings()).thenReturn(emptyFlow())
        `when`(searchViewModel.searchRequest).thenReturn(emptyFlow())

        searchResultViewHolder = SearchResultViewHolder(loadingSpinner, searchResultListView)
        searchResultListPresenter = SearchResultListPresenter(navigator, searchViewModel, searchActivity, testCoroutineScope)
        searchResultListPresenter.bind(searchResultViewHolder)
    }

    @Test
    fun testObserveSettings() = runBlocking {
        val settings = Settings.DEFAULT.copy(keepScreenOn = false)
        `when`(searchViewModel.settings()).thenReturn(flowOf(settings))

        searchResultListPresenter.onCreate()
        verify(searchResultListView, times(1)).setSettings(settings)
    }

    @Test
    fun testObserveSearchRequest() = runBlocking {
        val query = "query"
        `when`(searchViewModel.searchRequest).thenReturn(flowOf(SearchRequest(query, false, true, false, true)))
        `when`(searchViewModel.search(query, true, false, true))
                .thenReturn(flowOf(SearchResult(query, emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList())))

        searchResultListPresenter.onCreate()

        with(inOrder(loadingSpinner, searchResultListView)) {
            // loading
            verify(loadingSpinner, times(1)).fadeIn()
            verify(searchResultListView, times(1)).visibility = View.GONE

            // success
            verify(searchResultListView, times(1)).setItems(any())
            verify(searchResultListView, times(1)).scrollToPosition(0)
            verify(loadingSpinner, times(1)).visibility = View.GONE
            verify(searchResultListView, times(1)).fadeIn()
        }
    }

    @Test
    fun testObserveSearchRequestWithInstantSearch() = runBlocking {
        val query = "query"
        `when`(searchViewModel.searchRequest).thenReturn(flowOf(SearchRequest(query, true, false, false, true)))
        `when`(searchViewModel.search(query, false, false, true))
                .thenReturn(flowOf(SearchResult(query, emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList())))

        searchResultListPresenter.onCreate()

        with(inOrder(loadingSpinner, searchResultListView)) {
            // success
            verify(searchResultListView, times(1)).setItems(any())
            verify(searchResultListView, times(1)).scrollToPosition(0)
            verify(loadingSpinner, times(1)).visibility = View.GONE
            verify(searchResultListView, times(1)).visibility = View.VISIBLE
        }
    }

    @Test
    fun testObserveSearchRequestWithException() = runBlocking {
        val query = "query"
        `when`(searchViewModel.searchRequest).thenReturn(flowOf(SearchRequest(query, false, true, true, true)))
        `when`(searchViewModel.search(query, true, true, true)).thenReturn(flow { throw RuntimeException() })

        searchResultListPresenter.onCreate()

        with(inOrder(loadingSpinner, searchResultListView)) {
            // loading
            verify(loadingSpinner, times(1)).fadeIn()
            verify(searchResultListView, times(1)).visibility = View.GONE

            // error
            verify(loadingSpinner, times(1)).visibility = View.GONE
            verify(searchResultListView, times(1)).visibility = View.GONE
        }
    }

    @Test
    fun testToItems() {
        val query = "query"
        val expected = listOf(
                TitleItem("Notes", false),
                SearchNoteItem(VerseIndex(0, 0, 3), MockContents.kjvBookShortNames[0], MockContents.kjvVerses[3].text.text, "note", query, searchResultListPresenter::selectVerse),
                TitleItem("Bookmarks", false),
                SearchVerseItem(
                        VerseIndex(0, 0, 1), MockContents.kjvBookShortNames[0],
                        MockContents.kjvVerses[1].text.text, query, Highlight.COLOR_NONE, searchResultListPresenter::selectVerse
                ),
                TitleItem("Highlights", false),
                SearchVerseItem(
                        VerseIndex(0, 0, 2), MockContents.kjvBookShortNames[0],
                        MockContents.kjvVerses[2].text.text, query, Highlight.COLOR_BLUE, searchResultListPresenter::selectVerse
                ),
                TitleItem(MockContents.kjvBookNames[0], false),
                SearchVerseItem(
                        VerseIndex(0, 0, 0), MockContents.kjvBookShortNames[0],
                        MockContents.kjvVerses[0].text.text, query, Highlight.COLOR_NONE, searchResultListPresenter::selectVerse
                )
        )

        val searchResult = SearchResult(
                query, listOf(MockContents.kjvVerses[0]),
                listOf(Pair(Bookmark(VerseIndex(0, 0, 1), 0L), MockContents.kjvVerses[1])),
                listOf(Pair(Highlight(VerseIndex(0, 0, 2), Highlight.COLOR_BLUE, 0L), MockContents.kjvVerses[2])),
                listOf(Pair(Note(VerseIndex(0, 0, 3), "note", 0L), MockContents.kjvVerses[3])),
                MockContents.kjvBookNames, MockContents.kjvBookShortNames
        )
        val actual = with(searchResultListPresenter) { searchResult.toItems() }

        assertEquals(expected, actual)
    }

    @Test
    fun testSelectVerse() = runBlocking {
        val verseIndex = VerseIndex(1, 2, 3)
        searchResultListPresenter.selectVerse(verseIndex)
        verify(searchViewModel, times(1)).saveCurrentVerseIndex(verseIndex)
        verify(navigator, times(1)).navigate(searchActivity, Navigator.SCREEN_READING)
    }
}

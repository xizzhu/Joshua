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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.search.SearchActivity
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

        searchResultViewHolder = SearchResultViewHolder(loadingSpinner, searchResultListView)
        searchResultListPresenter = SearchResultListPresenter(navigator, searchViewModel, searchActivity, testCoroutineScope)
        searchResultListPresenter.bind(searchResultViewHolder)
    }

    @Test
    fun testObserveSettings() = testDispatcher.runBlockingTest {
        val settings = Settings.DEFAULT.copy(keepScreenOn = false)
        `when`(searchViewModel.settings()).thenReturn(flowOf(ViewData.loading(), ViewData.error(), ViewData.success(settings)))

        searchResultListPresenter.observeSettings()
        verify(searchResultListView, times(1)).setSettings(settings)
    }

    @Test
    fun testObserveQuery() = testDispatcher.runBlockingTest {
        `when`(searchViewModel.searchResult()).thenReturn(flowOf(
                ViewData.loading(),
                ViewData.error(exception = RuntimeException()),
                ViewData.success(SearchResult("query", true, emptyList(), emptyList(), emptyList()))
        ))

        searchResultListPresenter.observeQuery()

        with(inOrder(loadingSpinner, searchResultListView)) {
            // loading
            verify(loadingSpinner, times(1)).fadeIn()
            verify(searchResultListView, times(1)).visibility = View.GONE

            // error
            verify(loadingSpinner, times(1)).visibility = View.GONE

            // success
            verify(searchResultListView, times(1)).setItems(any())
            verify(searchResultListView, times(1)).scrollToPosition(0)
            verify(loadingSpinner, times(1)).visibility = View.GONE
            verify(searchResultListView, times(1)).visibility = View.VISIBLE
        }
    }

    @Test
    fun testToItems() {
        val query = "query"
        val expected = listOf(
                TitleItem(MockContents.kjvBookNames[0], false),
                SearchItem(
                        VerseIndex(0, 0, 0), MockContents.kjvBookShortNames[0],
                        MockContents.kjvVerses[0].text.text, query, searchResultListPresenter::selectVerse
                )
        )

        val searchResult = SearchResult(
                query, true, listOf(MockContents.kjvVerses[0]),
                MockContents.kjvBookNames, MockContents.kjvBookShortNames
        )
        val actual = with(searchResultListPresenter) { searchResult.toItems() }

        assertEquals(expected, actual)
    }
}

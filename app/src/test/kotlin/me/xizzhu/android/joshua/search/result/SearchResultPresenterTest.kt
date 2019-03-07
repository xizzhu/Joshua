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

package me.xizzhu.android.joshua.search.result

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.search.SearchInteractor
import me.xizzhu.android.joshua.search.SearchResult
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class SearchResultPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var searchInteractor: SearchInteractor
    @Mock
    private lateinit var searchResultView: SearchResultView
    private lateinit var searchResultPresenter: SearchResultPresenter

    @Before
    override fun setUp() {
        super.setUp()
        MockitoAnnotations.initMocks(this)
        searchResultPresenter = SearchResultPresenter(searchInteractor)
    }

    @Test
    fun testSelectVerse() {
        runBlocking {
            val verseIndex = VerseIndex(1, 2, 3)
            searchResultPresenter.selectVerse(verseIndex)
            verify(searchInteractor, times(1)).selectVerse(verseIndex)
            verify(searchInteractor, times(1)).openReading()
        }
    }

    @Test
    fun testObserveDefaultSearchResultAndState() {
        runBlocking {
            val searchStateChannel: BroadcastChannel<Boolean> = ConflatedBroadcastChannel(false)
            `when`(searchInteractor.observeSearchState()).thenReturn(searchStateChannel.openSubscription())

            val searchResultChannel: BroadcastChannel<SearchResult> = ConflatedBroadcastChannel(SearchResult.INVALID)
            `when`(searchInteractor.observeSearchResult()).thenReturn(searchResultChannel.openSubscription())

            searchResultPresenter.attachView(searchResultView)
            verify(searchResultView, never()).onSearchResultUpdated(any())
            verify(searchResultView, times(1)).onSearchCompleted()

            searchResultPresenter.detachView()
        }
    }

    @Test
    fun testObserveSearchResultAndState() {
        runBlocking {
            val searchStateChannel: BroadcastChannel<Boolean> = ConflatedBroadcastChannel(false)
            `when`(searchInteractor.observeSearchState()).thenReturn(searchStateChannel.openSubscription())

            val searchResultChannel: BroadcastChannel<SearchResult> = ConflatedBroadcastChannel(SearchResult.INVALID)
            `when`(searchInteractor.observeSearchResult()).thenReturn(searchResultChannel.openSubscription())

            searchResultPresenter.attachView(searchResultView)

            searchStateChannel.send(true)
            val searchResult = SearchResult(MockContents.kjvShortName, emptyList())
            searchResultChannel.send(searchResult)
            searchStateChannel.send(false)

            verify(searchResultView, times(1)).onSearchResultUpdated(searchResult)
            verify(searchResultView, times(1)).onSearchStarted()

            // once from initial state, and second time when search finishes
            verify(searchResultView, times(2)).onSearchCompleted()

            searchResultPresenter.detachView()
        }
    }
}

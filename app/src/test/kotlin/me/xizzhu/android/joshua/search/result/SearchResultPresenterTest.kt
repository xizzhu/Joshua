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
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.search.SearchInteractor
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*

class SearchResultPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var searchInteractor: SearchInteractor
    @Mock
    private lateinit var searchResultView: SearchResultView
    private lateinit var searchResultPresenter: SearchResultPresenter
    private lateinit var searchStateChannel: BroadcastChannel<Boolean>
    private lateinit var searchResultChannel: BroadcastChannel<List<Verse>>

    @Before
    override fun setup() {
        super.setup()

        searchStateChannel = ConflatedBroadcastChannel(false)
        `when`(searchInteractor.observeSearchState()).thenReturn(searchStateChannel.openSubscription())

        searchResultChannel = ConflatedBroadcastChannel(emptyList())
        `when`(searchInteractor.observeSearchResult()).thenReturn(searchResultChannel.openSubscription())

        searchResultPresenter = SearchResultPresenter(searchInteractor)
        searchResultPresenter.attachView(searchResultView)
    }

    @After
    override fun tearDown() {
        searchResultPresenter.detachView()
        super.tearDown()
    }

    @Test
    fun testSelectVerse() {
        runBlocking {
            val verseToSelect = VerseIndex(1, 2, 3)
            searchResultPresenter.selectVerse(verseToSelect)
            verify(searchResultView, never()).onVerseSelectionFailed(verseToSelect)
        }
    }

    @Test
    fun testSelectVerseWithException() {
        runBlocking {
            val verseToSelect = VerseIndex(1, 2, 3)
            `when`(searchInteractor.selectVerse(verseToSelect)).thenThrow(RuntimeException("Random exception"))

            searchResultPresenter.selectVerse(verseToSelect)
            verify(searchResultView, times(1)).onVerseSelectionFailed(verseToSelect)
        }
    }

    @Test
    fun testObserveDefaultSearchResultAndState() {
        runBlocking {
            verify(searchResultView, times(1)).onSearchResultUpdated(emptyList())
            verify(searchResultView, times(1)).onSearchCompleted()
        }
    }

    @Test
    fun testObserveSearchResultAndState() {
        runBlocking {
            searchStateChannel.send(true)
            val searchResult: List<Verse> = listOf(MockContents.kjvVerses[0])
            searchResultChannel.send(searchResult)
            searchStateChannel.send(false)

            verify(searchResultView, times(1)).onSearchResultUpdated(searchResult.toSearchResult())
            verify(searchResultView, times(1)).onSearchStarted()

            // once from initial state, and second time when search finishes
            verify(searchResultView, times(2)).onSearchCompleted()
        }
    }
}

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
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.search.SearchInteractor
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.ui.LoadingSpinnerState
import me.xizzhu.android.joshua.ui.recyclerview.toSearchItems
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
    private lateinit var settingsChannel: BroadcastChannel<Settings>
    private lateinit var searchStateChannel: BroadcastChannel<LoadingSpinnerState>
    private lateinit var searchResultChannel: BroadcastChannel<Pair<String, List<Verse>>>

    @Before
    override fun setup() {
        super.setup()

        settingsChannel = ConflatedBroadcastChannel(Settings.DEFAULT)
        `when`(searchInteractor.observeSettings()).thenReturn(settingsChannel.openSubscription())

        searchStateChannel = ConflatedBroadcastChannel(LoadingSpinnerState.NOT_LOADING)
        `when`(searchInteractor.observeSearchState()).thenReturn(searchStateChannel.openSubscription())

        searchResultChannel = ConflatedBroadcastChannel(Pair("", emptyList()))
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
            searchStateChannel.send(LoadingSpinnerState.IS_LOADING)
            val query = "query"
            val verses: List<Verse> = listOf(MockContents.kjvVerses[0])
            searchResultChannel.send(Pair(query, verses))
            searchStateChannel.send(LoadingSpinnerState.NOT_LOADING)

            verify(searchResultView, times(1)).onSearchResultUpdated(verses.toSearchItems(query, searchResultPresenter::selectVerse))
            verify(searchResultView, times(1)).onSearchStarted()

            // once from initial state, and second time when search finishes
            verify(searchResultView, times(2)).onSearchCompleted()
        }
    }
}

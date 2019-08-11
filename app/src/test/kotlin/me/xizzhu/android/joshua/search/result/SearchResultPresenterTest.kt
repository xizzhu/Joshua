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
    private lateinit var searchQueryChannel: BroadcastChannel<String>

    @Before
    override fun setup() {
        super.setup()

        runBlocking {
            settingsChannel = ConflatedBroadcastChannel(Settings.DEFAULT)
            `when`(searchInteractor.observeSettings()).thenReturn(settingsChannel.openSubscription())

            searchQueryChannel = ConflatedBroadcastChannel("")
            `when`(searchInteractor.observeSearchQuery()).thenReturn(searchQueryChannel.openSubscription())

            `when`(searchInteractor.readCurrentTranslation()).thenReturn(MockContents.kjvShortName)
            `when`(searchInteractor.readBookNames(MockContents.kjvShortName)).thenReturn(MockContents.kjvBookNames)
            `when`(searchInteractor.readBookShortNames(MockContents.kjvShortName)).thenReturn(MockContents.kjvBookShortNames)

            searchResultPresenter = SearchResultPresenter(searchInteractor)
            searchResultPresenter.attachView(searchResultView)
        }
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
    fun testSearch() {
        runBlocking {
            val query = "query"
            val verses: List<Verse> = listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[1], MockContents.kjvVerses[2])
            `when`(searchInteractor.search(query)).thenReturn(verses)

            searchResultPresenter.search(query)

            with(inOrder(searchInteractor, searchResultView)) {
                verify(searchInteractor, times(1)).notifyLoadingStarted()
                verify(searchResultView, times(1)).onSearchStarted()
                verify(searchResultView, times(1)).onSearchResultUpdated(
                        verses.toSearchResult(query, MockContents.kjvBookNames,
                                MockContents.kjvBookShortNames, searchResultPresenter::selectVerse)
                )
                verify(searchResultView, times(1)).onSearchCompleted()
                verify(searchInteractor, times(1)).notifyLoadingFinished()
            }
            verify(searchResultView, never()).onSearchFailed(anyString())
        }
    }

    @Test
    fun testSearchWithException() {
        runBlocking {
            val query = "query"
            `when`(searchInteractor.search(query)).thenThrow(RuntimeException("Random exception"))

            searchResultPresenter.search(query)

            with(inOrder(searchInteractor, searchResultView)) {
                verify(searchInteractor, times(1)).notifyLoadingStarted()
                verify(searchResultView, times(1)).onSearchStarted()
                verify(searchResultView, times(1)).onSearchFailed(query)
                verify(searchInteractor, times(1)).notifyLoadingFinished()
            }
            verify(searchResultView, never()).onSearchResultUpdated(any())
            verify(searchResultView, never()).onSearchCompleted()
        }
    }
}

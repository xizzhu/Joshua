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

package me.xizzhu.android.joshua.search

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.first
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.ui.LoadingSpinnerState
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchInteractorTest : BaseUnitTest() {
    @Mock
    private lateinit var searchActivity: SearchActivity
    @Mock
    private lateinit var navigator: Navigator
    @Mock
    private lateinit var bibleReadingManager: BibleReadingManager
    @Mock
    private lateinit var settingsManager: SettingsManager
    private lateinit var searchInteractor: SearchInteractor

    @Before
    override fun setup() {
        super.setup()
        searchInteractor = SearchInteractor(searchActivity, navigator, bibleReadingManager, settingsManager)
    }

    @Test
    fun testDefaultSearchState() {
        runBlocking {
            assertEquals(LoadingSpinnerState.NOT_LOADING, searchInteractor.observeSearchState().first())
        }
    }

    @Test
    fun testDefaultSearchResult() {
        runBlocking {
            val (query, verses) = searchInteractor.observeSearchResult().first()
            assertTrue(query.isEmpty())
            assertTrue(verses.isEmpty())
        }
    }

    @Test
    fun testSelectVerse() {
        runBlocking {
            val verseIndex = VerseIndex(1, 2, 3)
            searchInteractor.selectVerse(verseIndex)
            verify(bibleReadingManager, times(1)).saveCurrentVerseIndex(verseIndex)
        }
    }

    @Test
    fun testOpenReading() {
        searchInteractor.openReading()
        verify(navigator, times(1)).navigate(searchActivity, Navigator.SCREEN_READING)
    }

    @Test
    fun testSearch() {
        runBlocking {
            val channel = Channel<String>(Channel.UNLIMITED)
            channel.send(MockContents.kjvShortName)
            `when`(bibleReadingManager.observeCurrentTranslation()).thenReturn(channel)

            `when`(bibleReadingManager.readBookNames(MockContents.kjvShortName)).thenReturn(MockContents.kjvBookNames)

            val query = "query"
            `when`(bibleReadingManager.search(MockContents.kjvShortName, query)).thenReturn(MockContents.kjvVerses)

            searchInteractor.search(query)
            assertEquals(LoadingSpinnerState.NOT_LOADING, searchInteractor.observeSearchState().first())

            val (q, v) = searchInteractor.observeSearchResult().first()
            assertEquals(query, q)
            assertEquals(MockContents.kjvVerses.size, v.size)
            for (i in 0 until MockContents.kjvVerses.size) {
                assertEquals(MockContents.kjvVerses[i], v[i])
            }
        }
    }
}

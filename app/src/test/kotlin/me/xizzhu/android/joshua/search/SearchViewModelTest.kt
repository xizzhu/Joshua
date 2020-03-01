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

package me.xizzhu.android.joshua.search

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.mockito.Mock
import org.mockito.Mockito.`when`
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SearchViewModelTest : BaseUnitTest() {
    @Mock
    private lateinit var bibleReadingManager: BibleReadingManager
    @Mock
    private lateinit var settingsManager: SettingsManager

    private lateinit var searchViewModel: SearchViewModel

    @BeforeTest
    override fun setup() {
        super.setup()

        searchViewModel = SearchViewModel(bibleReadingManager, settingsManager)
    }

    @Test
    fun testSearchRequest() = testDispatcher.runBlockingTest {
        val requests = listOf(
                SearchRequest("1", true),
                SearchRequest("2", false),
                SearchRequest("", false),
                SearchRequest("", true),
                SearchRequest("3", true)
        )
        val searchRequest = async { searchViewModel.searchRequest().take(requests.size).toList() }
        requests.forEach { searchViewModel.requestSearch(it) }

        assertEquals(requests, searchRequest.await())
    }

    @Test
    fun testSearch() = testDispatcher.runBlockingTest {
        val currentTranslation = MockContents.kjvShortName
        val query = "query"
        `when`(bibleReadingManager.currentTranslation()).thenReturn(flowOf("", currentTranslation))
        `when`(bibleReadingManager.search(currentTranslation, query)).thenReturn(listOf(MockContents.kjvVerses[0]))
        `when`(bibleReadingManager.readBookNames(currentTranslation)).thenReturn(MockContents.kjvBookNames)
        `when`(bibleReadingManager.readBookShortNames(currentTranslation)).thenReturn(MockContents.kjvBookShortNames)

        assertEquals(
                listOf(
                        SearchResult(
                                query, listOf(MockContents.kjvVerses[0]),
                                MockContents.kjvBookNames, MockContents.kjvBookShortNames
                        )
                ),
                searchViewModel.search(query).toList()
        )
    }

    @Test
    fun testSearchResultWithException() = testDispatcher.runBlockingTest {
        val e = RuntimeException("random exception")
        `when`(bibleReadingManager.currentTranslation()).thenThrow(e)

        searchViewModel.search("")
                .onCompletion { assertEquals(e, it) }
                .catch { }
                .collect()
    }
}

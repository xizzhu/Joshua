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

import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.search.SearchActivity
import me.xizzhu.android.joshua.search.SearchResult
import me.xizzhu.android.joshua.search.SearchViewModel
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import org.mockito.Mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SearchResultListPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var navigator: Navigator
    @Mock
    private lateinit var searchViewModel: SearchViewModel
    @Mock
    private lateinit var searchActivity: SearchActivity

    private lateinit var searchResultListPresenter: SearchResultListPresenter

    @BeforeTest
    override fun setup() {
        super.setup()

        searchResultListPresenter = SearchResultListPresenter(navigator, searchViewModel, searchActivity, testCoroutineScope)
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

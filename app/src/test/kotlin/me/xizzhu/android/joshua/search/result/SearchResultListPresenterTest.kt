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

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.search.SearchActivity
import me.xizzhu.android.joshua.search.SearchViewModel
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SearchResultListPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var searchActivity: SearchActivity
    @Mock
    private lateinit var navigator: Navigator
    @Mock
    private lateinit var searchViewModel: SearchViewModel
    @Mock
    private lateinit var lifecycle: Lifecycle
    @Mock
    private lateinit var lifecycleCoroutineScope: LifecycleCoroutineScope

    private lateinit var searchResultListPresenter: SearchResultListPresenter

    @BeforeTest
    override fun setup() {
        super.setup()

        searchResultListPresenter = SearchResultListPresenter(searchActivity, navigator, searchViewModel, lifecycle, lifecycleCoroutineScope)
    }

    @Test
    fun testToSearchItems() = testDispatcher.runBlockingTest {
        val currentTranslation = MockContents.kjvShortName
        `when`(searchViewModel.bookNames(currentTranslation)).thenReturn(MockContents.kjvBookNames)
        `when`(searchViewModel.bookShortNames(currentTranslation)).thenReturn(MockContents.kjvBookShortNames)

        with(searchResultListPresenter) {
            val query = "query"
            assertEquals(
                    listOf(
                            TitleItem(MockContents.kjvBookNames[0], false),
                            SearchItem(MockContents.kjvVerses[0].verseIndex,
                                    MockContents.kjvBookShortNames[0], MockContents.kjvVerses[0].text.text, query, this::selectVerse),
                            SearchItem(MockContents.kjvVerses[1].verseIndex,
                                    MockContents.kjvBookShortNames[0], MockContents.kjvVerses[1].text.text, query, this::selectVerse)
                    ),
                    listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[1]).toSearchItems(currentTranslation, query)
            )
        }
    }
}

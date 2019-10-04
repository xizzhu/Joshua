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

import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.search.SearchActivity
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SearchResultListPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var searchActivity: SearchActivity
    @Mock
    private lateinit var navigator: Navigator
    @Mock
    private lateinit var searchResultInteractor: SearchResultInteractor
    @Mock
    private lateinit var searchResultListView: SearchResultListView

    private lateinit var searchResultViewHolder: SearchResultViewHolder
    private lateinit var searchResultListPresenter: SearchResultListPresenter

    @BeforeTest
    override fun setup() {
        super.setup()

        `when`(searchResultInteractor.settings()).thenReturn(emptyFlow())
        `when`(searchResultInteractor.searchResult()).thenReturn(emptyFlow())

        searchResultViewHolder = SearchResultViewHolder(searchResultListView)
        searchResultListPresenter = SearchResultListPresenter(searchActivity, navigator, searchResultInteractor, testDispatcher)
        searchResultListPresenter.bind(searchResultViewHolder)
    }

    @AfterTest
    override fun tearDown() {
        searchResultListPresenter.unbind()
        super.tearDown()
    }

    @Test
    fun testObserveSettings() = testDispatcher.runBlockingTest {
        val settings = listOf(
                ViewData.error(Settings.DEFAULT),
                ViewData.loading(Settings.DEFAULT),
                ViewData.success(Settings(false, true, 1, true))
        )
        `when`(searchResultInteractor.settings()).thenReturn(flow { settings.forEach { emit(it) } })

        searchResultListPresenter.start()
        with(inOrder(searchResultViewHolder.searchResultListView)) {
            settings.forEach {
                if (ViewData.STATUS_SUCCESS == it.status) {
                    verify(searchResultViewHolder.searchResultListView, times(1)).onSettingsUpdated(it.data)
                }
            }
        }

        searchResultListPresenter.stop()
    }

    @Test
    fun testToSearchItems() = testDispatcher.runBlockingTest {
        val currentTranslation = MockContents.kjvShortName
        `when`(searchResultInteractor.readCurrentTranslation()).thenReturn(currentTranslation)
        `when`(searchResultInteractor.readBookNames(currentTranslation)).thenReturn(MockContents.kjvBookNames)
        `when`(searchResultInteractor.readBookShortNames(currentTranslation)).thenReturn(MockContents.kjvBookShortNames)

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
                    listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[1]).toSearchItems(query)
            )
        }
    }
}

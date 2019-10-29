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

import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.infra.ui.LoadingSpinnerInteractor
import me.xizzhu.android.joshua.search.result.SearchResultInteractor
import me.xizzhu.android.joshua.search.toolbar.SearchToolbarInteractor
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.BeforeTest
import kotlin.test.Test

class SearchViewModelTest : BaseUnitTest() {
    @Mock
    private lateinit var settingsManager: SettingsManager
    @Mock
    private lateinit var searchToolbarInteractor: SearchToolbarInteractor
    @Mock
    private lateinit var loadingSpinnerInteractor: LoadingSpinnerInteractor
    @Mock
    private lateinit var searchResultInteractor: SearchResultInteractor

    private lateinit var searchViewModel: SearchViewModel

    @BeforeTest
    override fun setup() {
        super.setup()

        `when`(searchToolbarInteractor.query()).thenReturn(emptyFlow())
        `when`(searchResultInteractor.loadingState()).thenReturn(emptyFlow())

        searchViewModel = SearchViewModel(settingsManager, searchToolbarInteractor, loadingSpinnerInteractor, searchResultInteractor, testDispatcher)
    }

    @Test
    fun testUpdateQuery() = testDispatcher.runBlockingTest {
        val queries = listOf("query1", "query2", "query3")
        `when`(searchToolbarInteractor.query()).thenReturn(flow { queries.forEach { emit(ViewData.success(it)) } })

        searchViewModel.start()
        with(inOrder(searchResultInteractor)) {
            queries.forEach { query -> verify(searchResultInteractor, times(1)).updateQuery(ViewData.success(query)) }
        }
        searchViewModel.stop()
    }

    @Test
    fun testLoadingState() = testDispatcher.runBlockingTest {
        val loadingState = listOf(ViewData.error(), ViewData.loading(), ViewData.success(null))
        `when`(searchResultInteractor.loadingState()).thenReturn(flow { loadingState.forEach { emit(it) } })

        searchViewModel.start()
        with(inOrder(loadingSpinnerInteractor)) {
            loadingState.forEach { verify(loadingSpinnerInteractor, times(1)).updateLoadingState(it) }
        }
        searchViewModel.stop()
    }
}

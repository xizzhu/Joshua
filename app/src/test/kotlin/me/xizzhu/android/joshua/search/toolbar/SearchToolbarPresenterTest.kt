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

package me.xizzhu.android.joshua.search.toolbar

import android.app.SearchManager
import android.app.SearchableInfo
import android.content.Context
import me.xizzhu.android.joshua.search.SearchActivity
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SearchToolbarPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var searchToolbar: SearchToolbar
    @Mock
    private lateinit var searchActivity: SearchActivity
    @Mock
    private lateinit var searchManager: SearchManager
    @Mock
    private lateinit var searchable: SearchableInfo
    @Mock
    private lateinit var searchToolbarInteractor: SearchToolbarInteractor

    private lateinit var searchToolbarViewHolder: SearchToolbarViewHolder
    private lateinit var searchToolbarPresenter: SearchToolbarPresenter

    @BeforeTest
    override fun setup() {
        super.setup()

        `when`(searchActivity.getSystemService(Context.SEARCH_SERVICE)).thenReturn(searchManager)
        `when`(searchManager.getSearchableInfo(any())).thenReturn(searchable)

        searchToolbarViewHolder = SearchToolbarViewHolder(searchToolbar)
        searchToolbarPresenter = SearchToolbarPresenter(searchActivity, searchToolbarInteractor, testDispatcher)
    }

    @Test
    fun testBindView() {
        searchToolbarPresenter.create(searchToolbarViewHolder)
        verify(searchToolbar, times(1)).setOnQueryTextListener(searchToolbarPresenter.onQueryTextListener)
        verify(searchToolbar, times(1)).setSearchableInfo(searchable)

        searchToolbarPresenter.destroy()
        verify(searchToolbar, times(1)).setOnQueryTextListener(null)
    }

    @Test
    fun testSubmitQuery() {
        assertFalse(searchToolbarPresenter.onQueryTextListener.onQueryTextSubmit(""))
        assertFalse(searchToolbarPresenter.onQueryTextListener.onQueryTextSubmit("query"))

        with(inOrder(searchToolbarInteractor)) {
            verify(searchToolbarInteractor, times(1)).submitQuery("")
            verify(searchToolbarInteractor, times(1)).submitQuery("query")
        }
        verify(searchToolbarInteractor, never()).updateQuery(anyString())
    }

    @Test
    fun testUpdateQuery() {
        assertTrue(searchToolbarPresenter.onQueryTextListener.onQueryTextChange(""))
        assertTrue(searchToolbarPresenter.onQueryTextListener.onQueryTextChange("query"))

        with(inOrder(searchToolbarInteractor)) {
            verify(searchToolbarInteractor, times(1)).updateQuery("")
            verify(searchToolbarInteractor, times(1)).updateQuery("query")
        }
        verify(searchToolbarInteractor, never()).submitQuery(anyString())
    }
}

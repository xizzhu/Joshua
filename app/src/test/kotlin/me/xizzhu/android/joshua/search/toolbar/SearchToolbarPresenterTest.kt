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

package me.xizzhu.android.joshua.search.toolbar

import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import kotlin.test.BeforeTest
import kotlin.test.Test

class SearchToolbarPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var searchToolbar: SearchToolbar
    @Mock
    private lateinit var searchToolbarInteractor: SearchToolbarInteractor

    private lateinit var searchToolbarViewHolder: SearchToolbarViewHolder
    private lateinit var searchToolbarPresenter: SearchToolbarPresenter

    @BeforeTest
    override fun setup() {
        super.setup()

        searchToolbarViewHolder = SearchToolbarViewHolder(searchToolbar)
        searchToolbarPresenter = SearchToolbarPresenter(searchToolbarInteractor, testDispatcher)
    }

    @Test
    fun testBindView() {
        searchToolbarPresenter.bind(searchToolbarViewHolder)
        verify(searchToolbar, times(1)).setOnQueryTextListener(searchToolbarPresenter.onQueryTextListener)

        searchToolbarPresenter.unbind()
        verify(searchToolbar, times(1)).setOnQueryTextListener(null)
    }

    @Test
    fun testSubmitQuery() {
        val query = "query"
        searchToolbarPresenter.onQueryTextListener.onQueryTextSubmit("")
        searchToolbarPresenter.onQueryTextListener.onQueryTextSubmit(query)
        searchToolbarPresenter.onQueryTextListener.onQueryTextSubmit(query)
        verify(searchToolbarInteractor, times(1)).updateQuery(query)
    }
}

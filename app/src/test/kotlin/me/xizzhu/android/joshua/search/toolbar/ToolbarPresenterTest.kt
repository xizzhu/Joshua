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

import me.xizzhu.android.joshua.search.SearchInteractor
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ToolbarPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var searchInteractor: SearchInteractor
    private lateinit var toolbarPresenter: ToolbarPresenter

    @Before
    override fun setup() {
        super.setup()
        toolbarPresenter = ToolbarPresenter(searchInteractor)
    }

    @Test
    fun testQueryEmpty() {
        assertFalse(toolbarPresenter.updateSearchQuery(""))
    }

    @Test
    fun testQuery() {
        val query = "query"
        `when`(searchInteractor.updateSearchQuery(query)).thenReturn(true)
        assertTrue(toolbarPresenter.updateSearchQuery(query))
        verify(searchInteractor, times(1)).updateSearchQuery(query)
    }

    @Test
    fun testQueryWithFailure() {
        val query = "query"
        `when`(searchInteractor.updateSearchQuery(query)).thenReturn(false)
        assertFalse(toolbarPresenter.updateSearchQuery(query))
    }

    @Test
    fun testQueryWithException() {
        val query = "query"
        `when`(searchInteractor.updateSearchQuery(query)).thenThrow(RuntimeException("Random exception"))
        assertFalse(toolbarPresenter.updateSearchQuery(query))
    }
}

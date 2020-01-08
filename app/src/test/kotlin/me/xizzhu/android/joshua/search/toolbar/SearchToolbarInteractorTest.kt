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

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.tests.BaseUnitTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SearchToolbarInteractorTest : BaseUnitTest() {
    private lateinit var searchToolbarInteractor: SearchToolbarInteractor

    @BeforeTest
    override fun setup() {
        super.setup()
        searchToolbarInteractor = SearchToolbarInteractor(testDispatcher)
    }

    @Test
    fun testUpdateQuery() = testDispatcher.runBlockingTest {
        val queryAsync = async { searchToolbarInteractor.query().take(3).toList() }

        val queries = listOf("query 1", "", "another one")
        queries.forEach { searchToolbarInteractor.updateQuery(it) }
        assertEquals(queries.map { ViewData.loading(it) }, queryAsync.await())
    }

    @Test
    fun testSubmitQuery() = testDispatcher.runBlockingTest {
        val queryAsync = async { searchToolbarInteractor.query().take(3).toList() }

        val queries = listOf("query 1", "", "another one")
        queries.forEach { searchToolbarInteractor.submitQuery(it) }
        assertEquals(queries.map { ViewData.success(it) }, queryAsync.await())
    }
}

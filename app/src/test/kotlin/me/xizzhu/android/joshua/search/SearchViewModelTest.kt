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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.infra.arch.ViewData
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
    fun testUpdateQuery() = testDispatcher.runBlockingTest {
        val queryAsync = async { searchViewModel.query().take(3).toList() }

        val queries = listOf("query 1", "", "another one")
        queries.forEach { searchViewModel.updateQuery(it) }
        assertEquals(queries.map { ViewData.loading(it) }, queryAsync.await())
    }

    @Test
    fun testSubmitQuery() = testDispatcher.runBlockingTest {
        val queryAsync = async { searchViewModel.query().take(3).toList() }

        val queries = listOf("query 1", "", "another one")
        queries.forEach { searchViewModel.submitQuery(it) }
        assertEquals(queries.map { ViewData.success(it) }, queryAsync.await())
    }

    @Test
    fun testCurrentTranslation() = testDispatcher.runBlockingTest {
        val currentTranslation = MockContents.kjvShortName
        `when`(bibleReadingManager.currentTranslation()).thenReturn(flowOf("", currentTranslation))

        assertEquals(currentTranslation, searchViewModel.currentTranslation())
    }
}

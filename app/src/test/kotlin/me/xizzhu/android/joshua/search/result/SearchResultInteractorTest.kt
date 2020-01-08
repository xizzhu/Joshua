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
import org.mockito.Mockito.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SearchResultInteractorTest : BaseUnitTest() {
    @Mock
    private lateinit var bibleReadingManager: BibleReadingManager
    @Mock
    private lateinit var settingsManager: SettingsManager

    private lateinit var searchResultInteractor: SearchResultInteractor

    @BeforeTest
    override fun setup() {
        super.setup()

        searchResultInteractor = SearchResultInteractor(bibleReadingManager, settingsManager, testDispatcher)
    }

    @Test
    fun testUpdateQuery() = testDispatcher.runBlockingTest {
        val queryAsync = async { searchResultInteractor.query().take(3).toList() }

        val queries = listOf(ViewData.loading(), ViewData.success("query"), ViewData.error())
        queries.forEach { searchResultInteractor.updateQuery(it) }
        assertEquals(queries, queryAsync.await())
    }

    @Test
    fun testSearch() = testDispatcher.runBlockingTest {
        val query = "query"
        val currentTranslation = MockContents.kjvShortName
        val verses = MockContents.kjvVerses
        `when`(bibleReadingManager.currentTranslation()).thenReturn(flowOf(currentTranslation))
        `when`(bibleReadingManager.search(currentTranslation, query)).thenReturn(verses)

        assertEquals(ViewData.success(verses), searchResultInteractor.search(query))
    }

    @Test
    fun testBookNames() = testDispatcher.runBlockingTest {
        val currentTranslation = MockContents.kjvShortName
        val bookNames = MockContents.kjvBookNames
        `when`(bibleReadingManager.currentTranslation()).thenReturn(flowOf(currentTranslation))
        `when`(bibleReadingManager.readBookNames(currentTranslation)).thenReturn(bookNames)

        assertEquals(ViewData.success(bookNames), searchResultInteractor.bookNames())
    }

    @Test
    fun testBookShortNames() = testDispatcher.runBlockingTest {
        val currentTranslation = MockContents.kjvShortName
        val bookShortNames = MockContents.kjvBookShortNames
        `when`(bibleReadingManager.currentTranslation()).thenReturn(flowOf(currentTranslation))
        `when`(bibleReadingManager.readBookShortNames(currentTranslation)).thenReturn(bookShortNames)

        assertEquals(ViewData.success(bookShortNames), searchResultInteractor.bookShortNames())
    }
}

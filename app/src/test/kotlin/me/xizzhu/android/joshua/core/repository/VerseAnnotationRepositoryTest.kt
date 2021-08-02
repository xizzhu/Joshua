/*
 * Copyright (C) 2021 Xizhi Zhu
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

package me.xizzhu.android.joshua.core.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.Bookmark
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.repository.local.LocalVerseAnnotationStorage
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.Test
import kotlin.test.assertEquals

class VerseAnnotationRepositoryTest : BaseUnitTest() {
    @Mock
    private lateinit var localVerseAnnotationStorage: LocalVerseAnnotationStorage<Bookmark>

    private lateinit var verseAnnotationRepository: VerseAnnotationRepository<Bookmark>

    @Test
    fun testObserveInitialSortOrder() = runBlocking {
        `when`(localVerseAnnotationStorage.readSortOrder()).thenReturn(Constants.SORT_BY_BOOK)
        verseAnnotationRepository = VerseAnnotationRepository(localVerseAnnotationStorage, testDispatcher)
        assertEquals(Constants.SORT_BY_BOOK, verseAnnotationRepository.sortOrder.first())
    }

    @Test
    fun testObserveInitialSortOrderWithException() = runBlocking {
        `when`(localVerseAnnotationStorage.readSortOrder()).thenThrow(RuntimeException("Random exception"))
        verseAnnotationRepository = VerseAnnotationRepository(localVerseAnnotationStorage, testDispatcher)
        assertEquals(Constants.DEFAULT_SORT_ORDER, verseAnnotationRepository.sortOrder.first())
    }

    @Test
    fun testSaveThenReadSortOrder() = runBlocking {
        `when`(localVerseAnnotationStorage.readSortOrder()).thenReturn(Constants.DEFAULT_SORT_ORDER)
        verseAnnotationRepository = VerseAnnotationRepository(localVerseAnnotationStorage, testDispatcher)
        assertEquals(Constants.DEFAULT_SORT_ORDER, verseAnnotationRepository.sortOrder.first())

        verseAnnotationRepository.saveSortOrder(Constants.SORT_BY_BOOK)
        verify(localVerseAnnotationStorage, times(1)).saveSortOrder(Constants.SORT_BY_BOOK)
        assertEquals(Constants.SORT_BY_BOOK, verseAnnotationRepository.sortOrder.first())
    }
}

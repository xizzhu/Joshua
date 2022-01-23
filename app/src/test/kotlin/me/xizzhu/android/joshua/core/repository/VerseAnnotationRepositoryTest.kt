/*
 * Copyright (C) 2022 Xizhi Zhu
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

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.xizzhu.android.joshua.core.Bookmark
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.repository.local.LocalVerseAnnotationStorage
import me.xizzhu.android.joshua.tests.BaseUnitTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class VerseAnnotationRepositoryTest : BaseUnitTest() {
    private lateinit var localVerseAnnotationStorage: LocalVerseAnnotationStorage<Bookmark>

    @BeforeTest
    override fun setup() {
        super.setup()

        localVerseAnnotationStorage = mockk()
    }

    @Test
    fun `test sortOrder from constructor`() = runTest {
        coEvery { localVerseAnnotationStorage.readSortOrder() } returns Constants.SORT_BY_BOOK

        val verseAnnotationRepository = VerseAnnotationRepository<Bookmark>(localVerseAnnotationStorage, testScope)
        assertEquals(Constants.SORT_BY_BOOK, verseAnnotationRepository.sortOrder.first())
    }

    @Test
    fun `test sortOrder from constructor with exception`() = runTest {
        coEvery { localVerseAnnotationStorage.readSortOrder() } throws RuntimeException("Random exception")

        val verseAnnotationRepository = VerseAnnotationRepository<Bookmark>(localVerseAnnotationStorage, testScope)
        assertEquals(Constants.DEFAULT_SORT_ORDER, verseAnnotationRepository.sortOrder.first())
    }

    @Test
    fun `test saveSortOrder()`() = runTest {
        coEvery { localVerseAnnotationStorage.readSortOrder() } returns Constants.DEFAULT_SORT_ORDER
        coEvery { localVerseAnnotationStorage.saveSortOrder(Constants.SORT_BY_BOOK) } returns Unit

        val verseAnnotationRepository = VerseAnnotationRepository<Bookmark>(localVerseAnnotationStorage, testScope)
        assertEquals(Constants.DEFAULT_SORT_ORDER, verseAnnotationRepository.sortOrder.first())

        verseAnnotationRepository.saveSortOrder(Constants.SORT_BY_BOOK)
        coVerify(exactly = 1) { localVerseAnnotationStorage.saveSortOrder(Constants.SORT_BY_BOOK) }
        assertEquals(Constants.SORT_BY_BOOK, verseAnnotationRepository.sortOrder.first())
    }
}

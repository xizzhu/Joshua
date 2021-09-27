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

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.repository.local.LocalReadingStorage
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BibleReadingRepositoryTest : BaseUnitTest() {
    private lateinit var localReadingStorage: LocalReadingStorage

    @BeforeTest
    override fun setup() {
        super.setup()
        localReadingStorage = mockk()
    }

    @Test
    fun `test currentVerseIndex from constructor`() = runBlocking {
        coEvery { localReadingStorage.readCurrentVerseIndex() } returns VerseIndex(1, 2, 3)
        coEvery { localReadingStorage.readCurrentTranslation() } returns MockContents.kjvShortName
        coEvery { localReadingStorage.readParallelTranslations() } returns emptyList()

        val bibleReadingRepository = BibleReadingRepository(localReadingStorage, testDispatcher)
        assertEquals(VerseIndex(1, 2, 3), bibleReadingRepository.currentVerseIndex.first())
    }

    @Test
    fun `test currentVerseIndex from constructor with exception`() = runBlocking {
        coEvery { localReadingStorage.readCurrentVerseIndex() } throws RuntimeException("Random exception")
        coEvery { localReadingStorage.readCurrentTranslation() } returns MockContents.kjvShortName
        coEvery { localReadingStorage.readParallelTranslations() } returns emptyList()

        val bibleReadingRepository = BibleReadingRepository(localReadingStorage, testDispatcher)
        assertFalse(bibleReadingRepository.currentVerseIndex.first().isValid())
    }

    @Test
    fun `test saveCurrentVerseIndex()`() = runBlocking {
        coEvery { localReadingStorage.readCurrentVerseIndex() } returns VerseIndex(1, 2, 3)
        coEvery { localReadingStorage.saveCurrentVerseIndex(any()) } returns Unit
        coEvery { localReadingStorage.readCurrentTranslation() } returns MockContents.kjvShortName
        coEvery { localReadingStorage.readParallelTranslations() } returns emptyList()

        val bibleReadingRepository = BibleReadingRepository(localReadingStorage, testDispatcher)
        bibleReadingRepository.saveCurrentVerseIndex(VerseIndex(4, 5, 6))
        assertEquals(VerseIndex(4, 5, 6), bibleReadingRepository.currentVerseIndex.first())
    }

    @Test
    fun `test currentTranslation from constructor`() = runBlocking {
        coEvery { localReadingStorage.readCurrentVerseIndex() } returns VerseIndex(1, 2, 3)
        coEvery { localReadingStorage.readCurrentTranslation() } returns MockContents.kjvShortName
        coEvery { localReadingStorage.readParallelTranslations() } returns emptyList()

        val bibleReadingRepository = BibleReadingRepository(localReadingStorage, testDispatcher)
        assertEquals(MockContents.kjvShortName, bibleReadingRepository.currentTranslation.first())
    }

    @Test
    fun `test currentTranslation from constructor with exception`() = runBlocking {
        coEvery { localReadingStorage.readCurrentVerseIndex() } returns VerseIndex(1, 2, 3)
        coEvery { localReadingStorage.readCurrentTranslation() } throws RuntimeException("Random exception")
        coEvery { localReadingStorage.readParallelTranslations() } returns emptyList()

        val bibleReadingRepository = BibleReadingRepository(localReadingStorage, testDispatcher)
        assertTrue(bibleReadingRepository.currentTranslation.first().isEmpty())
    }

    @Test
    fun `test saveCurrentTranslation()`() = runBlocking {
        coEvery { localReadingStorage.readCurrentVerseIndex() } returns VerseIndex(1, 2, 3)
        coEvery { localReadingStorage.readCurrentTranslation() } returns MockContents.kjvShortName
        coEvery { localReadingStorage.readParallelTranslations() } returns emptyList()
        coEvery { localReadingStorage.saveCurrentTranslation(any()) } returns Unit

        val bibleReadingRepository = BibleReadingRepository(localReadingStorage, testDispatcher)
        bibleReadingRepository.saveCurrentTranslation(MockContents.cuvShortName)
        assertEquals(MockContents.cuvShortName, bibleReadingRepository.currentTranslation.first())
    }

    @Test
    fun `test parallelTranslations from constructor`() = runBlocking {
        coEvery { localReadingStorage.readCurrentVerseIndex() } returns VerseIndex(1, 2, 3)
        coEvery { localReadingStorage.readCurrentTranslation() } returns MockContents.kjvShortName
        coEvery { localReadingStorage.readParallelTranslations() } returns listOf(MockContents.cuvShortName)

        val bibleReadingRepository = BibleReadingRepository(localReadingStorage, testDispatcher)
        assertEquals(listOf(MockContents.cuvShortName), bibleReadingRepository.parallelTranslations.first())
    }

    @Test
    fun `test parallelTranslations from constructor with exception`() = runBlocking {
        coEvery { localReadingStorage.readCurrentVerseIndex() } returns VerseIndex(1, 2, 3)
        coEvery { localReadingStorage.readCurrentTranslation() } returns MockContents.kjvShortName
        coEvery { localReadingStorage.readParallelTranslations() } throws RuntimeException("Random exception")

        val bibleReadingRepository = BibleReadingRepository(localReadingStorage, testDispatcher)
        assertTrue(bibleReadingRepository.parallelTranslations.first().isEmpty())
    }

    @Test
    fun `test requestParallelTranslation() and removeParallelTranslation()`() = runBlocking {
        coEvery { localReadingStorage.readCurrentVerseIndex() } returns VerseIndex(1, 2, 3)
        coEvery { localReadingStorage.readCurrentTranslation() } returns MockContents.kjvShortName
        coEvery { localReadingStorage.readParallelTranslations() } returns emptyList()
        coEvery { localReadingStorage.saveParallelTranslations(any()) } returns Unit

        val bibleReadingRepository = BibleReadingRepository(localReadingStorage, testDispatcher)

        // adds a parallel
        bibleReadingRepository.requestParallelTranslation(MockContents.kjvShortName)
        assertEquals(listOf(MockContents.kjvShortName), bibleReadingRepository.parallelTranslations.first())
        coVerify(exactly = 1) { localReadingStorage.saveParallelTranslations(listOf(MockContents.kjvShortName)) }

        // adds the same parallel, and also a new one
        bibleReadingRepository.requestParallelTranslation(MockContents.kjvShortName)
        bibleReadingRepository.requestParallelTranslation(MockContents.cuvShortName)
        assertEquals(setOf(MockContents.kjvShortName, MockContents.cuvShortName), bibleReadingRepository.parallelTranslations.first().toSet())
        coVerify(exactly = 1) { localReadingStorage.saveParallelTranslations(listOf(MockContents.kjvShortName, MockContents.cuvShortName)) }

        // removes a non-exist parallel
        bibleReadingRepository.removeParallelTranslation("not_exist")
        assertEquals(setOf(MockContents.kjvShortName, MockContents.cuvShortName), bibleReadingRepository.parallelTranslations.first().toSet())

        // removes a parallel
        bibleReadingRepository.removeParallelTranslation(MockContents.kjvShortName)
        assertEquals(listOf(MockContents.cuvShortName), bibleReadingRepository.parallelTranslations.first())
        coVerify(exactly = 1) { localReadingStorage.saveParallelTranslations(listOf(MockContents.cuvShortName)) }

        // removes another parallel
        bibleReadingRepository.removeParallelTranslation(MockContents.cuvShortName)
        assertTrue(bibleReadingRepository.parallelTranslations.first().isEmpty())
        coVerify(exactly = 1) { localReadingStorage.saveParallelTranslations(emptyList()) }
    }

    @Test
    fun `test clearParallelTranslation()`() = runBlocking {
        coEvery { localReadingStorage.readCurrentVerseIndex() } returns VerseIndex(1, 2, 3)
        coEvery { localReadingStorage.readCurrentTranslation() } returns MockContents.kjvShortName
        coEvery { localReadingStorage.readParallelTranslations() } returns emptyList()
        coEvery { localReadingStorage.saveParallelTranslations(any()) } returns Unit

        val bibleReadingRepository = BibleReadingRepository(localReadingStorage, testDispatcher)
        bibleReadingRepository.requestParallelTranslation(MockContents.kjvShortName)
        bibleReadingRepository.requestParallelTranslation(MockContents.cuvShortName)
        assertEquals(setOf(MockContents.kjvShortName, MockContents.cuvShortName), bibleReadingRepository.parallelTranslations.first().toSet())

        bibleReadingRepository.clearParallelTranslation()
        assertTrue(bibleReadingRepository.parallelTranslations.first().isEmpty())
    }

    @Test
    fun `test readBookNames()`() = runBlocking {
        val bibleReadingRepository = BibleReadingRepository(localReadingStorage, testDispatcher)

        // no cache yet, read from LocalReadingStorage
        coEvery { localReadingStorage.readBookNames(MockContents.kjvShortName) } returns MockContents.kjvBookNames
        assertEquals(MockContents.kjvBookNames, bibleReadingRepository.readBookNames(MockContents.kjvShortName))

        // has cache now, read from there
        coEvery { localReadingStorage.readBookNames(any()) } throws IllegalStateException("Should read from in-memory cache")
        assertEquals(MockContents.kjvBookNames, bibleReadingRepository.readBookNames(MockContents.kjvShortName))
    }

    @Test
    fun `test readBookShortNames()`() = runBlocking {
        val bibleReadingRepository = BibleReadingRepository(localReadingStorage, testDispatcher)

        // no cache yet, read from LocalReadingStorage
        coEvery { localReadingStorage.readBookShortNames(MockContents.kjvShortName) } returns MockContents.kjvBookShortNames
        assertEquals(MockContents.kjvBookShortNames, bibleReadingRepository.readBookShortNames(MockContents.kjvShortName))

        // has cache now, read from there
        coEvery { localReadingStorage.readBookShortNames(any()) } throws IllegalStateException("Should read from in-memory cache")
        assertEquals(MockContents.kjvBookShortNames, bibleReadingRepository.readBookShortNames(MockContents.kjvShortName))
    }

    @Test
    fun `test readVerses()`() = runBlocking {
        val bibleReadingRepository = BibleReadingRepository(localReadingStorage, testDispatcher)

        // no cache yet, read from LocalReadingStorage
        coEvery { localReadingStorage.readBookNames(MockContents.kjvShortName) } returns MockContents.kjvBookNames
        coEvery { localReadingStorage.readBookShortNames(MockContents.kjvShortName) } returns MockContents.kjvBookShortNames
        coEvery { localReadingStorage.readVerses(MockContents.kjvShortName, 0, 0) } returns MockContents.kjvVerses
        assertEquals(MockContents.kjvVerses, bibleReadingRepository.readVerses(MockContents.kjvShortName, 0, 0))

        // has cache now, read from there
        coEvery { localReadingStorage.readBookNames(any()) } throws IllegalStateException("Should read from in-memory cache")
        coEvery { localReadingStorage.readVerses(any(), any(), any()) } throws IllegalStateException("Should read from in-memory cache")
        assertEquals(MockContents.kjvVerses, bibleReadingRepository.readVerses(MockContents.kjvShortName, 0, 0))
    }
}

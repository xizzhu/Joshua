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

package me.xizzhu.android.joshua.core.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.repository.local.LocalReadingStorage
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BibleReadingRepositoryTest : BaseUnitTest() {
    @Mock
    private lateinit var localReadingStorage: LocalReadingStorage

    @Test
    fun testObserveInitialCurrentVerseIndex() = runBlocking {
        `when`(localReadingStorage.readCurrentVerseIndex()).thenReturn(VerseIndex(1, 2, 3))
        `when`(localReadingStorage.readCurrentTranslation()).thenReturn(MockContents.kjvShortName)
        `when`(localReadingStorage.readParallelTranslations()).thenReturn(emptyList())
        val bibleReadingRepository = BibleReadingRepository(localReadingStorage, testDispatcher)

        assertEquals(VerseIndex(1, 2, 3), bibleReadingRepository.currentVerseIndex.first())
    }

    @Test
    fun testObserveInitialCurrentVerseIndexWithException() = runBlocking {
        `when`(localReadingStorage.readCurrentVerseIndex()).thenThrow(RuntimeException("Random exception"))
        `when`(localReadingStorage.readCurrentTranslation()).thenReturn(MockContents.kjvShortName)
        `when`(localReadingStorage.readParallelTranslations()).thenReturn(emptyList())
        val bibleReadingRepository = BibleReadingRepository(localReadingStorage, testDispatcher)

        assertFalse(bibleReadingRepository.currentVerseIndex.first().isValid())
    }

    @Test
    fun testSaveCurrentVerseIndex() = runBlocking {
        `when`(localReadingStorage.readCurrentVerseIndex()).thenReturn(VerseIndex(1, 2, 3))
        `when`(localReadingStorage.readCurrentTranslation()).thenReturn(MockContents.kjvShortName)
        `when`(localReadingStorage.readParallelTranslations()).thenReturn(emptyList())
        val bibleReadingRepository = BibleReadingRepository(localReadingStorage, testDispatcher)

        bibleReadingRepository.saveCurrentVerseIndex(VerseIndex(4, 5, 6))
        assertEquals(VerseIndex(4, 5, 6), bibleReadingRepository.currentVerseIndex.first())
    }

    @Test
    fun testObserveInitialCurrentTranslation() = runBlocking {
        `when`(localReadingStorage.readCurrentVerseIndex()).thenReturn(VerseIndex(1, 2, 3))
        `when`(localReadingStorage.readCurrentTranslation()).thenReturn(MockContents.kjvShortName)
        `when`(localReadingStorage.readParallelTranslations()).thenReturn(emptyList())
        val bibleReadingRepository = BibleReadingRepository(localReadingStorage, testDispatcher)

        assertEquals(MockContents.kjvShortName, bibleReadingRepository.currentTranslation.first())
    }

    @Test
    fun testObserveInitialCurrentTranslationWithException() = runBlocking {
        `when`(localReadingStorage.readCurrentVerseIndex()).thenReturn(VerseIndex(1, 2, 3))
        `when`(localReadingStorage.readCurrentTranslation()).thenThrow(RuntimeException("Random exception"))
        `when`(localReadingStorage.readParallelTranslations()).thenReturn(emptyList())
        val bibleReadingRepository = BibleReadingRepository(localReadingStorage, testDispatcher)

        assertTrue(bibleReadingRepository.currentTranslation.first().isEmpty())
    }

    @Test
    fun testSaveCurrentTranslation() = runBlocking {
        `when`(localReadingStorage.readCurrentVerseIndex()).thenReturn(VerseIndex(1, 2, 3))
        `when`(localReadingStorage.readCurrentTranslation()).thenReturn(MockContents.kjvShortName)
        `when`(localReadingStorage.readParallelTranslations()).thenReturn(emptyList())
        val bibleReadingRepository = BibleReadingRepository(localReadingStorage, testDispatcher)

        bibleReadingRepository.saveCurrentTranslation(MockContents.cuvShortName)
        assertEquals(MockContents.cuvShortName, bibleReadingRepository.currentTranslation.first())
    }

    @Test
    fun testObserveInitialParallelTranslations() = runBlocking {
        `when`(localReadingStorage.readCurrentVerseIndex()).thenReturn(VerseIndex(1, 2, 3))
        `when`(localReadingStorage.readCurrentTranslation()).thenReturn(MockContents.kjvShortName)
        `when`(localReadingStorage.readParallelTranslations()).thenReturn(listOf(MockContents.cuvShortName))
        val bibleReadingRepository = BibleReadingRepository(localReadingStorage, testDispatcher)

        assertEquals(listOf(MockContents.cuvShortName), bibleReadingRepository.parallelTranslations.first())
    }

    @Test
    fun testObserveInitialParallelTranslationsWithException() = runBlocking {
        `when`(localReadingStorage.readCurrentVerseIndex()).thenReturn(VerseIndex(1, 2, 3))
        `when`(localReadingStorage.readCurrentTranslation()).thenReturn(MockContents.kjvShortName)
        `when`(localReadingStorage.readParallelTranslations()).thenThrow(RuntimeException("Random exception"))
        val bibleReadingRepository = BibleReadingRepository(localReadingStorage, testDispatcher)

        assertTrue(bibleReadingRepository.parallelTranslations.first().isEmpty())
    }

    @Test
    fun testUpdateParallelTranslations() = runBlocking {
        `when`(localReadingStorage.readCurrentVerseIndex()).thenReturn(VerseIndex(1, 2, 3))
        `when`(localReadingStorage.readCurrentTranslation()).thenReturn(MockContents.kjvShortName)
        `when`(localReadingStorage.readParallelTranslations()).thenReturn(emptyList())
        val bibleReadingRepository = BibleReadingRepository(localReadingStorage, testDispatcher)

        // adds a parallel
        bibleReadingRepository.requestParallelTranslation(MockContents.kjvShortName)
        assertEquals(listOf(MockContents.kjvShortName), bibleReadingRepository.parallelTranslations.first())
        verify(localReadingStorage, times(1)).saveParallelTranslations(listOf(MockContents.kjvShortName))

        // adds the same parallel, and also a new one
        bibleReadingRepository.requestParallelTranslation(MockContents.kjvShortName)
        bibleReadingRepository.requestParallelTranslation(MockContents.cuvShortName)
        assertEquals(setOf(MockContents.kjvShortName, MockContents.cuvShortName), bibleReadingRepository.parallelTranslations.first().toSet())
        verify(localReadingStorage, times(1)).saveParallelTranslations(listOf(MockContents.kjvShortName, MockContents.cuvShortName))

        // removes a non-exist parallel
        bibleReadingRepository.removeParallelTranslation("not_exist")
        assertEquals(setOf(MockContents.kjvShortName, MockContents.cuvShortName), bibleReadingRepository.parallelTranslations.first().toSet())

        // removes a parallel
        bibleReadingRepository.removeParallelTranslation(MockContents.kjvShortName)
        assertEquals(listOf(MockContents.cuvShortName), bibleReadingRepository.parallelTranslations.first())
        verify(localReadingStorage, times(1)).saveParallelTranslations(listOf(MockContents.cuvShortName))

        // removes another parallel
        bibleReadingRepository.removeParallelTranslation(MockContents.cuvShortName)
        assertTrue(bibleReadingRepository.parallelTranslations.first().isEmpty())
        verify(localReadingStorage, times(1)).saveParallelTranslations(emptyList())
    }


    @Test
    fun testClearParallelTranslations() = runBlocking {
        `when`(localReadingStorage.readCurrentVerseIndex()).thenReturn(VerseIndex(1, 2, 3))
        `when`(localReadingStorage.readCurrentTranslation()).thenReturn(MockContents.kjvShortName)
        `when`(localReadingStorage.readParallelTranslations()).thenReturn(emptyList())
        val bibleReadingRepository = BibleReadingRepository(localReadingStorage, testDispatcher)

        bibleReadingRepository.requestParallelTranslation(MockContents.kjvShortName)
        bibleReadingRepository.requestParallelTranslation(MockContents.cuvShortName)
        assertEquals(setOf(MockContents.kjvShortName, MockContents.cuvShortName), bibleReadingRepository.parallelTranslations.first().toSet())

        bibleReadingRepository.clearParallelTranslation()
        assertTrue(bibleReadingRepository.parallelTranslations.first().isEmpty())
    }

    @Test
    fun testReadBookNames() = runBlocking {
        val bibleReadingRepository = BibleReadingRepository(localReadingStorage, testDispatcher)

        // no cache yet, read from LocalReadingStorage
        `when`(localReadingStorage.readBookNames(MockContents.kjvShortName))
                .thenReturn(MockContents.kjvBookNames)
        assertEquals(MockContents.kjvBookNames,
                bibleReadingRepository.readBookNames(MockContents.kjvShortName))

        // has cache now, read from there
        `when`(localReadingStorage.readBookNames(anyString()))
                .thenThrow(IllegalStateException("Should read from in-memory cache"))
        assertEquals(MockContents.kjvBookNames,
                bibleReadingRepository.readBookNames(MockContents.kjvShortName))
    }

    @Test
    fun testReadBookShortNames() = runBlocking {
        val bibleReadingRepository = BibleReadingRepository(localReadingStorage, testDispatcher)

        // no cache yet, read from LocalReadingStorage
        `when`(localReadingStorage.readBookShortNames(MockContents.kjvShortName))
                .thenReturn(MockContents.kjvBookShortNames)
        assertEquals(MockContents.kjvBookShortNames,
                bibleReadingRepository.readBookShortNames(MockContents.kjvShortName))

        // has cache now, read from there
        `when`(localReadingStorage.readBookShortNames(anyString()))
                .thenThrow(IllegalStateException("Should read from in-memory cache"))
        assertEquals(MockContents.kjvBookShortNames,
                bibleReadingRepository.readBookShortNames(MockContents.kjvShortName))
    }

    @Test
    fun testReadVerses() = runBlocking {
        val bibleReadingRepository = BibleReadingRepository(localReadingStorage, testDispatcher)

        // no cache yet, read from LocalReadingStorage
        `when`(localReadingStorage.readBookNames(MockContents.kjvShortName))
                .thenReturn(MockContents.kjvBookNames)
        `when`(localReadingStorage.readBookShortNames(MockContents.kjvShortName))
                .thenReturn(MockContents.kjvBookShortNames)
        `when`(localReadingStorage.readVerses(MockContents.kjvShortName, 0, 0))
                .thenReturn(MockContents.kjvVerses)
        assertEquals(MockContents.kjvVerses,
                bibleReadingRepository.readVerses(MockContents.kjvShortName, 0, 0))

        // has cache now, read from there
        `when`(localReadingStorage.readBookNames(anyString()))
                .thenThrow(IllegalStateException("Should read from in-memory cache"))
        `when`(localReadingStorage.readVerses(anyString(), anyInt(), anyInt()))
                .thenThrow(IllegalStateException("Should read from in-memory cache"))
        assertEquals(MockContents.kjvVerses,
                bibleReadingRepository.readVerses(MockContents.kjvShortName, 0, 0))
    }
}

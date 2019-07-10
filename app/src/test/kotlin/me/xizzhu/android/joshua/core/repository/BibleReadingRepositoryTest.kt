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

package me.xizzhu.android.joshua.core.repository

import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.repository.local.LocalReadingStorage
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyString
import kotlin.test.assertEquals

class BibleReadingRepositoryTest : BaseUnitTest() {
    @Mock
    private lateinit var localReadingStorage: LocalReadingStorage

    private lateinit var bibleReadingRepository: BibleReadingRepository

    @Before
    override fun setup() {
        super.setup()
        bibleReadingRepository = BibleReadingRepository(localReadingStorage)
    }

    @Test
    fun testReadBookNames() {
        runBlocking {
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
    }

    @Test
    fun testReadBookShortNames() {
        runBlocking {
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
    }

    @Test
    fun testReadVerses() {
        runBlocking {
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
}

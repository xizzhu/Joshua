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

package me.xizzhu.android.joshua.core

import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.first
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.repository.BibleReadingRepository
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BibleReadingManagerTest : BaseUnitTest() {
    @Mock
    private lateinit var bibleReadingRepository: BibleReadingRepository

    private lateinit var bibleReadingManager: BibleReadingManager

    @Before
    override fun setup() {
        super.setup()

        runBlocking {
            `when`(bibleReadingRepository.readCurrentTranslation()).thenReturn(MockContents.kjvShortName)
            `when`(bibleReadingRepository.readCurrentVerseIndex()).thenReturn(VerseIndex(1, 2, 3))
            bibleReadingManager = BibleReadingManager(bibleReadingRepository)
        }
    }

    @Test
    fun testObserveCurrentVerseIndex() {
        runBlocking {
            val observer = bibleReadingManager.observeCurrentVerseIndex()
            observer.consumeEach {
                if (it == VerseIndex(1, 2, 3)) {
                    observer.cancel()
                }
            }
        }
    }

    @Test
    fun testCurrentVerseIndex() {
        runBlocking {
            bibleReadingManager.saveCurrentVerseIndex(VerseIndex(4, 5, 6))
            val observer = bibleReadingManager.observeCurrentVerseIndex()
            observer.consumeEach {
                if (it == VerseIndex(4, 5, 6)) {
                    observer.cancel()
                }
            }
        }
    }

    @Test
    fun testCurrentVerseIndexWithException() {
        runBlocking {
            `when`(bibleReadingRepository.readCurrentVerseIndex()).thenThrow(RuntimeException("Random exception"))
            assertEquals(VerseIndex.INVALID, bibleReadingManager.observeCurrentVerseIndex().first())
        }
    }

    @Test
    fun testObserveCurrentTranslation() {
        runBlocking {
            val observer = bibleReadingManager.observeCurrentTranslation()
            observer.consumeEach {
                if (it == MockContents.kjvShortName) {
                    observer.cancel()
                }
            }
        }
    }

    @Test
    fun testObserveCurrentTranslationWithException() {
        runBlocking {
            `when`(bibleReadingRepository.readCurrentTranslation()).thenThrow(RuntimeException("Random exception"))
            assertEquals("", bibleReadingManager.observeCurrentTranslation().first())
        }
    }

    @Test
    fun testCurrentTranslation() {
        runBlocking {
            bibleReadingManager.saveCurrentTranslation(MockContents.cuvShortName)
            val observer = bibleReadingManager.observeCurrentTranslation()
            observer.consumeEach {
                if (it == MockContents.cuvShortName) {
                    observer.cancel()
                }
            }
        }
    }

    @Test
    fun testDefaultParallelTranslations() {
        runBlocking {
            assertTrue(bibleReadingManager.observeParallelTranslations().first().isEmpty())
        }
    }

    @Test
    fun testParallelTranslations() {
        runBlocking {
            bibleReadingManager.requestParallelTranslation(MockContents.kjvShortName)
            assertEquals(listOf(MockContents.kjvShortName),
                    bibleReadingManager.observeParallelTranslations().first())

            bibleReadingManager.requestParallelTranslation(MockContents.kjvShortName)
            bibleReadingManager.requestParallelTranslation(MockContents.cuvShortName)
            assertEquals(setOf(MockContents.kjvShortName, MockContents.cuvShortName),
                    bibleReadingManager.observeParallelTranslations().first().toSet())

            bibleReadingManager.removeParallelTranslation(MockContents.kjvShortName)
            assertEquals(listOf(MockContents.cuvShortName),
                    bibleReadingManager.observeParallelTranslations().first())

            bibleReadingManager.removeParallelTranslation(MockContents.cuvShortName)
            assertTrue(bibleReadingManager.observeParallelTranslations().first().isEmpty())
        }
    }

    @Test
    fun testRemoveNonExistParallelTranslations() {
        runBlocking {
            bibleReadingManager.requestParallelTranslation(MockContents.kjvShortName)
            bibleReadingManager.requestParallelTranslation(MockContents.cuvShortName)
            assertEquals(setOf(MockContents.kjvShortName, MockContents.cuvShortName),
                    bibleReadingManager.observeParallelTranslations().first().toSet())

            bibleReadingManager.removeParallelTranslation("not_exist")
            assertEquals(setOf(MockContents.kjvShortName, MockContents.cuvShortName),
                    bibleReadingManager.observeParallelTranslations().first().toSet())
        }
    }

    @Test
    fun testClearParallelTranslations() {
        runBlocking {
            bibleReadingManager.requestParallelTranslation(MockContents.kjvShortName)
            bibleReadingManager.requestParallelTranslation(MockContents.cuvShortName)
            assertEquals(setOf(MockContents.kjvShortName, MockContents.cuvShortName),
                    bibleReadingManager.observeParallelTranslations().first().toSet())

            bibleReadingManager.clearParallelTranslation()
            assertTrue(bibleReadingManager.observeParallelTranslations().first().isEmpty())
        }
    }
}

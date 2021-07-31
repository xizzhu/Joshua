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

package me.xizzhu.android.joshua.core.repository.remote.android

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.BaseUnitTest
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HttpStrongNumberServiceTest : BaseUnitTest() {
    private lateinit var strongNumberService: HttpStrongNumberService

    @Before
    override fun setup() {
        super.setup()

        strongNumberService = HttpStrongNumberService()
    }

    @Test
    fun testFetchIndexes() {
        runBlocking {
            prepareSnIndexes()

            val channel = Channel<Int>()
            var channelCalled = false
            launch {
                channel.consumeEach {
                    channelCalled = true
                    assertTrue(it in 0..100)
                }
            }

            val actual = strongNumberService.fetchIndexes(channel)
            channel.close()
            assertTrue(channelCalled)
            (0 until Bible.BOOK_COUNT).forEach { bookIndex ->
                (0 until Bible.getChapterCount(bookIndex)).forEach { chapterIndex ->
                    assertTrue(actual.indexes.containsKey(VerseIndex(bookIndex, chapterIndex, 0)))
                }
            }
            assertTrue(actual.reverseIndexes.isNotEmpty())
        }
    }

    @Test
    fun testFetchWords() {
        runBlocking {
            prepareSnEn()

            val channel = Channel<Int>()
            var channelCalled = false
            launch {
                channel.consumeEach {
                    channelCalled = true
                    assertTrue(it == 50 || it == 100)
                }
            }

            val actual = strongNumberService.fetchWords(channel)
            channel.close()
            assertTrue(channelCalled)
            assertEquals(Constants.STRONG_NUMBER_HEBREW_COUNT + Constants.STRONG_NUMBER_GREEK_COUNT, actual.words.size)
        }
    }
}

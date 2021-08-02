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
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslationInfo
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HttpTranslationServiceTest : BaseUnitTest() {
    private lateinit var httpTranslationService: HttpTranslationService

    @Before
    override fun setup() {
        super.setup()

        httpTranslationService = HttpTranslationService()
    }

    @Test
    fun testFetchTranslations() {
        runBlocking {
            prepareTranslationList()

            assertEquals(
                    listOf(
                            RemoteTranslationInfo("KJV", "King James Version", "en_gb", 1861133L),
                            RemoteTranslationInfo("中文和合本", "中文和合本（简体）", "zh_cn", 1781720L)
                    ),
                    httpTranslationService.fetchTranslations()
            )
        }
    }

    @Test
    fun testFetchTranslation() {
        runBlocking {
            prepareKjv()

            val channel = Channel<Int>()
            var channelCalled = false
            launch {
                channel.consumeEach {
                    channelCalled = true
                    assertTrue(it in 0..100)
                }
            }

            val actual = httpTranslationService.fetchTranslation(channel, MockContents.kjvRemoteTranslationInfo)
            channel.close()
            assertTrue(channelCalled)
            assertEquals(MockContents.kjvRemoteTranslationInfo, actual.translationInfo)
            assertEquals(Bible.BOOK_COUNT, actual.bookNames.size)
            assertEquals(Bible.TOTAL_CHAPTER_COUNT, actual.verses.size)
        }
    }
}

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

package me.xizzhu.android.joshua.core.repository.remote.android

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslationInfo
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.Before
import org.mockito.Mockito.*
import java.io.ByteArrayInputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HttpTranslationServiceTest : BaseUnitTest() {
    private lateinit var httpTranslationService: HttpTranslationService

    @Before
    override fun setup() {
        super.setup()

        httpTranslationService = spy(HttpTranslationService())
    }

    @Test
    fun testFetchTranslations() {
        runBlocking {
            doReturn(ByteArrayInputStream("{\"translations\":[{\"name\":\"Authorized King James\",\"shortName\":\"KJV\",\"language\":\"en_gb\",\"size\":1861134}]}".toByteArray()))
                    .`when`(httpTranslationService).getInputStream("list.json")
            assertEquals(listOf(RemoteTranslationInfo("KJV", "Authorized King James", "en_gb", 1861134L)),
                    httpTranslationService.fetchTranslations())
        }
    }

    @Test
    fun testFetchTranslation() {
        runBlocking {
            doReturn(InstrumentationRegistry.getInstrumentation().context.assets.open("KJV.zip"))
                    .`when`(httpTranslationService).getInputStream("KJV.zip")

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

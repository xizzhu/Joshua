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

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.tests.MockLocalTranslationStorage
import me.xizzhu.android.joshua.tests.MockRemoteTranslationService
import me.xizzhu.android.joshua.utils.onEach
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TranslationRepositoryTest {
    private lateinit var translationRepository: TranslationRepository

    @Before
    fun setup() {
        translationRepository = TranslationRepository(MockLocalTranslationStorage(), MockRemoteTranslationService())
    }

    @Test
    fun testDefaultLocalTranslations() {
        val expected = emptyList<TranslationInfo>()
        val actual = runBlocking { translationRepository.readTranslationsFromLocal() }
        assertEquals(expected, actual)
    }

    @Test
    fun testReload() {
        val expected = listOf(MockContents.translationInfo)
        val actual = runBlocking { translationRepository.reload(false) }
        assertEquals(expected, actual)
    }

    @Test
    fun testReloadAgain() {
        val expected = listOf(MockContents.translationInfo)
        val actual = runBlocking {
            translationRepository.reload(false)
            translationRepository.reload(false)
        }
        assertEquals(expected, actual)
    }

    @Test
    fun testReloadAgainWithForceRefresh() {
        val expected = listOf(MockContents.translationInfo)
        val actual = runBlocking {
            translationRepository.reload(false)
            translationRepository.reload(true)
        }
        assertEquals(expected, actual)
    }

    @Test
    fun testDownloadTranslation() {
        val expected = listOf(MockContents.downloadedTranslationInfo)
        val actual = runBlocking {
            val channel = Channel<Int>()
            launch {
                translationRepository.downloadTranslation(channel, MockContents.translationInfo)
                channel.close()
            }
            var called = false
            channel.onEach { called = true }
            assertTrue(called)

            translationRepository.readTranslationsFromLocal()
        }
        assertEquals(expected, actual)
    }
}

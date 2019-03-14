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

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.first
import me.xizzhu.android.joshua.core.repository.TranslationRepository
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.tests.MockLocalTranslationStorage
import me.xizzhu.android.joshua.tests.MockRemoteTranslationService
import me.xizzhu.android.joshua.utils.onEach
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TranslationManagerTest : BaseUnitTest() {
    private lateinit var translationManager: TranslationManager

    @Before
    override fun setup() {
        super.setup()
        translationManager = TranslationManager(TranslationRepository(
                MockLocalTranslationStorage(), MockRemoteTranslationService()))
    }

    @Test
    fun testDefaultAvailableTranslations() {
        val expected = emptyList<TranslationInfo>()
        val actual = runBlocking { translationManager.observeAvailableTranslations().first() }
        assertEquals(expected, actual)
    }

    @Test
    fun testDefaultDownloadedTranslations() {
        val expected = emptyList<TranslationInfo>()
        val actual = runBlocking { translationManager.observeDownloadedTranslations().first() }
        assertEquals(expected, actual)
    }

    @Test
    fun testReload() {
        runBlocking {
            val expectedAvailable = listOf(MockContents.kjvTranslationInfo)
            val expectedDownloaded = emptyList<TranslationInfo>()

            translationManager.reload(false)
            val actualAvailable = translationManager.observeAvailableTranslations().first()
            val actualDownloaded = translationManager.observeDownloadedTranslations().first()

            assertEquals(expectedAvailable, actualAvailable)
            assertEquals(expectedDownloaded, actualDownloaded)
        }
    }

    @Test
    fun testReloadThenDownload() {
        runBlocking {
            translationManager.reload(false)
            assertEquals(listOf(MockContents.kjvTranslationInfo), translationManager.observeAvailableTranslations().first())
            assertTrue(translationManager.observeDownloadedTranslations().first().isEmpty())

            val channel = Channel<Int>(Channel.UNLIMITED)
            translationManager.downloadTranslation(channel, MockContents.kjvDownloadedTranslationInfo)
            assertTrue(translationManager.observeAvailableTranslations().first().isEmpty())
            assertEquals(listOf(MockContents.kjvDownloadedTranslationInfo), translationManager.observeDownloadedTranslations().first())
        }
    }

    @Test
    fun testDownloadTranslation() {
        runBlocking {
            val expectedAvailable = emptyList<TranslationInfo>()
            val expectedDownloaded = listOf(MockContents.kjvDownloadedTranslationInfo)

            val channel = Channel<Int>()
            launch {
                translationManager.downloadTranslation(channel, MockContents.kjvTranslationInfo)
            }
            var called = false
            channel.onEach { called = true }
            assertTrue(called)

            val actualAvailable = translationManager.observeAvailableTranslations().first()
            val actualDownloaded = translationManager.observeDownloadedTranslations().first()

            assertEquals(expectedAvailable, actualAvailable)
            assertEquals(expectedDownloaded, actualDownloaded)
        }
    }

    @Test
    fun testDownloadTranslationThenReload() {
        runBlocking {
            val expectedAvailable = emptyList<TranslationInfo>()
            val expectedDownloaded = listOf(MockContents.kjvDownloadedTranslationInfo)

            val channel = Channel<Int>()
            launch {
                translationManager.downloadTranslation(channel, MockContents.kjvTranslationInfo)
            }
            var called = false
            channel.onEach { called = true }
            assertTrue(called)

            translationManager.reload(false)
            val actualAvailable = translationManager.observeAvailableTranslations().first()
            val actualDownloaded = translationManager.observeDownloadedTranslations().first()

            assertEquals(expectedAvailable, actualAvailable)
            assertEquals(expectedDownloaded, actualDownloaded)
        }
    }

    @Test
    fun testDownloadTranslationThenReloadWithForceRefresh() {
        runBlocking {
            val expectedAvailable = emptyList<TranslationInfo>()
            val expectedDownloaded = listOf(MockContents.kjvDownloadedTranslationInfo)

            val channel = Channel<Int>()
            launch {
                translationManager.downloadTranslation(channel, MockContents.kjvTranslationInfo)
            }
            var called = false
            channel.onEach { called = true }
            assertTrue(called)

            translationManager.reload(true)
            val actualAvailable = translationManager.observeAvailableTranslations().first()
            val actualDownloaded = translationManager.observeDownloadedTranslations().first()

            assertEquals(expectedAvailable, actualAvailable)
            assertEquals(expectedDownloaded, actualDownloaded)
        }
    }

    @Test
    fun testRemoveNonExistTranslation() {
        runBlocking {
            translationManager.removeTranslation(TranslationInfo("non_exist", "name", "language", 12345L, false))
            assertTrue(translationManager.observeAvailableTranslations().first().isEmpty())
            assertTrue(translationManager.observeDownloadedTranslations().first().isEmpty())
        }
    }

    @Test
    fun testDownloadThenRemove() {
        runBlocking {
            val channel = Channel<Int>(Channel.UNLIMITED)
            translationManager.downloadTranslation(channel, MockContents.kjvTranslationInfo)
            assertTrue(translationManager.observeAvailableTranslations().first().isEmpty())
            assertEquals(listOf(MockContents.kjvDownloadedTranslationInfo), translationManager.observeDownloadedTranslations().first())

            translationManager.removeTranslation(MockContents.kjvTranslationInfo)
            assertEquals(listOf(MockContents.kjvTranslationInfo), translationManager.observeAvailableTranslations().first())
            assertTrue(translationManager.observeDownloadedTranslations().first().isEmpty())
        }
    }

    @Test
    fun testUpdateTranslations() {
        runBlocking {
            translationManager.updateTranslations(listOf(MockContents.kjvTranslationInfo,
                    MockContents.kjvDownloadedTranslationInfo, MockContents.kjvDownloadedTranslationInfo,
                    MockContents.kjvTranslationInfo, MockContents.kjvTranslationInfo,
                    MockContents.kjvDownloadedTranslationInfo))
            assertEquals(listOf(MockContents.kjvDownloadedTranslationInfo), translationManager.observeDownloadedTranslations().first())
            assertEquals(listOf(MockContents.kjvTranslationInfo), translationManager.observeAvailableTranslations().first())
        }
    }

    @Test
    fun testNotifyTranslationsUpdated() {
        runBlocking {
            withTimeout(5000L) {
                var availableUpdated = 0
                val availableJob = launch(Dispatchers.Unconfined) {
                    val availableReceiver = translationManager.observeAvailableTranslations()
                    availableReceiver.onEach {
                        if (++availableUpdated == 3) {
                            availableReceiver.cancel()
                        }
                    }
                }

                var downloadedUpdated = 0
                val downloadedJob = launch(Dispatchers.Unconfined) {
                    val downloadedReceiver = translationManager.observeDownloadedTranslations()
                    downloadedReceiver.onEach {
                        if (++downloadedUpdated == 4) {
                            downloadedReceiver.cancel()
                        }
                    }
                }

                translationManager.notifyTranslationsUpdated(emptyList(), emptyList())
                translationManager.notifyTranslationsUpdated(emptyList(), emptyList())
                translationManager.notifyTranslationsUpdated(emptyList(), emptyList())

                translationManager.notifyTranslationsUpdated(
                        listOf(MockContents.kjvTranslationInfo), listOf(MockContents.kjvDownloadedTranslationInfo))
                translationManager.notifyTranslationsUpdated(
                        listOf(MockContents.kjvTranslationInfo), listOf(MockContents.kjvDownloadedTranslationInfo))

                translationManager.notifyTranslationsUpdated(listOf(MockContents.kjvTranslationInfo), emptyList())
                translationManager.notifyTranslationsUpdated(listOf(MockContents.kjvTranslationInfo), emptyList())

                translationManager.notifyTranslationsUpdated(emptyList(), listOf(MockContents.kjvDownloadedTranslationInfo))

                availableJob.join()
                downloadedJob.join()
            }
        }
    }
}

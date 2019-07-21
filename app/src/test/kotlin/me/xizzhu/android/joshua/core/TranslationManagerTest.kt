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
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.first
import me.xizzhu.android.joshua.core.repository.TranslationRepository
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TranslationManagerTest : BaseUnitTest() {
    @Mock
    private lateinit var translationRepository: TranslationRepository

    private lateinit var translationManager: TranslationManager

    @Before
    override fun setup() {
        super.setup()

        runBlocking {
            `when`(translationRepository.readTranslationsFromLocal()).thenReturn(emptyList())
            translationManager = TranslationManager(translationRepository)
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
                    availableReceiver.consumeEach {
                        if (++availableUpdated == 3) {
                            availableReceiver.cancel()
                        }
                    }
                }

                var downloadedUpdated = 0
                val downloadedJob = launch(Dispatchers.Unconfined) {
                    val downloadedReceiver = translationManager.observeDownloadedTranslations()
                    downloadedReceiver.consumeEach {
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

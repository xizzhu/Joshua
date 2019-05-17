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
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.core.repository.local.LocalTranslationStorage
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslation
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslationInfo
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslationService
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.tests.toMap
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TranslationRepositoryTest : BaseUnitTest() {
    @Mock
    private lateinit var localTranslationStorage: LocalTranslationStorage
    @Mock
    private lateinit var remoteTranslationService: RemoteTranslationService

    private lateinit var translationRepository: TranslationRepository

    @Before
    override fun setup() {
        super.setup()

        translationRepository = spy(TranslationRepository(localTranslationStorage, remoteTranslationService))
    }

    @Test
    fun testForceReload() {
        runBlocking {
            doReturn(listOf(MockContents.kjvTranslationInfo))
                    .`when`(translationRepository).readTranslationsFromBackend()

            val expected = listOf(MockContents.kjvTranslationInfo)
            val actual = translationRepository.reload(true)
            assertEquals(expected, actual)
            verify(translationRepository, times(1)).readTranslationsFromBackend()
            verify(translationRepository, never()).readTranslationsFromLocal()
        }
    }

    @Test
    fun testReloadWithTooOldTranslationList() {
        runBlocking {
            doReturn(true).`when`(translationRepository).translationListTooOld()
            doReturn(listOf(MockContents.kjvTranslationInfo))
                    .`when`(translationRepository).readTranslationsFromBackend()

            val expected = listOf(MockContents.kjvTranslationInfo)
            val actual = translationRepository.reload(false)
            assertEquals(expected, actual)
            verify(translationRepository, times(1)).readTranslationsFromBackend()
            verify(translationRepository, never()).readTranslationsFromLocal()
        }
    }

    @Test
    fun testReloadWithTooOldTranslationListFailedBackend() {
        runBlocking {
            doReturn(true).`when`(translationRepository).translationListTooOld()
            doThrow(RuntimeException("Random exception"))
                    .`when`(translationRepository).readTranslationsFromBackend()
            doReturn(listOf(MockContents.kjvDownloadedTranslationInfo))
                    .`when`(translationRepository).readTranslationsFromLocal()

            val expected = listOf(MockContents.kjvDownloadedTranslationInfo)
            val actual = translationRepository.reload(false)
            assertEquals(expected, actual)
            verify(translationRepository, times(1)).readTranslationsFromBackend()
            verify(translationRepository, times(1)).readTranslationsFromLocal()
        }
    }

    @Test
    fun testReloadWithFreshLocalTranslationList() {
        runBlocking {
            doReturn(false).`when`(translationRepository).translationListTooOld()
            doReturn(listOf(MockContents.kjvTranslationInfo))
                    .`when`(translationRepository).readTranslationsFromLocal()

            val expected = listOf(MockContents.kjvTranslationInfo)
            val actual = translationRepository.reload(false)
            assertEquals(expected, actual)
            verify(translationRepository, never()).readTranslationsFromBackend()
            verify(translationRepository, times(1)).readTranslationsFromLocal()
        }
    }

    @Test
    fun testReloadWithFreshButEmptyLocalTranslationList() {
        runBlocking {
            doReturn(false).`when`(translationRepository).translationListTooOld()
            doReturn(emptyList<TranslationInfo>()).`when`(translationRepository).readTranslationsFromLocal()
            doReturn(listOf(MockContents.kjvTranslationInfo))
                    .`when`(translationRepository).readTranslationsFromBackend()

            val expected = listOf(MockContents.kjvTranslationInfo)
            val actual = translationRepository.reload(false)
            assertEquals(expected, actual)
            verify(translationRepository, times(1)).readTranslationsFromBackend()
            verify(translationRepository, times(1)).readTranslationsFromLocal()
        }
    }

    @Test
    fun testReadTranslationsFromBackend() {
        runBlocking {
            `when`(remoteTranslationService.fetchTranslations()).thenReturn(
                    listOf(RemoteTranslationInfo.fromTranslationInfo(MockContents.kjvTranslationInfo),
                            RemoteTranslationInfo.fromTranslationInfo(MockContents.cuvTranslationInfo)))
            doReturn(listOf(MockContents.kjvDownloadedTranslationInfo))
                    .`when`(translationRepository).readTranslationsFromLocal()

            val expected = listOf(MockContents.kjvDownloadedTranslationInfo, MockContents.cuvTranslationInfo)
            val actual = translationRepository.readTranslationsFromBackend()
            assertEquals(expected, actual)
            verify(localTranslationStorage, times(1)).replaceTranslations(expected)
            verify(localTranslationStorage, times(1)).saveTranslationListRefreshTimestamp(anyLong())
        }
    }

    @Test
    fun testReadTranslationsFromBackendWithEmptyList() {
        runBlocking {
            `when`(remoteTranslationService.fetchTranslations()).thenReturn(listOf())
            doReturn(emptyList<TranslationInfo>()).`when`(translationRepository).readTranslationsFromLocal()

            assertTrue(translationRepository.readTranslationsFromBackend().isEmpty())
            verify(localTranslationStorage, times(1)).replaceTranslations(emptyList())
            verify(localTranslationStorage, never()).saveTranslationListRefreshTimestamp(anyLong())
        }
    }

    @Test
    fun testReadTranslationsFromBackendFailedToSaveRefreshTimestamp() {
        runBlocking {
            `when`(remoteTranslationService.fetchTranslations()).thenReturn(
                    listOf(RemoteTranslationInfo.fromTranslationInfo(MockContents.kjvTranslationInfo),
                            RemoteTranslationInfo.fromTranslationInfo(MockContents.cuvTranslationInfo)))
            doReturn(listOf(MockContents.kjvDownloadedTranslationInfo))
                    .`when`(translationRepository).readTranslationsFromLocal()
            doThrow(RuntimeException("Random exception"))
                    .`when`(localTranslationStorage).saveTranslationListRefreshTimestamp(anyLong())

            val expected = listOf(MockContents.kjvDownloadedTranslationInfo, MockContents.cuvTranslationInfo)
            val actual = translationRepository.readTranslationsFromBackend()
            assertEquals(expected, actual)
            verify(localTranslationStorage, times(1)).replaceTranslations(expected)
            verify(localTranslationStorage, times(1)).saveTranslationListRefreshTimestamp(anyLong())
        }
    }

    @Test
    fun testDownloadTranslation() {
        runBlocking {
            doReturn(0L).`when`(translationRepository).elapsedRealtime()
            doReturn(null).`when`(translationRepository).buildParams(any(), anyLong(), anyLong(), anyLong())

            val channel = Channel<Int>()
            `when`(remoteTranslationService.fetchTranslation(channel, RemoteTranslationInfo.fromTranslationInfo(MockContents.kjvTranslationInfo)))
                    .thenReturn(RemoteTranslation(RemoteTranslationInfo.fromTranslationInfo(MockContents.kjvTranslationInfo),
                            MockContents.kjvBookNames, MockContents.kjvBookShortNames, MockContents.kjvVerses.toMap()))

            launch {
                translationRepository.downloadTranslation(channel, MockContents.kjvTranslationInfo)
                channel.close()
            }

            var called = false
            channel.consumeEach {
                if (it == 100) {
                    called = true
                }
            }
            assertTrue(called)
            verify(localTranslationStorage, times(1))
                    .saveTranslation(MockContents.kjvDownloadedTranslationInfo, MockContents.kjvBookNames,
                            MockContents.kjvBookShortNames, MockContents.kjvVerses.toMap())
        }
    }
}

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

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
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
import me.xizzhu.android.joshua.utils.currentTimeMillis
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.*

class TranslationRepositoryTest : BaseUnitTest() {
    @Mock
    private lateinit var localTranslationStorage: LocalTranslationStorage

    @Mock
    private lateinit var remoteTranslationService: RemoteTranslationService

    @Test
    fun testObserveInitialTranslations() = runBlocking {
        `when`(localTranslationStorage.readTranslations())
                .thenReturn(listOf(MockContents.kjvDownloadedTranslationInfo, MockContents.cuvTranslationInfo))
        val translationRepository = TranslationRepository(localTranslationStorage, remoteTranslationService, testDispatcher)

        assertEquals(listOf(MockContents.kjvDownloadedTranslationInfo),
                translationRepository.downloadedTranslations.first())
        assertEquals(listOf(MockContents.cuvTranslationInfo),
                translationRepository.availableTranslations.first())
    }

    @Test
    fun testObserveInitialTranslationsWithException() = runBlocking {
        `when`(localTranslationStorage.readTranslations()).thenThrow(RuntimeException("Random exception"))
        val translationRepository = TranslationRepository(localTranslationStorage, remoteTranslationService, testDispatcher)

        assertTrue(translationRepository.downloadedTranslations.first().isEmpty())
        assertTrue(translationRepository.availableTranslations.first().isEmpty())
    }

    @Test
    fun testUpdateTranslations() = runBlocking {
        `when`(localTranslationStorage.readTranslations()).thenReturn(emptyList())
        val translationRepository = TranslationRepository(localTranslationStorage, remoteTranslationService, testDispatcher)

        assertTrue(translationRepository.downloadedTranslations.first().isEmpty())
        assertTrue(translationRepository.availableTranslations.first().isEmpty())

        translationRepository.updateTranslations(listOf(MockContents.kjvTranslationInfo,
                MockContents.kjvDownloadedTranslationInfo, MockContents.kjvDownloadedTranslationInfo,
                MockContents.kjvTranslationInfo, MockContents.kjvTranslationInfo,
                MockContents.kjvDownloadedTranslationInfo))

        assertEquals(listOf(MockContents.kjvDownloadedTranslationInfo), translationRepository.downloadedTranslations.first())
        assertEquals(listOf(MockContents.kjvTranslationInfo), translationRepository.availableTranslations.first())
    }

    @Test
    fun testForceReload() {
        runBlocking {
            `when`(localTranslationStorage.readTranslations()).thenReturn(emptyList())
            val translationRepository = spy(TranslationRepository(localTranslationStorage, remoteTranslationService, testDispatcher))
            doReturn(listOf(MockContents.kjvTranslationInfo)).`when`(translationRepository).readTranslationsFromBackend()

            translationRepository.reload(true)
            assertTrue(translationRepository.downloadedTranslations.first().isEmpty())
            assertEquals(listOf(MockContents.kjvTranslationInfo), translationRepository.availableTranslations.first())

            verify(translationRepository, times(1)).readTranslationsFromBackend()
            verify(translationRepository, never()).readTranslationsFromLocal()
        }
    }

    @Test(expected = IllegalStateException::class)
    fun testReloadWithEmptyList() = runBlocking {
        `when`(localTranslationStorage.readTranslations()).thenReturn(emptyList())
        val translationRepository = spy(TranslationRepository(localTranslationStorage, remoteTranslationService, testDispatcher))
        doReturn(emptyList<TranslationInfo>()).`when`(translationRepository).readTranslationsFromBackend()

        translationRepository.reload(true)
    }

    @Test
    fun testReloadWithTooOldTranslationList() {
        runBlocking {
            `when`(localTranslationStorage.readTranslations()).thenReturn(emptyList())
            val translationRepository = spy(TranslationRepository(localTranslationStorage, remoteTranslationService, testDispatcher))
            doReturn(true).`when`(translationRepository).translationListTooOld()
            doReturn(listOf(MockContents.kjvTranslationInfo)).`when`(translationRepository).readTranslationsFromBackend()

            translationRepository.reload(false)
            assertTrue(translationRepository.downloadedTranslations.first().isEmpty())
            assertEquals(listOf(MockContents.kjvTranslationInfo), translationRepository.availableTranslations.first())

            verify(translationRepository, times(1)).readTranslationsFromBackend()
            verify(translationRepository, never()).readTranslationsFromLocal()
        }
    }

    @Test
    fun testReloadWithTooOldTranslationListFailedBackend() {
        runBlocking {
            `when`(localTranslationStorage.readTranslations()).thenReturn(listOf(MockContents.kjvDownloadedTranslationInfo))
            val translationRepository = spy(TranslationRepository(localTranslationStorage, remoteTranslationService, testDispatcher))
            doReturn(true).`when`(translationRepository).translationListTooOld()
            doThrow(RuntimeException("Random exception")).`when`(translationRepository).readTranslationsFromBackend()

            translationRepository.reload(false)
            assertEquals(listOf(MockContents.kjvDownloadedTranslationInfo), translationRepository.downloadedTranslations.first())
            assertTrue(translationRepository.availableTranslations.first().isEmpty())

            verify(translationRepository, times(1)).readTranslationsFromBackend()
            verify(translationRepository, times(1)).readTranslationsFromLocal()
        }
    }

    @Test
    fun testReloadWithFreshLocalTranslationList() {
        runBlocking {
            `when`(localTranslationStorage.readTranslations()).thenReturn(listOf(MockContents.kjvTranslationInfo))
            val translationRepository = spy(TranslationRepository(localTranslationStorage, remoteTranslationService, testDispatcher))
            doReturn(false).`when`(translationRepository).translationListTooOld()

            translationRepository.reload(false)
            assertTrue(translationRepository.downloadedTranslations.first().isEmpty())
            assertEquals(listOf(MockContents.kjvTranslationInfo), translationRepository.availableTranslations.first())

            verify(translationRepository, never()).readTranslationsFromBackend()
            verify(translationRepository, times(1)).readTranslationsFromLocal()
        }
    }

    @Test
    fun testReloadWithFreshButEmptyLocalTranslationList() {
        runBlocking {
            `when`(localTranslationStorage.readTranslations()).thenReturn(emptyList())
            val translationRepository = spy(TranslationRepository(localTranslationStorage, remoteTranslationService, testDispatcher))
            doReturn(false).`when`(translationRepository).translationListTooOld()
            doReturn(listOf(MockContents.kjvTranslationInfo)).`when`(translationRepository).readTranslationsFromBackend()

            translationRepository.reload(false)
            assertTrue(translationRepository.downloadedTranslations.first().isEmpty())
            assertEquals(listOf(MockContents.kjvTranslationInfo), translationRepository.availableTranslations.first())

            verify(translationRepository, times(1)).readTranslationsFromBackend()
            verify(translationRepository, times(1)).readTranslationsFromLocal()
        }
    }

    @Test
    fun testTranslationListTooOld() = runBlocking {
        `when`(localTranslationStorage.readTranslationListRefreshTimestamp()).thenReturn(0L)
        `when`(localTranslationStorage.readTranslations()).thenReturn(emptyList())
        val translationRepository = TranslationRepository(localTranslationStorage, remoteTranslationService, testDispatcher)

        currentTimeMillis = 0L
        assertFalse(translationRepository.translationListTooOld())

        currentTimeMillis = TranslationRepository.TRANSLATION_LIST_REFRESH_INTERVAL_IN_MILLIS - 1L
        assertFalse(translationRepository.translationListTooOld())

        currentTimeMillis = TranslationRepository.TRANSLATION_LIST_REFRESH_INTERVAL_IN_MILLIS
        assertTrue(translationRepository.translationListTooOld())

        currentTimeMillis = TranslationRepository.TRANSLATION_LIST_REFRESH_INTERVAL_IN_MILLIS + 1L
        assertTrue(translationRepository.translationListTooOld())
    }

    @Test
    fun testReadTranslationsFromBackend() = runBlocking {
        currentTimeMillis = 12345L
        `when`(localTranslationStorage.readTranslations()).thenReturn(listOf(MockContents.kjvDownloadedTranslationInfo))
        `when`(remoteTranslationService.fetchTranslations()).thenReturn(
                listOf(RemoteTranslationInfo.fromTranslationInfo(MockContents.kjvTranslationInfo),
                        RemoteTranslationInfo.fromTranslationInfo(MockContents.cuvTranslationInfo)))
        val translationRepository = TranslationRepository(localTranslationStorage, remoteTranslationService, testDispatcher)

        val expected = listOf(MockContents.kjvDownloadedTranslationInfo, MockContents.cuvTranslationInfo)
        val actual = translationRepository.readTranslationsFromBackend()
        assertEquals(expected, actual)
        verify(localTranslationStorage, times(1)).replaceTranslations(expected)
        verify(localTranslationStorage, times(1)).saveTranslationListRefreshTimestamp(12345L)
    }

    @Test
    fun testReadTranslationsFromBackendWithEmptyList() = runBlocking {
        currentTimeMillis = 12345L
        `when`(localTranslationStorage.readTranslations()).thenReturn(emptyList())
        `when`(remoteTranslationService.fetchTranslations()).thenReturn(emptyList())
        val translationRepository = TranslationRepository(localTranslationStorage, remoteTranslationService, testDispatcher)

        assertTrue(translationRepository.readTranslationsFromBackend().isEmpty())
        verify(localTranslationStorage, times(1)).replaceTranslations(emptyList())
        verify(localTranslationStorage, never()).saveTranslationListRefreshTimestamp(12345L)
    }

    @Test
    fun testReadTranslationsFromBackendFailedToSaveRefreshTimestamp() = runBlocking {
        currentTimeMillis = 12345L
        `when`(localTranslationStorage.readTranslations()).thenReturn(listOf(MockContents.kjvDownloadedTranslationInfo))
        `when`(localTranslationStorage.saveTranslationListRefreshTimestamp(12345L)).thenThrow(RuntimeException("Random exception"))
        `when`(remoteTranslationService.fetchTranslations()).thenReturn(
                listOf(RemoteTranslationInfo.fromTranslationInfo(MockContents.kjvTranslationInfo),
                        RemoteTranslationInfo.fromTranslationInfo(MockContents.cuvTranslationInfo)))
        val translationRepository = TranslationRepository(localTranslationStorage, remoteTranslationService, testDispatcher)

        val expected = listOf(MockContents.kjvDownloadedTranslationInfo, MockContents.cuvTranslationInfo)
        val actual = translationRepository.readTranslationsFromBackend()
        assertEquals(expected, actual)
        verify(localTranslationStorage, times(1)).replaceTranslations(expected)
        verify(localTranslationStorage, times(1)).saveTranslationListRefreshTimestamp(12345L)
    }

    @Test
    fun testDownloadedTranslation() = runBlocking {
        `when`(localTranslationStorage.readTranslations()).thenReturn(emptyList())
        val translationRepository = spy(TranslationRepository(localTranslationStorage, remoteTranslationService, testDispatcher))
        doAnswer { Unit }.`when`(translationRepository).downloadTranslation(any(), any())

        translationRepository.updateTranslations(listOf(MockContents.cuvTranslationInfo, MockContents.kjvTranslationInfo))
        assertTrue(translationRepository.downloadedTranslations.first().isEmpty())
        assertEquals(setOf(MockContents.cuvTranslationInfo, MockContents.kjvTranslationInfo), translationRepository.availableTranslations.first().toSet())

        assertTrue(translationRepository.downloadTranslation(MockContents.kjvTranslationInfo).toList().isEmpty())
        assertEquals(listOf(MockContents.cuvTranslationInfo), translationRepository.availableTranslations.first())
        assertEquals(listOf(MockContents.kjvDownloadedTranslationInfo), translationRepository.downloadedTranslations.first())
    }

    @Test
    fun testDownloadTranslationInternal() = runBlocking {
        `when`(localTranslationStorage.readTranslations()).thenReturn(listOf(MockContents.kjvDownloadedTranslationInfo))

        val channel = Channel<Int>()
        `when`(remoteTranslationService.fetchTranslation(channel, RemoteTranslationInfo.fromTranslationInfo(MockContents.kjvTranslationInfo)))
                .thenReturn(RemoteTranslation(RemoteTranslationInfo.fromTranslationInfo(MockContents.kjvTranslationInfo),
                        MockContents.kjvBookNames, MockContents.kjvBookShortNames, MockContents.kjvVerses.toMap()))

        val translationRepository = TranslationRepository(localTranslationStorage, remoteTranslationService, testDispatcher)

        launch {
            translationRepository.downloadTranslation(channel, MockContents.kjvTranslationInfo)
            channel.close()
        }

        var called = false
        channel.consumeEach { if (it == 100) called = true }
        assertTrue(called)
        verify(localTranslationStorage, times(1))
                .saveTranslation(MockContents.kjvDownloadedTranslationInfo, MockContents.kjvBookNames,
                        MockContents.kjvBookShortNames, MockContents.kjvVerses.toMap())
    }

    @Test
    fun testRemoveNonExistTranslation() = runBlocking {
        `when`(localTranslationStorage.readTranslations()).thenReturn(emptyList())
        val translationRepository = TranslationRepository(localTranslationStorage, remoteTranslationService, testDispatcher)

        assertTrue(translationRepository.downloadedTranslations.first().isEmpty())
        assertTrue(translationRepository.availableTranslations.first().isEmpty())

        translationRepository.removeTranslation(TranslationInfo("non_exist", "name", "language", 12345L, false))

        assertTrue(translationRepository.availableTranslations.first().isEmpty())
        assertTrue(translationRepository.downloadedTranslations.first().isEmpty())
    }

    @Test
    fun testRemoveTranslation() = runBlocking {
        `when`(localTranslationStorage.readTranslations()).thenReturn(emptyList())
        val translationRepository = TranslationRepository(localTranslationStorage, remoteTranslationService, testDispatcher)

        translationRepository.updateTranslations(listOf(MockContents.cuvTranslationInfo, MockContents.kjvDownloadedTranslationInfo))
        assertEquals(listOf(MockContents.cuvTranslationInfo), translationRepository.availableTranslations.first())
        assertEquals(listOf(MockContents.kjvDownloadedTranslationInfo), translationRepository.downloadedTranslations.first())

        translationRepository.removeTranslation(MockContents.kjvDownloadedTranslationInfo)
        assertEquals(setOf(MockContents.kjvTranslationInfo, MockContents.cuvTranslationInfo), translationRepository.availableTranslations.first().toSet())
        assertTrue(translationRepository.downloadedTranslations.first().isEmpty())
    }
}

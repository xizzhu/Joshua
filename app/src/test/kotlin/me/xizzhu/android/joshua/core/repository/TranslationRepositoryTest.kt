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

package me.xizzhu.android.joshua.core.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.core.repository.local.LocalTranslationStorage
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslation
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslationInfo
import me.xizzhu.android.joshua.core.repository.remote.RemoteTranslationService
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.tests.toMap
import me.xizzhu.android.joshua.utils.currentTimeMillis
import kotlin.test.*

class TranslationRepositoryTest : BaseUnitTest() {
    private lateinit var localTranslationStorage: LocalTranslationStorage
    private lateinit var remoteTranslationService: RemoteTranslationService

    @BeforeTest
    override fun setup() {
        super.setup()

        localTranslationStorage = mockk()
        coEvery { localTranslationStorage.readTranslations() } returns emptyList()

        remoteTranslationService = mockk()
    }

    @Test
    fun `test loading translations by constructor`() = runTest {
        coEvery { localTranslationStorage.readTranslations() } returns listOf(
                MockContents.kjvDownloadedTranslationInfo, MockContents.cuvTranslationInfo
        )
        val translationRepository = TranslationRepository(localTranslationStorage, remoteTranslationService, testDispatcher)

        assertEquals(listOf(MockContents.kjvDownloadedTranslationInfo), translationRepository.downloadedTranslations.first())
        assertEquals(listOf(MockContents.cuvTranslationInfo), translationRepository.availableTranslations.first())
    }

    @Test
    fun `test loading translations by constructor with exception`() = runTest {
        coEvery { localTranslationStorage.readTranslations() } throws RuntimeException("Random exception")
        val translationRepository = TranslationRepository(localTranslationStorage, remoteTranslationService, testDispatcher)

        assertTrue(translationRepository.downloadedTranslations.first().isEmpty())
        assertTrue(translationRepository.availableTranslations.first().isEmpty())
    }

    @Test
    fun `test updateTranslations()`() = runTest {
        val translationRepository = TranslationRepository(localTranslationStorage, remoteTranslationService, testDispatcher)

        assertTrue(translationRepository.downloadedTranslations.first().isEmpty())
        assertTrue(translationRepository.availableTranslations.first().isEmpty())

        translationRepository.updateTranslations(listOf(
                MockContents.kjvTranslationInfo, MockContents.kjvDownloadedTranslationInfo, MockContents.kjvDownloadedTranslationInfo,
                MockContents.kjvTranslationInfo, MockContents.kjvTranslationInfo, MockContents.kjvDownloadedTranslationInfo
        ))

        assertEquals(listOf(MockContents.kjvDownloadedTranslationInfo), translationRepository.downloadedTranslations.first())
        assertEquals(listOf(MockContents.kjvTranslationInfo), translationRepository.availableTranslations.first())
    }

    @Test
    fun `test reload() with force refresh`() = runTest {
        val translationRepository = spyk(TranslationRepository(localTranslationStorage, remoteTranslationService, testDispatcher))
        coEvery { translationRepository.readTranslationsFromBackend() } returns listOf(MockContents.kjvTranslationInfo)

        translationRepository.reload(true)
        assertTrue(translationRepository.downloadedTranslations.first().isEmpty())
        assertEquals(listOf(MockContents.kjvTranslationInfo), translationRepository.availableTranslations.first())

        coVerify(exactly = 1) { translationRepository.readTranslationsFromBackend() }
        coVerify(exactly = 0) { translationRepository.readTranslationsFromLocal() }
    }

    @Test
    fun `test reload() with force refresh with empty translations list`(): Unit = runTest {
        val translationRepository = spyk(TranslationRepository(localTranslationStorage, remoteTranslationService, testDispatcher))
        coEvery { translationRepository.readTranslationsFromBackend() } returns emptyList()

        translationRepository.reload(true)
        assertTrue(translationRepository.downloadedTranslations.first().isEmpty())
        assertTrue(translationRepository.availableTranslations.first().isEmpty())
    }

    @Test
    fun `test reload() with too old translations list`() = runTest {
        val translationRepository = spyk(TranslationRepository(localTranslationStorage, remoteTranslationService, testDispatcher))
        coEvery { translationRepository.translationListTooOld() } returns true
        coEvery { translationRepository.readTranslationsFromBackend() } returns listOf(MockContents.kjvTranslationInfo)

        translationRepository.reload(false)
        assertTrue(translationRepository.downloadedTranslations.first().isEmpty())
        assertEquals(listOf(MockContents.kjvTranslationInfo), translationRepository.availableTranslations.first())

        coVerify(exactly = 1) { translationRepository.readTranslationsFromBackend() }
        coVerify(exactly = 0) { translationRepository.readTranslationsFromLocal() }
    }

    @Test
    fun `test reload() with empty translations list and error from backend`() = runTest {
        coEvery { localTranslationStorage.readTranslations() } returns listOf(MockContents.kjvDownloadedTranslationInfo)

        val translationRepository = spyk(TranslationRepository(localTranslationStorage, remoteTranslationService, testDispatcher))
        coEvery { translationRepository.translationListTooOld() } returns true
        coEvery { translationRepository.readTranslationsFromBackend() } throws RuntimeException("Random exception")

        translationRepository.reload(false)
        assertEquals(listOf(MockContents.kjvDownloadedTranslationInfo), translationRepository.downloadedTranslations.first())
        assertTrue(translationRepository.availableTranslations.first().isEmpty())

        coVerify(exactly = 1) {
            translationRepository.readTranslationsFromBackend()
            translationRepository.readTranslationsFromLocal()
        }
    }

    @Test
    fun `test reload() with refresh local translations list`() = runTest {
        coEvery { localTranslationStorage.readTranslations() } returns listOf(MockContents.kjvTranslationInfo)

        val translationRepository = spyk(TranslationRepository(localTranslationStorage, remoteTranslationService, testDispatcher))
        coEvery { translationRepository.translationListTooOld() } returns false

        translationRepository.reload(false)
        assertTrue(translationRepository.downloadedTranslations.first().isEmpty())
        assertEquals(listOf(MockContents.kjvTranslationInfo), translationRepository.availableTranslations.first())

        coVerify(exactly = 0) { translationRepository.readTranslationsFromBackend() }
        coVerify(exactly = 1) { translationRepository.readTranslationsFromLocal() }
    }

    @Test
    fun `test reload() with refresh but empty translations list`() = runTest {
        val translationRepository = spyk(TranslationRepository(localTranslationStorage, remoteTranslationService, testDispatcher))
        coEvery { translationRepository.translationListTooOld() } returns false
        coEvery { translationRepository.readTranslationsFromBackend() } returns listOf(MockContents.kjvTranslationInfo)

        translationRepository.reload(false)
        assertTrue(translationRepository.downloadedTranslations.first().isEmpty())
        assertEquals(listOf(MockContents.kjvTranslationInfo), translationRepository.availableTranslations.first())

        coVerify(exactly = 1) {
            translationRepository.readTranslationsFromBackend()
            translationRepository.readTranslationsFromLocal()
        }
    }

    @Test
    fun `test translationListTooOld()`() = runTest {
        coEvery { localTranslationStorage.readTranslationListRefreshTimestamp() } returns 0L

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
    fun `test readTranslationsFromBackend()`() = runTest {
        currentTimeMillis = 12345L
        coEvery { localTranslationStorage.readTranslations() } returns listOf(MockContents.kjvDownloadedTranslationInfo)
        coEvery { localTranslationStorage.replaceTranslations(any()) } returns Unit
        coEvery { remoteTranslationService.fetchTranslations() } returns listOf(
                RemoteTranslationInfo.fromTranslationInfo(MockContents.kjvTranslationInfo),
                RemoteTranslationInfo.fromTranslationInfo(MockContents.cuvTranslationInfo)
        )

        val translationRepository = TranslationRepository(localTranslationStorage, remoteTranslationService, testDispatcher)
        assertEquals(
                listOf(MockContents.kjvDownloadedTranslationInfo, MockContents.cuvTranslationInfo),
                translationRepository.readTranslationsFromBackend()
        )
        coVerify(exactly = 1) {
            localTranslationStorage.replaceTranslations(listOf(MockContents.kjvDownloadedTranslationInfo, MockContents.cuvTranslationInfo))
            localTranslationStorage.saveTranslationListRefreshTimestamp(12345L)

            remoteTranslationService.fetchTranslations()
        }
    }

    @Test
    fun `test readTranslationsFromBackend() with empty list`() = runTest {
        currentTimeMillis = 12345L
        coEvery { localTranslationStorage.replaceTranslations(emptyList()) } returns Unit
        coEvery { remoteTranslationService.fetchTranslations() } returns emptyList()

        val translationRepository = TranslationRepository(localTranslationStorage, remoteTranslationService, testDispatcher)
        assertTrue(translationRepository.readTranslationsFromBackend().isEmpty())
        coVerify(exactly = 1) {
            localTranslationStorage.replaceTranslations(emptyList())
            remoteTranslationService.fetchTranslations()
        }
        coVerify(exactly = 0) { localTranslationStorage.saveTranslationListRefreshTimestamp(12345L) }
    }

    @Test
    fun `test readTranslationsFromBackend() and failed to save refresh timestamp`() = runTest {
        currentTimeMillis = 12345L
        coEvery { localTranslationStorage.readTranslations() } returns listOf(MockContents.kjvDownloadedTranslationInfo)
        coEvery {
            localTranslationStorage.replaceTranslations(listOf(MockContents.kjvDownloadedTranslationInfo, MockContents.cuvTranslationInfo))
        } returns Unit
        coEvery { localTranslationStorage.saveTranslationListRefreshTimestamp(12345L) } throws RuntimeException("Random exception")
        coEvery { remoteTranslationService.fetchTranslations() } returns listOf(
                RemoteTranslationInfo.fromTranslationInfo(MockContents.kjvTranslationInfo),
                RemoteTranslationInfo.fromTranslationInfo(MockContents.cuvTranslationInfo)
        )

        val translationRepository = TranslationRepository(localTranslationStorage, remoteTranslationService, testDispatcher)
        assertEquals(
                listOf(MockContents.kjvDownloadedTranslationInfo, MockContents.cuvTranslationInfo),
                translationRepository.readTranslationsFromBackend()
        )
        coVerify(exactly = 1) {
            localTranslationStorage.replaceTranslations(listOf(MockContents.kjvDownloadedTranslationInfo, MockContents.cuvTranslationInfo))
            localTranslationStorage.saveTranslationListRefreshTimestamp(12345L)
        }
    }

    @Test
    fun `test downloadTranslation()`() = runTest {
        coEvery { localTranslationStorage.readTranslations() } returns listOf(MockContents.cuvTranslationInfo, MockContents.kjvTranslationInfo)

        val translationRepository = spyk(TranslationRepository(localTranslationStorage, remoteTranslationService, testDispatcher))
        coEvery { translationRepository.downloadTranslation(any(), MockContents.kjvTranslationInfo) } returns Unit

        assertTrue(translationRepository.downloadedTranslations.first().isEmpty())
        assertEquals(
                setOf(MockContents.cuvTranslationInfo, MockContents.kjvTranslationInfo),
                translationRepository.availableTranslations.first().toSet()
        )

        assertTrue(translationRepository.downloadTranslation(MockContents.kjvTranslationInfo).toList().isEmpty())
        assertEquals(listOf(MockContents.cuvTranslationInfo), translationRepository.availableTranslations.first())
        assertEquals(listOf(MockContents.kjvDownloadedTranslationInfo), translationRepository.downloadedTranslations.first())
        coVerify(exactly = 1) { translationRepository.downloadTranslation(any(), MockContents.kjvTranslationInfo) }
    }

    @Test
    fun `test downloadTranslation() internal`() = runTest {
        coEvery { localTranslationStorage.readTranslations() } returns listOf(MockContents.kjvDownloadedTranslationInfo)
        coEvery {
            localTranslationStorage.saveTranslation(
                    MockContents.kjvDownloadedTranslationInfo, MockContents.kjvBookNames,
                    MockContents.kjvBookShortNames, MockContents.kjvVerses.toMap()
            )
        } returns Unit

        val channel = Channel<Int>()
        coEvery {
            remoteTranslationService.fetchTranslation(channel, RemoteTranslationInfo.fromTranslationInfo(MockContents.kjvTranslationInfo))
        } returns RemoteTranslation(
                RemoteTranslationInfo.fromTranslationInfo(MockContents.kjvTranslationInfo),
                MockContents.kjvBookNames, MockContents.kjvBookShortNames, MockContents.kjvVerses.toMap()
        )

        val translationRepository = TranslationRepository(localTranslationStorage, remoteTranslationService, testDispatcher)

        launch {
            translationRepository.downloadTranslation(channel, MockContents.kjvTranslationInfo)
            channel.close()
        }

        var called = false
        channel.consumeEach { if (it == 100) called = true }
        assertTrue(called)
        coVerify(exactly = 1) {
            localTranslationStorage.saveTranslation(
                    MockContents.kjvDownloadedTranslationInfo, MockContents.kjvBookNames,
                    MockContents.kjvBookShortNames, MockContents.kjvVerses.toMap()
            )
        }
    }

    @Test
    fun `test removeTranslation() that does not exist`() = runTest {
        coEvery { localTranslationStorage.removeTranslation(any()) } returns Unit
        coEvery { remoteTranslationService.removeTranslationCache(any()) } returns Unit
        val translationRepository = TranslationRepository(localTranslationStorage, remoteTranslationService, testDispatcher)

        assertTrue(translationRepository.downloadedTranslations.first().isEmpty())
        assertTrue(translationRepository.availableTranslations.first().isEmpty())

        translationRepository.removeTranslation(TranslationInfo("non_exist", "name", "language", 12345L, false))
        assertTrue(translationRepository.availableTranslations.first().isEmpty())
        assertTrue(translationRepository.downloadedTranslations.first().isEmpty())
    }

    @Test
    fun `test removeTranslation()`() = runTest {
        coEvery { localTranslationStorage.readTranslations() } returns listOf(MockContents.cuvTranslationInfo, MockContents.kjvDownloadedTranslationInfo)
        coEvery { localTranslationStorage.removeTranslation(MockContents.kjvDownloadedTranslationInfo) } returns Unit
        coEvery { remoteTranslationService.removeTranslationCache(any()) } returns Unit

        val translationRepository = TranslationRepository(localTranslationStorage, remoteTranslationService, testDispatcher)
        assertEquals(listOf(MockContents.cuvTranslationInfo), translationRepository.availableTranslations.first())
        assertEquals(listOf(MockContents.kjvDownloadedTranslationInfo), translationRepository.downloadedTranslations.first())

        translationRepository.removeTranslation(MockContents.kjvDownloadedTranslationInfo)
        assertEquals(
                setOf(MockContents.kjvTranslationInfo, MockContents.cuvTranslationInfo),
                translationRepository.availableTranslations.first().toSet()
        )
        assertTrue(translationRepository.downloadedTranslations.first().isEmpty())
    }
}

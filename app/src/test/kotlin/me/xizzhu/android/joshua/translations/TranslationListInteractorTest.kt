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

package me.xizzhu.android.joshua.translations

import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.TranslationManager
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TranslationListInteractorTest : BaseUnitTest() {
    @Mock
    private lateinit var bibleReadingManager: BibleReadingManager
    @Mock
    private lateinit var translationManager: TranslationManager
    @Mock
    private lateinit var settingsManager: SettingsManager

    private lateinit var translationListInteractor: TranslationListInteractor

    @BeforeTest
    override fun setup() {
        super.setup()
        translationListInteractor = TranslationListInteractor(bibleReadingManager, translationManager, settingsManager, testDispatcher)
    }

    @Test
    fun testTranslationList() = testDispatcher.runBlockingTest {
        `when`(bibleReadingManager.observeCurrentTranslation()).thenReturn(flowOf(MockContents.kjvShortName))
        `when`(translationManager.observeAvailableTranslations()).thenReturn(flowOf(listOf(MockContents.cuvTranslationInfo)))
        `when`(translationManager.observeDownloadedTranslations()).thenReturn(flowOf(listOf(MockContents.kjvDownloadedTranslationInfo)))

        translationListInteractor.start()

        assertEquals(
                ViewData.success(
                        TranslationList(
                                MockContents.kjvShortName,
                                listOf(MockContents.cuvTranslationInfo),
                                listOf(MockContents.kjvDownloadedTranslationInfo)
                        )
                ),
                translationListInteractor.translationList().first()
        )

        translationListInteractor.stop()
    }

    @Test
    fun testLoadTranslationList() = testDispatcher.runBlockingTest {
        translationListInteractor.loadTranslationList(true)

        assertEquals(
                ViewData.loading(TranslationList.EMPTY),
                translationListInteractor.translationList().first()
        )
        verify(translationManager, times(1)).reload(true)
    }

    @Test
    fun testLoadTranslationListWithException() = testDispatcher.runBlockingTest {
        val translationListAsync = async { translationListInteractor.translationList().take(2).toList() }

        val exception = RuntimeException("random exception")
        `when`(translationManager.reload(true)).thenThrow(exception)
        translationListInteractor.loadTranslationList(true)

        assertEquals(
                listOf(ViewData.loading(TranslationList.EMPTY), ViewData.error(TranslationList.EMPTY, exception)),
                translationListAsync.await()
        )
    }

    @Test
    fun testDownloadTranslation() = testDispatcher.runBlockingTest {
        `when`(bibleReadingManager.observeCurrentTranslation()).thenReturn(flowOf(""))

        val translationDownloadAsync = async { translationListInteractor.translationDownload().take(3).toList() }

        val translationToDownload = MockContents.kjvTranslationInfo
        val downloadProgressChannel = Channel<Int>()
        translationListInteractor.downloadTranslation(translationToDownload, downloadProgressChannel)

        val progress = 89
        downloadProgressChannel.send(progress)
        downloadProgressChannel.close()

        // the loading progress is emitted in a separate coroutine, we can not guarantee the order
        assertEquals(
                setOf(
                        ViewData.loading(TranslationDownload(translationToDownload, 0)),
                        ViewData.loading(TranslationDownload(translationToDownload, progress)),
                        ViewData.success(TranslationDownload(translationToDownload, 0))
                ),
                translationDownloadAsync.await().toSet()
        )

        with(inOrder(translationManager, bibleReadingManager)) {
            verify(translationManager, times(1)).downloadTranslation(downloadProgressChannel, translationToDownload)
            verify(bibleReadingManager, times(1)).observeCurrentTranslation()
            verify(bibleReadingManager, times(1)).saveCurrentTranslation(translationToDownload.shortName)
        }
    }

    @Test
    fun testDownloadTranslationWithException() = testDispatcher.runBlockingTest {
        val exception = RuntimeException("random exception")
        `when`(translationManager.downloadTranslation(any(), any())).thenThrow(exception)

        val translationDownloadAsync = async { translationListInteractor.translationDownload().take(2).toList() }

        val translationToDownload = MockContents.kjvTranslationInfo
        translationListInteractor.downloadTranslation(translationToDownload)

        assertEquals(
                listOf(
                        ViewData.loading(TranslationDownload(translationToDownload, 0)),
                        ViewData.error(TranslationDownload(translationToDownload, 0))
                ),
                translationDownloadAsync.await()
        )
    }

    @Test
    fun testRemoveTranslation() = testDispatcher.runBlockingTest {
        val translationRemovalAsync = async { translationListInteractor.translationRemoval().take(2).toList() }

        val translationToRemove = MockContents.kjvDownloadedTranslationInfo
        translationListInteractor.removeTranslation(translationToRemove)

        assertEquals(
                listOf(ViewData.loading(translationToRemove), ViewData.success(translationToRemove)),
                translationRemovalAsync.await()
        )
        verify(translationManager, times(1)).removeTranslation(translationToRemove)
    }

    @Test
    fun testRemoveTranslationWithException() = testDispatcher.runBlockingTest {
        val translationRemovalAsync = async { translationListInteractor.translationRemoval().take(2).toList() }

        val translationToRemove = MockContents.kjvDownloadedTranslationInfo
        val exception = RuntimeException("random exception")
        `when`(translationManager.removeTranslation(translationToRemove)).thenThrow(exception)
        translationListInteractor.removeTranslation(translationToRemove)

        assertEquals(
                listOf(ViewData.loading(translationToRemove), ViewData.error(translationToRemove, exception)),
                translationRemovalAsync.await()
        )
    }
}

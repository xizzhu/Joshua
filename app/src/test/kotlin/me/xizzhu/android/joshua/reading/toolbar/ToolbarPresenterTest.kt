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

package me.xizzhu.android.joshua.reading.toolbar

import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.reading.ReadingInteractor
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*

class ToolbarPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var readingInteractor: ReadingInteractor
    @Mock
    private lateinit var toolbarView: ToolbarView

    private lateinit var toolbarPresenter: ToolbarPresenter
    private lateinit var currentTranslationChannel: ConflatedBroadcastChannel<String>
    private lateinit var currentVerseIndexChannel: ConflatedBroadcastChannel<VerseIndex>
    private lateinit var downloadedTranslationsChannel: ConflatedBroadcastChannel<List<TranslationInfo>>

    @Before
    override fun setup() {
        super.setup()

        runBlocking {
            currentTranslationChannel = ConflatedBroadcastChannel("")
            `when`(readingInteractor.observeCurrentTranslation()).thenReturn(currentTranslationChannel.asFlow())

            currentVerseIndexChannel = ConflatedBroadcastChannel(VerseIndex.INVALID)
            `when`(readingInteractor.observeCurrentVerseIndex()).thenReturn(currentVerseIndexChannel.asFlow())

            downloadedTranslationsChannel = ConflatedBroadcastChannel(emptyList())
            `when`(readingInteractor.observeDownloadedTranslations()).thenReturn(downloadedTranslationsChannel.asFlow())

            `when`(readingInteractor.observeParallelTranslations()).thenReturn(flow { emit(emptyList<String>()) })

            toolbarPresenter = ToolbarPresenter(readingInteractor)
            toolbarPresenter.attachView(toolbarView)
        }
    }

    @After
    override fun tearDown() {
        toolbarPresenter.detachView()
        super.tearDown()
    }

    @Test
    fun testObserveCurrentTranslation() {
        runBlocking {
            verify(toolbarView, never()).onCurrentTranslationUpdated(any())
            verify(toolbarView, never()).onBookShortNamesUpdated(any())

            `when`(readingInteractor.readBookShortNames(MockContents.kjvShortName)).thenReturn(MockContents.kjvBookShortNames)
            currentTranslationChannel.send(MockContents.kjvShortName)
            verify(toolbarView, times(1)).onCurrentTranslationUpdated(MockContents.kjvShortName)
            verify(toolbarView, times(1)).onBookShortNamesUpdated(MockContents.kjvBookShortNames)
        }
    }

    @Test
    fun testObserveCurrentVerseIndex() {
        runBlocking {
            verify(toolbarView, never()).onCurrentVerseIndexUpdated(any())

            val verseIndex = VerseIndex(1, 2, 3)
            currentVerseIndexChannel.send(verseIndex)
            verify(toolbarView, times(1)).onCurrentVerseIndexUpdated(verseIndex)
        }
    }

    @Test
    fun testObserveDownloadedTranslations() {
        runBlocking {
            verify(toolbarView, times(1)).onNoTranslationsDownloaded()

            downloadedTranslationsChannel.send(listOf(MockContents.kjvDownloadedTranslationInfo))
            verify(toolbarView, times(1))
                    .onDownloadedTranslationsLoaded(listOf(MockContents.kjvDownloadedTranslationInfo))
        }
    }

    @Test
    fun testUpdateCurrentTranslation() {
        runBlocking {
            `when`(readingInteractor.saveCurrentTranslation(MockContents.kjvShortName))
                    .then { runBlocking { currentTranslationChannel.send(MockContents.kjvShortName) } }

            toolbarPresenter.updateCurrentTranslation(MockContents.kjvShortName)
            verify(toolbarView, times(1)).onCurrentTranslationUpdated(MockContents.kjvShortName)
            verify(toolbarView, never()).onCurrentTranslationUpdateFailed(anyString())
        }
    }

    @Test
    fun testUpdateCurrentTranslationWithException() {
        runBlocking {
            `when`(readingInteractor.saveCurrentTranslation(MockContents.kjvShortName)).thenThrow(RuntimeException("Random exception"))

            toolbarPresenter.updateCurrentTranslation(MockContents.kjvShortName)
            verify(toolbarView, never()).onCurrentTranslationUpdated(anyString())
            verify(toolbarView, times(1)).onCurrentTranslationUpdateFailed(MockContents.kjvShortName)
        }
    }

    @Test
    fun testUpdateCurrentTranslationWithExceptionFromClearParallel() {
        runBlocking {
            `when`(readingInteractor.saveCurrentTranslation(MockContents.kjvShortName))
                    .then { runBlocking { currentTranslationChannel.send(MockContents.kjvShortName) } }
            `when`(readingInteractor.clearParallelTranslation())
                    .thenThrow(RuntimeException("Random exception"))

            toolbarPresenter.updateCurrentTranslation(MockContents.kjvShortName)
            verify(toolbarView, times(1)).onCurrentTranslationUpdated(MockContents.kjvShortName)
            verify(toolbarView, never()).onCurrentTranslationUpdateFailed(anyString())
        }
    }

    @Test
    fun testOpenTranslation() {
        toolbarPresenter.openTranslationManagement()
        verify(toolbarView, never()).onFailedToNavigateToTranslationManagement()
    }

    @Test
    fun testOpenTranslationWithException() {
        `when`(readingInteractor.openTranslationManagement()).thenThrow(RuntimeException("Random exception"))

        toolbarPresenter.openTranslationManagement()
        verify(toolbarView, times(1)).onFailedToNavigateToTranslationManagement()
    }

    @Test
    fun testOpenReadingProgress() {
        toolbarPresenter.openReadingProgress()
        verify(toolbarView, never()).onFailedToNavigateToReadingProgress()
    }

    @Test
    fun testOpenReadingProgressWithException() {
        `when`(readingInteractor.openReadingProgress()).thenThrow(RuntimeException("Random exception"))

        toolbarPresenter.openReadingProgress()
        verify(toolbarView, times(1)).onFailedToNavigateToReadingProgress()
    }

    @Test
    fun testOpenSettings() {
        toolbarPresenter.openSettings()
        verify(toolbarView, never()).onFailedToNavigateToSettings()
    }

    @Test
    fun testOpenSettingsWithException() {
        `when`(readingInteractor.openSettings()).thenThrow(RuntimeException("Random exception"))

        toolbarPresenter.openSettings()
        verify(toolbarView, times(1)).onFailedToNavigateToSettings()
    }
}

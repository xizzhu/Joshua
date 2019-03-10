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

        currentTranslationChannel = ConflatedBroadcastChannel("")
        `when`(readingInteractor.observeCurrentTranslation()).then { currentTranslationChannel.openSubscription() }

        currentVerseIndexChannel = ConflatedBroadcastChannel(VerseIndex.INVALID)
        `when`(readingInteractor.observeCurrentVerseIndex()).then { currentVerseIndexChannel.openSubscription() }

        downloadedTranslationsChannel = ConflatedBroadcastChannel(emptyList())
        `when`(readingInteractor.observeDownloadedTranslations()).then { downloadedTranslationsChannel.openSubscription() }

        toolbarPresenter = ToolbarPresenter(readingInteractor)
        toolbarPresenter.attachView(toolbarView)
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
            verify(toolbarView, never()).onBookNamesUpdated(any())

            `when`(readingInteractor.readBookNames(MockContents.kjvShortName)).thenReturn(MockContents.kjvBookNames)
            currentTranslationChannel.send(MockContents.kjvShortName)
            verify(toolbarView, times(1)).onCurrentTranslationUpdated(MockContents.kjvShortName)
            verify(toolbarView, times(1)).onBookNamesUpdated(MockContents.kjvBookNames)
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
            verify(toolbarView, never()).onCurrentTranslationUpdateFailed(MockContents.kjvShortName)
        }
    }

    @Test
    fun testUpdateCurrentTranslationWithException() {
        runBlocking {
            `when`(readingInteractor.saveCurrentTranslation(MockContents.kjvShortName)).thenThrow(RuntimeException("Random exception"))

            toolbarPresenter.updateCurrentTranslation(MockContents.kjvShortName)
            verify(toolbarView, never()).onCurrentTranslationUpdated(MockContents.kjvShortName)
            verify(toolbarView, times(1)).onCurrentTranslationUpdateFailed(MockContents.kjvShortName)
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
}

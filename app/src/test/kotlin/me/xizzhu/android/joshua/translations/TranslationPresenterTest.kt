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

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.TranslationInfo
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*

class TranslationPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var translationInteractor: TranslationInteractor
    @Mock
    private lateinit var translationView: TranslationView
    private lateinit var translationPresenter: TranslationPresenter
    private lateinit var translationLoadingStateChannel: ConflatedBroadcastChannel<Boolean>
    private lateinit var availableTranslationsChannel: ConflatedBroadcastChannel<List<TranslationInfo>>
    private lateinit var downloadedTranslationsChannel: ConflatedBroadcastChannel<List<TranslationInfo>>
    private lateinit var currentTranslationChannel: ConflatedBroadcastChannel<String>

    @Before
    override fun setUp() {
        super.setUp()

        translationLoadingStateChannel = ConflatedBroadcastChannel(true)
        `when`(translationInteractor.observeTranslationsLoadingState()).then { translationLoadingStateChannel.openSubscription() }

        availableTranslationsChannel = ConflatedBroadcastChannel(emptyList())
        `when`(translationInteractor.observeAvailableTranslations()).then { availableTranslationsChannel.openSubscription() }

        downloadedTranslationsChannel = ConflatedBroadcastChannel(emptyList())
        `when`(translationInteractor.observeDownloadedTranslations()).then { downloadedTranslationsChannel.openSubscription() }

        currentTranslationChannel = ConflatedBroadcastChannel("")
        `when`(translationInteractor.observeCurrentTranslation()).then { currentTranslationChannel.openSubscription() }

        translationPresenter = TranslationPresenter(translationInteractor)
    }

    @Test
    fun testObserveCurrentTranslation() {
        runBlocking {
            translationPresenter.attachView(translationView)
            verify(translationView, times(1)).onCurrentTranslationUpdated("")

            currentTranslationChannel.send(MockContents.kjvShortName)
            verify(translationView, times(1)).onCurrentTranslationUpdated(MockContents.kjvShortName)

            translationPresenter.detachView()
        }
    }

    @Test
    fun testObserveAvailableTranslations() {
        runBlocking {
            `when`(translationInteractor.reload(false)).then {
                runBlocking { availableTranslationsChannel.send(listOf(MockContents.cuvTranslationInfo)) }
            }

            translationPresenter.attachView(translationView)
            verify(translationView, times(1)).onAvailableTranslationsUpdated(emptyList())
            verify(translationView, times(1)).onAvailableTranslationsUpdated(listOf(MockContents.cuvTranslationInfo))

            translationPresenter.detachView()
        }
    }

    @Test
    fun testObserveDownloadedTranslations() {
        runBlocking {
            `when`(translationInteractor.reload(false)).then {
                runBlocking { downloadedTranslationsChannel.send(listOf(MockContents.kjvDownloadedTranslationInfo)) }
            }

            translationPresenter.attachView(translationView)
            verify(translationView, times(1)).onDownloadedTranslationsUpdated(emptyList())
            verify(translationView, times(1)).onDownloadedTranslationsUpdated(listOf(MockContents.kjvDownloadedTranslationInfo))

            translationPresenter.detachView()
        }
    }

    @Test
    fun testObserveTranslationsLoadingState() {
        runBlocking {
            translationPresenter.attachView(translationView)
            verify(translationView, times(1)).onTranslationsLoadingStarted()

            translationLoadingStateChannel.send(false)
            verify(translationView, times(1)).onTranslationsLoadingCompleted()

            translationPresenter.detachView()
        }
    }

    @Test
    fun testDownloadTranslation() {
        runBlocking {
            translationPresenter.attachView(translationView)

            val downloadProgressChannel = Channel<Int>()
            translationPresenter.downloadTranslation(MockContents.kjvTranslationInfo, downloadProgressChannel)

            val progress = 89
            downloadProgressChannel.send(progress)
            downloadProgressChannel.close()

            verify(translationInteractor, times(1))
                    .downloadTranslation(downloadProgressChannel, MockContents.kjvTranslationInfo)
            verify(translationView, times(1))
                    .onTranslationDownloadProgressed(progress)
            verify(translationInteractor, times(1))
                    .saveCurrentTranslation(MockContents.kjvShortName)
            verify(translationView, times(1))
                    .onTranslationDownloaded()

            translationPresenter.detachView()
        }
    }

    @Test
    fun testDownloadTranslationWithException() {
        runBlocking {
            val downloadProgressChannel = Channel<Int>()
            `when`(translationInteractor.downloadTranslation(downloadProgressChannel, MockContents.kjvTranslationInfo))
                    .thenThrow(RuntimeException("Random exception"))

            translationPresenter.attachView(translationView)
            translationPresenter.downloadTranslation(MockContents.kjvTranslationInfo, downloadProgressChannel)

            verify(translationInteractor, times(1))
                    .downloadTranslation(downloadProgressChannel, MockContents.kjvTranslationInfo)
            verify(translationView, never()).onTranslationDownloaded()
            verify(translationView, times(1)).onError(any())

            translationPresenter.detachView()
        }
    }
}

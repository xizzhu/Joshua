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

package me.xizzhu.android.joshua.progress

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.ReadingProgress
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*

class ReadingProgressPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var readingProgressInteractor: ReadingProgressInteractor
    @Mock
    private lateinit var view: ReadingProgressView

    private lateinit var presenter: ReadingProgressPresenter

    @Before
    override fun setup() {
        super.setup()

        runBlocking {
            `when`(readingProgressInteractor.observeSettings()).thenReturn(flowOf(Settings.DEFAULT))

            presenter = ReadingProgressPresenter(readingProgressInteractor)
        }
    }

    @Test
    fun testLoadReadingProgress() {
        runBlocking {
            val bookNames = List(Bible.BOOK_COUNT) { i -> i.toString() }
            val readingProgress = ReadingProgress(1, 4321L, emptyList())
            `when`(readingProgressInteractor.readCurrentTranslation()).thenReturn(MockContents.kjvShortName)
            `when`(readingProgressInteractor.readBookNames(MockContents.kjvShortName)).thenReturn(bookNames)
            `when`(readingProgressInteractor.readReadingProgress()).thenReturn(readingProgress)
            presenter.attachView(view)

            with(inOrder(readingProgressInteractor, view)) {
                verify(readingProgressInteractor, times(1)).notifyLoadingStarted()
                verify(view, times(1)).onReadingProgressLoadingStarted()
                verify(view, times(1)).onReadingProgressLoaded(any())
                verify(view, times(1)).onReadingProgressLoadingCompleted()
                verify(readingProgressInteractor, times(1)).notifyLoadingFinished()
            }
            verify(view, never()).onReadingProgressLoadFailed()

            presenter.detachView()
        }
    }

    @Test
    fun testLoadReadingProgressWithException() {
        runBlocking {
            `when`(readingProgressInteractor.readCurrentTranslation()).thenThrow(RuntimeException("Random exception"))
            presenter.attachView(view)

            with(inOrder(readingProgressInteractor, view)) {
                verify(readingProgressInteractor, times(1)).notifyLoadingStarted()
                verify(view, times(1)).onReadingProgressLoadingStarted()
                verify(view, times(1)).onReadingProgressLoadFailed()
                verify(readingProgressInteractor, times(1)).notifyLoadingFinished()
            }
            verify(view, never()).onReadingProgressLoaded(any())
            verify(view, never()).onReadingProgressLoadingCompleted()

            presenter.detachView()
        }
    }

    @Test
    fun testOpenChapter() {
        runBlocking {
            presenter.openChapter(1, 2)
            verify(readingProgressInteractor, times(1)).openChapter(VerseIndex(1, 2, 0))
        }
    }

    @Test
    fun testOpenChapterWithException() {
        runBlocking {
            `when`(readingProgressInteractor.openChapter(VerseIndex(1, 2, 0)))
                    .thenThrow(RuntimeException("Random exception"))

            presenter.openChapter(1, 2)
            verify(readingProgressInteractor, times(1)).openChapter(VerseIndex(1, 2, 0))
        }
    }
}

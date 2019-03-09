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

package me.xizzhu.android.joshua.reading.verse

import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.reading.ReadingInteractor
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*

class VersePresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var readingInteractor: ReadingInteractor
    @Mock
    private lateinit var verseView: VerseView

    private lateinit var versePresenter: VersePresenter
    private lateinit var currentTranslationChannel: ConflatedBroadcastChannel<String>
    private lateinit var currentVerseIndexChannel: ConflatedBroadcastChannel<VerseIndex>

    @Before
    override fun setup() {
        super.setup()

        currentTranslationChannel = ConflatedBroadcastChannel("")
        `when`(readingInteractor.observeCurrentTranslation()).then { currentTranslationChannel.openSubscription() }

        currentVerseIndexChannel = ConflatedBroadcastChannel(VerseIndex.INVALID)
        `when`(readingInteractor.observeCurrentVerseIndex()).then { currentVerseIndexChannel.openSubscription() }

        versePresenter = VersePresenter(readingInteractor)
        versePresenter.attachView(verseView)
    }

    @After
    override fun tearDown() {
        versePresenter.detachView()
        super.tearDown()
    }

    @Test
    fun testObserveCurrentTranslation() {
        runBlocking {
            verify(verseView, never()).onCurrentTranslationUpdated(any())

            `when`(readingInteractor.readBookNames(MockContents.kjvShortName)).thenReturn(MockContents.kjvBookNames)
            currentTranslationChannel.send(MockContents.kjvShortName)
            verify(verseView, times(1)).onCurrentTranslationUpdated(MockContents.kjvShortName)
        }
    }

    @Test
    fun testObserveCurrentVerseIndex() {
        runBlocking {
            verify(verseView, never()).onCurrentVerseIndexUpdated(any())

            val verseIndex = VerseIndex(1, 2, 3)
            currentVerseIndexChannel.send(verseIndex)
            verify(verseView, times(1)).onCurrentVerseIndexUpdated(verseIndex)
        }
    }

    @Test
    fun testLoadVerses() {
        runBlocking {
            val translationShortName = MockContents.kjvShortName
            val bookIndex = 1
            val chapterIndex = 2
            `when`(readingInteractor.readVerses(translationShortName, bookIndex, chapterIndex)).thenReturn(MockContents.kjvVerses)

            versePresenter.loadVerses(translationShortName, bookIndex, chapterIndex)
            verify(verseView, times(1)).onVersesLoaded(bookIndex, chapterIndex, MockContents.kjvVerses)
            verify(verseView, never()).onVersesLoadFailed(translationShortName, bookIndex, chapterIndex)
        }
    }

    @Test
    fun testLoadVersesWithExceptions() {
        runBlocking {
            val translationShortName = MockContents.kjvShortName
            val bookIndex = 1
            val chapterIndex = 2
            `when`(readingInteractor.readVerses(translationShortName, bookIndex, chapterIndex)).thenThrow(RuntimeException("Random exception"))

            versePresenter.loadVerses(translationShortName, bookIndex, chapterIndex)
            verify(verseView, never()).onVersesLoaded(bookIndex, chapterIndex, MockContents.kjvVerses)
            verify(verseView, times(1)).onVersesLoadFailed(translationShortName, bookIndex, chapterIndex)
        }
    }
}

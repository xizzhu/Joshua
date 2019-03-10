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

package me.xizzhu.android.joshua.reading.chapter

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

class ChapterListPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var readingInteractor: ReadingInteractor
    @Mock
    private lateinit var chapterListView: ChapterListView

    private lateinit var chapterListPresenter: ChapterListPresenter
    private lateinit var currentTranslationChannel: ConflatedBroadcastChannel<String>
    private lateinit var currentVerseIndexChannel: ConflatedBroadcastChannel<VerseIndex>

    @Before
    override fun setup() {
        super.setup()

        currentTranslationChannel = ConflatedBroadcastChannel("")
        `when`(readingInteractor.observeCurrentTranslation()).then { currentTranslationChannel.openSubscription() }

        currentVerseIndexChannel = ConflatedBroadcastChannel(VerseIndex.INVALID)
        `when`(readingInteractor.observeCurrentVerseIndex()).then { currentVerseIndexChannel.openSubscription() }

        chapterListPresenter = ChapterListPresenter(readingInteractor)
        chapterListPresenter.attachView(chapterListView)
    }

    @After
    override fun tearDown() {
        chapterListPresenter.detachView()
        super.tearDown()
    }

    @Test
    fun testObserveCurrentTranslation() {
        runBlocking {
            verify(chapterListView, never()).onBookNamesUpdated(any())

            `when`(readingInteractor.readBookNames(MockContents.kjvShortName)).thenReturn(MockContents.kjvBookNames)
            currentTranslationChannel.send(MockContents.kjvShortName)
            verify(chapterListView, times(1)).onBookNamesUpdated(MockContents.kjvBookNames)
        }
    }

    @Test
    fun testObserveCurrentVerseIndex() {
        runBlocking {
            verify(chapterListView, never()).onCurrentVerseIndexUpdated(any())

            val verseIndex = VerseIndex(1, 2, 3)
            currentVerseIndexChannel.send(verseIndex)
            verify(chapterListView, times(1)).onCurrentVerseIndexUpdated(verseIndex)
        }
    }

    @Test
    fun testSelectChapter() {
        runBlocking {
            val bookIndex = 1
            val chapterIndex = 2
            chapterListPresenter.selectChapter(bookIndex, chapterIndex)
            verify(chapterListView, never()).onChapterSelectionFailed(bookIndex, chapterIndex)
        }
    }

    @Test
    fun testSelectChapterWithException() {
        runBlocking {
            `when`(readingInteractor.saveCurrentVerseIndex(any())).thenThrow(RuntimeException("Random exception"))

            val bookIndex = 1
            val chapterIndex = 2
            chapterListPresenter.selectChapter(bookIndex, chapterIndex)
            verify(chapterListView, times(1)).onChapterSelectionFailed(bookIndex, chapterIndex)
        }
    }
}

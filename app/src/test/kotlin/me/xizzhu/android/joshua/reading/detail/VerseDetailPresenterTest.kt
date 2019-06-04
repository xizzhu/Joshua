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

package me.xizzhu.android.joshua.reading.detail

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.core.Bookmark
import me.xizzhu.android.joshua.core.Note
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.reading.ReadingInteractor
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.ui.recyclerview.VerseTextItem
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.assertEquals

class VerseDetailPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var readingInteractor: ReadingInteractor
    @Mock
    private lateinit var verseDetailView: VerseDetailView
    private lateinit var settingsChannel: ConflatedBroadcastChannel<Settings>
    private lateinit var verseDetailOpenState: ConflatedBroadcastChannel<Pair<VerseIndex, Int>>
    private lateinit var currentTranslationShortName: BroadcastChannel<String>
    private lateinit var verseDetailPresenter: VerseDetailPresenter

    @Before
    override fun setup() {
        super.setup()

        runBlocking {
            settingsChannel = ConflatedBroadcastChannel(Settings.DEFAULT)
            `when`(readingInteractor.observeSettings()).thenReturn(settingsChannel.openSubscription())

            verseDetailOpenState = ConflatedBroadcastChannel()
            `when`(readingInteractor.observeVerseDetailOpenState()).thenReturn(verseDetailOpenState.openSubscription())

            currentTranslationShortName = ConflatedBroadcastChannel(MockContents.kjvShortName)
            `when`(readingInteractor.observeCurrentTranslation()).thenReturn(currentTranslationShortName.openSubscription())

            verseDetailPresenter = VerseDetailPresenter(readingInteractor)

            verseDetailPresenter.attachView(verseDetailView)
        }
    }

    @After
    override fun tearDown() {
        verseDetailPresenter.detachView()
        super.tearDown()
    }

    @Test
    fun testShow() {
        runBlocking {
            val verseIndex = VerseIndex(0, 0, 0)
            `when`(readingInteractor.readVerseWithParallel(MockContents.kjvShortName, verseIndex)).thenReturn(MockContents.kjvVerses[0])
            `when`(readingInteractor.readBookmark(verseIndex)).thenReturn(Bookmark(verseIndex, -1L))
            `when`(readingInteractor.readNote(verseIndex)).thenReturn(Note(verseIndex, "", -1L))

            verseDetailOpenState.send(Pair(verseIndex, 0))

            verify(verseDetailView, times(1)).show(0)
            verify(verseDetailView, times(1)).onVerseDetailLoaded(VerseDetail.INVALID)
            verify(verseDetailView, times(1)).onVerseDetailLoaded(
                    VerseDetail(verseIndex, listOf(VerseTextItem(verseIndex, MockContents.kjvVerses[0].text,
                            verseDetailPresenter::onVerseClicked, verseDetailPresenter::onVerseLongClicked)), false, ""))
            verify(verseDetailView, never()).hide()
        }
    }

    @Test
    fun testLoadVerseDetail() {
        runBlocking {
            val verseIndex = VerseIndex(0, 0, 0)
            `when`(readingInteractor.readVerseWithParallel(MockContents.kjvShortName, verseIndex)).thenReturn(MockContents.kjvVerses[0])
            `when`(readingInteractor.readBookmark(verseIndex)).thenReturn(Bookmark(verseIndex, -1L))
            `when`(readingInteractor.readNote(verseIndex)).thenReturn(Note(verseIndex, "", -1L))

            verseDetailPresenter.loadVerseDetail(verseIndex)

            verify(verseDetailView, times(1)).onVerseDetailLoaded(VerseDetail.INVALID)
            verify(verseDetailView, times(1)).onVerseDetailLoaded(
                    VerseDetail(verseIndex, listOf(VerseTextItem(verseIndex, MockContents.kjvVerses[0].text,
                            verseDetailPresenter::onVerseClicked, verseDetailPresenter::onVerseLongClicked)), false, ""))
            verify(verseDetailView, never()).onVerseDetailLoadFailed(verseIndex)
        }
    }

    @Test
    fun testLoadVerseDetailWithException() {
        runBlocking {
            val verseIndex = VerseIndex(0, 0, 0)
            `when`(readingInteractor.readVerseWithParallel(MockContents.kjvShortName, verseIndex)).thenThrow(RuntimeException("Random exception"))
            `when`(readingInteractor.readBookmark(verseIndex)).thenThrow(RuntimeException("Random exception"))

            verseDetailPresenter.loadVerseDetail(verseIndex)

            verify(verseDetailView, times(1)).onVerseDetailLoaded(VerseDetail.INVALID)
            verify(verseDetailView, times(1)).onVerseDetailLoadFailed(verseIndex)
        }
    }

    @Test
    fun testOnVerseClickedWithSameTranslation() {
        runBlocking {
            currentTranslationShortName.send(MockContents.kjvShortName)
            verseDetailPresenter.onVerseClicked(MockContents.kjvShortName)
            verify(readingInteractor, never()).saveCurrentTranslation(anyString())
            verify(readingInteractor, never()).closeVerseDetail()
            verify(verseDetailView, never()).onVerseTextClickFailed()
        }
    }

    @Test
    fun testOnVerseClickedWithDifferentTranslation() {
        runBlocking {
            currentTranslationShortName.send(MockContents.cuvShortName)
            verseDetailPresenter.onVerseClicked(MockContents.kjvShortName)
            verify(readingInteractor, times(1)).saveCurrentTranslation(MockContents.kjvShortName)
            verify(readingInteractor, times(1)).closeVerseDetail()
            verify(verseDetailView, never()).onVerseTextClickFailed()
        }
    }

    @Test
    fun testOnVerseClickedWithException() {
        runBlocking {
            `when`(readingInteractor.observeCurrentTranslation()).thenThrow(RuntimeException("Random exception"))
            verseDetailPresenter.onVerseClicked(MockContents.kjvShortName)
            verify(readingInteractor, never()).saveCurrentTranslation(anyString())
            verify(readingInteractor, never()).closeVerseDetail()
            verify(verseDetailView, times(1)).onVerseTextClickFailed()
        }
    }

    @Test
    fun testOnVerseLongClicked() {
        `when`(readingInteractor.copyToClipBoard(any())).thenReturn(true)
        verseDetailPresenter.onVerseLongClicked(MockContents.kjvVerses[0])
        verify(readingInteractor, times(1)).copyToClipBoard(listOf(MockContents.kjvVerses[0]))
        verify(verseDetailView, times(1)).onVerseTextCopied()
        verify(verseDetailView, never()).onVerseTextClickFailed()
    }

    @Test
    fun testOnVerseLongClickedWithCopyFailed() {
        `when`(readingInteractor.copyToClipBoard(any())).thenReturn(false)
        verseDetailPresenter.onVerseLongClicked(MockContents.kjvVerses[0])
        verify(readingInteractor, times(1)).copyToClipBoard(listOf(MockContents.kjvVerses[0]))
        verify(verseDetailView, never()).onVerseTextCopied()
        verify(verseDetailView, times(1)).onVerseTextClickFailed()
    }

    @Test
    fun testHide() {
        runBlocking {
            verseDetailOpenState.send(Pair(VerseIndex.INVALID, 0))

            verify(verseDetailView, never()).show(0)
            verify(verseDetailView, never()).onVerseDetailLoaded(any())
            verify(verseDetailView, times(1)).hide()
        }
    }

    @Test
    fun testAddBookmark() {
        runBlocking {
            verseDetailPresenter.verseDetail = VerseDetail(VerseIndex(0, 0, 0), emptyList(), false, "")

            verseDetailPresenter.updateBookmark()

            val expected = VerseDetail(VerseIndex(0, 0, 0), emptyList(), true, "")
            verify(verseDetailView, times(1)).onVerseDetailLoaded(expected)
            assertEquals(expected, verseDetailPresenter.verseDetail)
        }
    }

    @Test
    fun testRemoveBookmark() {
        runBlocking {
            verseDetailPresenter.verseDetail = VerseDetail(VerseIndex(0, 0, 0), emptyList(), true, "")

            verseDetailPresenter.updateBookmark()

            val expected = VerseDetail(VerseIndex(0, 0, 0), emptyList(), false, "")
            verify(verseDetailView, times(1)).onVerseDetailLoaded(expected)
            assertEquals(expected, verseDetailPresenter.verseDetail)
        }
    }

    @Test
    fun testUpdateNote() {
        runBlocking {
            verseDetailPresenter.verseDetail = VerseDetail(VerseIndex(0, 0, 0), emptyList(), false, "")

            verseDetailPresenter.updateNote("Random")
            verify(readingInteractor, never()).removeNote(VerseIndex(0, 0, 0))
            verify(readingInteractor, times(1)).saveNote(VerseIndex(0, 0, 0), "Random")
            assertEquals(VerseDetail(VerseIndex(0, 0, 0), emptyList(), false, "Random"), verseDetailPresenter.verseDetail)
        }
    }

    @Test
    fun testUpdateEmptyNote() {
        runBlocking {
            verseDetailPresenter.verseDetail = VerseDetail(VerseIndex(0, 0, 0), emptyList(), false, "")

            verseDetailPresenter.updateNote("")
            verify(readingInteractor, times(1)).removeNote(VerseIndex(0, 0, 0))
            verify(readingInteractor, never()).saveNote(any(), anyString())
            assertEquals(VerseDetail(VerseIndex(0, 0, 0), emptyList(), false, ""), verseDetailPresenter.verseDetail)
        }
    }
}

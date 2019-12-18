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

import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.reading.ReadingActivity
import me.xizzhu.android.joshua.reading.VerseDetailRequest
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.*

class VerseDetailPresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var verseDetailViewLayout: VerseDetailViewLayout
    @Mock
    private lateinit var readingActivity: ReadingActivity
    @Mock
    private lateinit var verseDetailInteractor: VerseDetailInteractor

    private lateinit var verseDetailViewHolder: VerseDetailViewHolder
    private lateinit var verseDetailPresenter: VerseDetailPresenter

    @BeforeTest
    override fun setup() {
        super.setup()

        `when`(verseDetailInteractor.settings()).thenReturn(emptyFlow())
        `when`(verseDetailInteractor.verseDetailRequest()).thenReturn(emptyFlow())

        verseDetailViewHolder = VerseDetailViewHolder(verseDetailViewLayout)
        verseDetailPresenter = VerseDetailPresenter(readingActivity, verseDetailInteractor, testDispatcher)
        verseDetailPresenter.bind(verseDetailViewHolder)
    }

    @AfterTest
    override fun tearDown() {
        verseDetailPresenter.unbind()

        super.tearDown()
    }

    @Test
    fun testObserveSettings() = testDispatcher.runBlockingTest {
        val settings = Settings(false, true, 1, true, true)
        `when`(verseDetailInteractor.settings()).thenReturn(flowOf(ViewData.loading(), ViewData.success(settings), ViewData.error()))

        verseDetailPresenter.start()
        verify(verseDetailViewHolder.verseDetailViewLayout, times(1)).setSettings(settings)
        verify(verseDetailViewHolder.verseDetailViewLayout, never()).setSettings(Settings.DEFAULT)

        verseDetailPresenter.stop()
    }

    @Test
    fun testObserveVerseDetailRequestShow() = testDispatcher.runBlockingTest {
        val verseIndex = VerseIndex(0, 0, 0)
        val currentTranslation = MockContents.kjvShortName
        val bookNames = MockContents.kjvBookNames
        val verses = MockContents.kjvVerses
        `when`(verseDetailInteractor.verseDetailRequest()).thenReturn(flowOf(VerseDetailRequest(verseIndex, VerseDetailRequest.VERSES)))
        `when`(verseDetailInteractor.readBookmark(verseIndex)).thenReturn(Bookmark(verseIndex, 0L))
        `when`(verseDetailInteractor.readHighlight(verseIndex)).thenReturn(Highlight(verseIndex, Highlight.COLOR_NONE, 0L))
        `when`(verseDetailInteractor.readNote(verseIndex)).thenReturn(Note(verseIndex, "", 0L))
        `when`(verseDetailInteractor.currentTranslation()).thenReturn(currentTranslation)
        `when`(verseDetailInteractor.downloadedTranslations()).thenReturn(emptyList())
        `when`(verseDetailInteractor.readBookNames(currentTranslation)).thenReturn(bookNames)
        `when`(verseDetailInteractor.readVerses(currentTranslation, emptyList(), verseIndex.bookIndex, verseIndex.chapterIndex)).thenReturn(verses)

        verseDetailPresenter.start()
        verify(verseDetailViewHolder.verseDetailViewLayout, times(1)).show(VerseDetailRequest.VERSES)
        verify(verseDetailViewHolder.verseDetailViewLayout, times(1)).setVerseDetail(
                VerseDetail(
                        verseIndex,
                        listOf(
                                VerseTextItem(
                                        verses[verseIndex.verseIndex].verseIndex, 0,
                                        verses[verseIndex.verseIndex].text, bookNames[verseIndex.verseIndex],
                                        verseDetailPresenter::onVerseClicked, verseDetailPresenter::onVerseLongClicked
                                )
                        ),
                        false,
                        Highlight.COLOR_NONE,
                        "")
        )
        verify(verseDetailViewHolder.verseDetailViewLayout, never()).hide()

        verseDetailPresenter.stop()
    }

    @Test
    fun testObserveVerseDetailRequestShowWithFollowingEmptyVerses() = testDispatcher.runBlockingTest {
        val verseIndex = VerseIndex(0, 0, 0)
        val currentTranslation = MockContents.msgShortName
        `when`(verseDetailInteractor.verseDetailRequest()).thenReturn(flowOf(VerseDetailRequest(verseIndex, VerseDetailRequest.VERSES)))
        `when`(verseDetailInteractor.readBookmark(verseIndex)).thenReturn(Bookmark(verseIndex, 0L))
        `when`(verseDetailInteractor.readHighlight(verseIndex)).thenReturn(Highlight(verseIndex, Highlight.COLOR_NONE, 0L))
        `when`(verseDetailInteractor.readNote(verseIndex)).thenReturn(Note(verseIndex, "", 0L))
        `when`(verseDetailInteractor.currentTranslation()).thenReturn(currentTranslation)
        `when`(verseDetailInteractor.downloadedTranslations()).thenReturn(listOf(MockContents.msgTranslationInfo, MockContents.kjvTranslationInfo))
        `when`(verseDetailInteractor.readBookNames(MockContents.msgShortName)).thenReturn(MockContents.msgBookNames)
        `when`(verseDetailInteractor.readBookNames(MockContents.kjvShortName)).thenReturn(MockContents.kjvBookNames)
        `when`(verseDetailInteractor.readVerses(currentTranslation, listOf(MockContents.kjvShortName), verseIndex.bookIndex, verseIndex.chapterIndex)).thenReturn(MockContents.msgVersesWithKjvParallel)

        verseDetailPresenter.start()
        verify(verseDetailViewHolder.verseDetailViewLayout, times(1)).show(VerseDetailRequest.VERSES)
        verify(verseDetailViewHolder.verseDetailViewLayout, times(1)).setVerseDetail(
                VerseDetail(
                        verseIndex,
                        listOf(
                                VerseTextItem(
                                        verseIndex.copy(verseIndex = 0), 1,
                                        MockContents.msgVersesWithKjvParallel[0].text, MockContents.msgBookNames[0],
                                        verseDetailPresenter::onVerseClicked, verseDetailPresenter::onVerseLongClicked
                                ),
                                VerseTextItem(
                                        verseIndex.copy(verseIndex = 0), 1,
                                        Verse.Text(
                                                MockContents.msgVersesWithKjvParallel[0].parallel[0].translationShortName,
                                                MockContents.msgVersesWithKjvParallel[0].parallel[0].text + " " + MockContents.msgVersesWithKjvParallel[1].parallel[0].text
                                        ),
                                        MockContents.kjvBookNames[0], verseDetailPresenter::onVerseClicked, verseDetailPresenter::onVerseLongClicked)
                        ),
                        false,
                        Highlight.COLOR_NONE,
                        "")
        )
        verify(verseDetailViewHolder.verseDetailViewLayout, never()).hide()

        verseDetailPresenter.stop()
    }

    @Test
    fun testCloseWithoutDetail() {
        assertFalse(verseDetailPresenter.close())
        verify(verseDetailViewHolder.verseDetailViewLayout, times(1)).hide()
        verify(verseDetailInteractor, never()).closeVerseDetail(any())
    }

    @Test
    fun testCloseWithDetail() {
        verseDetailPresenter.verseDetail = VerseDetail(VerseIndex(1, 2, 3), emptyList(), false, Highlight.COLOR_NONE, "")

        assertTrue(verseDetailPresenter.close())
        verify(verseDetailViewHolder.verseDetailViewLayout, times(1)).hide()
        verify(verseDetailInteractor, times(1)).closeVerseDetail(VerseIndex(1, 2, 3))
        assertNull(verseDetailPresenter.verseDetail)
    }
}

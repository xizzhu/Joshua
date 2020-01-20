/*
 * Copyright (C) 2020 Xizhi Zhu
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

import androidx.appcompat.view.ActionMode
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.reading.ReadingActivity
import me.xizzhu.android.joshua.reading.VerseDetailRequest
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.*

class VersePresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var actionMode: ActionMode
    @Mock
    private lateinit var versePager: VerseViewPager
    @Mock
    private lateinit var readingActivity: ReadingActivity
    @Mock
    private lateinit var verseInteractor: VerseInteractor

    private lateinit var verseViewHolder: VerseViewHolder
    private lateinit var versePresenter: VersePresenter

    @BeforeTest
    override fun setup() {
        super.setup()

        `when`(readingActivity.startSupportActionMode(any())).thenReturn(actionMode)
        `when`(verseInteractor.settings()).thenReturn(emptyFlow())
        `when`(verseInteractor.currentTranslation()).thenReturn(emptyFlow())
        `when`(verseInteractor.currentVerseIndex()).thenReturn(emptyFlow())
        `when`(verseInteractor.parallelTranslations()).thenReturn(emptyFlow())
        `when`(verseInteractor.verseUpdates()).thenReturn(emptyFlow())

        verseViewHolder = VerseViewHolder(versePager)
        versePresenter = VersePresenter(readingActivity, verseInteractor, testDispatcher)
    }

    @Test
    fun testLoadVersesWithSimpleReadingModeOn() = testDispatcher.runBlockingTest {
        val currentTranslation = ""
        val bookIndex = 0
        val chapterIndex = 0
        `when`(verseInteractor.readVerses(currentTranslation, bookIndex, chapterIndex)).thenReturn(MockContents.kjvVerses)
        `when`(verseInteractor.readBookNames(currentTranslation)).thenReturn(MockContents.kjvBookNames)
        `when`(verseInteractor.readHighlights(bookIndex, chapterIndex)).thenReturn(emptyList())
        `when`(verseInteractor.settings()).thenReturn(flowOf(ViewData.success(Settings.DEFAULT.copy(simpleReadingModeOn = true))))

        versePresenter.create(verseViewHolder)
        versePresenter.loadVerses(bookIndex, chapterIndex)
        verify(verseViewHolder.versePager, times(1)).setVerses(
                bookIndex, chapterIndex,
                MockContents.kjvVerses.toSimpleVerseItems(
                        MockContents.kjvBookNames[0], emptyList(),
                        versePresenter::onVerseClicked, versePresenter::onVerseLongClicked
                ))

        versePresenter.destroy()
    }

    @Test
    fun testLoadVersesWithSimpleReadingModeOff() = testDispatcher.runBlockingTest {
        val currentTranslation = ""
        val bookIndex = 0
        val chapterIndex = 0
        `when`(verseInteractor.readVerses(currentTranslation, bookIndex, chapterIndex)).thenReturn(MockContents.kjvVerses)
        `when`(verseInteractor.readBookNames(currentTranslation)).thenReturn(MockContents.kjvBookNames)
        `when`(verseInteractor.readHighlights(bookIndex, chapterIndex)).thenReturn(emptyList())
        `when`(verseInteractor.settings()).thenReturn(flowOf(ViewData.success(Settings.DEFAULT.copy(simpleReadingModeOn = false))))
        `when`(verseInteractor.readBookmarks(bookIndex, chapterIndex)).thenReturn(emptyList())
        `when`(verseInteractor.readNotes(bookIndex, chapterIndex)).thenReturn(emptyList())

        versePresenter.create(verseViewHolder)
        versePresenter.loadVerses(bookIndex, chapterIndex)
        verify(verseViewHolder.versePager, times(1)).setVerses(
                bookIndex, chapterIndex,
                MockContents.kjvVerses.toVerseItems(
                        MockContents.kjvBookNames[0], emptyList(), emptyList(), emptyList(),
                        versePresenter::onVerseClicked, versePresenter::onVerseLongClicked,
                        versePresenter::onBookmarkClicked, versePresenter::onHighlightClicked,
                        versePresenter::onNoteClicked
                ))

        versePresenter.destroy()
    }

    @Test
    fun testObserveSettings() = testDispatcher.runBlockingTest {
        val settings = Settings(false, true, 1, true, true)
        `when`(verseInteractor.settings()).thenReturn(flowOf(ViewData.loading(), ViewData.success(settings), ViewData.error()))

        versePresenter.create(verseViewHolder)
        verify(verseViewHolder.versePager, times(1)).setSettings(settings)
        verify(verseViewHolder.versePager, never()).setSettings(Settings.DEFAULT)

        versePresenter.destroy()
    }

    @Test
    fun testObserveCurrent() = testDispatcher.runBlockingTest {
        val verseIndex = VerseIndex(1, 2, 3)
        val translation = MockContents.kjvShortName
        val parallelTranslations = listOf(emptyList(), listOf(MockContents.kjvShortName), emptyList(), listOf(MockContents.bbeShortName, MockContents.cuvShortName))
        `when`(verseInteractor.currentVerseIndex()).thenReturn(flowOf(verseIndex))
        `when`(verseInteractor.currentTranslation()).thenReturn(flowOf(translation))
        `when`(verseInteractor.parallelTranslations()).thenReturn(flow { parallelTranslations.forEach { emit(it) } })

        versePresenter.create(verseViewHolder)
        with(inOrder(verseViewHolder.versePager)) {
            parallelTranslations.forEach { parallel ->
                verify(verseViewHolder.versePager, times(1)).setCurrent(verseIndex, translation, parallel)
            }
        }
        versePresenter.destroy()
    }

    @Test
    fun testOnVerseClickedWithoutActionMode() {
        versePresenter.create(verseViewHolder)

        val verse = MockContents.kjvVerses[0]
        versePresenter.onVerseClicked(verse)
        with(inOrder(verseInteractor, verseViewHolder.versePager)) {
            verify(verseInteractor, times(1)).requestVerseDetail(VerseDetailRequest(verse.verseIndex, VerseDetailRequest.VERSES))
            verify(verseViewHolder.versePager, times(1)).selectVerse(verse.verseIndex)
        }
        verify(verseViewHolder.versePager, never()).deselectVerse(any())

        versePresenter.destroy()
    }

    @Test
    fun testOnVerseClicked() {
        versePresenter.create(verseViewHolder)

        val verse1 = MockContents.kjvVerses[0]
        val verse2 = MockContents.kjvVerses[1]

        with(inOrder(verseViewHolder.versePager, actionMode)) {
            // select verse1
            versePresenter.onVerseLongClicked(verse1)
            verify(actionMode, never()).finish()
            verify(verseViewHolder.versePager, times(1)).selectVerse(verse1.verseIndex)

            // select verse2
            versePresenter.onVerseClicked(verse2)
            verify(actionMode, never()).finish()
            verify(verseViewHolder.versePager, times(1)).selectVerse(verse2.verseIndex)

            // deselect verse2
            versePresenter.onVerseClicked(verse2)
            verify(actionMode, never()).finish()
            verify(verseViewHolder.versePager, times(1)).deselectVerse(verse2.verseIndex)

            // deselect verse1
            versePresenter.onVerseClicked(verse1)
            verify(actionMode, times(1)).finish()
            verify(verseViewHolder.versePager, times(1)).deselectVerse(verse1.verseIndex)
        }
        verify(verseInteractor, never()).requestVerseDetail(any())

        versePresenter.destroy()
    }

    @Test
    fun testOnBookmarkClickedHadBookmark() = testDispatcher.runBlockingTest {
        versePresenter.create(verseViewHolder)

        val verseIndex = VerseIndex(1, 2, 3)
        versePresenter.onBookmarkClicked(verseIndex, true)
        verify(verseInteractor, times(1)).removeBookmark(verseIndex)
        verify(verseInteractor, never()).addBookmark(any())

        versePresenter.destroy()
    }

    @Test
    fun testOnBookmarkClickedHadNoBookmark() = testDispatcher.runBlockingTest {
        versePresenter.create(verseViewHolder)

        val verseIndex = VerseIndex(1, 2, 3)
        versePresenter.onBookmarkClicked(verseIndex, false)
        verify(verseInteractor, times(1)).addBookmark(verseIndex)
        verify(verseInteractor, never()).removeBookmark(any())

        versePresenter.destroy()
    }

    @Test
    fun testUpdateHighlight() = testDispatcher.runBlockingTest {
        versePresenter.create(verseViewHolder)

        val verseIndex = VerseIndex(1, 2, 3)
        val highlightColor = Highlight.COLOR_BLUE
        versePresenter.updateHighlight(verseIndex, highlightColor)
        verify(verseInteractor, times(1)).saveHighlight(verseIndex, highlightColor)
        verify(verseInteractor, never()).removeHighlight(any())

        versePresenter.destroy()
    }

    @Test
    fun testRemoveHighlight() = testDispatcher.runBlockingTest {
        versePresenter.create(verseViewHolder)

        val verseIndex = VerseIndex(1, 2, 3)
        val highlightColor = Highlight.COLOR_NONE
        versePresenter.updateHighlight(verseIndex, highlightColor)
        verify(verseInteractor, times(1)).removeHighlight(verseIndex)
        verify(verseInteractor, never()).saveHighlight(any(), anyInt())

        versePresenter.destroy()
    }

    @Test
    fun testOnNoteClicked() {
        versePresenter.create(verseViewHolder)

        versePresenter.onNoteClicked(VerseIndex(1, 2, 3))

        with(inOrder(verseInteractor, verseViewHolder.versePager)) {
            verify(verseInteractor, times(1)).requestVerseDetail(VerseDetailRequest(VerseIndex(1, 2, 3), VerseDetailRequest.NOTE))
            verify(verseViewHolder.versePager, times(1)).selectVerse(VerseIndex(1, 2, 3))
        }

        versePresenter.destroy()
    }
}

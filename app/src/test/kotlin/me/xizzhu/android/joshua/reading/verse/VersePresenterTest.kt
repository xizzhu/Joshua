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
        `when`(verseInteractor.verseDetailRequest()).thenReturn(emptyFlow())

        verseViewHolder = VerseViewHolder(versePager)
        versePresenter = VersePresenter(readingActivity, verseInteractor, testDispatcher)
        versePresenter.bind(verseViewHolder)
    }

    @AfterTest
    override fun tearDown() {
        versePresenter.unbind()

        super.tearDown()
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

        versePresenter.loadVerses(bookIndex, chapterIndex)
        verify(verseViewHolder.versePager, times(1)).onVersesLoaded(
                bookIndex, chapterIndex,
                MockContents.kjvVerses.toSimpleVerseItems(
                        MockContents.kjvBookNames[0], emptyList(),
                        versePresenter::onVerseClicked, versePresenter::onVerseLongClicked
                ))
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

        versePresenter.loadVerses(bookIndex, chapterIndex)
        verify(verseViewHolder.versePager, times(1)).onVersesLoaded(
                bookIndex, chapterIndex,
                MockContents.kjvVerses.toVerseItems(
                        MockContents.kjvBookNames[0], emptyList(), emptyList(), emptyList(),
                        versePresenter::onVerseClicked, versePresenter::onVerseLongClicked,
                        versePresenter::onBookmarkClicked, versePresenter::onHighlightClicked,
                        versePresenter::onNoteClicked
                ))
    }

    @Test
    fun testObserveSettings() = testDispatcher.runBlockingTest {
        val settings = Settings(false, true, 1, true)
        `when`(verseInteractor.settings()).thenReturn(flowOf(
                ViewData.loading(Settings.DEFAULT),
                ViewData.success(settings),
                ViewData.error(Settings.DEFAULT)
        ))

        versePresenter.start()
        verify(verseViewHolder.versePager, times(1)).onSettingsUpdated(settings)
        verify(verseViewHolder.versePager, never()).onSettingsUpdated(Settings.DEFAULT)

        versePresenter.stop()
    }

    @Test
    fun testObserveCurrentTranslation() = testDispatcher.runBlockingTest {
        val translation = MockContents.kjvShortName
        `when`(verseInteractor.currentTranslation()).thenReturn(flowOf("", translation, ""))

        versePresenter.start()
        verify(verseViewHolder.versePager, times(1)).onCurrentTranslationUpdated(translation)
        versePresenter.stop()
    }

    @Test
    fun testObserveCurrentVerseIndex() = testDispatcher.runBlockingTest {
        val verseIndex = VerseIndex(1, 2, 3)
        `when`(verseInteractor.currentVerseIndex()).thenReturn(flowOf(VerseIndex.INVALID, verseIndex, VerseIndex.INVALID))

        versePresenter.start()
        verify(verseViewHolder.versePager, times(1)).onCurrentVerseIndexUpdated(verseIndex)
        versePresenter.stop()
    }

    @Test
    fun testObserveParallelTranslations() = testDispatcher.runBlockingTest {
        val parallelTranslations = listOf(emptyList(), listOf(MockContents.kjvShortName), emptyList(), listOf(MockContents.bbeShortName, MockContents.cuvShortName))
        `when`(verseInteractor.parallelTranslations()).thenReturn(flow { parallelTranslations.forEach { emit(it) } })

        versePresenter.start()
        with(inOrder(verseViewHolder.versePager)) {
            parallelTranslations.forEach {
                verify(verseViewHolder.versePager, times(1)).onParallelTranslationsUpdated(it)
            }
        }
        versePresenter.stop()
    }

    @Test
    fun testObserveVerseDetailRequest() = testDispatcher.runBlockingTest {
        val verseIndex = VerseIndex(1, 2, 3)
        `when`(verseInteractor.verseDetailRequest()).thenReturn(flow {
            listOf(VerseDetailRequest.HIDE, VerseDetailRequest.VERSES, VerseDetailRequest.HIDE).forEach { emit(VerseDetailRequest(verseIndex, it)) }
        })

        versePresenter.start()
        with(inOrder(verseViewHolder.versePager)) {
            verify(verseViewHolder.versePager, times(1)).onVerseDeselected(verseIndex)
            verify(verseViewHolder.versePager, times(1)).onVerseSelected(verseIndex)
            verify(verseViewHolder.versePager, times(1)).onVerseDeselected(verseIndex)
        }
        versePresenter.stop()
    }

    @Test
    fun testOnVerseClickedWithoutActionMode() {
        val verse = MockContents.kjvVerses[0]
        versePresenter.onVerseClicked(verse)
        verify(verseInteractor, times(1)).requestVerseDetail(VerseDetailRequest(verse.verseIndex, VerseDetailRequest.VERSES))
        verify(verseViewHolder.versePager, never()).onVerseDeselected(any())
        verify(verseViewHolder.versePager, never()).onVerseSelected(any())
    }

    @Test
    fun testOnVerseClicked() {
        val verse1 = MockContents.kjvVerses[0]
        val verse2 = MockContents.kjvVerses[1]

        with(inOrder(verseViewHolder.versePager, actionMode)) {
            // select verse1
            versePresenter.onVerseLongClicked(verse1)
            verify(actionMode, never()).finish()
            verify(verseViewHolder.versePager, times(1)).onVerseSelected(verse1.verseIndex)

            // select verse2
            versePresenter.onVerseClicked(verse2)
            verify(actionMode, never()).finish()
            verify(verseViewHolder.versePager, times(1)).onVerseSelected(verse2.verseIndex)

            // deselect verse2
            versePresenter.onVerseClicked(verse2)
            verify(actionMode, never()).finish()
            verify(verseViewHolder.versePager, times(1)).onVerseDeselected(verse2.verseIndex)

            // deselect verse1
            versePresenter.onVerseClicked(verse1)
            verify(actionMode, times(1)).finish()
            verify(verseViewHolder.versePager, times(1)).onVerseDeselected(verse1.verseIndex)
        }
        verify(verseInteractor, never()).requestVerseDetail(any())
    }

    @Test
    fun testOnBookmarkClickedHadBookmark() = testDispatcher.runBlockingTest {
        val verseIndex = VerseIndex(1, 2, 3)
        versePresenter.onBookmarkClicked(verseIndex, true)
        verify(verseInteractor, times(1)).removeBookmark(verseIndex)
        verify(verseInteractor, never()).addBookmark(any())
    }

    @Test
    fun testOnBookmarkClickedHadNoBookmark() = testDispatcher.runBlockingTest {
        val verseIndex = VerseIndex(1, 2, 3)
        versePresenter.onBookmarkClicked(verseIndex, false)
        verify(verseInteractor, times(1)).addBookmark(verseIndex)
        verify(verseInteractor, never()).removeBookmark(any())
    }

    @Test
    fun testUpdateHighlight() = testDispatcher.runBlockingTest {
        val verseIndex = VerseIndex(1, 2, 3)
        val highlightColor = Highlight.COLOR_BLUE
        versePresenter.updateHighlight(verseIndex, highlightColor)
        verify(verseInteractor, times(1)).saveHighlight(verseIndex, highlightColor)
        verify(verseInteractor, never()).removeHighlight(any())
    }

    @Test
    fun testRemoveHighlight() = testDispatcher.runBlockingTest {
        val verseIndex = VerseIndex(1, 2, 3)
        val highlightColor = Highlight.COLOR_NONE
        versePresenter.updateHighlight(verseIndex, highlightColor)
        verify(verseInteractor, times(1)).removeHighlight(verseIndex)
        verify(verseInteractor, never()).saveHighlight(any(), anyInt())
    }
}

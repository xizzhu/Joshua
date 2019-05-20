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

import android.view.MenuItem
import androidx.appcompat.view.ActionMode
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.runBlocking
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.reading.ReadingInteractor
import me.xizzhu.android.joshua.reading.detail.VerseDetailPagerAdapter
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.ui.recyclerview.SimpleVerseItem
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VersePresenterTest : BaseUnitTest() {
    @Mock
    private lateinit var readingInteractor: ReadingInteractor
    @Mock
    private lateinit var verseView: VerseView
    @Mock
    private lateinit var actionMode: ActionMode
    @Mock
    private lateinit var item: MenuItem

    private lateinit var versePresenter: VersePresenter
    private lateinit var settingsChannel: ConflatedBroadcastChannel<Settings>
    private lateinit var currentTranslationChannel: ConflatedBroadcastChannel<String>
    private lateinit var currentVerseIndexChannel: ConflatedBroadcastChannel<VerseIndex>
    private lateinit var parallelTranslationsChannel: ConflatedBroadcastChannel<List<String>>
    private lateinit var verseDetailOpenState: ConflatedBroadcastChannel<Pair<VerseIndex, Int>>

    @Before
    override fun setup() {
        super.setup()

        settingsChannel = ConflatedBroadcastChannel(Settings.DEFAULT)
        `when`(readingInteractor.observeSettings()).thenReturn(settingsChannel.openSubscription())

        currentTranslationChannel = ConflatedBroadcastChannel("")
        `when`(readingInteractor.observeCurrentTranslation()).then { currentTranslationChannel.openSubscription() }

        currentVerseIndexChannel = ConflatedBroadcastChannel(VerseIndex.INVALID)
        `when`(readingInteractor.observeCurrentVerseIndex()).then { currentVerseIndexChannel.openSubscription() }

        parallelTranslationsChannel = ConflatedBroadcastChannel(emptyList())
        `when`(readingInteractor.observeParallelTranslations()).then { parallelTranslationsChannel.openSubscription() }

        verseDetailOpenState = ConflatedBroadcastChannel()
        `when`(readingInteractor.observeVerseDetailOpenState()).thenReturn(verseDetailOpenState.openSubscription())

        versePresenter = VersePresenter(readingInteractor)
        versePresenter.attachView(verseView)
    }

    @After
    override fun tearDown() {
        versePresenter.detachView()
        super.tearDown()
    }

    @Test
    fun testOnActionCopyItemClickedSuccess() {
        runBlocking {
            `when`(item.itemId).thenReturn(R.id.action_copy)
            `when`(readingInteractor.copyToClipBoard(any())).thenReturn(true)
            assertTrue(versePresenter.actionModeCallback.onActionItemClicked(actionMode, item))
            verify(verseView, times(1)).onVersesCopied()
            verify(verseView, never()).onVersesCopyShareFailed()
            verify(actionMode, times(1)).finish()
        }
    }

    @Test
    fun testOnActionCopyItemClickedFailure() {
        runBlocking {
            `when`(item.itemId).thenReturn(R.id.action_copy)
            `when`(readingInteractor.copyToClipBoard(any())).thenReturn(false)
            assertTrue(versePresenter.actionModeCallback.onActionItemClicked(actionMode, item))
            verify(verseView, never()).onVersesCopied()
            verify(verseView, times(1)).onVersesCopyShareFailed()
            verify(actionMode, times(1)).finish()
        }
    }

    @Test
    fun testOnActionShareItemClickedSuccess() {
        `when`(item.itemId).thenReturn(R.id.action_share)
        `when`(readingInteractor.share(any())).thenReturn(true)
        assertTrue(versePresenter.actionModeCallback.onActionItemClicked(actionMode, item))
        verify(verseView, never()).onVersesCopyShareFailed()
        verify(actionMode, times(1)).finish()
    }

    @Test
    fun testOnActionShareItemClickedFailure() {
        `when`(item.itemId).thenReturn(R.id.action_share)
        `when`(readingInteractor.share(any())).thenReturn(false)
        assertTrue(versePresenter.actionModeCallback.onActionItemClicked(actionMode, item))
        verify(verseView, times(1)).onVersesCopyShareFailed()
        verify(actionMode, times(1)).finish()
    }

    @Test
    fun testDestroyActionMode() {
        versePresenter.selectedVerses.add(MockContents.kjvVerses[0])
        versePresenter.actionModeCallback.onDestroyActionMode(actionMode)
        verify(verseView, times(1)).onVerseDeselected(MockContents.kjvVerses[0].verseIndex)
        assertTrue(versePresenter.selectedVerses.isEmpty())
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
    fun testSelectChapter() {
        runBlocking {
            val bookIndex = 1
            val chapterIndex = 2
            versePresenter.selectChapter(bookIndex, chapterIndex)
            verify(verseView, never()).onChapterSelectionFailed(bookIndex, chapterIndex)
        }
    }

    @Test
    fun testSelectChapterWithException() {
        runBlocking {
            `when`(readingInteractor.saveCurrentVerseIndex(any())).thenThrow(RuntimeException("Random exception"))

            val bookIndex = 1
            val chapterIndex = 2
            versePresenter.selectChapter(bookIndex, chapterIndex)
            verify(verseView, times(1)).onChapterSelectionFailed(bookIndex, chapterIndex)
        }
    }

    @Test
    fun testLoadVerses() {
        runBlocking {
            val bookIndex = 1
            val chapterIndex = 2
            `when`(readingInteractor.readVerses("", bookIndex, chapterIndex)).thenReturn(MockContents.kjvVerses)

            versePresenter.loadVerses(bookIndex, chapterIndex)
            verify(verseView, times(1)).onVersesLoaded(
                    bookIndex, chapterIndex, MockContents.kjvVerses.map { SimpleVerseItem(it, MockContents.kjvVerses.size) })
            verify(verseView, never()).onVersesLoadFailed(bookIndex, chapterIndex)
        }
    }

    @Test
    fun testLoadVersesWithExceptions() {
        runBlocking {
            val bookIndex = 1
            val chapterIndex = 2
            `when`(readingInteractor.readVerses("", bookIndex, chapterIndex)).thenThrow(RuntimeException("Random exception"))

            versePresenter.loadVerses(bookIndex, chapterIndex)
            verify(verseView, never()).onVersesLoaded(
                    bookIndex, chapterIndex, MockContents.kjvVerses.map { SimpleVerseItem(it, MockContents.kjvVerses.size) })
            verify(verseView, times(1)).onVersesLoadFailed(bookIndex, chapterIndex)
        }
    }

    @Test
    fun testLoadVersesWithParallelTranslations() {
        runBlocking {
            val translationShortName = MockContents.kjvShortName
            val parallelTranslations = listOf(MockContents.cuvShortName)
            val bookIndex = 1
            val chapterIndex = 2
            `when`(readingInteractor.readVerses(translationShortName, parallelTranslations, bookIndex, chapterIndex))
                    .thenReturn(MockContents.kjvVerses)

            currentTranslationChannel.send(translationShortName)
            parallelTranslationsChannel.send(parallelTranslations)

            versePresenter.loadVerses(bookIndex, chapterIndex)
            verify(verseView, times(1)).onVersesLoaded(
                    bookIndex, chapterIndex, MockContents.kjvVerses.map { SimpleVerseItem(it, MockContents.kjvVerses.size) })
            verify(verseView, never()).onVersesLoadFailed(bookIndex, chapterIndex)
        }
    }

    @Test
    fun testLoadVersesWithParallelTranslationsWithException() {
        runBlocking {
            val translationShortName = MockContents.kjvShortName
            val parallelTranslations = listOf(MockContents.cuvShortName)
            val bookIndex = 1
            val chapterIndex = 2
            `when`(readingInteractor.readVerses(translationShortName, parallelTranslations, bookIndex, chapterIndex))
                    .thenThrow(RuntimeException("Random exception"))

            currentTranslationChannel.send(translationShortName)
            parallelTranslationsChannel.send(parallelTranslations)

            versePresenter.loadVerses(bookIndex, chapterIndex)
            verify(verseView, never()).onVersesLoaded(
                    bookIndex, chapterIndex, MockContents.kjvVerses.map { SimpleVerseItem(it, MockContents.kjvVerses.size) })
            verify(verseView, times(1)).onVersesLoadFailed(bookIndex, chapterIndex)
        }
    }

    @Test
    fun testOnVerseClickedWithoutActionMode() {
        runBlocking {
            val verse = MockContents.kjvVerses[0]
            versePresenter.onVerseClicked(verse)
            assertTrue(versePresenter.selectedVerses.isEmpty())
            verify(verseView, never()).onVerseDeselected(any())
            verify(readingInteractor, times(1)).openVerseDetail(verse.verseIndex, VerseDetailPagerAdapter.PAGE_VERSES)
        }
    }

    @Test
    fun testOnVerseDetailOpened() {
        runBlocking {
            val verseIndex = VerseIndex(0, 0, 0)
            verseDetailOpenState.send(Pair(verseIndex, 0))

            assertEquals(verseIndex, versePresenter.selectedVerse)
            verify(verseView, never()).onVerseDeselected(any())
            verify(verseView, times(1)).onVerseSelected(verseIndex)
        }
    }

    @Test
    fun testOnVerseDetailOpenedWithAnotherVerseSelected() {
        runBlocking {
            versePresenter.selectedVerse = VerseIndex(1, 1, 1)
            val verseIndex = VerseIndex(0, 0, 0)
            verseDetailOpenState.send(Pair(verseIndex, 0))

            assertEquals(verseIndex, versePresenter.selectedVerse)
            verify(verseView, times(1)).onVerseDeselected(VerseIndex(1, 1, 1))
            verify(verseView, times(1)).onVerseSelected(verseIndex)
        }
    }

    @Test
    fun testOnVerseDetailClosed() {
        runBlocking {
            val verseIndex = VerseIndex(0, 0, 0)
            versePresenter.selectedVerse = verseIndex

            verseDetailOpenState.send(Pair(VerseIndex.INVALID, 0))

            assertFalse(versePresenter.selectedVerse.isValid())
            verify(verseView, times(1)).onVerseDeselected(verseIndex)
            verify(verseView, never()).onVerseSelected(any())
        }
    }

    @Test
    fun testVerseSelectionAndDeselection() {
        val actionMode = mock(ActionMode::class.java)
        `when`(readingInteractor.startActionMode(any())).thenReturn(actionMode)

        val verse = MockContents.kjvVerses[0]
        versePresenter.onVerseLongClicked(verse)
        assertEquals(1, versePresenter.selectedVerses.size)
        verify(readingInteractor, times(1)).startActionMode(any())
        verify(verseView, times(1)).onVerseSelected(verse.verseIndex)

        val anotherVerse = MockContents.kjvVerses[5]
        versePresenter.onVerseLongClicked(anotherVerse)
        assertEquals(2, versePresenter.selectedVerses.size)
        verify(verseView, times(1)).onVerseSelected(anotherVerse.verseIndex)

        versePresenter.onVerseClicked(anotherVerse)
        assertEquals(1, versePresenter.selectedVerses.size)
        verify(verseView, times(1)).onVerseDeselected(anotherVerse.verseIndex)

        versePresenter.onVerseClicked(verse)
        assertTrue(versePresenter.selectedVerses.isEmpty())
        verify(verseView, times(1)).onVerseDeselected(verse.verseIndex)
        verify(actionMode, times(1)).finish()
    }

    @Test
    fun testVerseSelectionAndUpdateCurrentVerseIndex() {
        runBlocking {
            val actionMode = mock(ActionMode::class.java)
            `when`(readingInteractor.startActionMode(any())).thenReturn(actionMode)

            currentVerseIndexChannel.send(VerseIndex(0, 0, 0))

            val verse = MockContents.kjvVerses[0]
            versePresenter.onVerseLongClicked(verse)
            assertEquals(1, versePresenter.selectedVerses.size)
            verify(readingInteractor, times(1)).startActionMode(any())
            verify(verseView, times(1)).onVerseSelected(verse.verseIndex)

            currentVerseIndexChannel.send(VerseIndex(0, 0, 5))
            verify(actionMode, never()).finish()

            currentVerseIndexChannel.send(VerseIndex(0, 1, 5))
            verify(actionMode, times(1)).finish()
        }
    }
}

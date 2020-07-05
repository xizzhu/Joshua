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

package me.xizzhu.android.joshua.reading

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.utils.currentTimeMillis
import org.mockito.Mock
import org.mockito.Mockito.*
import kotlin.test.*

class ReadingViewModelTest : BaseUnitTest() {
    @Mock
    private lateinit var bibleReadingManager: BibleReadingManager

    @Mock
    private lateinit var readingProgressManager: ReadingProgressManager

    @Mock
    private lateinit var translationManager: TranslationManager

    @Mock
    private lateinit var bookmarkManager: VerseAnnotationManager<Bookmark>

    @Mock
    private lateinit var highlightManager: VerseAnnotationManager<Highlight>

    @Mock
    private lateinit var noteManager: VerseAnnotationManager<Note>

    @Mock
    private lateinit var strongNumberManager: StrongNumberManager

    @Mock
    private lateinit var settingsManager: SettingsManager

    private lateinit var readingViewModel: ReadingViewModel

    @BeforeTest
    override fun setup() {
        super.setup()

        readingViewModel = ReadingViewModel(bibleReadingManager, readingProgressManager, translationManager,
                bookmarkManager, highlightManager, noteManager, strongNumberManager, settingsManager, false)
    }

    @Test
    fun testDownloadedTranslations() = runBlocking {
        `when`(translationManager.downloadedTranslations()).thenReturn(
                flowOf(
                        emptyList(),
                        emptyList(),
                        listOf(MockContents.kjvTranslationInfo, MockContents.bbeTranslationInfo),
                        listOf(MockContents.kjvTranslationInfo, MockContents.bbeTranslationInfo),
                        listOf(MockContents.bbeTranslationInfo)
                )
        )

        assertEquals(
                listOf(
                        emptyList(),
                        listOf(MockContents.kjvTranslationInfo, MockContents.bbeTranslationInfo),
                        listOf(MockContents.bbeTranslationInfo)
                ),
                readingViewModel.downloadedTranslations().toList()
        )
    }

    @Test
    fun testHasDownloadedTranslation() = runBlocking {
        `when`(translationManager.downloadedTranslations()).thenReturn(flowOf(emptyList()))
        assertFalse(readingViewModel.hasDownloadedTranslation())

        `when`(translationManager.downloadedTranslations()).thenReturn(flowOf(listOf(MockContents.bbeTranslationInfo)))
        assertTrue(readingViewModel.hasDownloadedTranslation())
    }

    @Test
    fun testCurrentTranslation() = runBlocking {
        `when`(bibleReadingManager.currentTranslation()).thenReturn(flowOf("", MockContents.kjvShortName, "", ""))

        assertEquals(listOf(MockContents.kjvShortName), readingViewModel.currentTranslation().toList())
    }

    @Test
    fun testCurrentTranslationViewData() = runBlocking {
        `when`(bibleReadingManager.currentTranslation()).thenReturn(flowOf("", MockContents.kjvShortName, "", ""))
        `when`(bibleReadingManager.parallelTranslations()).thenReturn(flowOf(emptyList(), listOf(MockContents.cuvShortName)))

        assertEquals(
                listOf(
                        CurrentTranslationViewData(MockContents.kjvShortName, emptyList()),
                        CurrentTranslationViewData(MockContents.kjvShortName, listOf(MockContents.cuvShortName))
                ),
                readingViewModel.currentTranslationViewData().toList()
        )
    }

    @Test
    fun testCurrentVerseIndex() = runBlocking {
        `when`(bibleReadingManager.currentVerseIndex()).thenReturn(
                flowOf(VerseIndex.INVALID, VerseIndex(1, 2, 3), VerseIndex.INVALID, VerseIndex.INVALID)
        )

        assertEquals(
                listOf(VerseIndex(1, 2, 3)),
                readingViewModel.currentVerseIndex().toList()
        )
    }

    @Test
    fun testCurrentVerseIndexViewData() = runBlocking {
        `when`(bibleReadingManager.currentVerseIndex()).thenReturn(flowOf(VerseIndex.INVALID, VerseIndex(0, 0, 0)))
        `when`(bibleReadingManager.currentTranslation()).thenReturn(flowOf("", MockContents.kjvShortName, "", ""))
        `when`(bibleReadingManager.readBookNames(MockContents.kjvShortName)).thenReturn(MockContents.kjvBookNames)
        `when`(bibleReadingManager.readBookShortNames(MockContents.kjvShortName)).thenReturn(MockContents.kjvBookShortNames)

        assertEquals(
                listOf(CurrentVerseIndexViewData(
                        VerseIndex(0, 0, 0),
                        MockContents.kjvBookNames[0],
                        MockContents.kjvBookShortNames[0]
                )),
                readingViewModel.currentVerseIndexViewData().toList()
        )
    }

    @Test
    fun testChapterList() = runBlocking {
        `when`(bibleReadingManager.currentVerseIndex()).thenReturn(flowOf(VerseIndex(0, 0, 0)))
        `when`(bibleReadingManager.currentTranslation()).thenReturn(flowOf("", MockContents.kjvShortName, "", ""))
        `when`(bibleReadingManager.readBookNames(MockContents.kjvShortName)).thenReturn(MockContents.kjvBookNames)

        assertEquals(
                listOf(ChapterListViewData(VerseIndex(0, 0, 0), MockContents.kjvBookNames)),
                readingViewModel.chapterList().toList()
        )
    }

    @Test
    fun testBookName() = runBlocking {
        val e = RuntimeException("random exception")
        `when`(bibleReadingManager.readBookNames(MockContents.cuvShortName)).thenThrow(e)
        `when`(bibleReadingManager.readBookNames(MockContents.kjvShortName)).thenReturn(MockContents.kjvBookNames)

        assertEquals(
                listOf(MockContents.kjvBookNames[0]),
                readingViewModel.bookName(MockContents.kjvShortName, 0).toList()
        )
    }

    @Test
    fun testBookNameWithException() = runBlocking {
        val e = RuntimeException("random exception")
        `when`(bibleReadingManager.readBookNames(MockContents.cuvShortName)).thenThrow(e)
        `when`(bibleReadingManager.readBookNames(MockContents.kjvShortName)).thenReturn(MockContents.kjvBookNames)

        readingViewModel.bookName(MockContents.cuvShortName, 0)
                .onCompletion { assertEquals(e, it) }
                .catch {}
                .collect()
    }

    @Test
    fun testVerses() = runBlocking {
        val verseIndex = VerseIndex(0, 0, 0)
        val verses = MockContents.kjvVerses
        val bookmarks = listOf(Bookmark(verseIndex, 123L))
        val highlights = listOf(Highlight(verseIndex, Highlight.COLOR_BLUE, 456L))
        val notes = listOf(Note(verseIndex, "random note", 789L))
        `when`(bibleReadingManager.currentTranslation()).thenReturn(flowOf("", MockContents.kjvShortName, "", ""))
        `when`(bibleReadingManager.parallelTranslations()).thenReturn(flowOf(emptyList()))
        `when`(bibleReadingManager.readVerses(MockContents.kjvShortName, verseIndex.bookIndex, verseIndex.chapterIndex)).thenReturn(verses)
        `when`(bookmarkManager.read(verseIndex.bookIndex, verseIndex.chapterIndex)).thenReturn(bookmarks)
        `when`(highlightManager.read(verseIndex.bookIndex, verseIndex.chapterIndex)).thenReturn(highlights)
        `when`(noteManager.read(verseIndex.bookIndex, verseIndex.chapterIndex)).thenReturn(notes)
        `when`(settingsManager.settings()).thenReturn(flowOf(Settings.DEFAULT))

        assertEquals(
                listOf(VersesViewData(Settings.DEFAULT.simpleReadingModeOn, verses, bookmarks, highlights, notes)),
                readingViewModel.verses(verseIndex.bookIndex, verseIndex.chapterIndex).toList()
        )
    }

    @Test
    fun testSaveBookmark() = runBlockingTest {
        currentTimeMillis = 1234L
        val verseUpdates = async { readingViewModel.verseUpdates().take(2).toList() }

        readingViewModel.saveBookmark(VerseIndex(1, 2, 3), true)
        readingViewModel.saveBookmark(VerseIndex(4, 5, 6), false)

        with(inOrder(bookmarkManager)) {
            verify(bookmarkManager, times(1)).remove(VerseIndex(1, 2, 3))
            verify(bookmarkManager, times(1)).save(Bookmark(VerseIndex(4, 5, 6), 1234L))
        }
        assertEquals(
                listOf(
                        VerseUpdate(VerseIndex(1, 2, 3), VerseUpdate.BOOKMARK_REMOVED),
                        VerseUpdate(VerseIndex(4, 5, 6), VerseUpdate.BOOKMARK_ADDED)
                ),
                verseUpdates.await()
        )
    }

    @Test
    fun testSaveHighlight() = runBlockingTest {
        currentTimeMillis = 1234L
        val verseUpdates = async { readingViewModel.verseUpdates().take(2).toList() }

        readingViewModel.saveHighlight(VerseIndex(1, 2, 3), Highlight.COLOR_BLUE)
        readingViewModel.saveHighlight(VerseIndex(4, 5, 6), Highlight.COLOR_NONE)

        with(inOrder(highlightManager)) {
            verify(highlightManager, times(1)).save(Highlight(VerseIndex(1, 2, 3), Highlight.COLOR_BLUE, 1234L))
            verify(highlightManager, times(1)).remove(VerseIndex(4, 5, 6))
        }
        assertEquals(
                listOf(
                        VerseUpdate(VerseIndex(1, 2, 3), VerseUpdate.HIGHLIGHT_UPDATED, Highlight.COLOR_BLUE),
                        VerseUpdate(VerseIndex(4, 5, 6), VerseUpdate.HIGHLIGHT_UPDATED, Highlight.COLOR_NONE)
                ),
                verseUpdates.await()
        )
    }

    @Test
    fun testSaveNote() = runBlockingTest {
        currentTimeMillis = 1234L
        val verseUpdates = async { readingViewModel.verseUpdates().take(2).toList() }

        readingViewModel.saveNote(VerseIndex(1, 2, 3), "random notes")
        readingViewModel.saveNote(VerseIndex(4, 5, 6), "")

        with(inOrder(noteManager)) {
            verify(noteManager, times(1)).save(Note(VerseIndex(1, 2, 3), "random notes", 1234L))
            verify(noteManager, times(1)).remove(VerseIndex(4, 5, 6))
        }
        assertEquals(
                listOf(
                        VerseUpdate(VerseIndex(1, 2, 3), VerseUpdate.NOTE_ADDED),
                        VerseUpdate(VerseIndex(4, 5, 6), VerseUpdate.NOTE_REMOVED)
                ),
                verseUpdates.await()
        )
    }
}

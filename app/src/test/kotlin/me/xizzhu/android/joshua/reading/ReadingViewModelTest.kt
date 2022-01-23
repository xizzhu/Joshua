/*
 * Copyright (C) 2022 Xizhi Zhu
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

import android.app.Application
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.Bookmark
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.Note
import me.xizzhu.android.joshua.core.ReadingProgressManager
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.StrongNumber
import me.xizzhu.android.joshua.core.StrongNumberManager
import me.xizzhu.android.joshua.core.TranslationManager
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseAnnotationManager
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.BaseViewModel
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.utils.currentTimeMillis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

class ReadingViewModelTest : BaseUnitTest() {
    private lateinit var bibleReadingManager: BibleReadingManager
    private lateinit var readingProgressManager: ReadingProgressManager
    private lateinit var translationManager: TranslationManager
    private lateinit var bookmarkManager: VerseAnnotationManager<Bookmark>
    private lateinit var highlightManager: VerseAnnotationManager<Highlight>
    private lateinit var noteManager: VerseAnnotationManager<Note>
    private lateinit var strongNumberManager: StrongNumberManager
    private lateinit var settingsManager: SettingsManager
    private lateinit var application: Application

    private lateinit var readingViewModel: ReadingViewModel

    override fun setup() {
        super.setup()

        bibleReadingManager = mockk()
        every { bibleReadingManager.currentTranslation() } returns emptyFlow()
        every { bibleReadingManager.parallelTranslations() } returns emptyFlow()
        every { bibleReadingManager.currentVerseIndex() } returns emptyFlow()
        readingProgressManager = mockk()
        translationManager = mockk()
        every { translationManager.downloadedTranslations() } returns emptyFlow()
        bookmarkManager = mockk()
        highlightManager = mockk()
        noteManager = mockk()
        strongNumberManager = mockk()
        settingsManager = mockk()
        application = mockk()

        readingViewModel = ReadingViewModel(
                bibleReadingManager, readingProgressManager, translationManager,
                bookmarkManager, highlightManager, noteManager,
                strongNumberManager, settingsManager, application
        )
    }

    @Test
    fun `test currentReadingStatus`() = runTest {
        every { bibleReadingManager.currentTranslation() } returns flow {
            delay(1000) // give time to set up the listener
            emit(MockContents.kjvShortName)
        }
        every { bibleReadingManager.parallelTranslations() } returns flowOf(listOf(MockContents.cuvShortName))
        every { bibleReadingManager.currentVerseIndex() } returns flowOf(VerseIndex(1, 2, 3))
        coEvery { bibleReadingManager.readBookNames(MockContents.kjvShortName) } returns MockContents.kjvBookNames
        coEvery { bibleReadingManager.readBookShortNames(MockContents.kjvShortName) } returns MockContents.kjvBookShortNames
        every { translationManager.downloadedTranslations() } returns flowOf(listOf(
                MockContents.kjvDownloadedTranslationInfo, MockContents.cuvDownloadedTranslationInfo, MockContents.bbeDownloadedTranslationInfo
        ))

        readingViewModel = ReadingViewModel(
                bibleReadingManager, readingProgressManager, translationManager,
                bookmarkManager, highlightManager, noteManager,
                strongNumberManager, settingsManager, application
        )

        val actual = readingViewModel.currentReadingStatus().take(2).toList()
        assertTrue(actual[0] is BaseViewModel.ViewData.Loading)
        assertEquals(MockContents.kjvShortName, (actual[1] as BaseViewModel.ViewData.Success).data.currentTranslation)
        assertEquals(listOf(MockContents.cuvShortName), (actual[1] as BaseViewModel.ViewData.Success).data.parallelTranslations)
        assertEquals(
                listOf(MockContents.bbeShortName, MockContents.kjvShortName, MockContents.cuvShortName),
                (actual[1] as BaseViewModel.ViewData.Success).data.downloadedTranslations
        )
        assertEquals(VerseIndex(1, 2, 3), (actual[1] as BaseViewModel.ViewData.Success).data.currentVerseIndex)
        assertEquals(MockContents.kjvBookNames, (actual[1] as BaseViewModel.ViewData.Success).data.bookNames)
        assertEquals(MockContents.kjvBookShortNames, (actual[1] as BaseViewModel.ViewData.Success).data.bookShortNames)
    }

    @Test
    fun `test loadVerses with simple reading mode on`() = runTest {
        every { bibleReadingManager.currentTranslation() } returns flowOf(MockContents.kjvShortName)
        every { bibleReadingManager.parallelTranslations() } returns flowOf(emptyList())
        coEvery { bibleReadingManager.readVerses(MockContents.kjvShortName, 0, 0) } returns MockContents.kjvVerses
        coEvery { highlightManager.read(0, 0) } returns emptyList()
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT.copy(simpleReadingModeOn = true))

        with(readingViewModel.loadVerses(0, 0).toList()) {
            assertEquals(2, size)
            assertTrue(this[0] is BaseViewModel.ViewData.Loading)
            assertEquals(MockContents.kjvVerses.size, (this[1] as BaseViewModel.ViewData.Success).data.items.size)
        }
    }

    @Test
    fun `test loadVerses with simple reading mode off`() = runTest {
        every { bibleReadingManager.currentTranslation() } returns flowOf(MockContents.kjvShortName)
        every { bibleReadingManager.parallelTranslations() } returns flowOf(listOf(MockContents.cuvShortName))
        coEvery { bibleReadingManager.readVerses(MockContents.kjvShortName, listOf(MockContents.cuvShortName), 0, 0) } returns MockContents.kjvVersesWithCuvParallel
        coEvery { bookmarkManager.read(0, 0) } returns emptyList()
        coEvery { highlightManager.read(0, 0) } returns emptyList()
        coEvery { noteManager.read(0, 0) } returns emptyList()
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT.copy(simpleReadingModeOn = false))

        with(readingViewModel.loadVerses(0, 0).toList()) {
            assertEquals(2, size)
            assertTrue(this[0] is BaseViewModel.ViewData.Loading)
        }
    }

    @Test
    fun `test loadVerseDetail`() = runTest {
        val verseIndex = VerseIndex(0, 0, 0)
        coEvery { bookmarkManager.read(verseIndex) } returns Bookmark(verseIndex, 12345L)
        coEvery { highlightManager.read(verseIndex) } returns Highlight(verseIndex, Highlight.COLOR_PURPLE, 12345L)
        coEvery { noteManager.read(verseIndex) } returns Note(verseIndex, "just a note", 12345L)
        coEvery { strongNumberManager.readStrongNumber(verseIndex) } returns MockContents.strongNumber.getValue(verseIndex)
        every { bibleReadingManager.currentTranslation() } returns flowOf(MockContents.kjvShortName)
        every { translationManager.downloadedTranslations() } returns flowOf(listOf(
                MockContents.kjvDownloadedTranslationInfo, MockContents.cuvDownloadedTranslationInfo, MockContents.bbeDownloadedTranslationInfo
        ))
        coEvery {
            bibleReadingManager.readVerses(
                    MockContents.kjvShortName, listOf(MockContents.bbeShortName, MockContents.cuvShortName),
                    verseIndex.bookIndex, verseIndex.chapterIndex
            )
        } returns MockContents.kjvVersesWithBbeCuvParallel
        coEvery { bibleReadingManager.readBookNames(MockContents.kjvShortName) } returns MockContents.kjvBookNames
        coEvery { bibleReadingManager.readBookNames(MockContents.bbeShortName) } returns MockContents.bbeBookNames
        coEvery { bibleReadingManager.readBookNames(MockContents.cuvShortName) } returns MockContents.cuvBookNames

        val actual = readingViewModel.loadVerseDetail(verseIndex).toList()
        assertEquals(2, actual.size)
        assertTrue(actual[0] is BaseViewModel.ViewData.Loading)
        assertEquals(VerseIndex(0, 0, 0), (actual[1] as BaseViewModel.ViewData.Success).data.verseIndex)
        assertEquals(3, (actual[1] as BaseViewModel.ViewData.Success).data.verseTextItems.size)
        (actual[1] as BaseViewModel.ViewData.Success).data.verseTextItems.forEachIndexed { index, item ->
            assertEquals(VerseIndex(0, 0, 0), item.verseIndex)
            assertEquals(
                    when (index) {
                        0 -> Verse.Text("KJV", "In the beginning God created the heaven and the earth.")
                        1 -> Verse.Text("BBE", "At the first God made the heaven and the earth.")
                        2 -> Verse.Text("中文和合本", "起初神创造天地。")
                        else -> fail()
                    },
                    item.verseText
            )
        }
        assertTrue((actual[1] as BaseViewModel.ViewData.Success).data.bookmarked)
        assertEquals(Highlight.COLOR_PURPLE, (actual[1] as BaseViewModel.ViewData.Success).data.highlightColor)
        assertEquals("just a note", (actual[1] as BaseViewModel.ViewData.Success).data.note)
        assertEquals(
                listOf(
                        StrongNumber("H7225", "beginning, chief(-est), first(-fruits, part, time), principal thing."),
                        StrongNumber("H1254", "choose, create (creator), cut down, dispatch, do, make (fat)."),
                        StrongNumber("H430", "angels, [idiom] exceeding, God (gods) (-dess, -ly), [idiom] (very) great, judges, [idiom] mighty."),
                        StrongNumber("H853", "(as such unrepresented in English)."),
                        StrongNumber("H8064", "mantle"),
                        StrongNumber("H776", "[idiom] common, country, earth, field, ground, land, [idiom] natins, way, [phrase] wilderness, world.")
                ),
                (actual[1] as BaseViewModel.ViewData.Success).data.strongNumberItems.map { it.strongNumber }
        )
    }

    @Test
    fun `test loadVerseDetail with empty verses`() = runTest {
        val verseIndex = VerseIndex(0, 0, 0)
        coEvery { bookmarkManager.read(verseIndex) } returns Bookmark(verseIndex, 0L)
        coEvery { highlightManager.read(verseIndex) } returns Highlight(verseIndex, Highlight.COLOR_PURPLE, 0L)
        coEvery { noteManager.read(verseIndex) } returns Note(verseIndex, "just a note", 0L)
        coEvery { strongNumberManager.readStrongNumber(verseIndex) } returns MockContents.strongNumber.getValue(verseIndex)
        every { bibleReadingManager.currentTranslation() } returns flowOf(MockContents.msgShortName)
        every { translationManager.downloadedTranslations() } returns flowOf(listOf(
                MockContents.kjvDownloadedTranslationInfo, MockContents.msgDownloadedTranslationInfo
        ))
        coEvery {
            bibleReadingManager.readVerses(
                    MockContents.msgShortName, listOf(MockContents.kjvShortName), verseIndex.bookIndex, verseIndex.chapterIndex
            )
        } returns MockContents.msgVersesWithKjvParallel
        coEvery { bibleReadingManager.readBookNames(MockContents.kjvShortName) } returns MockContents.kjvBookNames
        coEvery { bibleReadingManager.readBookNames(MockContents.msgShortName) } returns MockContents.msgBookNames

        val actual = readingViewModel.loadVerseDetail(verseIndex).toList()
        assertEquals(2, actual.size)
        assertTrue(actual[0] is BaseViewModel.ViewData.Loading)
        assertEquals(VerseIndex(0, 0, 0), (actual[1] as BaseViewModel.ViewData.Success).data.verseIndex)
        assertEquals(2, (actual[1] as BaseViewModel.ViewData.Success).data.verseTextItems.size)
        (actual[1] as BaseViewModel.ViewData.Success).data.verseTextItems.forEachIndexed { index, item ->
            assertEquals(VerseIndex(0, 0, 0), item.verseIndex)
            assertEquals(
                    when (index) {
                        0 -> Verse.Text("MSG", "First this: God created the Heavens and Earth—all you see, all you don't see. Earth was a soup of nothingness, a bottomless emptiness, an inky blackness. God's Spirit brooded like a bird above the watery abyss.")
                        1 -> Verse.Text("KJV", "In the beginning God created the heaven and the earth. And the earth was without form, and void; and darkness was upon the face of the deep. And the Spirit of God moved upon the face of the waters.")
                        else -> fail()
                    },
                    item.verseText
            )
        }
        assertFalse((actual[1] as BaseViewModel.ViewData.Success).data.bookmarked)
        assertEquals(Highlight.COLOR_NONE, (actual[1] as BaseViewModel.ViewData.Success).data.highlightColor)
        assertTrue((actual[1] as BaseViewModel.ViewData.Success).data.note.isEmpty())
        assertEquals(
                listOf(
                        StrongNumber("H7225", "beginning, chief(-est), first(-fruits, part, time), principal thing."),
                        StrongNumber("H1254", "choose, create (creator), cut down, dispatch, do, make (fat)."),
                        StrongNumber("H430", "angels, [idiom] exceeding, God (gods) (-dess, -ly), [idiom] (very) great, judges, [idiom] mighty."),
                        StrongNumber("H853", "(as such unrepresented in English)."),
                        StrongNumber("H8064", "mantle"),
                        StrongNumber("H776", "[idiom] common, country, earth, field, ground, land, [idiom] natins, way, [phrase] wilderness, world.")
                ),
                (actual[1] as BaseViewModel.ViewData.Success).data.strongNumberItems.map { it.strongNumber }
        )
    }

    @Test
    fun `test saveBookmark`() = runTest {
        currentTimeMillis = 1L
        coEvery { bookmarkManager.save(Bookmark(VerseIndex(1, 2, 3), 1L)) } returns Unit
        coEvery { bookmarkManager.remove(VerseIndex(1, 2, 3)) } returns Unit

        val verseUpdates = async { readingViewModel.verseUpdates().take(2).toList() }
        delay(1000) // make sure the async task is launched

        with(readingViewModel.saveBookmark(VerseIndex(1, 2, 3), toBeBookmarked = true).toList()) {
            assertTrue(this[0] is BaseViewModel.ViewData.Loading)
            assertTrue(this[1] is BaseViewModel.ViewData.Success)
        }
        with(readingViewModel.saveBookmark(VerseIndex(1, 2, 3), toBeBookmarked = false).toList()) {
            assertTrue(this[0] is BaseViewModel.ViewData.Loading)
            assertTrue(this[1] is BaseViewModel.ViewData.Success)
        }

        with(verseUpdates.await()) {
            assertEquals(2, size)

            assertEquals(VerseIndex(1, 2, 3), this[0].verseIndex)
            assertEquals(VerseUpdate.BOOKMARK_ADDED, this[0].operation)
            assertNull(this[0].data)

            assertEquals(VerseIndex(1, 2, 3), this[1].verseIndex)
            assertEquals(VerseUpdate.BOOKMARK_REMOVED, this[1].operation)
            assertNull(this[1].data)
        }
    }

    @Test
    fun `test saveHighlight`() = runTest {
        currentTimeMillis = 1L
        coEvery { highlightManager.save(Highlight(VerseIndex(1, 2, 3), Highlight.COLOR_BLUE, 1L)) } returns Unit
        coEvery { highlightManager.save(Highlight(VerseIndex(1, 2, 3), Highlight.COLOR_PURPLE, 1L)) } returns Unit
        coEvery { highlightManager.remove(VerseIndex(1, 2, 3)) } returns Unit

        val verseUpdates = async { readingViewModel.verseUpdates().take(3).toList() }
        delay(1000) // make sure the async task is launched

        with(readingViewModel.saveHighlight(VerseIndex(1, 2, 3), Highlight.COLOR_BLUE).toList()) {
            assertTrue(this[0] is BaseViewModel.ViewData.Loading)
            assertTrue(this[1] is BaseViewModel.ViewData.Success)
        }
        with(readingViewModel.saveHighlight(VerseIndex(1, 2, 3), Highlight.COLOR_PURPLE).toList()) {
            assertTrue(this[0] is BaseViewModel.ViewData.Loading)
            assertTrue(this[1] is BaseViewModel.ViewData.Success)
        }
        with(readingViewModel.saveHighlight(VerseIndex(1, 2, 3), Highlight.COLOR_NONE).toList()) {
            assertTrue(this[0] is BaseViewModel.ViewData.Loading)
            assertTrue(this[1] is BaseViewModel.ViewData.Success)
        }

        with(verseUpdates.await()) {
            assertEquals(3, size)

            assertEquals(VerseIndex(1, 2, 3), this[0].verseIndex)
            assertEquals(VerseUpdate.HIGHLIGHT_UPDATED, this[0].operation)
            assertEquals(Highlight.COLOR_BLUE, this[0].data)

            assertEquals(VerseIndex(1, 2, 3), this[1].verseIndex)
            assertEquals(VerseUpdate.HIGHLIGHT_UPDATED, this[1].operation)
            assertEquals(Highlight.COLOR_PURPLE, this[1].data)

            assertEquals(VerseIndex(1, 2, 3), this[2].verseIndex)
            assertEquals(VerseUpdate.HIGHLIGHT_UPDATED, this[2].operation)
            assertEquals(Highlight.COLOR_NONE, this[2].data)
        }
    }

    @Test
    fun `test saveNote`() = runTest {
        currentTimeMillis = 1L
        coEvery { noteManager.save(Note(VerseIndex(1, 2, 3), "note", 1L)) } returns Unit
        coEvery { noteManager.save(Note(VerseIndex(1, 2, 3), "updated note", 1L)) } returns Unit
        coEvery { noteManager.remove(VerseIndex(1, 2, 3)) } returns Unit

        val verseUpdates = async { readingViewModel.verseUpdates().take(3).toList() }
        delay(1000) // make sure the async task is launched

        with(readingViewModel.saveNote(VerseIndex(1, 2, 3), "note").toList()) {
            assertTrue(this[0] is BaseViewModel.ViewData.Loading)
            assertTrue(this[1] is BaseViewModel.ViewData.Success)
        }
        with(readingViewModel.saveNote(VerseIndex(1, 2, 3), "updated note").toList()) {
            assertTrue(this[0] is BaseViewModel.ViewData.Loading)
            assertTrue(this[1] is BaseViewModel.ViewData.Success)
        }
        with(readingViewModel.saveNote(VerseIndex(1, 2, 3), "").toList()) {
            assertTrue(this[0] is BaseViewModel.ViewData.Loading)
            assertTrue(this[1] is BaseViewModel.ViewData.Success)
        }

        with(verseUpdates.await()) {
            assertEquals(3, size)

            assertEquals(VerseIndex(1, 2, 3), this[0].verseIndex)
            assertEquals(VerseUpdate.NOTE_ADDED, this[0].operation)
            assertNull(this[0].data)

            assertEquals(VerseIndex(1, 2, 3), this[1].verseIndex)
            assertEquals(VerseUpdate.NOTE_ADDED, this[1].operation)
            assertNull(this[1].data)

            assertEquals(VerseIndex(1, 2, 3), this[2].verseIndex)
            assertEquals(VerseUpdate.NOTE_REMOVED, this[2].operation)
            assertNull(this[2].data)
        }
    }

    @Test
    fun `test downloadStrongNumber`() = runTest {
        every { strongNumberManager.download() } returns flowOf(0, 20, 99, -1, 100)
        with(readingViewModel.downloadStrongNumber().toList()) {
            assertEquals(5, size)

            assertEquals(0, (this[0] as BaseViewModel.ViewData.Loading).data)
            assertEquals(20, (this[1] as BaseViewModel.ViewData.Loading).data)
            assertEquals(99, (this[2] as BaseViewModel.ViewData.Loading).data)
            assertTrue(this[3] is BaseViewModel.ViewData.Failure)
            assertEquals(100, (this[4] as BaseViewModel.ViewData.Success).data)
        }
    }

    @Test
    fun `test downloadStrongNumber with exception`() = runTest {
        every { strongNumberManager.download() } returns flow { throw RuntimeException("random exception") }
        with(readingViewModel.downloadStrongNumber().toList()) {
            assertEquals(1, size)

            assertTrue(this[0] is BaseViewModel.ViewData.Failure)
        }
    }
}

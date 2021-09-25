/*
 * Copyright (C) 2021 Xizhi Zhu
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
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.Bookmark
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.Note
import me.xizzhu.android.joshua.core.ReadingProgressManager
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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
    fun `test loadVerseDetail`() = testDispatcher.runBlockingTest {
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
    fun `test loadVerseDetail with empty verses`() = testDispatcher.runBlockingTest {
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
}

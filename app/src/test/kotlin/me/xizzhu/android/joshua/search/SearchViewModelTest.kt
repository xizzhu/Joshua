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

package me.xizzhu.android.joshua.search

import android.app.Application
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.*

@RunWith(RobolectricTestRunner::class)
class SearchViewModelTest : BaseUnitTest() {
    private lateinit var bibleReadingManager: BibleReadingManager
    private lateinit var searchManager: SearchManager
    private lateinit var settingsManager: SettingsManager
    private lateinit var application: Application

    private lateinit var searchViewModel: SearchViewModel

    @BeforeTest
    override fun setup() {
        super.setup()

        bibleReadingManager = mockk()
        coEvery { bibleReadingManager.currentTranslation() } returns flowOf(MockContents.kjvShortName)
        coEvery { bibleReadingManager.readBookNames(MockContents.kjvShortName) } returns MockContents.kjvBookNames
        coEvery { bibleReadingManager.readBookShortNames(MockContents.kjvShortName) } returns MockContents.kjvBookShortNames
        coEvery { bibleReadingManager.readVerses(MockContents.kjvShortName, any()) } returns emptyMap()

        searchManager = mockk()
        every { searchManager.configuration() } returns flowOf(SearchConfiguration(true, true, true, true, true))
        every { searchManager.saveConfiguration(any()) } returns Unit
        coEvery { searchManager.search(any()) } returns SearchResult(emptyList(), emptyList(), emptyList(), emptyList())

        settingsManager = mockk()
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT)

        application = mockk()
        every { application.getString(R.string.title_bookmarks) } returns "Bookmarks"
        every { application.getString(R.string.title_highlights) } returns "Highlights"
        every { application.getString(R.string.title_notes) } returns "Notes"
        every { application.getString(R.string.toast_search_result, any()) } answers { "${(it.invocation.args[1] as Array<Any?>)[0]} result(s) found." }

        searchViewModel = SearchViewModel(bibleReadingManager, searchManager, settingsManager, application)
    }

    @Test
    fun `test includeOldTestament`() = runTest {
        searchViewModel.includeOldTestament(false)
        searchViewModel.includeOldTestament(false)
        searchViewModel.includeOldTestament(true)
        searchViewModel.includeOldTestament(true)

        verify(exactly = 2) { searchManager.saveConfiguration(any()) }
    }

    @Test
    fun `test includeNewTestament`() = runTest {
        searchViewModel.includeNewTestament(false)
        searchViewModel.includeNewTestament(false)
        searchViewModel.includeNewTestament(true)
        searchViewModel.includeNewTestament(true)

        verify(exactly = 2) { searchManager.saveConfiguration(any()) }
    }

    @Test
    fun `test includeBookmarks`() = runTest {
        searchViewModel.includeBookmarks(false)
        searchViewModel.includeBookmarks(false)
        searchViewModel.includeBookmarks(true)
        searchViewModel.includeBookmarks(true)

        verify(exactly = 2) { searchManager.saveConfiguration(any()) }
    }

    @Test
    fun `test includeHighlights`() = runTest {
        searchViewModel.includeHighlights(false)
        searchViewModel.includeHighlights(false)
        searchViewModel.includeHighlights(true)
        searchViewModel.includeHighlights(true)

        verify(exactly = 2) { searchManager.saveConfiguration(any()) }
    }

    @Test
    fun `test includeNotes`() = runTest {
        searchViewModel.includeNotes(false)
        searchViewModel.includeNotes(false)
        searchViewModel.includeNotes(true)
        searchViewModel.includeNotes(true)

        verify(exactly = 2) { searchManager.saveConfiguration(any()) }
    }

    @Test
    fun `test search() with empty query`() = runTest {
        searchViewModel.search("", true)
        delay(1000L)

        val actual = searchViewModel.viewState().first()
        assertTrue(actual.searchQuery.isEmpty())
        assertTrue(actual.instantSearch)
        assertTrue(actual.searchResults.isEmpty())
    }

    @Test
    fun `test search() with one-character query`() = runTest {
        searchViewModel.search("1", true)
        delay(1000L)

        val actual = searchViewModel.viewState().first()
        assertEquals("1", actual.searchQuery)
        assertTrue(actual.instantSearch)
        assertTrue(actual.searchResults.isEmpty())
    }

    @Test
    fun `test search() with empty result`() = runTest {
        searchViewModel.search("query", false)
        delay(1000L)

        val actual = searchViewModel.viewState().first()
        assertEquals("query", actual.searchQuery)
        assertFalse(actual.instantSearch)
        assertTrue(actual.searchResults.isEmpty())
    }

    @Test
    fun `test calling search() multiple times`() = runTest {
        coEvery { searchManager.search("not used") } returns SearchResult(listOf(MockContents.kjvVerses[1]), emptyList(), emptyList(), emptyList())
        coEvery { searchManager.search("query") } returns SearchResult(listOf(MockContents.kjvVerses[0]), emptyList(), emptyList(), emptyList())

        searchViewModel.search("invalid", true)
        delay(1000L)
        val actual1 = searchViewModel.viewState().first()
        assertEquals("invalid", actual1.searchQuery)
        assertTrue(actual1.instantSearch)
        assertTrue(actual1.searchResults.isEmpty())

        searchViewModel.search("not used", false)
        delay(100L) // delay is not long enough, so the search should NOT be executed
        val actual2 = searchViewModel.viewState().first()
        assertEquals("invalid", actual2.searchQuery)
        assertTrue(actual2.instantSearch)
        assertTrue(actual2.searchResults.isEmpty())

        searchViewModel.search("query", false)
        delay(1000L)

        val actual = searchViewModel.viewState().first()
        assertEquals("query", actual.searchQuery)
        assertFalse(actual.instantSearch)

        assertEquals(2, actual.searchResults.size)
        assertEquals("Genesis", (actual.searchResults[0] as TitleItem).title.toString())
        assertEquals(
                "Gen. 1:1\nIn the beginning God created the heaven and the earth.",
                (actual.searchResults[1] as SearchVerseItem).textForDisplay.toString()
        )
    }

    @Test
    fun `test search() with verses only`() = runTest {
        coEvery { searchManager.search("query") } returns SearchResult(
                listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[2], MockContents.kjvExtraVerses[0], MockContents.kjvExtraVerses[1]),
                emptyList(),
                emptyList(),
                emptyList()
        )

        searchViewModel.search("query", false)
        delay(1000L)

        val actual = searchViewModel.viewState().first()
        assertEquals("query", actual.searchQuery)
        assertFalse(actual.instantSearch)

        assertEquals(6, actual.searchResults.size)
        assertEquals("Genesis", (actual.searchResults[0] as TitleItem).title.toString())
        assertEquals(
                "Gen. 1:1\nIn the beginning God created the heaven and the earth.",
                (actual.searchResults[1] as SearchVerseItem).textForDisplay.toString()
        )
        assertEquals(
                "Gen. 1:3\nAnd God said, Let there be light: and there was light.",
                (actual.searchResults[2] as SearchVerseItem).textForDisplay.toString()
        )
        assertEquals(
                "Gen. 10:10\nAnd the beginning of his kingdom was Babel, and Erech, and Accad, and Calneh, in the land of Shinar.",
                (actual.searchResults[3] as SearchVerseItem).textForDisplay.toString()
        )
        assertEquals("Exodus", (actual.searchResults[4] as TitleItem).title.toString())
        assertEquals(
                "Ex. 23:19\nThe first of the firstfruits of thy land thou shalt bring into the house of the LORD thy God. Thou shalt not seethe a kid in his mother’s milk.",
                (actual.searchResults[5] as SearchVerseItem).textForDisplay.toString()
        )
    }

    @Test
    fun `test search() with verses and bookmarks`() = runTest {
        coEvery { searchManager.search("query") } returns SearchResult(
                listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[2]),
                listOf(Pair(Bookmark(VerseIndex(0, 0, 0), 12345L), MockContents.kjvVerses[0])),
                emptyList(),
                emptyList()
        )

        searchViewModel.search("query", false)
        delay(1000L)

        val actual = searchViewModel.viewState().first()
        assertEquals("query", actual.searchQuery)
        assertFalse(actual.instantSearch)

        assertEquals(5, actual.searchResults.size)
        assertEquals("Bookmarks", (actual.searchResults[0] as TitleItem).title.toString())
        assertEquals(
                "Gen. 1:1\nIn the beginning God created the heaven and the earth.",
                (actual.searchResults[1] as SearchVerseItem).textForDisplay.toString()
        )
        assertEquals("Genesis", (actual.searchResults[2] as TitleItem).title.toString())
        assertEquals(
                "Gen. 1:1\nIn the beginning God created the heaven and the earth.",
                (actual.searchResults[3] as SearchVerseItem).textForDisplay.toString()
        )
        assertEquals(
                "Gen. 1:3\nAnd God said, Let there be light: and there was light.",
                (actual.searchResults[4] as SearchVerseItem).textForDisplay.toString()
        )
    }

    @Test
    fun `test search() with verses and highlights`() = runTest {
        coEvery { searchManager.search("query") } returns SearchResult(
                listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[2]),
                emptyList(),
                listOf(Pair(Highlight(VerseIndex(0, 0, 0), Highlight.COLOR_PINK, 12345L), MockContents.kjvVerses[0])),
                emptyList()
        )

        searchViewModel.search("query", false)
        delay(1000L)

        val actual = searchViewModel.viewState().first()
        assertEquals("query", actual.searchQuery)
        assertFalse(actual.instantSearch)

        assertEquals(5, actual.searchResults.size)
        assertEquals("Highlights", (actual.searchResults[0] as TitleItem).title.toString())
        assertEquals(
                "Gen. 1:1\nIn the beginning God created the heaven and the earth.",
                (actual.searchResults[1] as SearchVerseItem).textForDisplay.toString()
        )
        assertEquals("Genesis", (actual.searchResults[2] as TitleItem).title.toString())
        assertEquals(
                "Gen. 1:1\nIn the beginning God created the heaven and the earth.",
                (actual.searchResults[3] as SearchVerseItem).textForDisplay.toString()
        )
        assertEquals(
                "Gen. 1:3\nAnd God said, Let there be light: and there was light.",
                (actual.searchResults[4] as SearchVerseItem).textForDisplay.toString()
        )
    }

    @Test
    fun `test search() with verses and notes`() = runTest {
        coEvery { searchManager.search("query") } returns SearchResult(
                listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[2]),
                emptyList(),
                emptyList(),
                listOf(Pair(Note(VerseIndex(0, 0, 9), "just a note", 12345L), MockContents.kjvVerses[9]))
        )

        searchViewModel.search("query", false)
        delay(1000L)

        val actual = searchViewModel.viewState().first()
        assertEquals("query", actual.searchQuery)
        assertFalse(actual.instantSearch)

        assertEquals(5, actual.searchResults.size)
        assertEquals("Notes", (actual.searchResults[0] as TitleItem).title.toString())
        assertEquals(
                "Gen. 1:10 And God called the dry land Earth; and the gathering together of the waters called he Seas: and God saw that it was good.",
                (actual.searchResults[1] as SearchNoteItem).verseForDisplay.toString()
        )
        assertEquals("just a note", (actual.searchResults[1] as SearchNoteItem).noteForDisplay.toString())
        assertEquals("Genesis", (actual.searchResults[2] as TitleItem).title.toString())
        assertEquals(
                "Gen. 1:1\nIn the beginning God created the heaven and the earth.",
                (actual.searchResults[3] as SearchVerseItem).textForDisplay.toString()
        )
        assertEquals(
                "Gen. 1:3\nAnd God said, Let there be light: and there was light.",
                (actual.searchResults[4] as SearchVerseItem).textForDisplay.toString()
        )
    }

    @Test
    fun `test search() with verses, bookmarks, highlights, and notes`() = runTest {
        coEvery { searchManager.search("query") } returns SearchResult(
                listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[2]),
                listOf(Pair(Bookmark(VerseIndex(0, 0, 0), 12345L), MockContents.kjvVerses[0])),
                listOf(Pair(Highlight(VerseIndex(0, 0, 0), Highlight.COLOR_PINK, 12345L), MockContents.kjvVerses[0])),
                listOf(Pair(Note(VerseIndex(0, 0, 9), "just a note", 12345L), MockContents.kjvVerses[9]))
        )

        searchViewModel.search("query", false)
        delay(1000L)

        val actual = searchViewModel.viewState().first()
        assertEquals("query", actual.searchQuery)
        assertFalse(actual.instantSearch)

        assertEquals(9, actual.searchResults.size)
        assertEquals("Notes", (actual.searchResults[0] as TitleItem).title.toString())
        assertEquals(
                "Gen. 1:10 And God called the dry land Earth; and the gathering together of the waters called he Seas: and God saw that it was good.",
                (actual.searchResults[1] as SearchNoteItem).verseForDisplay.toString()
        )
        assertEquals("just a note", (actual.searchResults[1] as SearchNoteItem).noteForDisplay.toString())
        assertEquals("Bookmarks", (actual.searchResults[2] as TitleItem).title.toString())
        assertEquals(
                "Gen. 1:1\nIn the beginning God created the heaven and the earth.",
                (actual.searchResults[3] as SearchVerseItem).textForDisplay.toString()
        )
        assertEquals("Highlights", (actual.searchResults[4] as TitleItem).title.toString())
        assertEquals(
                "Gen. 1:1\nIn the beginning God created the heaven and the earth.",
                (actual.searchResults[5] as SearchVerseItem).textForDisplay.toString()
        )
        assertEquals("Genesis", (actual.searchResults[6] as TitleItem).title.toString())
        assertEquals(
                "Gen. 1:1\nIn the beginning God created the heaven and the earth.",
                (actual.searchResults[7] as SearchVerseItem).textForDisplay.toString()
        )
        assertEquals(
                "Gen. 1:3\nAnd God said, Let there be light: and there was light.",
                (actual.searchResults[8] as SearchVerseItem).textForDisplay.toString()
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test loadVersesForPreview() with invalid verse index`() = runTest {
        searchViewModel.loadVersesForPreview(VerseIndex.INVALID).getOrThrow()
    }

    @Test
    fun `test loadVersesForPreview()`() = runTest {
        coEvery { bibleReadingManager.currentTranslation() } returns flowOf(MockContents.kjvShortName)
        coEvery {
            bibleReadingManager.readVerses(MockContents.kjvShortName, 0, 0)
        } returns listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[1], MockContents.kjvVerses[2])
        coEvery { bibleReadingManager.readBookShortNames(MockContents.kjvShortName) } returns MockContents.kjvBookShortNames
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT)

        val actual = searchViewModel.loadVersesForPreview(VerseIndex(0, 0, 1)).getOrThrow()
        assertEquals(Settings.DEFAULT, actual.settings)
        assertEquals("Gen., 1", actual.title)
        assertEquals(3, actual.items.size)
        assertEquals(1, actual.currentPosition)
    }


    @Test
    fun `test toSearchVersePreviewItems() with single verse`() {
        searchViewModel.searchRequest.value = SearchViewModel.SearchRequest("God", true)
        val actual = with(searchViewModel) { toSearchVersePreviewItems(listOf(MockContents.kjvVerses[0])) }
        assertEquals(1, actual.size)
        assertEquals("1:1 In the beginning God created the heaven and the earth.", actual[0].textForDisplay.toString())
    }

    @Test
    fun `test toSearchVersePreviewItems() with multiple verses`() {
        searchViewModel.searchRequest.value = SearchViewModel.SearchRequest("God", true)
        val actual = with(searchViewModel) { toSearchVersePreviewItems(listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[1])) }
        assertEquals(2, actual.size)
        assertEquals("1:1 In the beginning God created the heaven and the earth.", actual[0].textForDisplay.toString())
        assertEquals(
                "1:2 And the earth was without form, and void; and darkness was upon the face of the deep. And the Spirit of God moved upon the face of the waters.",
                actual[1].textForDisplay.toString()
        )
    }

    @Test
    fun `test toSearchVersePreviewItems() with multiple verses but not consecutive`() {
        searchViewModel.searchRequest.value = SearchViewModel.SearchRequest("God", true)
        val actual = with(searchViewModel) { toSearchVersePreviewItems(listOf(MockContents.msgVerses[0], MockContents.msgVerses[1], MockContents.msgVerses[2])) }
        assertEquals(2, actual.size)
        assertEquals(
                "1:1-2 First this: God created the Heavens and Earth—all you see, all you don't see. Earth was a soup of nothingness, a bottomless emptiness, an inky blackness. God's Spirit brooded like a bird above the watery abyss.",
                actual[0].textForDisplay.toString()
        )
        assertEquals(
                "1:3 God spoke: \"Light!\"\nAnd light appeared.\nGod saw that light was good\nand separated light from dark.\nGod named the light Day,\nhe named the dark Night.\nIt was evening, it was morning—\nDay One.",
                actual[1].textForDisplay.toString()
        )
    }
}

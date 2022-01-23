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
import me.xizzhu.android.joshua.infra.BaseViewModel
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
        coEvery { searchManager.search(any()) } returns SearchResult(emptyList(), emptyList(), emptyList(), emptyList())

        settingsManager = mockk()

        application = mockk()
        every { application.getString(R.string.title_bookmarks) } returns "Bookmarks"
        every { application.getString(R.string.title_highlights) } returns "Highlights"
        every { application.getString(R.string.title_notes) } returns "Notes"
        every { application.getString(R.string.toast_search_result, any()) } answers { "${(it.invocation.args[1] as Array<Any?>)[0]} result(s) found." }

        searchViewModel = SearchViewModel(bibleReadingManager, searchManager, settingsManager, application)
    }

    @Test
    fun `test includeOldTestament`() = runTest {
        every {
            searchManager.saveConfiguration(SearchConfiguration(
                    includeOldTestament = false, includeNewTestament = true, includeBookmarks = true, includeHighlights = true, includeNotes = true
            ))
        } returns Unit

        searchViewModel.includeOldTestament(false)
        searchViewModel.includeOldTestament(true)

        verify(exactly = 1) { searchManager.saveConfiguration(any()) }
    }

    @Test
    fun `test includeNewTestament`() = runTest {
        every {
            searchManager.saveConfiguration(SearchConfiguration(
                    includeOldTestament = true, includeNewTestament = false, includeBookmarks = true, includeHighlights = true, includeNotes = true
            ))
        } returns Unit

        searchViewModel.includeNewTestament(false)
        searchViewModel.includeNewTestament(true)

        verify(exactly = 1) { searchManager.saveConfiguration(any()) }
    }

    @Test
    fun `test includeBookmarks`() = runTest {
        every {
            searchManager.saveConfiguration(SearchConfiguration(
                    includeOldTestament = true, includeNewTestament = true, includeBookmarks = false, includeHighlights = true, includeNotes = true
            ))
        } returns Unit

        searchViewModel.includeBookmarks(false)
        searchViewModel.includeBookmarks(true)

        verify(exactly = 1) { searchManager.saveConfiguration(any()) }
    }

    @Test
    fun `test includeHighlights`() = runTest {
        every {
            searchManager.saveConfiguration(SearchConfiguration(
                    includeOldTestament = true, includeNewTestament = true, includeBookmarks = true, includeHighlights = false, includeNotes = true
            ))
        } returns Unit

        searchViewModel.includeHighlights(false)
        searchViewModel.includeHighlights(true)

        verify(exactly = 1) { searchManager.saveConfiguration(any()) }
    }

    @Test
    fun `test includeNotes`() = runTest {
        every {
            searchManager.saveConfiguration(SearchConfiguration(
                    includeOldTestament = true, includeNewTestament = true, includeBookmarks = true, includeHighlights = true, includeNotes = false
            ))
        } returns Unit

        searchViewModel.includeNotes(false)
        searchViewModel.includeNotes(true)

        verify(exactly = 1) { searchManager.saveConfiguration(any()) }
    }

    @Test
    fun `test search() with empty query`() = runTest {
        searchViewModel.search("", true)
        delay(1000L)

        val actual = searchViewModel.searchResult().first()
        assertTrue(actual is BaseViewModel.ViewData.Success)
        assertTrue(actual.data.items.isEmpty())
        assertTrue(actual.data.query.isEmpty())
        assertTrue(actual.data.instanceSearch)
        assertTrue(actual.data.toast.isEmpty())
    }

    @Test
    fun `test search() with empty result`() = runTest {
        searchViewModel.search("query", false)
        delay(1000L)

        searchViewModel.searchResult().first().assertSuccessEmpty("query", false)
    }

    private fun BaseViewModel.ViewData<SearchResultViewData>.assertSuccessEmpty(query: String, instanceSearch: Boolean) {
        assertTrue(this is BaseViewModel.ViewData.Success)
        assertTrue(data.items.isEmpty())
        assertEquals(query, data.query)
        assertEquals(instanceSearch, data.instanceSearch)
        assertEquals("0 result(s) found.", data.toast)
    }

    @Test
    fun `test calling search() multiple times`() = runTest {
        coEvery { searchManager.search("not used") } returns SearchResult(listOf(MockContents.kjvVerses[1]), emptyList(), emptyList(), emptyList())
        coEvery { searchManager.search("query") } returns SearchResult(listOf(MockContents.kjvVerses[0]), emptyList(), emptyList(), emptyList())

        searchViewModel.search("invalid", true)
        delay(1000L)
        searchViewModel.searchResult().first().assertSuccessEmpty("invalid", true)

        searchViewModel.search("not used", false)
        delay(100L) // delay is not long enough, so the search should NOT be executed
        searchViewModel.searchResult().first().assertSuccessEmpty("invalid", true)

        searchViewModel.search("query", false)
        delay(1000L)

        val actual = searchViewModel.searchResult().first()
        assertTrue(actual is BaseViewModel.ViewData.Success)

        assertEquals(2, actual.data.items.size)
        assertEquals("Genesis", (actual.data.items[0] as TitleItem).title.toString())
        assertEquals(
                "Gen. 1:1\nIn the beginning God created the heaven and the earth.",
                (actual.data.items[1] as SearchVerseItem).textForDisplay.toString()
        )

        assertEquals("query", actual.data.query)
        assertFalse(actual.data.instanceSearch)
        assertEquals("1 result(s) found.", actual.data.toast)
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

        val actual = searchViewModel.searchResult().first()
        assertTrue(actual is BaseViewModel.ViewData.Success)

        assertEquals(6, actual.data.items.size)
        assertEquals("Genesis", (actual.data.items[0] as TitleItem).title.toString())
        assertEquals(
                "Gen. 1:1\nIn the beginning God created the heaven and the earth.",
                (actual.data.items[1] as SearchVerseItem).textForDisplay.toString()
        )
        assertEquals(
                "Gen. 1:3\nAnd God said, Let there be light: and there was light.",
                (actual.data.items[2] as SearchVerseItem).textForDisplay.toString()
        )
        assertEquals(
                "Gen. 10:10\nAnd the beginning of his kingdom was Babel, and Erech, and Accad, and Calneh, in the land of Shinar.",
                (actual.data.items[3] as SearchVerseItem).textForDisplay.toString()
        )
        assertEquals("Exodus", (actual.data.items[4] as TitleItem).title.toString())
        assertEquals(
                "Ex. 23:19\nThe first of the firstfruits of thy land thou shalt bring into the house of the LORD thy God. Thou shalt not seethe a kid in his mother’s milk.",
                (actual.data.items[5] as SearchVerseItem).textForDisplay.toString()
        )

        assertEquals("query", actual.data.query)
        assertFalse(actual.data.instanceSearch)
        assertEquals("4 result(s) found.", actual.data.toast)
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

        val actual = searchViewModel.searchResult().first()
        assertTrue(actual is BaseViewModel.ViewData.Success)

        assertEquals(5, actual.data.items.size)
        assertEquals("Bookmarks", (actual.data.items[0] as TitleItem).title.toString())
        assertEquals(
                "Gen. 1:1\nIn the beginning God created the heaven and the earth.",
                (actual.data.items[1] as SearchVerseItem).textForDisplay.toString()
        )
        assertEquals("Genesis", (actual.data.items[2] as TitleItem).title.toString())
        assertEquals(
                "Gen. 1:1\nIn the beginning God created the heaven and the earth.",
                (actual.data.items[3] as SearchVerseItem).textForDisplay.toString()
        )
        assertEquals(
                "Gen. 1:3\nAnd God said, Let there be light: and there was light.",
                (actual.data.items[4] as SearchVerseItem).textForDisplay.toString()
        )

        assertEquals("query", actual.data.query)
        assertFalse(actual.data.instanceSearch)
        assertEquals("3 result(s) found.", actual.data.toast)
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

        val actual = searchViewModel.searchResult().first()
        assertTrue(actual is BaseViewModel.ViewData.Success)

        assertEquals(5, actual.data.items.size)
        assertEquals("Highlights", (actual.data.items[0] as TitleItem).title.toString())
        assertEquals(
                "Gen. 1:1\nIn the beginning God created the heaven and the earth.",
                (actual.data.items[1] as SearchVerseItem).textForDisplay.toString()
        )
        assertEquals("Genesis", (actual.data.items[2] as TitleItem).title.toString())
        assertEquals(
                "Gen. 1:1\nIn the beginning God created the heaven and the earth.",
                (actual.data.items[3] as SearchVerseItem).textForDisplay.toString()
        )
        assertEquals(
                "Gen. 1:3\nAnd God said, Let there be light: and there was light.",
                (actual.data.items[4] as SearchVerseItem).textForDisplay.toString()
        )

        assertEquals("query", actual.data.query)
        assertFalse(actual.data.instanceSearch)
        assertEquals("3 result(s) found.", actual.data.toast)
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

        val actual = searchViewModel.searchResult().first()
        assertTrue(actual is BaseViewModel.ViewData.Success)

        assertEquals(5, actual.data.items.size)
        assertEquals("Notes", (actual.data.items[0] as TitleItem).title.toString())
        assertEquals(
                "Gen. 1:10 And God called the dry land Earth; and the gathering together of the waters called he Seas: and God saw that it was good.",
                (actual.data.items[1] as SearchNoteItem).verseForDisplay.toString()
        )
        assertEquals("just a note", (actual.data.items[1] as SearchNoteItem).noteForDisplay.toString())
        assertEquals("Genesis", (actual.data.items[2] as TitleItem).title.toString())
        assertEquals(
                "Gen. 1:1\nIn the beginning God created the heaven and the earth.",
                (actual.data.items[3] as SearchVerseItem).textForDisplay.toString()
        )
        assertEquals(
                "Gen. 1:3\nAnd God said, Let there be light: and there was light.",
                (actual.data.items[4] as SearchVerseItem).textForDisplay.toString()
        )

        assertEquals("query", actual.data.query)
        assertFalse(actual.data.instanceSearch)
        assertEquals("3 result(s) found.", actual.data.toast)
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

        val actual = searchViewModel.searchResult().first()
        assertTrue(actual is BaseViewModel.ViewData.Success)

        assertEquals(9, actual.data.items.size)
        assertEquals("Notes", (actual.data.items[0] as TitleItem).title.toString())
        assertEquals(
                "Gen. 1:10 And God called the dry land Earth; and the gathering together of the waters called he Seas: and God saw that it was good.",
                (actual.data.items[1] as SearchNoteItem).verseForDisplay.toString()
        )
        assertEquals("just a note", (actual.data.items[1] as SearchNoteItem).noteForDisplay.toString())
        assertEquals("Bookmarks", (actual.data.items[2] as TitleItem).title.toString())
        assertEquals(
                "Gen. 1:1\nIn the beginning God created the heaven and the earth.",
                (actual.data.items[3] as SearchVerseItem).textForDisplay.toString()
        )
        assertEquals("Highlights", (actual.data.items[4] as TitleItem).title.toString())
        assertEquals(
                "Gen. 1:1\nIn the beginning God created the heaven and the earth.",
                (actual.data.items[5] as SearchVerseItem).textForDisplay.toString()
        )
        assertEquals("Genesis", (actual.data.items[6] as TitleItem).title.toString())
        assertEquals(
                "Gen. 1:1\nIn the beginning God created the heaven and the earth.",
                (actual.data.items[7] as SearchVerseItem).textForDisplay.toString()
        )
        assertEquals(
                "Gen. 1:3\nAnd God said, Let there be light: and there was light.",
                (actual.data.items[8] as SearchVerseItem).textForDisplay.toString()
        )

        assertEquals("query", actual.data.query)
        assertFalse(actual.data.instanceSearch)
        assertEquals("5 result(s) found.", actual.data.toast)
    }

    @Test
    fun `test loadVersesForPreview() with invalid verse index`() = runTest {
        val actual = searchViewModel.loadVersesForPreview(VerseIndex.INVALID).toList()
        assertEquals(2, actual.size)
        assertTrue(actual[0] is BaseViewModel.ViewData.Loading)
        assertTrue(actual[1] is BaseViewModel.ViewData.Failure)
    }

    @Test
    fun `test loadVersesForPreview()`() = runTest {
        coEvery { bibleReadingManager.currentTranslation() } returns flowOf(MockContents.kjvShortName)
        coEvery {
            bibleReadingManager.readVerses(MockContents.kjvShortName, 0, 0)
        } returns listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[1], MockContents.kjvVerses[2])
        coEvery { bibleReadingManager.readBookShortNames(MockContents.kjvShortName) } returns MockContents.kjvBookShortNames
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT)

        val actual = searchViewModel.loadVersesForPreview(VerseIndex(0, 0, 1)).toList()
        assertEquals(2, actual.size)
        assertTrue(actual[0] is BaseViewModel.ViewData.Loading)

        assertEquals(Settings.DEFAULT, (actual[1] as BaseViewModel.ViewData.Success).data.settings)
        assertEquals("Gen., 1", (actual[1] as BaseViewModel.ViewData.Success).data.title)
        assertEquals(3, (actual[1] as BaseViewModel.ViewData.Success).data.items.size)
        assertEquals(1, (actual[1] as BaseViewModel.ViewData.Success).data.currentPosition)
    }
}

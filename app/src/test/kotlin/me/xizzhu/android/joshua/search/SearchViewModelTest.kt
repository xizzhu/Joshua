/*
 * Copyright (C) 2023 Xizhi Zhu
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.*
import me.xizzhu.android.joshua.preview.PreviewItem

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

        bibleReadingManager = mockk<BibleReadingManager>().apply {
            every { currentTranslation() } returns flowOf(MockContents.kjvShortName)
            coEvery { readBookNames(MockContents.kjvShortName) } returns MockContents.kjvBookNames
            coEvery { readBookShortNames(MockContents.kjvShortName) } returns MockContents.kjvBookShortNames
            coEvery { readVerses(MockContents.kjvShortName, any()) } returns emptyMap()
        }
        searchManager = mockk<SearchManager>().apply {
            every { configuration() } returns flowOf(SearchConfiguration(
                includeOldTestament = true, includeNewTestament = true, includeBookmarks = true, includeHighlights = true, includeNotes = true
            ))
            coEvery { search(any()) } returns SearchResult(emptyList(), emptyList(), emptyList(), emptyList())
        }
        settingsManager = mockk<SettingsManager>().apply {
            every { settings() } returns flowOf(Settings.DEFAULT)
        }
        application = mockk<Application>().apply {
            every { getString(R.string.title_bookmarks) } returns "Bookmarks"
            every { getString(R.string.title_highlights) } returns "Highlights"
            every { getString(R.string.title_notes) } returns "Notes"
            every { getString(R.string.toast_search_result, any()) } answers { "${(it.invocation.args[1] as Array<Any?>)[0]} result(s) found." }
        }

        searchViewModel = SearchViewModel(bibleReadingManager, searchManager, settingsManager, testCoroutineDispatcherProvider, application)
    }

    @Test
    fun `test includeOldTestament(), with exception`() = runTest {
        coEvery { searchManager.configuration() } throws RuntimeException("random exception")

        searchViewModel.includeOldTestament(false)
        assertEquals(
            SearchViewModel.ViewState(
                loading = false,
                searchConfig = SearchConfiguration(
                    includeOldTestament = true, includeNewTestament = true, includeBookmarks = true, includeHighlights = true, includeNotes = true
                ),
                instantSearch = false,
                items = emptyList(),
                scrollItemsToPosition = -1,
                preview = null,
                searchResultSummary = null,
                error = SearchViewModel.ViewState.Error.SearchConfigUpdatingError
            ),
            searchViewModel.viewState().first()
        )

        searchViewModel.markErrorAsShown(SearchViewModel.ViewState.Error.VerseSearchingError)
        assertEquals(
            SearchViewModel.ViewState(
                loading = false,
                searchConfig = SearchConfiguration(
                    includeOldTestament = true, includeNewTestament = true, includeBookmarks = true, includeHighlights = true, includeNotes = true
                ),
                instantSearch = false,
                items = emptyList(),
                scrollItemsToPosition = -1,
                preview = null,
                searchResultSummary = null,
                error = SearchViewModel.ViewState.Error.SearchConfigUpdatingError
            ),
            searchViewModel.viewState().first()
        )

        searchViewModel.markErrorAsShown(SearchViewModel.ViewState.Error.SearchConfigUpdatingError)
        assertEquals(
            SearchViewModel.ViewState(
                loading = false,
                searchConfig = SearchConfiguration(
                    includeOldTestament = true, includeNewTestament = true, includeBookmarks = true, includeHighlights = true, includeNotes = true
                ),
                instantSearch = false,
                items = emptyList(),
                scrollItemsToPosition = -1,
                preview = null,
                searchResultSummary = null,
                error = null
            ),
            searchViewModel.viewState().first()
        )
    }

    @Test
    fun `test includeOldTestament()`() = runTest {
        every {
            searchManager.saveConfiguration(SearchConfiguration(
                includeOldTestament = false, includeNewTestament = true, includeBookmarks = true, includeHighlights = true, includeNotes = true
            ))
        } returns Unit

        searchViewModel.includeOldTestament(false)
        searchViewModel.includeOldTestament(true)

        assertEquals(
            SearchViewModel.ViewState(
                loading = false,
                searchConfig = SearchConfiguration(
                    includeOldTestament = true, includeNewTestament = true, includeBookmarks = true, includeHighlights = true, includeNotes = true
                ),
                instantSearch = false,
                items = emptyList(),
                scrollItemsToPosition = -1,
                preview = null,
                searchResultSummary = null,
                error = null
            ),
            searchViewModel.viewState().first()
        )
        verify(exactly = 1) { searchManager.saveConfiguration(any()) }
    }

    @Test
    fun `test includeNewTestament()`() = runTest {
        every {
            searchManager.saveConfiguration(SearchConfiguration(
                includeOldTestament = true, includeNewTestament = false, includeBookmarks = true, includeHighlights = true, includeNotes = true
            ))
        } returns Unit

        searchViewModel.includeNewTestament(false)
        searchViewModel.includeNewTestament(true)

        assertEquals(
            SearchViewModel.ViewState(
                loading = false,
                searchConfig = SearchConfiguration(
                    includeOldTestament = true, includeNewTestament = true, includeBookmarks = true, includeHighlights = true, includeNotes = true
                ),
                instantSearch = false,
                items = emptyList(),
                scrollItemsToPosition = -1,
                preview = null,
                searchResultSummary = null,
                error = null
            ),
            searchViewModel.viewState().first()
        )
        verify(exactly = 1) { searchManager.saveConfiguration(any()) }
    }

    @Test
    fun `test includeBookmarks()`() = runTest {
        every {
            searchManager.saveConfiguration(SearchConfiguration(
                includeOldTestament = true, includeNewTestament = true, includeBookmarks = false, includeHighlights = true, includeNotes = true
            ))
        } returns Unit

        searchViewModel.includeBookmarks(false)
        searchViewModel.includeBookmarks(true)

        assertEquals(
            SearchViewModel.ViewState(
                loading = false,
                searchConfig = SearchConfiguration(
                    includeOldTestament = true, includeNewTestament = true, includeBookmarks = true, includeHighlights = true, includeNotes = true
                ),
                instantSearch = false,
                items = emptyList(),
                scrollItemsToPosition = -1,
                preview = null,
                searchResultSummary = null,
                error = null
            ),
            searchViewModel.viewState().first()
        )
        verify(exactly = 1) { searchManager.saveConfiguration(any()) }
    }

    @Test
    fun `test includeHighlights()`() = runTest {
        every {
            searchManager.saveConfiguration(SearchConfiguration(
                includeOldTestament = true, includeNewTestament = true, includeBookmarks = true, includeHighlights = false, includeNotes = true
            ))
        } returns Unit

        searchViewModel.includeHighlights(false)
        searchViewModel.includeHighlights(true)

        assertEquals(
            SearchViewModel.ViewState(
                loading = false,
                searchConfig = SearchConfiguration(
                    includeOldTestament = true, includeNewTestament = true, includeBookmarks = true, includeHighlights = true, includeNotes = true
                ),
                instantSearch = false,
                items = emptyList(),
                scrollItemsToPosition = -1,
                preview = null,
                searchResultSummary = null,
                error = null
            ),
            searchViewModel.viewState().first()
        )
        verify(exactly = 1) { searchManager.saveConfiguration(any()) }
    }

    @Test
    fun `test includeNotes()`() = runTest {
        every {
            searchManager.saveConfiguration(SearchConfiguration(
                includeOldTestament = true, includeNewTestament = true, includeBookmarks = true, includeHighlights = true, includeNotes = false
            ))
        } returns Unit

        searchViewModel.includeNotes(false)
        searchViewModel.includeNotes(true)

        assertEquals(
            SearchViewModel.ViewState(
                loading = false,
                searchConfig = SearchConfiguration(
                    includeOldTestament = true, includeNewTestament = true, includeBookmarks = true, includeHighlights = true, includeNotes = true
                ),
                instantSearch = false,
                items = emptyList(),
                scrollItemsToPosition = -1,
                preview = null,
                searchResultSummary = null,
                error = null
            ),
            searchViewModel.viewState().first()
        )
        verify(exactly = 1) { searchManager.saveConfiguration(any()) }
    }

    @Test
    fun `test search(), with empty query`() = runTest {
        searchViewModel.search("", true)
        delay(1000L)

        assertEquals(
            SearchViewModel.ViewState(
                loading = false,
                searchConfig = SearchConfiguration(
                    includeOldTestament = true, includeNewTestament = true, includeBookmarks = true, includeHighlights = true, includeNotes = true
                ),
                instantSearch = false,
                items = emptyList(),
                scrollItemsToPosition = -1,
                preview = null,
                searchResultSummary = null,
                error = null
            ),
            searchViewModel.viewState().first()
        )
    }

    @Test
    fun `test search(), with exception`() = runTest {
        every { bibleReadingManager.currentTranslation() } throws RuntimeException("random error")
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT)

        searchViewModel.search("query", instanceSearch = false)
        searchViewModel.retrySearch()

        assertEquals(
            SearchViewModel.ViewState(
                loading = false,
                searchConfig = SearchConfiguration(
                    includeOldTestament = true, includeNewTestament = true, includeBookmarks = true, includeHighlights = true, includeNotes = true
                ),
                instantSearch = false,
                items = emptyList(),
                scrollItemsToPosition = -1,
                preview = null,
                searchResultSummary = null,
                error = SearchViewModel.ViewState.Error.VerseSearchingError
            ),
            searchViewModel.viewState().first()
        )
    }

    @Test
    fun `test search(), with empty result`() = runTest {
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT)

        searchViewModel = SearchViewModel(bibleReadingManager, searchManager, settingsManager, testCoroutineDispatcherProvider, application)

        searchViewModel.search("query", instanceSearch = false)
        delay(1000L)

        assertEquals(
            SearchViewModel.ViewState(
                loading = false,
                searchConfig = SearchConfiguration(
                    includeOldTestament = true, includeNewTestament = true, includeBookmarks = true, includeHighlights = true, includeNotes = true
                ),
                instantSearch = false,
                items = emptyList(),
                scrollItemsToPosition = 0,
                preview = null,
                searchResultSummary = "0 result(s) found.",
                error = null
            ),
            searchViewModel.viewState().first()
        )

        searchViewModel.markSearchResultSummaryAsShown()
        assertEquals(
            SearchViewModel.ViewState(
                loading = false,
                searchConfig = SearchConfiguration(
                    includeOldTestament = true, includeNewTestament = true, includeBookmarks = true, includeHighlights = true, includeNotes = true
                ),
                instantSearch = false,
                items = emptyList(),
                scrollItemsToPosition = 0,
                preview = null,
                searchResultSummary = null,
                error = null
            ),
            searchViewModel.viewState().first()
        )
    }

    @Test
    fun `test search(), called multiple times`() = runTest {
        coEvery { searchManager.search("not used") } returns SearchResult(listOf(MockContents.kjvVerses[1]), emptyList(), emptyList(), emptyList())
        coEvery { searchManager.search("query") } returns SearchResult(listOf(MockContents.kjvVerses[0]), emptyList(), emptyList(), emptyList())

        searchViewModel.search("invalid", instanceSearch = true)
        delay(1000L)
        assertEquals(
            SearchViewModel.ViewState(
                loading = false,
                searchConfig = SearchConfiguration(
                    includeOldTestament = true, includeNewTestament = true, includeBookmarks = true, includeHighlights = true, includeNotes = true
                ),
                instantSearch = true,
                items = emptyList(),
                scrollItemsToPosition = 0,
                preview = null,
                searchResultSummary = null,
                error = null
            ),
            searchViewModel.viewState().first()
        )

        searchViewModel.search("not used", false)
        delay(100L) // delay is not long enough, so the search should NOT be executed
        assertEquals(
            SearchViewModel.ViewState(
                loading = false,
                searchConfig = SearchConfiguration(
                    includeOldTestament = true, includeNewTestament = true, includeBookmarks = true, includeHighlights = true, includeNotes = true
                ),
                instantSearch = true,
                items = emptyList(),
                scrollItemsToPosition = 0,
                preview = null,
                searchResultSummary = null,
                error = null
            ),
            searchViewModel.viewState().first()
        )

        searchViewModel.search("query", instanceSearch = false)
        delay(1000L)

        assertEquals(
            SearchViewModel.ViewState(
                loading = false,
                searchConfig = SearchConfiguration(includeOldTestament = true, includeNewTestament = true, includeBookmarks = true, includeHighlights = true, includeNotes = true),
                instantSearch = false,
                items = listOf(
                    SearchItem.Header(Settings.DEFAULT, "Genesis"),
                    SearchItem.Verse(
                        settings = Settings.DEFAULT,
                        verseIndex = VerseIndex(0, 0, 0),
                        bookShortName = "Gen.",
                        verseText = "In the beginning God created the heaven and the earth.",
                        query = "query",
                        highlightColor = Highlight.COLOR_NONE
                    )
                ),
                scrollItemsToPosition = 0,
                preview = null,
                searchResultSummary = "1 result(s) found.",
                error = null
            ),
            searchViewModel.viewState().first()
        )

        searchViewModel.markItemsAsScrolled()
        assertEquals(
            SearchViewModel.ViewState(
                loading = false,
                searchConfig = SearchConfiguration(includeOldTestament = true, includeNewTestament = true, includeBookmarks = true, includeHighlights = true, includeNotes = true),
                instantSearch = false,
                items = listOf(
                    SearchItem.Header(Settings.DEFAULT, "Genesis"),
                    SearchItem.Verse(
                        settings = Settings.DEFAULT,
                        verseIndex = VerseIndex(0, 0, 0),
                        bookShortName = "Gen.",
                        verseText = "In the beginning God created the heaven and the earth.",
                        query = "query",
                        highlightColor = Highlight.COLOR_NONE
                    )
                ),
                scrollItemsToPosition = -1,
                preview = null,
                searchResultSummary = "1 result(s) found.",
                error = null
            ),
            searchViewModel.viewState().first()
        )
    }

    @Test
    fun `test search(), with verses only`() = runTest {
        coEvery { searchManager.search("query") } returns SearchResult(
            listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[2], MockContents.kjvExtraVerses[0], MockContents.kjvExtraVerses[1]),
            emptyList(),
            emptyList(),
            emptyList()
        )

        searchViewModel.search("query", instanceSearch = true)
        delay(1000L)

        assertEquals(
            SearchViewModel.ViewState(
                loading = false,
                searchConfig = SearchConfiguration(includeOldTestament = true, includeNewTestament = true, includeBookmarks = true, includeHighlights = true, includeNotes = true),
                instantSearch = true,
                items = listOf(
                    SearchItem.Header(Settings.DEFAULT, "Genesis"),
                    SearchItem.Verse(
                        settings = Settings.DEFAULT,
                        verseIndex = VerseIndex(0, 0, 0),
                        bookShortName = "Gen.",
                        verseText = "In the beginning God created the heaven and the earth.",
                        query = "query",
                        highlightColor = Highlight.COLOR_NONE
                    ),
                    SearchItem.Verse(
                        settings = Settings.DEFAULT,
                        verseIndex = VerseIndex(0, 0, 2),
                        bookShortName = "Gen.",
                        verseText = "And God said, Let there be light: and there was light.",
                        query = "query",
                        highlightColor = Highlight.COLOR_NONE
                    ),
                    SearchItem.Verse(
                        settings = Settings.DEFAULT,
                        verseIndex = VerseIndex(0, 9, 9),
                        bookShortName = "Gen.",
                        verseText = "And the beginning of his kingdom was Babel, and Erech, and Accad, and Calneh, in the land of Shinar.",
                        query = "query",
                        highlightColor = Highlight.COLOR_NONE
                    ),
                    SearchItem.Header(Settings.DEFAULT, "Exodus"),
                    SearchItem.Verse(
                        settings = Settings.DEFAULT,
                        verseIndex = VerseIndex(1, 22, 18),
                        bookShortName = "Ex.",
                        verseText = "The first of the firstfruits of thy land thou shalt bring into the house of the LORD thy God. Thou shalt not seethe a kid in his mother’s milk.",
                        query = "query",
                        highlightColor = Highlight.COLOR_NONE
                    ),
                ),
                scrollItemsToPosition = 0,
                preview = null,
                searchResultSummary = null,
                error = null
            ),
            searchViewModel.viewState().first()
        )
    }

    @Test
    fun `test search(), with verses and bookmarks`() = runTest {
        coEvery { searchManager.search("query") } returns SearchResult(
            listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[2]),
            listOf(Pair(Bookmark(VerseIndex(0, 0, 0), 12345L), MockContents.kjvVerses[0])),
            emptyList(),
            emptyList()
        )

        searchViewModel.search("query", instanceSearch = false)
        delay(1000L)

        assertEquals(
            SearchViewModel.ViewState(
                loading = false,
                searchConfig = SearchConfiguration(includeOldTestament = true, includeNewTestament = true, includeBookmarks = true, includeHighlights = true, includeNotes = true),
                instantSearch = false,
                items = listOf(
                    SearchItem.Header(Settings.DEFAULT, "Bookmarks"),
                    SearchItem.Verse(
                        settings = Settings.DEFAULT,
                        verseIndex = VerseIndex(0, 0, 0),
                        bookShortName = "Gen.",
                        verseText = "In the beginning God created the heaven and the earth.",
                        query = "query",
                        highlightColor = Highlight.COLOR_NONE
                    ),
                    SearchItem.Header(Settings.DEFAULT, "Genesis"),
                    SearchItem.Verse(
                        settings = Settings.DEFAULT,
                        verseIndex = VerseIndex(0, 0, 0),
                        bookShortName = "Gen.",
                        verseText = "In the beginning God created the heaven and the earth.",
                        query = "query",
                        highlightColor = Highlight.COLOR_NONE
                    ),
                    SearchItem.Verse(
                        settings = Settings.DEFAULT,
                        verseIndex = VerseIndex(0, 0, 2),
                        bookShortName = "Gen.",
                        verseText = "And God said, Let there be light: and there was light.",
                        query = "query",
                        highlightColor = Highlight.COLOR_NONE
                    ),
                ),
                scrollItemsToPosition = 0,
                preview = null,
                searchResultSummary = "3 result(s) found.",
                error = null
            ),
            searchViewModel.viewState().first()
        )
    }

    @Test
    fun `test search(), with verses and highlights`() = runTest {
        coEvery { searchManager.search("query") } returns SearchResult(
            listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[2]),
            emptyList(),
            listOf(Pair(Highlight(VerseIndex(0, 0, 0), Highlight.COLOR_PINK, 12345L), MockContents.kjvVerses[0])),
            emptyList()
        )

        searchViewModel.search("query", instanceSearch = false)
        delay(1000L)

        assertEquals(
            SearchViewModel.ViewState(
                loading = false,
                searchConfig = SearchConfiguration(includeOldTestament = true, includeNewTestament = true, includeBookmarks = true, includeHighlights = true, includeNotes = true),
                instantSearch = false,
                items = listOf(
                    SearchItem.Header(Settings.DEFAULT, "Highlights"),
                    SearchItem.Verse(
                        settings = Settings.DEFAULT,
                        verseIndex = VerseIndex(0, 0, 0),
                        bookShortName = "Gen.",
                        verseText = "In the beginning God created the heaven and the earth.",
                        query = "query",
                        highlightColor = Highlight.COLOR_PINK
                    ),
                    SearchItem.Header(Settings.DEFAULT, "Genesis"),
                    SearchItem.Verse(
                        settings = Settings.DEFAULT,
                        verseIndex = VerseIndex(0, 0, 0),
                        bookShortName = "Gen.",
                        verseText = "In the beginning God created the heaven and the earth.",
                        query = "query",
                        highlightColor = Highlight.COLOR_NONE
                    ),
                    SearchItem.Verse(
                        settings = Settings.DEFAULT,
                        verseIndex = VerseIndex(0, 0, 2),
                        bookShortName = "Gen.",
                        verseText = "And God said, Let there be light: and there was light.",
                        query = "query",
                        highlightColor = Highlight.COLOR_NONE
                    ),
                ),
                scrollItemsToPosition = 0,
                preview = null,
                searchResultSummary = "3 result(s) found.",
                error = null
            ),
            searchViewModel.viewState().first()
        )
    }

    @Test
    fun `test search(), with verses and notes`() = runTest {
        coEvery { searchManager.search("query") } returns SearchResult(
            listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[2]),
            emptyList(),
            emptyList(),
            listOf(Pair(Note(VerseIndex(0, 0, 9), "just a note", 12345L), MockContents.kjvVerses[9]))
        )

        searchViewModel.search("query", instanceSearch = false)
        delay(1000L)

        assertEquals(
            SearchViewModel.ViewState(
                loading = false,
                searchConfig = SearchConfiguration(includeOldTestament = true, includeNewTestament = true, includeBookmarks = true, includeHighlights = true, includeNotes = true),
                instantSearch = false,
                items = listOf(
                    SearchItem.Header(Settings.DEFAULT, "Notes"),
                    SearchItem.Note(
                        settings = Settings.DEFAULT,
                        verseIndex = VerseIndex(0, 0, 9),
                        bookShortName = "Gen.",
                        verseText = "And God called the dry land Earth; and the gathering together of the waters called he Seas: and God saw that it was good.",
                        query = "query",
                        note = "just a note"
                    ),
                    SearchItem.Header(Settings.DEFAULT, "Genesis"),
                    SearchItem.Verse(
                        settings = Settings.DEFAULT,
                        verseIndex = VerseIndex(0, 0, 0),
                        bookShortName = "Gen.",
                        verseText = "In the beginning God created the heaven and the earth.",
                        query = "query",
                        highlightColor = Highlight.COLOR_NONE
                    ),
                    SearchItem.Verse(
                        settings = Settings.DEFAULT,
                        verseIndex = VerseIndex(0, 0, 2),
                        bookShortName = "Gen.",
                        verseText = "And God said, Let there be light: and there was light.",
                        query = "query",
                        highlightColor = Highlight.COLOR_NONE
                    ),
                ),
                scrollItemsToPosition = 0,
                preview = null,
                searchResultSummary = "3 result(s) found.",
                error = null
            ),
            searchViewModel.viewState().first()
        )
    }

    @Test
    fun `test search(), with verses, bookmarks, highlights, and notes`() = runTest {
        coEvery { searchManager.search("query") } returns SearchResult(
            listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[2]),
            listOf(Pair(Bookmark(VerseIndex(0, 0, 0), 12345L), MockContents.kjvVerses[0])),
            listOf(Pair(Highlight(VerseIndex(0, 0, 0), Highlight.COLOR_PINK, 12345L), MockContents.kjvVerses[0])),
            listOf(Pair(Note(VerseIndex(0, 0, 9), "just a note", 12345L), MockContents.kjvVerses[9]))
        )

        searchViewModel.search("query", instanceSearch = false)
        delay(1000L)

        assertEquals(
            SearchViewModel.ViewState(
                loading = false,
                searchConfig = SearchConfiguration(includeOldTestament = true, includeNewTestament = true, includeBookmarks = true, includeHighlights = true, includeNotes = true),
                instantSearch = false,
                items = listOf(
                    SearchItem.Header(Settings.DEFAULT, "Notes"),
                    SearchItem.Note(
                        settings = Settings.DEFAULT,
                        verseIndex = VerseIndex(0, 0, 9),
                        bookShortName = "Gen.",
                        verseText = "And God called the dry land Earth; and the gathering together of the waters called he Seas: and God saw that it was good.",
                        query = "query",
                        note = "just a note"
                    ),
                    SearchItem.Header(Settings.DEFAULT, "Bookmarks"),
                    SearchItem.Verse(
                        settings = Settings.DEFAULT,
                        verseIndex = VerseIndex(0, 0, 0),
                        bookShortName = "Gen.",
                        verseText = "In the beginning God created the heaven and the earth.",
                        query = "query",
                        highlightColor = Highlight.COLOR_NONE
                    ),
                    SearchItem.Header(Settings.DEFAULT, "Highlights"),
                    SearchItem.Verse(
                        settings = Settings.DEFAULT,
                        verseIndex = VerseIndex(0, 0, 0),
                        bookShortName = "Gen.",
                        verseText = "In the beginning God created the heaven and the earth.",
                        query = "query",
                        highlightColor = Highlight.COLOR_PINK
                    ),
                    SearchItem.Header(Settings.DEFAULT, "Genesis"),
                    SearchItem.Verse(
                        settings = Settings.DEFAULT,
                        verseIndex = VerseIndex(0, 0, 0),
                        bookShortName = "Gen.",
                        verseText = "In the beginning God created the heaven and the earth.",
                        query = "query",
                        highlightColor = Highlight.COLOR_NONE
                    ),
                    SearchItem.Verse(
                        settings = Settings.DEFAULT,
                        verseIndex = VerseIndex(0, 0, 2),
                        bookShortName = "Gen.",
                        verseText = "And God said, Let there be light: and there was light.",
                        query = "query",
                        highlightColor = Highlight.COLOR_NONE
                    ),
                ),
                scrollItemsToPosition = 0,
                preview = null,
                searchResultSummary = "5 result(s) found.",
                error = null
            ),
            searchViewModel.viewState().first()
        )
    }

    @Test
    fun `test openVerse() with exception`() = runTest {
        coEvery { bibleReadingManager.saveCurrentVerseIndex(VerseIndex(0, 0, 0)) } throws RuntimeException("random exception")

        searchViewModel.openVerse(VerseIndex(0, 0, 0))

        assertEquals(
            SearchViewModel.ViewState(
                loading = false,
                searchConfig = SearchConfiguration(
                    includeOldTestament = true, includeNewTestament = true, includeBookmarks = true, includeHighlights = true, includeNotes = true
                ),
                instantSearch = false,
                items = emptyList(),
                scrollItemsToPosition = -1,
                preview = null,
                searchResultSummary = null,
                error = SearchViewModel.ViewState.Error.VerseOpeningError(VerseIndex(0, 0, 0))
            ),
            searchViewModel.viewState().first()
        )
    }

    @Test
    fun `test openVerse()`() = runTest {
        coEvery { bibleReadingManager.saveCurrentVerseIndex(VerseIndex(0, 0, 0)) } returns Unit

        val viewAction = async(Dispatchers.Unconfined) { searchViewModel.viewAction().first() }

        searchViewModel.openVerse(VerseIndex(0, 0, 0))

        assertEquals(
            SearchViewModel.ViewState(
                loading = false,
                searchConfig = SearchConfiguration(
                    includeOldTestament = true, includeNewTestament = true, includeBookmarks = true, includeHighlights = true, includeNotes = true
                ),
                instantSearch = false,
                items = emptyList(),
                scrollItemsToPosition = -1,
                preview = null,
                searchResultSummary = null,
                error = null
            ),
            searchViewModel.viewState().first()
        )
        assertEquals(SearchViewModel.ViewAction.OpenReadingScreen, viewAction.await())
    }

    @Test
    fun `test loadPreview() with invalid verse index`() = runTest {
        searchViewModel.loadPreview(VerseIndex.INVALID)

        assertEquals(
            SearchViewModel.ViewState(
                loading = false,
                searchConfig = SearchConfiguration(
                    includeOldTestament = true, includeNewTestament = true, includeBookmarks = true, includeHighlights = true, includeNotes = true
                ),
                instantSearch = false,
                items = emptyList(),
                scrollItemsToPosition = -1,
                preview = null,
                searchResultSummary = null,
                error = SearchViewModel.ViewState.Error.PreviewLoadingError(VerseIndex.INVALID)
            ),
            searchViewModel.viewState().first()
        )
    }

    @Test
    fun `test loadPreview()`() = runTest {
        coEvery { bibleReadingManager.currentTranslation() } returns flowOf(MockContents.kjvShortName)
        coEvery {
            bibleReadingManager.readVerses(MockContents.kjvShortName, 0, 0)
        } returns listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[1], MockContents.kjvVerses[2])
        coEvery { bibleReadingManager.readBookShortNames(MockContents.kjvShortName) } returns MockContents.kjvBookShortNames
        every { settingsManager.settings() } returns flowOf(Settings.DEFAULT)

        searchViewModel.loadPreview(VerseIndex(0, 0, 1))

        val actual = searchViewModel.viewState().first()
        assertFalse(actual.loading)
        assertEquals(
            SearchConfiguration(includeOldTestament = true, includeNewTestament = true, includeBookmarks = true, includeHighlights = true, includeNotes = true),
            actual.searchConfig
        )
        assertFalse(actual.instantSearch)
        assertTrue(actual.items.isEmpty())
        assertEquals("Gen., 1", actual.preview?.title)
        assertEquals(3, actual.preview?.items?.size)
        assertEquals(
            "1:1 In the beginning God created the heaven and the earth.",
            (actual.preview?.items?.get(0) as PreviewItem.VerseWithQuery).textForDisplay.toString()
        )
        assertEquals(
            "1:2 And the earth was without form, and void; and darkness was upon the face of the deep. And the Spirit of God moved upon the face of the waters.",
            (actual.preview?.items?.get(1) as PreviewItem.VerseWithQuery).textForDisplay.toString()
        )
        assertEquals("1:3 And God said, Let there be light: and there was light.", (actual.preview?.items?.get(2) as PreviewItem.VerseWithQuery).textForDisplay.toString())
        assertEquals(1, actual.preview?.currentPosition)
        assertNull(actual.searchResultSummary)
        assertNull(actual.error)

        searchViewModel.markPreviewAsClosed()
        assertEquals(
            SearchViewModel.ViewState(
                loading = false,
                searchConfig = SearchConfiguration(
                    includeOldTestament = true, includeNewTestament = true, includeBookmarks = true, includeHighlights = true, includeNotes = true
                ),
                instantSearch = false,
                items = emptyList(),
                scrollItemsToPosition = -1,
                preview = null,
                searchResultSummary = null,
                error = null
            ),
            searchViewModel.viewState().first()
        )
    }
}

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

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.tests.BaseUnitTest
import me.xizzhu.android.joshua.tests.MockContents
import org.mockito.Mock
import org.mockito.Mockito.`when`
import kotlin.test.*

class SearchViewModelTest : BaseUnitTest() {
    @Mock
    private lateinit var bibleReadingManager: BibleReadingManager

    @Mock
    private lateinit var bookmarkManager: VerseAnnotationManager<Bookmark>

    @Mock
    private lateinit var highlightManager: VerseAnnotationManager<Highlight>

    @Mock
    private lateinit var noteManager: VerseAnnotationManager<Note>

    @Mock
    private lateinit var settingsManager: SettingsManager

    private lateinit var searchViewModel: SearchViewModel

    @BeforeTest
    override fun setup() {
        super.setup()

        searchViewModel = SearchViewModel(bibleReadingManager, bookmarkManager, highlightManager, noteManager, settingsManager)
    }

    @Test
    fun testSearchRequest() = runBlockingTest {
        val requests = listOf(
                SearchRequest("1", true, true, false, true),
                SearchRequest("2", false, false, false, false),
                SearchRequest("", false, true, true, true),
                SearchRequest("", true, false, false, true),
                SearchRequest("3", true, true, true, false)
        )
        val searchRequest = async { searchViewModel.searchRequest.take(requests.size).toList() }
        requests.forEach { searchViewModel.requestSearch(it) }

        assertEquals(requests, searchRequest.await())
    }

    @Test
    fun testSearch() = runBlocking {
        val currentTranslation = MockContents.kjvShortName
        val query = "query"
        `when`(bibleReadingManager.currentTranslation()).thenReturn(flowOf("", currentTranslation))
        `when`(bibleReadingManager.search(currentTranslation, query)).thenReturn(listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[1], MockContents.kjvVerses[2], MockContents.kjvVerses[3]))
        `when`(bibleReadingManager.readBookNames(currentTranslation)).thenReturn(MockContents.kjvBookNames)
        `when`(bibleReadingManager.readBookShortNames(currentTranslation)).thenReturn(MockContents.kjvBookShortNames)
        `when`(bibleReadingManager.readVerses(currentTranslation, listOf(VerseIndex(0, 0, 1))))
                .thenReturn(mapOf(Pair(VerseIndex(0, 0, 1), MockContents.kjvVerses[1])))
        `when`(bibleReadingManager.readVerses(currentTranslation, listOf(VerseIndex(0, 0, 2))))
                .thenReturn(mapOf(Pair(VerseIndex(0, 0, 2), MockContents.kjvVerses[2])))
        `when`(bibleReadingManager.readVerses(currentTranslation, listOf(VerseIndex(0, 0, 3))))
                .thenReturn(mapOf(Pair(VerseIndex(0, 0, 3), MockContents.kjvVerses[3])))
        `when`(bookmarkManager.read(Constants.SORT_BY_BOOK)).thenReturn(listOf(Bookmark(VerseIndex(0, 0, 1), 0L)))
        `when`(highlightManager.read(Constants.SORT_BY_BOOK)).thenReturn(listOf(Highlight(VerseIndex(0, 0, 2), Highlight.COLOR_BLUE, 0L)))
        `when`(noteManager.search(query)).thenReturn(listOf(Note(VerseIndex(0, 0, 3), "note", 0L)))

        assertEquals(
                listOf(
                        SearchResult(
                                query, listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[1], MockContents.kjvVerses[2], MockContents.kjvVerses[3]),
                                listOf(Pair(Bookmark(VerseIndex(0, 0, 1), 0L), MockContents.kjvVerses[1])),
                                listOf(Pair(Highlight(VerseIndex(0, 0, 2), Highlight.COLOR_BLUE, 0L), MockContents.kjvVerses[2])),
                                listOf(Pair(Note(VerseIndex(0, 0, 3), "note", 0L), MockContents.kjvVerses[3])),
                                MockContents.kjvBookNames, MockContents.kjvBookShortNames
                        )
                ),
                searchViewModel.search(query, true, true, true).toList()
        )
    }

    @Test
    fun testSearchWithoutBookmarks() = runBlocking {
        val currentTranslation = MockContents.kjvShortName
        val query = "query"
        `when`(bibleReadingManager.currentTranslation()).thenReturn(flowOf("", currentTranslation))
        `when`(bibleReadingManager.search(currentTranslation, query)).thenReturn(listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[1], MockContents.kjvVerses[2], MockContents.kjvVerses[3]))
        `when`(bibleReadingManager.readBookNames(currentTranslation)).thenReturn(MockContents.kjvBookNames)
        `when`(bibleReadingManager.readBookShortNames(currentTranslation)).thenReturn(MockContents.kjvBookShortNames)
        `when`(bibleReadingManager.readVerses(currentTranslation, listOf(VerseIndex(0, 0, 1))))
                .thenReturn(mapOf(Pair(VerseIndex(0, 0, 1), MockContents.kjvVerses[1])))
        `when`(bibleReadingManager.readVerses(currentTranslation, listOf(VerseIndex(0, 0, 2))))
                .thenReturn(mapOf(Pair(VerseIndex(0, 0, 2), MockContents.kjvVerses[2])))
        `when`(bibleReadingManager.readVerses(currentTranslation, listOf(VerseIndex(0, 0, 3))))
                .thenReturn(mapOf(Pair(VerseIndex(0, 0, 3), MockContents.kjvVerses[3])))
        `when`(bookmarkManager.read(Constants.SORT_BY_BOOK)).thenReturn(listOf(Bookmark(VerseIndex(0, 0, 1), 0L)))
        `when`(highlightManager.read(Constants.SORT_BY_BOOK)).thenReturn(listOf(Highlight(VerseIndex(0, 0, 2), Highlight.COLOR_BLUE, 0L)))
        `when`(noteManager.search(query)).thenReturn(listOf(Note(VerseIndex(0, 0, 3), "note", 0L)))

        assertEquals(
                listOf(
                        SearchResult(
                                query, listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[1], MockContents.kjvVerses[2], MockContents.kjvVerses[3]),
                                emptyList(),
                                listOf(Pair(Highlight(VerseIndex(0, 0, 2), Highlight.COLOR_BLUE, 0L), MockContents.kjvVerses[2])),
                                listOf(Pair(Note(VerseIndex(0, 0, 3), "note", 0L), MockContents.kjvVerses[3])),
                                MockContents.kjvBookNames, MockContents.kjvBookShortNames
                        )
                ),
                searchViewModel.search(query, false, true, true).toList()
        )
    }

    @Test
    fun testSearchWithoutHighlights() = runBlocking {
        val currentTranslation = MockContents.kjvShortName
        val query = "query"
        `when`(bibleReadingManager.currentTranslation()).thenReturn(flowOf("", currentTranslation))
        `when`(bibleReadingManager.search(currentTranslation, query)).thenReturn(listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[1], MockContents.kjvVerses[2], MockContents.kjvVerses[3]))
        `when`(bibleReadingManager.readBookNames(currentTranslation)).thenReturn(MockContents.kjvBookNames)
        `when`(bibleReadingManager.readBookShortNames(currentTranslation)).thenReturn(MockContents.kjvBookShortNames)
        `when`(bibleReadingManager.readVerses(currentTranslation, listOf(VerseIndex(0, 0, 1))))
                .thenReturn(mapOf(Pair(VerseIndex(0, 0, 1), MockContents.kjvVerses[1])))
        `when`(bibleReadingManager.readVerses(currentTranslation, listOf(VerseIndex(0, 0, 2))))
                .thenReturn(mapOf(Pair(VerseIndex(0, 0, 2), MockContents.kjvVerses[2])))
        `when`(bibleReadingManager.readVerses(currentTranslation, listOf(VerseIndex(0, 0, 3))))
                .thenReturn(mapOf(Pair(VerseIndex(0, 0, 3), MockContents.kjvVerses[3])))
        `when`(bookmarkManager.read(Constants.SORT_BY_BOOK)).thenReturn(listOf(Bookmark(VerseIndex(0, 0, 1), 0L)))
        `when`(highlightManager.read(Constants.SORT_BY_BOOK)).thenReturn(listOf(Highlight(VerseIndex(0, 0, 2), Highlight.COLOR_BLUE, 0L)))
        `when`(noteManager.search(query)).thenReturn(listOf(Note(VerseIndex(0, 0, 3), "note", 0L)))

        assertEquals(
                listOf(
                        SearchResult(
                                query, listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[1], MockContents.kjvVerses[2], MockContents.kjvVerses[3]),
                                listOf(Pair(Bookmark(VerseIndex(0, 0, 1), 0L), MockContents.kjvVerses[1])),
                                emptyList(),
                                listOf(Pair(Note(VerseIndex(0, 0, 3), "note", 0L), MockContents.kjvVerses[3])),
                                MockContents.kjvBookNames, MockContents.kjvBookShortNames
                        )
                ),
                searchViewModel.search(query, true, false, true).toList()
        )
    }

    @Test
    fun testSearchWithoutBookmarksAndHighlights() = runBlocking {
        val currentTranslation = MockContents.kjvShortName
        val query = "query"
        `when`(bibleReadingManager.currentTranslation()).thenReturn(flowOf("", currentTranslation))
        `when`(bibleReadingManager.search(currentTranslation, query)).thenReturn(listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[1], MockContents.kjvVerses[2], MockContents.kjvVerses[3]))
        `when`(bibleReadingManager.readBookNames(currentTranslation)).thenReturn(MockContents.kjvBookNames)
        `when`(bibleReadingManager.readBookShortNames(currentTranslation)).thenReturn(MockContents.kjvBookShortNames)
        `when`(bibleReadingManager.readVerses(currentTranslation, listOf(VerseIndex(0, 0, 1))))
                .thenReturn(mapOf(Pair(VerseIndex(0, 0, 1), MockContents.kjvVerses[1])))
        `when`(bibleReadingManager.readVerses(currentTranslation, listOf(VerseIndex(0, 0, 2))))
                .thenReturn(mapOf(Pair(VerseIndex(0, 0, 2), MockContents.kjvVerses[2])))
        `when`(bibleReadingManager.readVerses(currentTranslation, listOf(VerseIndex(0, 0, 3))))
                .thenReturn(mapOf(Pair(VerseIndex(0, 0, 3), MockContents.kjvVerses[3])))
        `when`(bookmarkManager.read(Constants.SORT_BY_BOOK)).thenReturn(listOf(Bookmark(VerseIndex(0, 0, 1), 0L)))
        `when`(highlightManager.read(Constants.SORT_BY_BOOK)).thenReturn(listOf(Highlight(VerseIndex(0, 0, 2), Highlight.COLOR_BLUE, 0L)))
        `when`(noteManager.search(query)).thenReturn(listOf(Note(VerseIndex(0, 0, 3), "note", 0L)))

        assertEquals(
                listOf(
                        SearchResult(
                                query, listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[1], MockContents.kjvVerses[2], MockContents.kjvVerses[3]),
                                emptyList(),
                                emptyList(),
                                listOf(Pair(Note(VerseIndex(0, 0, 3), "note", 0L), MockContents.kjvVerses[3])),
                                MockContents.kjvBookNames, MockContents.kjvBookShortNames
                        )
                ),
                searchViewModel.search(query, false, false, true).toList()
        )
    }

    @Test
    fun testSearchWithoutNote() = runBlocking {
        val currentTranslation = MockContents.kjvShortName
        val query = "query"
        `when`(bibleReadingManager.currentTranslation()).thenReturn(flowOf("", currentTranslation))
        `when`(bibleReadingManager.search(currentTranslation, query)).thenReturn(listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[1], MockContents.kjvVerses[2], MockContents.kjvVerses[3]))
        `when`(bibleReadingManager.readBookNames(currentTranslation)).thenReturn(MockContents.kjvBookNames)
        `when`(bibleReadingManager.readBookShortNames(currentTranslation)).thenReturn(MockContents.kjvBookShortNames)
        `when`(bibleReadingManager.readVerses(currentTranslation, listOf(VerseIndex(0, 0, 1))))
                .thenReturn(mapOf(Pair(VerseIndex(0, 0, 1), MockContents.kjvVerses[1])))
        `when`(bibleReadingManager.readVerses(currentTranslation, listOf(VerseIndex(0, 0, 2))))
                .thenReturn(mapOf(Pair(VerseIndex(0, 0, 2), MockContents.kjvVerses[2])))
        `when`(bibleReadingManager.readVerses(currentTranslation, listOf(VerseIndex(0, 0, 3))))
                .thenReturn(mapOf(Pair(VerseIndex(0, 0, 3), MockContents.kjvVerses[3])))
        `when`(bookmarkManager.read(Constants.SORT_BY_BOOK)).thenReturn(listOf(Bookmark(VerseIndex(0, 0, 1), 0L)))
        `when`(highlightManager.read(Constants.SORT_BY_BOOK)).thenReturn(listOf(Highlight(VerseIndex(0, 0, 2), Highlight.COLOR_BLUE, 0L)))
        `when`(noteManager.search(query)).thenReturn(listOf(Note(VerseIndex(0, 0, 3), "note", 0L)))

        assertEquals(
                listOf(
                        SearchResult(
                                query, listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[1], MockContents.kjvVerses[2], MockContents.kjvVerses[3]),
                                listOf(Pair(Bookmark(VerseIndex(0, 0, 1), 0L), MockContents.kjvVerses[1])),
                                listOf(Pair(Highlight(VerseIndex(0, 0, 2), Highlight.COLOR_BLUE, 0L), MockContents.kjvVerses[2])),
                                emptyList(),
                                MockContents.kjvBookNames, MockContents.kjvBookShortNames
                        )
                ),
                searchViewModel.search(query, true, true, false).toList()
        )
    }

    @Test
    fun testSearchWithoutBookmarksHighlightsAndNotes() = runBlocking {
        val currentTranslation = MockContents.kjvShortName
        val query = "query"
        `when`(bibleReadingManager.currentTranslation()).thenReturn(flowOf("", currentTranslation))
        `when`(bibleReadingManager.search(currentTranslation, query)).thenReturn(listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[1], MockContents.kjvVerses[2], MockContents.kjvVerses[3]))
        `when`(bibleReadingManager.readBookNames(currentTranslation)).thenReturn(MockContents.kjvBookNames)
        `when`(bibleReadingManager.readBookShortNames(currentTranslation)).thenReturn(MockContents.kjvBookShortNames)
        `when`(bibleReadingManager.readVerses(currentTranslation, listOf(VerseIndex(0, 0, 1))))
                .thenReturn(mapOf(Pair(VerseIndex(0, 0, 1), MockContents.kjvVerses[1])))
        `when`(bibleReadingManager.readVerses(currentTranslation, listOf(VerseIndex(0, 0, 2))))
                .thenReturn(mapOf(Pair(VerseIndex(0, 0, 2), MockContents.kjvVerses[2])))
        `when`(bibleReadingManager.readVerses(currentTranslation, listOf(VerseIndex(0, 0, 3))))
                .thenReturn(mapOf(Pair(VerseIndex(0, 0, 3), MockContents.kjvVerses[3])))
        `when`(bookmarkManager.read(Constants.SORT_BY_BOOK)).thenReturn(listOf(Bookmark(VerseIndex(0, 0, 1), 0L)))
        `when`(highlightManager.read(Constants.SORT_BY_BOOK)).thenReturn(listOf(Highlight(VerseIndex(0, 0, 2), Highlight.COLOR_BLUE, 0L)))
        `when`(noteManager.search(query)).thenReturn(listOf(Note(VerseIndex(0, 0, 3), "note", 0L)))

        assertEquals(
                listOf(
                        SearchResult(
                                query, listOf(MockContents.kjvVerses[0], MockContents.kjvVerses[1], MockContents.kjvVerses[2], MockContents.kjvVerses[3]),
                                emptyList(),
                                emptyList(),
                                emptyList(),
                                MockContents.kjvBookNames, MockContents.kjvBookShortNames
                        )
                ),
                searchViewModel.search(query, false, false, false).toList()
        )
    }

    @Test
    fun testSearchWithException() = runBlocking {
        val e = RuntimeException("random exception")
        `when`(bibleReadingManager.currentTranslation()).thenThrow(e)

        searchViewModel.search("", true, true, true)
                .onCompletion { assertEquals(e, it) }
                .catch { }
                .collect()
    }
}

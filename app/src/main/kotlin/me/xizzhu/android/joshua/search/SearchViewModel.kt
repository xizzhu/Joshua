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
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.Bookmark
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.Note
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseAnnotationManager
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.BaseViewModel
import me.xizzhu.android.joshua.infra.onFailure
import me.xizzhu.android.joshua.infra.viewData
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import me.xizzhu.android.joshua.utils.firstNotEmpty
import me.xizzhu.android.logger.Log
import javax.inject.Inject

class SearchResultViewData(val items: List<BaseItem>, val query: String, val instanceSearch: Boolean, val toast: String)

@HiltViewModel
class SearchViewModel @Inject constructor(
        private val bibleReadingManager: BibleReadingManager,
        private val bookmarkManager: VerseAnnotationManager<Bookmark>,
        private val highlightManager: VerseAnnotationManager<Highlight>,
        private val noteManager: VerseAnnotationManager<Note>,
        settingsManager: SettingsManager,
        application: Application
) : BaseViewModel(settingsManager, application) {
    private class SearchRequest(val query: String, val instanceSearch: Boolean)

    var includeBookmarks: Boolean = true
    var includeHighlights: Boolean = true
    var includeNotes: Boolean = true

    private val searchRequest: MutableStateFlow<SearchRequest?> = MutableStateFlow(null)
    private val searchResult: MutableStateFlow<ViewData<SearchResultViewData>?> = MutableStateFlow(null)

    init {
        searchRequest.filterNotNull()
                .debounce(250L)
                .distinctUntilChangedBy { it.query }
                .mapLatest { it }
                .onEach { doSearch(it.query, it.instanceSearch) }
                .launchIn(viewModelScope)
    }

    fun searchResult(): Flow<ViewData<SearchResultViewData>> = searchResult.filterNotNull()

    fun search(query: String, instanceSearch: Boolean) {
        searchRequest.value = SearchRequest(query, instanceSearch)
    }

    fun retrySearch() {
        val searchRequest = searchRequest.value ?: return
        doSearch(searchRequest.query, searchRequest.instanceSearch)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun doSearch(query: String, instanceSearch: Boolean) {
        if (query.isEmpty()) {
            searchResult.value = ViewData.Success(SearchResultViewData(emptyList(), "", instanceSearch, ""))
            return
        }

        viewModelScope.launch {
            try {
                if (!instanceSearch) {
                    searchResult.value = ViewData.Loading()
                }

                val currentTranslation = bibleReadingManager.currentTranslation().firstNotEmpty()
                val verses = bibleReadingManager.search(currentTranslation, query)
                val bookmarks: List<Pair<Bookmark, Verse>>
                val highlights: List<Pair<Highlight, Verse>>
                if (includeBookmarks || includeHighlights) {
                    val versesWithIndexes = verses.associateBy { it.verseIndex }
                    bookmarks = if (includeBookmarks) {
                        bookmarkManager.read(Constants.SORT_BY_BOOK).mapNotNull { bookmark ->
                            versesWithIndexes[bookmark.verseIndex]?.let { verse -> Pair(bookmark, verse) }
                        }
                    } else {
                        emptyList()
                    }
                    highlights = if (includeHighlights) {
                        highlightManager.read(Constants.SORT_BY_BOOK).mapNotNull { highlight ->
                            versesWithIndexes[highlight.verseIndex]?.let { verse -> Pair(highlight, verse) }
                        }
                    } else {
                        emptyList()
                    }
                } else {
                    bookmarks = emptyList()
                    highlights = emptyList()
                }

                val notes = if (includeNotes) {
                    noteManager.search(query)
                            .takeIf { it.isNotEmpty() }
                            ?.let { notes ->
                                val versesWithNotes = bibleReadingManager.readVerses(currentTranslation, notes.map { it.verseIndex })
                                arrayListOf<Pair<Note, Verse>>().apply {
                                    ensureCapacity(notes.size)
                                    notes.forEach { note ->
                                        // TODO What to do if the notes is attached to a verse that is empty / not exist in this translation?
                                        // https://github.com/xizzhu/Joshua/issues/153
                                        versesWithNotes[note.verseIndex]?.let { add(Pair(note, it)) }
                                    }
                                }
                            }
                            ?: emptyList()
                } else {
                    emptyList()
                }

                val bookNames = bibleReadingManager.readBookNames(currentTranslation)
                val bookShortNames = bibleReadingManager.readBookShortNames(currentTranslation)

                searchResult.value = ViewData.Success(SearchResultViewData(
                        items = buildSearchResultItems(
                                query = query,
                                verses = verses,
                                bookmarks = bookmarks,
                                highlights = highlights,
                                notes = notes,
                                bookNames = bookNames,
                                bookShortNames = bookShortNames
                        ),
                        query = query,
                        instanceSearch = instanceSearch,
                        toast = application.getString(R.string.toast_verses_searched, notes.size + bookmarks.size + highlights.size + verses.size)
                ))
            } catch (e: Exception) {
                Log.e(tag, "Error occurred which searching, query=$query instanceSearch=$instanceSearch, includeBookmarks=$includeBookmarks, includeHighlights=$includeHighlights, includeNotes=includeNotes", e)
                searchResult.value = ViewData.Failure(e)
            }
        }
    }

    private fun buildSearchResultItems(
            query: String, verses: List<Verse>, bookmarks: List<Pair<Bookmark, Verse>>, highlights: List<Pair<Highlight, Verse>>,
            notes: List<Pair<Note, Verse>>, bookNames: List<String>, bookShortNames: List<String>
    ): List<BaseItem> {
        val items = ArrayList<BaseItem>(
                (if (notes.isEmpty()) 0 else notes.size + 1)
                        + (if (bookmarks.isEmpty()) 0 else bookmarks.size + 1)
                        + (if (highlights.isEmpty()) 0 else highlights.size + 1)
                        + verses.size + Bible.BOOK_COUNT
        )

        if (notes.isNotEmpty()) {
            items.add(TitleItem(application.getString(R.string.title_notes), false))
            notes.forEach { note ->
                items.add(SearchNoteItem(
                        note.first.verseIndex, bookShortNames[note.first.verseIndex.bookIndex], note.second.text.text, note.first.note, query
                ))
            }
        }

        if (bookmarks.isNotEmpty()) {
            items.add(TitleItem(application.getString(R.string.title_bookmarks), false))
            bookmarks.forEach { bookmark ->
                items.add(SearchVerseItem(bookmark.first.verseIndex, bookShortNames[bookmark.first.verseIndex.bookIndex],
                        bookmark.second.text.text, query, Highlight.COLOR_NONE))
            }
        }

        if (highlights.isNotEmpty()) {
            items.add(TitleItem(application.getString(R.string.title_highlights), false))
            highlights.forEach { highlight ->
                items.add(SearchVerseItem(highlight.first.verseIndex, bookShortNames[highlight.first.verseIndex.bookIndex],
                        highlight.second.text.text, query, highlight.first.color))
            }
        }

        var lastVerseBookIndex = -1
        verses.forEach { verse ->
            val currentVerseBookIndex = verse.verseIndex.bookIndex
            if (lastVerseBookIndex != currentVerseBookIndex) {
                items.add(TitleItem(bookNames[currentVerseBookIndex], false))
                lastVerseBookIndex = currentVerseBookIndex
            }
            items.add(SearchVerseItem(verse.verseIndex, bookShortNames[currentVerseBookIndex], verse.text.text, query, Highlight.COLOR_NONE))
        }

        return items
    }

    fun saveCurrentVerseIndex(verseToOpen: VerseIndex): Flow<ViewData<Unit>> = viewData {
        bibleReadingManager.saveCurrentVerseIndex(verseToOpen)
    }.onFailure { Log.e(tag, "Failed to save current verse", it) }
}

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
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.infra.BaseViewModelV2
import me.xizzhu.android.joshua.preview.PreviewViewData
import me.xizzhu.android.joshua.preview.loadPreviewV2
import me.xizzhu.android.joshua.preview.nextNonEmpty
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import me.xizzhu.android.joshua.utils.firstNotEmpty
import me.xizzhu.android.logger.Log
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
        private val bibleReadingManager: BibleReadingManager,
        private val searchManager: SearchManager,
        settingsManager: SettingsManager,
        application: Application
) : BaseViewModelV2<SearchViewModel.ViewAction, SearchViewModel.ViewState>(
        settingsManager = settingsManager,
        application = application,
        initialViewState = ViewState(
                settings = null,
                searchConfig = null,
                loading = false,
                searchQuery = "",
                instantSearch = false,
                searchResults = emptyList(),
        )
) {
    sealed class ViewAction {
        object OpenReadingScreen : ViewAction()
        class ShowOpenPreviewFailedError(val verseIndex: VerseIndex) : ViewAction()
        class ShowOpenVerseFailedError(val verseToOpen: VerseIndex) : ViewAction()
        class ShowPreview(val previewViewData: PreviewViewData) : ViewAction()
        class ShowToast(val message: String) : ViewAction()
        object ShowSearchFailedError : ViewAction()
    }

    data class ViewState(
            val settings: Settings?,
            val searchConfig: SearchConfiguration?,
            val loading: Boolean,
            val searchQuery: String,
            val instantSearch: Boolean,
            val searchResults: List<BaseItem>,
    )

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal class SearchRequest(val query: String, val instantSearch: Boolean)

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal val searchRequest: MutableStateFlow<SearchRequest?> = MutableStateFlow(null)

    init {
        settings().onEach { settings -> emitViewState { it.copy(settings = settings) } }.launchIn(viewModelScope)

        searchManager.configuration()
                .onEach { searchConfig ->
                    emitViewState { currentViewState -> currentViewState.copy(searchConfig = searchConfig) }
                    retrySearch()
                }
                .launchIn(viewModelScope)

        searchRequest.filterNotNull()
                .debounce(250L)
                .distinctUntilChangedBy { request ->
                    if (request.instantSearch || request.query.length >= 2) {
                        request.query
                    } else {
                        "${request.query}:${request.instantSearch}"
                    }
                }
                .mapLatest { it }
                .onEach { doSearch(it.query, it.instantSearch) }
                .launchIn(viewModelScope)
    }

    fun includeOldTestament(include: Boolean) {
        updateSearchConfig { it.copy(includeOldTestament = include) }
    }

    private inline fun updateSearchConfig(crossinline op: (SearchConfiguration) -> SearchConfiguration) {
        emitViewState { currentViewState ->
            currentViewState.searchConfig?.let { currentSearchConfig ->
                op(currentSearchConfig).takeIf { it != currentSearchConfig }
                        ?.also { searchManager.saveConfiguration(it) }
                        ?.let { currentViewState.copy(searchConfig = it) }
            }
        }
    }

    fun includeNewTestament(include: Boolean) {
        updateSearchConfig { it.copy(includeNewTestament = include) }
    }

    fun includeBookmarks(include: Boolean) {
        updateSearchConfig { it.copy(includeBookmarks = include) }
    }

    fun includeHighlights(include: Boolean) {
        updateSearchConfig { it.copy(includeHighlights = include) }
    }

    fun includeNotes(include: Boolean) {
        updateSearchConfig { it.copy(includeNotes = include) }
    }

    fun search(query: String, instantSearch: Boolean) {
        searchRequest.value = SearchRequest(query, instantSearch)
    }

    fun retrySearch() {
        searchRequest.value?.let { request -> doSearch(request.query, request.instantSearch) }
    }

    private fun doSearch(query: String, instantSearch: Boolean) {
        if (instantSearch && query.length < 2) {
            emitViewState { currentViewState ->
                currentViewState.copy(searchQuery = query, instantSearch = instantSearch, searchResults = emptyList())
            }
            return
        }

        viewModelScope.launch {
            try {
                if (!instantSearch) {
                    emitViewState { currentViewState ->
                        currentViewState.copy(loading = true, searchQuery = query, instantSearch = instantSearch, searchResults = emptyList())
                    }
                }

                val currentTranslation = bibleReadingManager.currentTranslation().firstNotEmpty()
                val searchResult = searchManager.search(query)
                val searchResultItems = buildSearchResultItems(
                        query = query,
                        verses = searchResult.verses,
                        bookmarks = searchResult.bookmarks,
                        highlights = searchResult.highlights,
                        notes = searchResult.notes,
                        bookNames = bibleReadingManager.readBookNames(currentTranslation),
                        bookShortNames = bibleReadingManager.readBookShortNames(currentTranslation)
                )
                emitViewState { currentViewState ->
                    currentViewState.copy(loading = false, searchQuery = query, instantSearch = instantSearch, searchResults = searchResultItems)
                }
                if (!instantSearch) {
                    val searchResultCount = searchResult.verses.size + searchResult.bookmarks.size + searchResult.highlights.size + searchResult.notes.size
                    emitViewAction(ViewAction.ShowToast(application.getString(R.string.toast_search_result, searchResultCount)))
                }
            } catch (e: Exception) {
                Log.e(tag, "Error occurred which searching, query=$query instantSearch=$instantSearch, searchConfig=${viewState().firstOrNull()?.searchConfig}", e)
                emitViewAction(ViewAction.ShowSearchFailedError)
                emitViewState { currentViewState ->
                    currentViewState.copy(loading = false, searchResults = emptyList())
                }
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

    fun openVerse(verseToOpen: VerseIndex) {
        viewModelScope.launch {
            try {
                bibleReadingManager.saveCurrentVerseIndex(verseToOpen)
                emitViewAction(ViewAction.OpenReadingScreen)
            } catch (e: Exception) {
                Log.e(tag, "Failed to save current verse", e)
                emitViewAction(ViewAction.ShowOpenVerseFailedError(verseToOpen))
            }
        }
    }

    fun showPreview(verseIndex: VerseIndex) {
        viewModelScope.launch {
            try {
                emitViewAction(ViewAction.ShowPreview(
                        previewViewData = loadPreviewV2(bibleReadingManager, settingsManager, verseIndex, ::toSearchVersePreviewItems)
                ))
            } catch (e: Exception) {
                Log.e(tag, "Failed to load verses for preview", e)
                emitViewAction(ViewAction.ShowOpenPreviewFailedError(verseIndex))
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun toSearchVersePreviewItems(verses: List<Verse>): List<SearchVersePreviewItem> {
        val items = ArrayList<SearchVersePreviewItem>(verses.size)

        val query = searchRequest.value?.query ?: ""
        val verseIterator = verses.iterator()
        var verse: Verse? = null
        while (verse != null || verseIterator.hasNext()) {
            verse = verse ?: verseIterator.next()

            val (nextVerse, followingEmptyVerseCount) = verseIterator.nextNonEmpty(verse)

            items.add(SearchVersePreviewItem(verse, query, followingEmptyVerseCount))

            verse = nextVerse
        }

        return items
    }
}

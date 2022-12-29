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
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.Bookmark
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.Note
import me.xizzhu.android.joshua.core.SearchConfiguration
import me.xizzhu.android.joshua.core.SearchManager
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.provider.CoroutineDispatcherProvider
import me.xizzhu.android.joshua.infra.BaseViewModelV2
import me.xizzhu.android.joshua.utils.firstNotEmpty
import me.xizzhu.android.logger.Log
import javax.inject.Inject
import me.xizzhu.android.joshua.preview.Preview
import me.xizzhu.android.joshua.preview.buildPreviewVerseWithQueryItems

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val bibleReadingManager: BibleReadingManager,
    private val searchManager: SearchManager,
    private val settingsManager: SettingsManager,
    private val coroutineDispatcherProvider: CoroutineDispatcherProvider,
    private val application: Application
) : BaseViewModelV2<SearchViewModel.ViewAction, SearchViewModel.ViewState>(
    initialViewState = ViewState(
        loading = false,
        searchConfig = null,
        instantSearch = false,
        items = emptyList(),
        scrollItemsToPosition = -1,
        searchResultSummary = null,
        preview = null,
        error = null,
    )
) {
    sealed class ViewAction {
        object OpenReadingScreen : ViewAction()
    }

    data class ViewState(
        val loading: Boolean,
        val searchConfig: SearchConfiguration?,
        val instantSearch: Boolean,
        val items: List<SearchItem>,
        val scrollItemsToPosition: Int,
        val searchResultSummary: String?,
        val preview: Preview?,
        val error: Error?,
    ) {
        sealed class Error {
            data class PreviewLoadingError(val verseToPreview: VerseIndex) : Error()
            object SearchConfigUpdatingError : Error()
            data class VerseOpeningError(val verseToOpen: VerseIndex) : Error()
            object VerseSearchingError : Error()
        }
    }

    private class SearchRequest(val query: String, val instantSearch: Boolean)

    private val searchRequest: MutableStateFlow<SearchRequest?> = MutableStateFlow(null)
    private val searchSuggestions: RecentSearchProvider.SearchSuggestions by lazy(LazyThreadSafetyMode.NONE) {
        RecentSearchProvider.SearchSuggestions(application, viewModelScope, coroutineDispatcherProvider)
    }

    init {
        searchManager.configuration()
            .onEach { searchConfig ->
                updateViewState { it.copy(searchConfig = searchConfig) }
                retrySearch()
            }
            .launchIn(viewModelScope)

        combine(
            settingsManager.settings(),
            searchRequest.filterNotNull()
                .debounce(250L)
                .distinctUntilChangedBy { it.query }
                .mapLatest { it }
        ) { settings, searchRequest ->
            doSearch(settings, searchRequest.query, searchRequest.instantSearch)

            if (!searchRequest.instantSearch) {
                searchSuggestions.saveRecentQuery(searchRequest.query)
            }
        }.flowOn(coroutineDispatcherProvider.default).launchIn(viewModelScope)
    }

    fun includeOldTestament(include: Boolean) {
        updateSearchConfig { it.copy(includeOldTestament = include) }
    }

    private fun updateSearchConfig(op: (SearchConfiguration) -> SearchConfiguration) {
        viewModelScope.launch(coroutineDispatcherProvider.default) {
            runCatching {
                val current = searchManager.configuration().first()
                op(current).takeIf { it != current }?.let { searchManager.saveConfiguration(it) }
            }.onFailure { e ->
                Log.e(tag, "Error occurred which updating search config", e)
                updateViewState { it.copy(error = ViewState.Error.SearchConfigUpdatingError) }
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

    fun search(query: String, instanceSearch: Boolean) {
        searchRequest.value = SearchRequest(query.trim(), instanceSearch)
    }

    fun retrySearch() {
        viewModelScope.launch(coroutineDispatcherProvider.default) {
            searchRequest.value?.let { request ->
                doSearch(
                    settings = settingsManager.settings().first(),
                    query = request.query,
                    instantSearch = request.instantSearch
                )
            }
        }
    }

    private suspend fun doSearch(settings: Settings, query: String, instantSearch: Boolean) {
        if (query.isBlank()) {
            updateViewState { it.copy(items = emptyList()) }
            return
        }

        runCatching {
            if (!instantSearch) {
                updateViewState { it.copy(loading = true, items = emptyList()) }
            }

            val currentTranslation = bibleReadingManager.currentTranslation().firstNotEmpty()
            val searchResult = searchManager.search(query)
            val items = buildSearchResultItems(
                settings = settings,
                query = query,
                verses = searchResult.verses,
                bookmarks = searchResult.bookmarks,
                highlights = searchResult.highlights,
                notes = searchResult.notes,
                bookNames = bibleReadingManager.readBookNames(currentTranslation),
                bookShortNames = bibleReadingManager.readBookShortNames(currentTranslation)
            )
            val toast = if (instantSearch) {
                null
            } else {
                application.getString(R.string.toast_search_result,
                    searchResult.verses.size + searchResult.bookmarks.size + searchResult.highlights.size + searchResult.notes.size)
            }
            updateViewState { current ->
                current.copy(
                    loading = false,
                    instantSearch = instantSearch,
                    items = items,
                    scrollItemsToPosition = 0,
                    searchResultSummary = toast,
                )
            }
        }.onFailure { e ->
            Log.e(tag, "Error occurred which searching, query=$query instantSearch=$instantSearch", e)
            updateViewState { it.copy(loading = false, error = ViewState.Error.VerseSearchingError) }
        }
    }

    private fun buildSearchResultItems(
        settings: Settings,
        query: String,
        verses: List<Verse>,
        bookmarks: List<Pair<Bookmark, Verse>>,
        highlights: List<Pair<Highlight, Verse>>,
        notes: List<Pair<Note, Verse>>,
        bookNames: List<String>,
        bookShortNames: List<String>
    ): List<SearchItem> {
        val items = ArrayList<SearchItem>(
            (if (notes.isEmpty()) 0 else notes.size + 1)
                + (if (bookmarks.isEmpty()) 0 else bookmarks.size + 1)
                + (if (highlights.isEmpty()) 0 else highlights.size + 1)
                + verses.size
                + Bible.BOOK_COUNT
        )

        if (notes.isNotEmpty()) {
            items.add(SearchItem.Header(settings, application.getString(R.string.title_notes)))
            notes.forEach { note ->
                items.add(SearchItem.Note(
                    settings = settings,
                    verseIndex = note.first.verseIndex,
                    bookShortName = bookShortNames[note.first.verseIndex.bookIndex],
                    verseText = note.second.text.text,
                    query = query,
                    note = note.first.note,
                ))
            }
        }

        if (bookmarks.isNotEmpty()) {
            items.add(SearchItem.Header(settings, application.getString(R.string.title_bookmarks)))
            bookmarks.forEach { bookmark ->
                items.add(SearchItem.Verse(
                    settings = settings,
                    verseIndex = bookmark.first.verseIndex,
                    bookShortName = bookShortNames[bookmark.first.verseIndex.bookIndex],
                    verseText = bookmark.second.text.text,
                    query = query,
                    highlightColor = Highlight.COLOR_NONE
                ))
            }
        }

        if (highlights.isNotEmpty()) {
            items.add(SearchItem.Header(settings, application.getString(R.string.title_highlights)))
            highlights.forEach { highlight ->
                items.add(SearchItem.Verse(
                    settings = settings,
                    verseIndex = highlight.first.verseIndex,
                    bookShortName = bookShortNames[highlight.first.verseIndex.bookIndex],
                    verseText = highlight.second.text.text,
                    query = query,
                    highlightColor = highlight.first.color
                ))
            }
        }

        var lastVerseBookIndex = -1
        verses.forEach { verse ->
            val currentVerseBookIndex = verse.verseIndex.bookIndex
            if (lastVerseBookIndex != currentVerseBookIndex) {
                items.add(SearchItem.Header(settings, bookNames[currentVerseBookIndex]))
                lastVerseBookIndex = currentVerseBookIndex
            }
            items.add(SearchItem.Verse(
                settings = settings,
                verseIndex = verse.verseIndex,
                bookShortName = bookShortNames[currentVerseBookIndex],
                verseText = verse.text.text,
                query = query,
                highlightColor = Highlight.COLOR_NONE
            ))
        }

        return items
    }

    fun openVerse(verseToOpen: VerseIndex) {
        viewModelScope.launch(coroutineDispatcherProvider.default) {
            runCatching {
                bibleReadingManager.saveCurrentVerseIndex(verseToOpen)
                emitViewAction(ViewAction.OpenReadingScreen)
            }.onFailure { e ->
                Log.e(tag, "Failed to save current verse", e)
                updateViewState { it.copy(error = ViewState.Error.VerseOpeningError(verseToOpen)) }
            }
        }
    }

    fun loadPreview(verseToPreview: VerseIndex) {
        viewModelScope.launch(coroutineDispatcherProvider.default) {
            runCatching {
                val currentTranslation = bibleReadingManager.currentTranslation().firstNotEmpty()
                val preview = Preview(
                    title = "${bibleReadingManager.readBookShortNames(currentTranslation)[verseToPreview.bookIndex]}, ${verseToPreview.chapterIndex + 1}",
                    items = buildPreviewVerseWithQueryItems(
                        settings = settingsManager.settings().first(),
                        query = searchRequest.value?.query.orEmpty(),
                        verses = bibleReadingManager.readVerses(currentTranslation, verseToPreview.bookIndex, verseToPreview.chapterIndex)
                    ),
                    currentPosition = verseToPreview.verseIndex
                )
                updateViewState { it.copy(preview = preview) }
            }.onFailure { e ->
                Log.e(tag, "Failed to load verses for preview", e)
                updateViewState { it.copy(error = ViewState.Error.PreviewLoadingError(verseToPreview)) }
            }
        }
    }

    fun markItemsAsScrolled() {
        updateViewState { it.copy(scrollItemsToPosition = -1) }
    }

    fun markSearchResultSummaryAsShown() {
        updateViewState { it.copy(searchResultSummary = null) }
    }

    fun markPreviewAsClosed() {
        updateViewState { it.copy(preview = null) }
    }

    fun markErrorAsShown(error: ViewState.Error) {
        updateViewState { current -> if (current.error == error) current.copy(error = null) else null }
    }

    fun clearSearchHistory() {
        searchSuggestions.clearHistory()
    }
}

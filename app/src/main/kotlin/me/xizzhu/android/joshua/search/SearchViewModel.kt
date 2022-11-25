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
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
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
    private val settingsManager: SettingsManager,
    private val coroutineDispatcherProvider: CoroutineDispatcherProvider,
    private val application: Application
) : BaseViewModelV2<SearchViewModel.ViewAction, SearchViewModel.ViewState>(
    initialViewState = ViewState(
        settings = null,
        loading = false,
        searchConfig = null,
        instantSearch = false,
        items = emptyList(),
        preview = null,
        toast = null,
        error = null,
    )
) {
    sealed class ViewAction {
        object OpenReadingScreen : ViewAction()
    }

    data class ViewState(
        val settings: Settings?,
        val loading: Boolean,
        val searchConfig: SearchConfiguration?,
        val instantSearch: Boolean,
        val items: List<BaseItem>,
        val preview: PreviewViewData?,
        val toast: String?,
        val error: Error?,
    ) {
        sealed class Error {
            data class PreviewLoadingError(val verseToPreview: VerseIndex) : Error()
            object SearchConfigUpdatingError : Error()
            data class VerseOpeningError(val verseToOpen: VerseIndex) : Error()
            object VerseSearchingError : Error()
        }
    }

    private class SearchRequest(val query: String, val instanceSearch: Boolean)

    private val searchRequest: MutableStateFlow<SearchRequest?> = MutableStateFlow(null)

    init {
        settingsManager.settings().onEach { settings -> updateViewState { it.copy(settings = settings) } }.launchIn(viewModelScope)

        searchManager.configuration()
            .onEach { searchConfig ->
                updateViewState { it.copy(searchConfig = searchConfig) }
                retrySearch()
            }
            .launchIn(viewModelScope)

        searchRequest.filterNotNull()
            .debounce(250L)
            .distinctUntilChangedBy { it.query }
            .mapLatest { it }
            .onEach { doSearch(it.query, it.instanceSearch) }
            .launchIn(viewModelScope)
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
        searchRequest.value = SearchRequest(query, instanceSearch)
    }

    fun retrySearch() {
        searchRequest.value?.let { request -> doSearch(request.query, request.instanceSearch) }
    }

    private fun doSearch(query: String, instantSearch: Boolean) {
        if (query.isBlank()) {
            updateViewState { it.copy(items = emptyList()) }
            return
        }

        viewModelScope.launch(coroutineDispatcherProvider.default) {
            runCatching {
                if (!instantSearch) {
                    updateViewState { it.copy(loading = true, items = emptyList()) }
                }

                val currentTranslation = bibleReadingManager.currentTranslation().firstNotEmpty()
                val searchResult = searchManager.search(query)
                val items = buildSearchResultItems(
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
                        toast = toast,
                    )
                }
            }.onFailure { e ->
                Log.e(tag, "Error occurred which searching, query=$query instantSearch=$instantSearch", e)
                updateViewState { it.copy(loading = false, error = ViewState.Error.VerseSearchingError) }
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
                val preview = loadPreviewV2(bibleReadingManager, settingsManager, verseToPreview, ::toSearchVersePreviewItems)
                updateViewState { it.copy(preview = preview) }
            }.onFailure { e ->
                Log.e(tag, "Failed to load verses for preview", e)
                updateViewState { it.copy(error = ViewState.Error.PreviewLoadingError(verseToPreview)) }
            }
        }
    }

    private fun toSearchVersePreviewItems(verses: List<Verse>): List<SearchVersePreviewItem> {
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

    fun markPreviewAsClosed() {
        updateViewState { it.copy(preview = null) }
    }

    fun markToastAsShown() {
        updateViewState { it.copy(toast = null) }
    }

    fun markErrorAsShown(error: ViewState.Error) {
        updateViewState { current -> if (current.error == error) current.copy(error = null) else null }
    }
}

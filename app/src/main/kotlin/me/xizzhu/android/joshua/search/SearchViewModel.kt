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
import me.xizzhu.android.joshua.infra.BaseViewModel
import me.xizzhu.android.joshua.infra.onFailure
import me.xizzhu.android.joshua.infra.viewData
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import me.xizzhu.android.joshua.utils.firstNotEmpty
import me.xizzhu.android.logger.Log
import javax.inject.Inject

class SearchConfigurationViewData(val searchConfig: SearchConfiguration)

class SearchResultViewData(val items: List<BaseItem>, val query: String, val instanceSearch: Boolean, val toast: String)

class PreviewViewData(val settings: Settings, val title: String, val items: List<BaseItem>, val currentPosition: Int)

@HiltViewModel
class SearchViewModel @Inject constructor(
        private val bibleReadingManager: BibleReadingManager,
        private val searchManager: SearchManager,
        settingsManager: SettingsManager,
        application: Application
) : BaseViewModel(settingsManager, application) {
    private class SearchRequest(val query: String, val instanceSearch: Boolean)

    private val searchConfig: MutableStateFlow<ViewData<SearchConfigurationViewData>> = MutableStateFlow(ViewData.Loading())
    private val searchRequest: MutableStateFlow<SearchRequest?> = MutableStateFlow(null)
    private val searchResult: MutableStateFlow<ViewData<SearchResultViewData>?> = MutableStateFlow(null)

    init {
        searchManager.configuration()
                .onEach {
                    searchConfig.value = ViewData.Success(SearchConfigurationViewData(it))
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

    fun searchConfig(): Flow<ViewData<SearchConfigurationViewData>> = searchConfig

    fun includeOldTestament(include: Boolean) {
        currentSearchConfig()?.searchConfig?.copy(includeOldTestament = include)?.let { searchManager.saveConfiguration(it) }
    }

    private fun currentSearchConfig(): SearchConfigurationViewData? = (searchConfig.value as? ViewData.Success)?.data

    fun includeNewTestament(include: Boolean) {
        currentSearchConfig()?.searchConfig?.copy(includeNewTestament = include)?.let { searchManager.saveConfiguration(it) }
    }

    fun includeBookmarks(include: Boolean) {
        currentSearchConfig()?.searchConfig?.copy(includeBookmarks = include)?.let { searchManager.saveConfiguration(it) }
    }

    fun includeHighlights(include: Boolean) {
        currentSearchConfig()?.searchConfig?.copy(includeHighlights = include)?.let { searchManager.saveConfiguration(it) }
    }

    fun includeNotes(include: Boolean) {
        currentSearchConfig()?.searchConfig?.copy(includeNotes = include)?.let { searchManager.saveConfiguration(it) }
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
                searchResult.value = searchManager.search(query).let { searchResult ->
                    ViewData.Success(SearchResultViewData(
                            items = buildSearchResultItems(
                                    query = query,
                                    verses = searchResult.verses,
                                    bookmarks = searchResult.bookmarks,
                                    highlights = searchResult.highlights,
                                    notes = searchResult.notes,
                                    bookNames = bibleReadingManager.readBookNames(currentTranslation),
                                    bookShortNames = bibleReadingManager.readBookShortNames(currentTranslation)
                            ),
                            query = query,
                            instanceSearch = instanceSearch,
                            toast = application.getString(R.string.toast_search_result,
                                    searchResult.verses.size + searchResult.bookmarks.size + searchResult.highlights.size + searchResult.notes.size)
                    ))
                }
            } catch (e: Exception) {
                Log.e(tag, "Error occurred which searching, query=$query instanceSearch=$instanceSearch, searchConfig=$searchConfig", e)
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

    fun loadVersesForPreview(verseIndex: VerseIndex): Flow<ViewData<PreviewViewData>> = viewData {
        val currentTranslation = bibleReadingManager.currentTranslation().firstNotEmpty()
        val query = searchRequest.value?.query ?: ""
        val items = bibleReadingManager.readVerses(currentTranslation, verseIndex.bookIndex, verseIndex.chapterIndex)
                .toSearchVersePreviewItems(query)
        PreviewViewData(
                settings = settings().first(),
                title = "${bibleReadingManager.readBookShortNames(currentTranslation)[verseIndex.bookIndex]}, ${verseIndex.chapterIndex + 1}",
                items = items,
                currentPosition = verseIndex.verseIndex
        )
    }.onFailure { Log.e(tag, "Failed to load verses for preview", it) }
}

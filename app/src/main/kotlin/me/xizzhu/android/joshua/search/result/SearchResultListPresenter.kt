/*
 * Copyright (C) 2020 Xizhi Zhu
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

package me.xizzhu.android.joshua.search.result

import android.view.View
import android.widget.ProgressBar
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.activity.BaseSettingsPresenter
import me.xizzhu.android.joshua.infra.arch.*
import me.xizzhu.android.joshua.search.SearchActivity
import me.xizzhu.android.joshua.search.SearchRequest
import me.xizzhu.android.joshua.search.SearchResult
import me.xizzhu.android.joshua.search.SearchViewModel
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.CommonRecyclerView
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import me.xizzhu.android.joshua.ui.toast
import me.xizzhu.android.logger.Log

data class SearchResultViewHolder(val loadingSpinner: ProgressBar,
                                  val searchResultListView: CommonRecyclerView) : ViewHolder

class SearchResultListPresenter(
        private val navigator: Navigator, searchViewModel: SearchViewModel,
        searchActivity: SearchActivity, coroutineScope: CoroutineScope = searchActivity.lifecycleScope
) : BaseSettingsPresenter<SearchResultViewHolder, SearchViewModel, SearchActivity>(searchViewModel, searchActivity, coroutineScope) {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        observeSettings()
        observeSearchRequest()
    }

    private fun observeSettings() {
        viewModel.settings().onEach { viewHolder.searchResultListView.setSettings(it) }.launchIn(coroutineScope)
    }

    private fun observeSearchRequest() {
        viewModel.searchRequest
                .debounce(250L)
                .distinctUntilChangedBy { it.query }
                .mapLatest { it }
                .onEach { search(it) }
                .launchIn(coroutineScope)
    }

    private fun search(searchRequest: SearchRequest) {
        viewModel.search(searchRequest.query)
                .onStart {
                    if (!searchRequest.instantSearch) {
                        viewHolder.loadingSpinner.fadeIn()
                        viewHolder.searchResultListView.visibility = View.GONE
                    }
                }.onEach { searchResult ->
                    viewHolder.searchResultListView.setItems(searchResult.toItems())

                    viewHolder.searchResultListView.scrollToPosition(0)
                    viewHolder.loadingSpinner.visibility = View.GONE

                    if (searchRequest.instantSearch) {
                        viewHolder.searchResultListView.visibility = View.VISIBLE
                    } else {
                        viewHolder.searchResultListView.fadeIn()
                        activity.toast(activity.getString(R.string.toast_verses_searched, searchResult.verses.size))
                    }
                }.catch { e ->
                    Log.e(tag, "Failed to load search verses", e)
                    if (!searchRequest.instantSearch) {
                        viewHolder.loadingSpinner.visibility = View.GONE
                        viewHolder.searchResultListView.visibility = View.GONE
                        activity.dialog(false, R.string.dialog_search_error,
                                { _, _ -> search(searchRequest) })
                    }
                }.launchIn(coroutineScope)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun SearchResult.toItems(): List<BaseItem> {
        val items = ArrayList<BaseItem>(verses.size + Bible.BOOK_COUNT)

        if (notes.isNotEmpty()) {
            items.add(TitleItem(activity.getString(R.string.title_notes), false))
            notes.forEach { note ->
                items.add(SearchNoteItem(
                        note.first.verseIndex, bookShortNames[note.first.verseIndex.bookIndex], note.second.text.text, note.first.note, query, ::selectVerse
                ))
            }
        }

        if (bookmarks.isNotEmpty()) {
            items.add(TitleItem(activity.getString(R.string.title_bookmarks), false))
            bookmarks.forEach { bookmark ->
                items.add(SearchVerseItem(bookmark.first.verseIndex, bookShortNames[bookmark.first.verseIndex.bookIndex],
                        bookmark.second.text.text, query, Highlight.COLOR_NONE, ::selectVerse))
            }
        }

        if (highlights.isNotEmpty()) {
            items.add(TitleItem(activity.getString(R.string.title_highlights), false))
            highlights.forEach { highlight ->
                items.add(SearchVerseItem(highlight.first.verseIndex, bookShortNames[highlight.first.verseIndex.bookIndex],
                        highlight.second.text.text, query, highlight.first.color, ::selectVerse))
            }
        }

        var lastVerseBookIndex = -1
        verses.forEach { verse ->
            val currentVerseBookIndex = verse.verseIndex.bookIndex
            if (lastVerseBookIndex != currentVerseBookIndex) {
                items.add(TitleItem(bookNames[currentVerseBookIndex], false))
                lastVerseBookIndex = currentVerseBookIndex
            }
            items.add(SearchVerseItem(verse.verseIndex, bookShortNames[currentVerseBookIndex],
                    verse.text.text, query, Highlight.COLOR_NONE, ::selectVerse))
        }

        return items
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun selectVerse(verseToSelect: VerseIndex) {
        coroutineScope.launch {
            try {
                viewModel.saveCurrentVerseIndex(verseToSelect)
                navigator.navigate(activity, Navigator.SCREEN_READING)
            } catch (e: Exception) {
                Log.e(tag, "Failed to select verse and open reading activity", e)
                activity.dialog(true, R.string.dialog_verse_selection_error,
                        { _, _ -> selectVerse(verseToSelect) })
            }
        }
    }
}

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

import android.content.DialogInterface
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.activity.BaseSettingsPresenter
import me.xizzhu.android.joshua.infra.arch.*
import me.xizzhu.android.joshua.search.SearchActivity
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
        private val searchActivity: SearchActivity, private val navigator: Navigator,
        searchViewModel: SearchViewModel, lifecycle: Lifecycle,
        lifecycleCoroutineScope: LifecycleCoroutineScope = lifecycle.coroutineScope
) : BaseSettingsPresenter<SearchResultViewHolder, SearchViewModel>(searchViewModel, lifecycle, lifecycleCoroutineScope) {
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun observeSettings() {
        viewModel.settings().onEachSuccess { viewHolder.searchResultListView.setSettings(it) }.launchIn(lifecycleScope)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun observeQuery() {
        viewModel.searchResult().onEach(
                onLoading = {
                    viewHolder.loadingSpinner.fadeIn()
                    viewHolder.searchResultListView.visibility = View.GONE
                },
                onSuccess = { viewData ->
                    viewHolder.searchResultListView.setItems(viewData.toItems())

                    viewHolder.searchResultListView.scrollToPosition(0)
                    viewHolder.loadingSpinner.visibility = View.GONE

                    if (viewData.instantSearch) {
                        viewHolder.searchResultListView.visibility = View.VISIBLE
                    } else {
                        viewHolder.searchResultListView.fadeIn()
                        searchActivity.toast(searchActivity.getString(R.string.toast_verses_searched, viewData.verses.size))
                    }
                },
                onError = { _, e ->
                    Log.e(tag, "Failed to load search verses", e!!)
                    viewHolder.loadingSpinner.visibility = View.GONE
                    // TODO
                }
        ).launchIn(lifecycleScope)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun SearchResult.toItems(): List<BaseItem> {
        val items = ArrayList<BaseItem>(verses.size + Bible.BOOK_COUNT)
        var lastVerseBookIndex = -1
        verses.forEach { verse ->
            val currentVerseBookIndex = verse.verseIndex.bookIndex
            if (lastVerseBookIndex != currentVerseBookIndex) {
                items.add(TitleItem(bookNames[currentVerseBookIndex], false))
                lastVerseBookIndex = currentVerseBookIndex
            }
            items.add(SearchItem(verse.verseIndex, bookShortNames[currentVerseBookIndex],
                    verse.text.text, query, this@SearchResultListPresenter::selectVerse))
        }
        return items
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun selectVerse(verseToSelect: VerseIndex) {
        lifecycleScope.launch {
            try {
                viewModel.saveCurrentVerseIndex(verseToSelect)
                navigator.navigate(searchActivity, Navigator.SCREEN_READING)
            } catch (e: Exception) {
                Log.e(tag, "Failed to select verse and open reading activity", e)
                searchActivity.dialog(true, R.string.dialog_verse_selection_error,
                        DialogInterface.OnClickListener { _, _ -> selectVerse(verseToSelect) })
            }
        }
    }
}

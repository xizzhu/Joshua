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
import androidx.annotation.UiThread
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleCoroutineScope
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

class SearchResultListPresenter(private val searchActivity: SearchActivity,
                                private val navigator: Navigator,
                                searchViewModel: SearchViewModel,
                                lifecycleCoroutineScope: LifecycleCoroutineScope)
    : BaseSettingsPresenter<SearchResultViewHolder, SearchViewModel>(searchViewModel, lifecycleCoroutineScope) {
    @UiThread
    override fun onBind(viewHolder: SearchResultViewHolder) {
        super.onBind(viewHolder)
        observeSettings()
        observeQuery()
    }

    private fun observeSettings() {
        viewModel.settings().onEachSuccess { viewHolder.searchResultListView.setSettings(it) }.launchIn(lifecycleScope)
    }

    private fun observeQuery() {
        viewModel.query()
                .debounce(250L)
                .distinctUntilChangedBy { if (it.status == ViewData.STATUS_ERROR) "" else it.data!! }
                .mapLatest { viewData ->
                    when (viewData.status) {
                        ViewData.STATUS_LOADING -> instantSearch(viewData.data!!)
                        ViewData.STATUS_SUCCESS -> search(viewData.dataOnSuccessOrThrow("Missing query when observing"))
                        ViewData.STATUS_ERROR -> {
                            Log.e(tag, "Error occurred while observing query",
                                    viewData.exception
                                            ?: RuntimeException("Error occurred while observing query"))
                        }
                    }
                }.launchIn(lifecycleScope)
    }

    private suspend fun instantSearch(query: String) {
        try {
            with(viewHolder.searchResultListView) {
                val currentTranslation = viewModel.currentTranslation()
                setItems(viewModel.search(currentTranslation, query).toSearchItems(currentTranslation, query))
                scrollToPosition(0)
                visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            viewHolder.searchResultListView.visibility = View.GONE
            Log.e(tag, "Failed to search verses", e)
        }
    }

    private suspend fun search(query: String) {
        try {
            with(viewHolder) {
                loadingSpinner.fadeIn()

                searchResultListView.visibility = View.GONE

                val currentTranslation = viewModel.currentTranslation()
                val verses = viewModel.search(currentTranslation, query)
                searchResultListView.setItems(verses.toSearchItems(currentTranslation, query))
                searchResultListView.scrollToPosition(0)
                searchResultListView.fadeIn()

                searchActivity.toast(searchActivity.getString(R.string.toast_verses_searched, verses.size))
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to search verses", e)
            searchActivity.dialog(true, R.string.dialog_search_error,
                    DialogInterface.OnClickListener { _, _ -> lifecycleScope.launch { search(query) } })
        } finally {
            viewHolder.loadingSpinner.visibility = View.GONE
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    suspend fun List<Verse>.toSearchItems(currentTranslation: String, query: String): List<BaseItem> {
        val bookNames = viewModel.bookNames(currentTranslation)
        val bookShortNames = viewModel.bookShortNames(currentTranslation)
        val items = ArrayList<BaseItem>(size + Bible.BOOK_COUNT)
        var lastVerseBookIndex = -1
        forEach { verse ->
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

/*
 * Copyright (C) 2019 Xizhi Zhu
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
import androidx.annotation.UiThread
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.infra.arch.ViewHolder
import me.xizzhu.android.joshua.infra.arch.collectOnSuccess
import me.xizzhu.android.joshua.infra.interactors.BaseSettingsAwarePresenter
import me.xizzhu.android.joshua.search.SearchActivity
import me.xizzhu.android.joshua.ui.DialogHelper
import me.xizzhu.android.joshua.ui.ToastHelper
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.CommonRecyclerView
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import me.xizzhu.android.logger.Log

data class SearchResultViewHolder(val searchResultListView: CommonRecyclerView) : ViewHolder

class SearchResultListPresenter(private val searchActivity: SearchActivity,
                                private val navigator: Navigator,
                                searchResultInteractor: SearchResultInteractor,
                                dispatcher: CoroutineDispatcher = Dispatchers.Main)
    : BaseSettingsAwarePresenter<SearchResultViewHolder, SearchResultInteractor>(searchResultInteractor, dispatcher) {
    @UiThread
    override fun onStart() {
        super.onStart()

        observeSettings()
        observeSearchResult()
    }

    private fun observeSettings() {
        coroutineScope.launch { interactor.settings().collectOnSuccess { viewHolder?.searchResultListView?.setSettings(it) } }
    }

    private fun observeSearchResult() {
        coroutineScope.launch {
            interactor.searchResult().collect { viewData ->
                when (viewData.status) {
                    ViewData.STATUS_LOADING -> viewHolder?.searchResultListView?.visibility = View.GONE
                    ViewData.STATUS_SUCCESS -> viewData.data
                            ?.let { searchResult ->
                                viewHolder?.searchResultListView?.run {
                                    setItems(searchResult.verses.toSearchItems(searchResult.query))
                                    scrollToPosition(0)
                                    fadeIn()
                                    ToastHelper.showToast(searchActivity, searchActivity.getString(R.string.toast_verses_searched, searchResult.verses.size))
                                }
                            }
                            ?: throw IllegalStateException("Missing search result")
                    ViewData.STATUS_ERROR -> viewData.data
                            ?.let { searchResult ->
                                DialogHelper.showDialog(searchActivity, true, R.string.dialog_search_error,
                                        DialogInterface.OnClickListener { _, _ -> interactor.search(searchResult.query) })
                            }
                            ?: throw IllegalStateException("Missing search result")
                    else -> throw IllegalStateException("Unsupported view data status: ${viewData.status}")
                }
            }
        }
    }

    @VisibleForTesting
    suspend fun List<Verse>.toSearchItems(query: String): List<BaseItem> {
        val bookNames = interactor.readBookNames()
        val bookShortNames = interactor.readBookShortNames()
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

    @VisibleForTesting
    fun selectVerse(verseToSelect: VerseIndex) {
        coroutineScope.launch {
            try {
                interactor.saveCurrentVerseIndex(verseToSelect)
                navigator.navigate(searchActivity, Navigator.SCREEN_READING)
            } catch (e: Exception) {
                Log.e(tag, "Failed to select verse and open reading activity", e)
                DialogHelper.showDialog(searchActivity, true, R.string.dialog_verse_selection_error,
                        DialogInterface.OnClickListener { _, _ -> selectVerse(verseToSelect) })
            }
        }
    }
}

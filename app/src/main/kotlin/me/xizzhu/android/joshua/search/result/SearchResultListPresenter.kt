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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.arch.*
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
        observeQuery()
    }

    private fun observeSettings() {
        interactor.settings().onEachSuccess { viewHolder?.searchResultListView?.setSettings(it) }.launchIn(coroutineScope)
    }

    private fun observeQuery() {
        interactor.query()
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
                }.launchIn(coroutineScope)
    }

    @VisibleForTesting
    suspend fun instantSearch(query: String) {
        try {
            viewHolder?.searchResultListView?.run {
                setItems(interactor.search(query).dataOnSuccessOrThrow("Failed to search verses").toSearchItems(query))
                scrollToPosition(0)
                visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            viewHolder?.searchResultListView?.visibility = View.GONE
            Log.e(tag, "Failed to search verses", e)
        }
    }

    @VisibleForTesting
    fun search(query: String) {
        coroutineScope.launch {
            try {
                interactor.updateLoadingState(ViewData.loading())

                viewHolder?.searchResultListView?.run {
                    visibility = View.GONE

                    val verses = interactor.search(query).dataOnSuccessOrThrow("Failed to search verses")
                    setItems(verses.toSearchItems(query))

                    scrollToPosition(0)
                    fadeIn()
                    ToastHelper.showToast(searchActivity, searchActivity.getString(R.string.toast_verses_searched, verses.size))
                }

                interactor.updateLoadingState(ViewData.success(null))
            } catch (e: Exception) {
                Log.e(tag, "Failed to search verses", e)
                interactor.updateLoadingState(ViewData.error(exception = e))
                DialogHelper.showDialog(searchActivity, true, R.string.dialog_search_error,
                        DialogInterface.OnClickListener { _, _ -> search(query) })
            }
        }
    }

    @VisibleForTesting
    suspend fun List<Verse>.toSearchItems(query: String): List<BaseItem> {
        val bookNames = interactor.bookNames().dataOnSuccessOrThrow("Failed to load book names")
        val bookShortNames = interactor.bookShortNames().dataOnSuccessOrThrow("Failed to load book short names")
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

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

package me.xizzhu.android.joshua.search.toolbar

import android.app.SearchManager
import android.content.Context
import android.provider.SearchRecentSuggestions
import androidx.annotation.UiThread
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.SearchView
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.infra.arch.ViewHolder
import me.xizzhu.android.joshua.infra.arch.ViewPresenter
import me.xizzhu.android.joshua.search.SearchActivity
import me.xizzhu.android.joshua.ui.hideKeyboard

data class SearchToolbarViewHolder(val searchToolbar: SearchToolbar) : ViewHolder

class SearchToolbarPresenter(private val searchActivity: SearchActivity,
                             searchToolbarInteractor: SearchToolbarInteractor,
                             dispatcher: CoroutineDispatcher = Dispatchers.Main)
    : ViewPresenter<SearchToolbarViewHolder, SearchToolbarInteractor>(searchToolbarInteractor, dispatcher) {
    private val searchRecentSuggestions: SearchRecentSuggestions = RecentSearchProvider.createSearchRecentSuggestions(searchActivity)

    @VisibleForTesting
    val onQueryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String): Boolean {
            searchRecentSuggestions.saveRecentQuery(query, null)
            interactor.submitQuery(query)
            viewHolder?.searchToolbar?.hideKeyboard()

            // so that the system can close the search suggestion
            return false
        }

        override fun onQueryTextChange(newText: String): Boolean {
            interactor.updateQuery(newText)
            return true
        }
    }

    @UiThread
    override fun onBind(viewHolder: SearchToolbarViewHolder) {
        super.onBind(viewHolder)

        with(viewHolder.searchToolbar) {
            setOnQueryTextListener(onQueryTextListener)
            setSearchableInfo((searchActivity.getSystemService(Context.SEARCH_SERVICE) as SearchManager)
                    .getSearchableInfo(searchActivity.componentName))
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_clear_search_history -> {
                        coroutineScope.launch(Dispatchers.IO) { searchRecentSuggestions.clearHistory() }
                        true
                    }
                    else -> false
                }
            }
        }
    }

    @UiThread
    override fun onUnbind() {
        viewHolder?.searchToolbar?.setOnQueryTextListener(null)
        super.onUnbind()
    }
}

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

package me.xizzhu.android.joshua.search.toolbar

import android.app.SearchManager
import android.content.Context
import android.provider.SearchRecentSuggestions
import androidx.annotation.UiThread
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.infra.activity.BaseSettingsPresenter
import me.xizzhu.android.joshua.infra.arch.ViewHolder
import me.xizzhu.android.joshua.search.SearchActivity
import me.xizzhu.android.joshua.search.SearchQuery
import me.xizzhu.android.joshua.search.SearchViewModel
import me.xizzhu.android.joshua.ui.hideKeyboard

data class SearchToolbarViewHolder(val searchToolbar: SearchToolbar) : ViewHolder

class SearchToolbarPresenter(
        searchViewModel: SearchViewModel, searchActivity: SearchActivity,
        coroutineScope: CoroutineScope = searchActivity.lifecycleScope
) : BaseSettingsPresenter<SearchToolbarViewHolder, SearchViewModel, SearchActivity>(searchViewModel, searchActivity, coroutineScope) {
    private val searchRecentSuggestions: SearchRecentSuggestions = RecentSearchProvider.createSearchRecentSuggestions(searchActivity)

    private val onQueryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String): Boolean {
            searchRecentSuggestions.saveRecentQuery(query, null)
            viewModel.updateQuery(SearchQuery(query, false))
            viewHolder.searchToolbar.hideKeyboard()

            // so that the system can close the search suggestion
            return false
        }

        override fun onQueryTextChange(newText: String): Boolean {
            viewModel.updateQuery(SearchQuery(newText, true))
            return true
        }
    }

    @UiThread
    override fun onBind() {
        super.onBind()

        with(viewHolder.searchToolbar) {
            setOnQueryTextListener(onQueryTextListener)
            setSearchableInfo((activity.getSystemService(Context.SEARCH_SERVICE) as SearchManager)
                    .getSearchableInfo(activity.componentName))
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
}

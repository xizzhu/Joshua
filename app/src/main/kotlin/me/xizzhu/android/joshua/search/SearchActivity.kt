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

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.databinding.ActivitySearchBinding
import me.xizzhu.android.joshua.infra.*
import me.xizzhu.android.joshua.ui.*

@AndroidEntryPoint
class SearchActivity : BaseActivityV2<ActivitySearchBinding, SearchViewModel>(), SearchNoteItem.Callback, SearchVerseItem.Callback, SearchVersePreviewItem.Callback {
    private val searchViewModel: SearchViewModel by viewModels()

    private lateinit var searchRecentSuggestions: SearchRecentSuggestions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        searchRecentSuggestions = RecentSearchProvider.createSearchRecentSuggestions(this)
        searchViewModel.viewAction().onEach(::onViewAction).launchIn(lifecycleScope)
        searchViewModel.viewState().onEach(::onViewState).launchIn(lifecycleScope)
        initializeListeners()
    }

    private fun onViewAction(viewAction: SearchViewModel.ViewAction) = when (viewAction) {
        is SearchViewModel.ViewAction.ShowToast -> toast(viewAction.message)
        SearchViewModel.ViewAction.ShowSearchFailedError -> {
            with(viewBinding) {
                loadingSpinner.visibility = View.GONE
                dialog(false, R.string.dialog_title_error, R.string.dialog_message_failed_to_search,
                        { _, _ -> searchViewModel.retrySearch() }, { _, _ -> finish() })
            }
        }
    }

    private fun onViewState(viewState: SearchViewModel.ViewState) = with(viewBinding) {
        viewState.settings?.let { searchResult.setSettings(it) }
        viewState.searchConfig?.let { searchConfig ->
            toolbar.setSearchConfiguration(
                    includeOldTestament = searchConfig.includeOldTestament,
                    includeNewTestament = searchConfig.includeNewTestament,
                    includeBookmarks = searchConfig.includeBookmarks,
                    includeHighlights = searchConfig.includeHighlights,
                    includeNotes = searchConfig.includeNotes,
            )
        }
        if (viewState.loading) {
            loadingSpinner.fadeIn()
            searchResult.visibility = View.GONE
        } else {
            loadingSpinner.visibility = View.GONE
            if (viewState.instantSearch) {
                searchResult.visibility = View.VISIBLE
            } else {
                searchResult.fadeIn()
            }
        }
        searchResult.setItems(viewState.searchResults)
        searchResult.scrollToPosition(0)
    }

    private fun initializeListeners() {
        viewBinding.toolbar.initialize(
                onIncludeOldTestamentChanged = { searchViewModel.includeOldTestament(it) },
                onIncludeNewTestamentChanged = { searchViewModel.includeNewTestament(it) },
                onIncludeBookmarksChanged = { searchViewModel.includeBookmarks(it) },
                onIncludeHighlightsChanged = { searchViewModel.includeHighlights(it) },
                onIncludeNotesChanged = { searchViewModel.includeNotes(it) },
                onQueryTextListener = object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String): Boolean {
                        searchRecentSuggestions.saveRecentQuery(query, null)
                        searchViewModel.search(query, false)
                        viewBinding.toolbar.hideKeyboard()

                        // so that the system can close the search suggestion
                        return false
                    }

                    override fun onQueryTextChange(newText: String): Boolean {
                        searchViewModel.search(newText, true)
                        return true
                    }
                },
                clearHistory = { lifecycleScope.launch(Dispatchers.IO) { searchRecentSuggestions.clearHistory() } }
        )

        // It's possible that the system has no SearchManager available, and on some devices getSearchableInfo() could return null.
        // See https://console.firebase.google.com/u/0/project/joshua-production/crashlytics/app/android:me.xizzhu.android.joshua/issues/45465ea5dc4f7722c6ce6b8889196249?time=last-seven-days&type=all
        (applicationContext.getSystemService(Context.SEARCH_SERVICE) as? SearchManager)
                ?.getSearchableInfo(componentName)?.let { viewBinding.toolbar.setSearchableInfo(it) }
    }

    override fun inflateViewBinding(): ActivitySearchBinding = ActivitySearchBinding.inflate(layoutInflater)

    override fun viewModel(): SearchViewModel = searchViewModel

    override fun openVerse(verseToOpen: VerseIndex) {
        lifecycleScope.launch {
            searchViewModel.saveCurrentVerseIndex(verseToOpen)
                    .onSuccess { navigator.navigate(this@SearchActivity, Navigator.SCREEN_READING) }
                    .onFailure { dialog(true, R.string.dialog_title_error, R.string.dialog_message_failed_to_select_verse, { _, _ -> openVerse(verseToOpen) }) }
        }
    }

    override fun showPreview(verseIndex: VerseIndex) {
        lifecycleScope.launch {
            searchViewModel.loadVersesForPreview(verseIndex)
                .onSuccess { preview -> listDialog(preview.title, preview.settings, preview.items, preview.currentPosition) }
                    .onFailure { openVerse(verseIndex) } // Very unlikely to fail, so just falls back to open the verse.
        }
    }
}

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
import me.xizzhu.android.joshua.infra.BaseActivity
import me.xizzhu.android.joshua.infra.onEach
import me.xizzhu.android.joshua.infra.onFailure
import me.xizzhu.android.joshua.infra.onSuccess
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.hideKeyboard
import me.xizzhu.android.joshua.ui.toast
import me.xizzhu.android.logger.Log

@AndroidEntryPoint
class SearchActivity : BaseActivity<ActivitySearchBinding>(), SearchNoteItem.Callback, SearchVerseItem.Callback {
    private val searchViewModel: SearchViewModel by viewModels()

    private lateinit var searchRecentSuggestions: SearchRecentSuggestions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        searchRecentSuggestions = RecentSearchProvider.createSearchRecentSuggestions(this)
        observeSettings()
        observeSearchResults()
        initializeListeners()
    }

    private fun observeSettings() {
        searchViewModel.settings()
                .onEach { viewBinding.searchResult.setSettings(it) }
                .launchIn(lifecycleScope)
    }

    private fun observeSearchResults() {
        searchViewModel.searchResult()
                .onEach(
                        onLoading = {
                            with(viewBinding) {
                                loadingSpinner.fadeIn()
                                searchResult.visibility = View.GONE
                            }
                        },
                        onSuccess = { viewData ->
                            with(viewBinding) {
                                searchResult.setItems(viewData.items)
                                searchResult.scrollToPosition(0)

                                if (viewData.instanceSearch) {
                                    searchResult.visibility = View.VISIBLE
                                } else {
                                    searchResult.fadeIn()
                                    toast(viewData.toast)
                                }

                                loadingSpinner.visibility = View.GONE
                            }
                        },
                        onFailure = {
                            with(viewBinding) {
                                loadingSpinner.visibility = View.GONE
                                dialog(false, R.string.dialog_search_error,
                                        { _, _ -> searchViewModel.retrySearch() }, { _, _ -> finish() })
                            }
                        }
                )
                .launchIn(lifecycleScope)
    }

    private fun initializeListeners() {
        with(viewBinding.toolbar) {
            initialize(
                    includeBookmarks = searchViewModel.includeBookmarks,
                    onIncludeBookmarksChanged = { searchViewModel.includeBookmarks = it },
                    includeHighlights = searchViewModel.includeHighlights,
                    onIncludeHighlightsChanged = { searchViewModel.includeHighlights = it },
                    includeNotes = searchViewModel.includeNotes,
                    onIncludeNotesChanged = { searchViewModel.includeNotes = it },
                    onQueryTextListener = object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String): Boolean {
                            searchRecentSuggestions.saveRecentQuery(query, null)
                            searchViewModel.search(query, false)
                            hideKeyboard()

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

            try {
                setSearchableInfo((applicationContext.getSystemService(Context.SEARCH_SERVICE) as SearchManager).getSearchableInfo(componentName))
            } catch (e: Exception) {
                // Do nothing if it fails on some weird devices.
                // https://console.firebase.google.com/u/0/project/joshua-production/crashlytics/app/android:me.xizzhu.android.joshua/issues/526587497106cd43ddd9ea7568d34f94
                Log.e(this@SearchActivity.tag, "", e)
            }
        }
    }

    override fun inflateViewBinding(): ActivitySearchBinding = ActivitySearchBinding.inflate(layoutInflater)

    override fun openVerse(verseToOpen: VerseIndex) {
        searchViewModel.openVerse(verseToOpen)
                .onSuccess { navigator.navigate(this, Navigator.SCREEN_READING) }
                .onFailure { dialog(true, R.string.dialog_verse_selection_error, { _, _ -> openVerse(verseToOpen) }) }
                .launchIn(lifecycleScope)
    }
}

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
import me.xizzhu.android.joshua.infra.BaseActivity
import me.xizzhu.android.joshua.infra.onEach
import me.xizzhu.android.joshua.infra.onFailure
import me.xizzhu.android.joshua.infra.onSuccess
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.hideKeyboard
import me.xizzhu.android.joshua.ui.listDialog
import me.xizzhu.android.joshua.ui.toast

@AndroidEntryPoint
class SearchActivity : BaseActivity<ActivitySearchBinding, SearchViewModel>(), SearchNoteItem.Callback, SearchVerseItem.Callback, SearchVersePreviewItem.Callback {
    private val searchViewModel: SearchViewModel by viewModels()

    private lateinit var searchRecentSuggestions: SearchRecentSuggestions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        searchRecentSuggestions = RecentSearchProvider.createSearchRecentSuggestions(this)
        observeSettings()
        observeSearchConfiguration()
        observeSearchResults()
        initializeListeners()
    }

    private fun observeSettings() {
        searchViewModel.settings().onEach { viewBinding.searchResult.setSettings(it) }.launchIn(lifecycleScope)
    }

    private fun observeSearchConfiguration() {
        searchViewModel.searchConfig()
                .onSuccess { searchConfiguration ->
                    viewBinding.toolbar.setSearchConfiguration(
                            includeOldTestament = searchConfiguration.searchConfig.includeOldTestament,
                            includeNewTestament = searchConfiguration.searchConfig.includeNewTestament,
                            includeBookmarks = searchConfiguration.searchConfig.includeBookmarks,
                            includeHighlights = searchConfiguration.searchConfig.includeHighlights,
                            includeNotes = searchConfiguration.searchConfig.includeNotes,
                    )
                }
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
                                dialog(false, R.string.dialog_title_error, R.string.dialog_message_failed_to_search,
                                        { _, _ -> searchViewModel.retrySearch() }, { _, _ -> finish() })
                            }
                        }
                )
                .launchIn(lifecycleScope)
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
        searchViewModel.saveCurrentVerseIndex(verseToOpen)
                .onSuccess { navigator.navigate(this, Navigator.SCREEN_READING) }
                .onFailure { dialog(true, R.string.dialog_title_error, R.string.dialog_message_failed_to_select_verse, { _, _ -> openVerse(verseToOpen) }) }
                .launchIn(lifecycleScope)
    }

    override fun showPreview(verseIndex: VerseIndex) {
        searchViewModel.loadVersesForPreview(verseIndex)
                .onSuccess { preview -> listDialog(preview.title, preview.settings, preview.items, preview.currentPosition) }
                .onFailure { openVerse(verseIndex) } // Very unlikely to fail, so just falls back to open the verse.
                .launchIn(lifecycleScope)
    }
}

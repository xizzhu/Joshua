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
import android.provider.SearchRecentSuggestions
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.provider.CoroutineDispatcherProvider
import me.xizzhu.android.joshua.databinding.ActivitySearchBinding
import me.xizzhu.android.joshua.infra.BaseActivityV2
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.hideKeyboard
import me.xizzhu.android.joshua.ui.listDialog
import me.xizzhu.android.joshua.ui.toast
import javax.inject.Inject

@AndroidEntryPoint
class SearchActivity : BaseActivityV2<ActivitySearchBinding, SearchViewModel.ViewAction, SearchViewModel.ViewState, SearchViewModel>(), SearchVersePreviewItem.Callback {
    @Inject
    lateinit var coroutineDispatcherProvider: CoroutineDispatcherProvider

    private lateinit var searchRecentSuggestions: SearchRecentSuggestions
    private lateinit var adapter: SearchAdapter

    override val viewModel: SearchViewModel by viewModels()

    override val viewBinding: ActivitySearchBinding by lazy(LazyThreadSafetyMode.NONE) { ActivitySearchBinding.inflate(layoutInflater) }

    override fun initializeView() {
        searchRecentSuggestions = RecentSearchProvider.createSearchRecentSuggestions(this)

        viewBinding.toolbar.initialize(
            onIncludeOldTestamentChanged = { viewModel.includeOldTestament(it) },
            onIncludeNewTestamentChanged = { viewModel.includeNewTestament(it) },
            onIncludeBookmarksChanged = { viewModel.includeBookmarks(it) },
            onIncludeHighlightsChanged = { viewModel.includeHighlights(it) },
            onIncludeNotesChanged = { viewModel.includeNotes(it) },
            onQueryTextListener = object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    searchRecentSuggestions.saveRecentQuery(query, null)
                    viewModel.search(query, false)
                    viewBinding.toolbar.hideKeyboard()

                    // so that the system can close the search suggestion
                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    viewModel.search(newText, true)
                    return true
                }
            },
            clearHistory = { lifecycleScope.launch(coroutineDispatcherProvider.io) { searchRecentSuggestions.clearHistory() } }
        )

        // It's possible that the system has no SearchManager available, and on some devices getSearchableInfo() could return null.
        // See https://console.firebase.google.com/u/0/project/joshua-production/crashlytics/app/android:me.xizzhu.android.joshua/issues/45465ea5dc4f7722c6ce6b8889196249?time=last-seven-days&type=all
        (applicationContext.getSystemService(Context.SEARCH_SERVICE) as? SearchManager)
            ?.getSearchableInfo(componentName)?.let { viewBinding.toolbar.setSearchableInfo(it) }

        adapter = SearchAdapter(
            inflater = layoutInflater,
            executor = coroutineDispatcherProvider.default.asExecutor()
        ) { viewEvent ->
            when (viewEvent) {
                is SearchAdapter.ViewEvent.OpenVerse -> viewModel.openVerse(viewEvent.verseToOpen)
                is SearchAdapter.ViewEvent.ShowPreview -> viewModel.loadPreview(viewEvent.verseToPreview)
            }
        }
        viewBinding.searchResult.adapter = adapter
    }

    override fun onViewActionEmitted(viewAction: SearchViewModel.ViewAction) = when (viewAction) {
        SearchViewModel.ViewAction.OpenReadingScreen -> navigator.navigate(this, Navigator.SCREEN_READING)
    }

    override fun onViewStateUpdated(viewState: SearchViewModel.ViewState) = with(viewBinding) {
        if (viewState.loading) {
            loadingSpinner.fadeIn()
            searchResult.isVisible = false
        } else {
            if (viewState.instantSearch) {
                searchResult.isVisible = true
            } else {
                searchResult.fadeIn()
            }

            loadingSpinner.isVisible = false
        }

        viewState.searchConfig?.let { searchConfig ->
            toolbar.setSearchConfiguration(
                includeOldTestament = searchConfig.includeOldTestament,
                includeNewTestament = searchConfig.includeNewTestament,
                includeBookmarks = searchConfig.includeBookmarks,
                includeHighlights = searchConfig.includeHighlights,
                includeNotes = searchConfig.includeNotes,
            )
        }

        adapter.submitList(viewState.items)
        if (viewState.scrollItemsToPosition >= 0) {
            searchResult.scrollToPosition(viewState.scrollItemsToPosition)
            viewModel.markItemsAsScrolled()
        }

        viewState.preview?.let { preview ->
            listDialog(
                title = preview.title,
                settings = preview.settings,
                items = preview.items,
                selected = preview.currentPosition,
                onDismiss = { viewModel.markPreviewAsClosed() }
            )
        }

        viewState.toast?.let {
            toast(it)
            viewModel.markToastAsShown()
        }

        when (val error = viewState.error) {
            is SearchViewModel.ViewState.Error.PreviewLoadingError -> {
                viewModel.markErrorAsShown(error)

                // Very unlikely to fail, so just falls back to open the verse.
                openVerse(error.verseToPreview)
            }
            is SearchViewModel.ViewState.Error.SearchConfigUpdatingError -> {
                toast(R.string.toast_unknown_error)
                viewModel.markErrorAsShown(error)
            }
            is SearchViewModel.ViewState.Error.VerseOpeningError -> {
                dialog(
                    cancelable = true,
                    title = R.string.dialog_title_error,
                    message = R.string.dialog_message_failed_to_select_verse,
                    onPositive = { _, _ -> openVerse(error.verseToOpen) },
                    onDismiss = { viewModel.markErrorAsShown(error) }
                )
            }
            is SearchViewModel.ViewState.Error.VerseSearchingError -> {
                dialog(
                    cancelable = false,
                    title = R.string.dialog_title_error,
                    message = R.string.dialog_message_failed_to_search,
                    onPositive = { _, _ -> viewModel.retrySearch() },
                    onNegative = { _, _ -> finish() },
                    onDismiss = { viewModel.markErrorAsShown(error) }
                )
            }
            null -> {
                // Do nothing
            }
        }
    }

    override fun openVerse(verseToOpen: VerseIndex) {
        viewModel.openVerse(verseToOpen)
    }
}

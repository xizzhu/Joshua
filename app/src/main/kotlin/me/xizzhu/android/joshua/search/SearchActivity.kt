/*
 * Copyright (C) 2023 Xizhi Zhu
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
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.asExecutor
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.provider.CoroutineDispatcherProvider
import me.xizzhu.android.joshua.databinding.ActivitySearchBinding
import me.xizzhu.android.joshua.infra.BaseActivityV2
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.hideKeyboard
import me.xizzhu.android.joshua.ui.listDialog
import me.xizzhu.android.joshua.ui.toast
import javax.inject.Inject
import me.xizzhu.android.joshua.preview.Preview
import me.xizzhu.android.joshua.preview.PreviewAdapter

@AndroidEntryPoint
class SearchActivity : BaseActivityV2<ActivitySearchBinding, SearchViewModel.ViewAction, SearchViewModel.ViewState, SearchViewModel>() {
    @Inject
    lateinit var coroutineDispatcherProvider: CoroutineDispatcherProvider

    private lateinit var adapter: SearchAdapter

    override val viewModel: SearchViewModel by viewModels()

    override val viewBinding: ActivitySearchBinding by lazy(LazyThreadSafetyMode.NONE) { ActivitySearchBinding.inflate(layoutInflater) }

    override fun initializeView() {
        viewBinding.toolbar.initialize(
            onIncludeOldTestamentChanged = viewModel::includeOldTestament,
            onIncludeNewTestamentChanged = viewModel::includeNewTestament,
            onIncludeBookmarksChanged = viewModel::includeBookmarks,
            onIncludeHighlightsChanged = viewModel::includeHighlights,
            onIncludeNotesChanged = viewModel::includeNotes,
            onQueryTextListener = object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
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
            clearHistory = viewModel::clearSearchHistory,
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

    override fun onViewStateUpdated(viewState: SearchViewModel.ViewState): Unit = with(viewBinding) {
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

        viewState.searchResultSummary?.let {
            toast(it)
            viewModel.markSearchResultSummaryAsShown()
        }

        viewState.preview?.handle()
        viewState.error?.handle()
    }

    private fun Preview.handle() {
        val previewAdapter = PreviewAdapter(
            inflater = layoutInflater,
            executor = coroutineDispatcherProvider.default.asExecutor()
        ) { viewEvent ->
            when (viewEvent) {
                is PreviewAdapter.ViewEvent.OpenVerse -> viewModel.openVerse(viewEvent.verseToOpen)
            }
        }
        listDialog(
            title = title,
            adapter = previewAdapter,
            scrollToPosition = currentPosition,
            onDismiss = viewModel::markPreviewAsClosed,
        )
        previewAdapter.submitList(items)
    }

    private fun SearchViewModel.ViewState.Error.handle() = when (this) {
        is SearchViewModel.ViewState.Error.PreviewLoadingError -> {
            viewModel.markErrorAsShown(this)

            // Very unlikely to fail, so just falls back to open the verse.
            viewModel.openVerse(verseToPreview)
        }
        is SearchViewModel.ViewState.Error.SearchConfigUpdatingError -> {
            toast(R.string.toast_unknown_error)
            viewModel.markErrorAsShown(this)
        }
        is SearchViewModel.ViewState.Error.VerseOpeningError -> {
            dialog(
                cancelable = true,
                title = R.string.dialog_title_error,
                message = R.string.dialog_message_failed_to_select_verse,
                onPositive = { _, _ -> viewModel.openVerse(verseToOpen) },
                onDismiss = { viewModel.markErrorAsShown(this) }
            )
        }
        is SearchViewModel.ViewState.Error.VerseSearchingError -> {
            dialog(
                cancelable = false,
                title = R.string.dialog_title_error,
                message = R.string.dialog_message_failed_to_search,
                onPositive = { _, _ -> viewModel.retrySearch() },
                onNegative = { _, _ -> navigator.goBack(this@SearchActivity) },
                onDismiss = { viewModel.markErrorAsShown(this) }
            )
        }
    }
}

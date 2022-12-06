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

package me.xizzhu.android.joshua.annotated

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import kotlinx.coroutines.asExecutor
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.VerseAnnotation
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.provider.CoroutineDispatcherProvider
import me.xizzhu.android.joshua.databinding.ActivityAnnotatedBinding
import me.xizzhu.android.joshua.infra.BaseActivityV2
import me.xizzhu.android.joshua.preview.VersePreviewItem
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.listDialog
import javax.inject.Inject
import me.xizzhu.android.joshua.preview.PreviewAdapter

abstract class AnnotatedVerseActivity<V : VerseAnnotation, VM : AnnotatedVerseViewModel<V>>(
    @StringRes private val toolbarText: Int
) : BaseActivityV2<ActivityAnnotatedBinding, AnnotatedVerseViewModel.ViewAction, AnnotatedVerseViewModel.ViewState, VM>(), VersePreviewItem.Callback {
    @Inject
    lateinit var coroutineDispatcherProvider: CoroutineDispatcherProvider

    private lateinit var adapter: AnnotatedVerseAdapter

    override val viewBinding: ActivityAnnotatedBinding by lazy(LazyThreadSafetyMode.NONE) { ActivityAnnotatedBinding.inflate(layoutInflater) }

    override fun initializeView() {
        adapter = AnnotatedVerseAdapter(
            inflater = layoutInflater,
            executor = coroutineDispatcherProvider.default.asExecutor()
        ) { viewEvent ->
            when (viewEvent) {
                is AnnotatedVerseAdapter.ViewEvent.OpenVerse -> viewModel.openVerse(viewEvent.verseToOpen)
                is AnnotatedVerseAdapter.ViewEvent.ShowPreview -> viewModel.loadPreview(viewEvent.verseToPreview)
            }
        }
        viewBinding.verseList.adapter = adapter
    }

    override fun onViewActionEmitted(viewAction: AnnotatedVerseViewModel.ViewAction) = when (viewAction) {
        AnnotatedVerseViewModel.ViewAction.OpenReadingScreen -> navigator.navigate(this, Navigator.SCREEN_READING, extrasForOpeningVerse())
    }

    override fun onViewStateUpdated(viewState: AnnotatedVerseViewModel.ViewState) = with(viewBinding) {
        toolbar.setSortOrder(viewState.sortOrder)

        if (viewState.loading) {
            loadingSpinner.fadeIn()
            verseList.isVisible = false
        } else {
            loadingSpinner.isVisible = false
            verseList.fadeIn()
        }

        adapter.submitList(viewState.items)

        viewState.preview?.let { preview ->
            val previewAdapter = PreviewAdapter(
                inflater = layoutInflater,
                executor = coroutineDispatcherProvider.default.asExecutor()
            ) { viewEvent ->
                when (viewEvent) {
                    is PreviewAdapter.ViewEvent.OpenVerse -> viewModel.openVerse(viewEvent.verseToOpen)
                }
            }
            listDialog(
                title = preview.title,
                adapter = previewAdapter,
                scrollToPosition = preview.currentPosition,
                onDismiss = { viewModel.markPreviewAsClosed() }
            )
            previewAdapter.submitList(preview.items)
        }

        when (val error = viewState.error) {
            is AnnotatedVerseViewModel.ViewState.Error.AnnotatedVersesLoadingError -> {
                dialog(
                    cancelable = false,
                    title = R.string.dialog_title_error,
                    message = R.string.dialog_message_failed_to_load_annotated_verses,
                    onPositive = { _, _ -> viewModel.loadAnnotatedVerses() },
                    onNegative = { _, _ -> finish() },
                    onDismiss = { viewModel.markErrorAsShown(error) }
                )
            }
            is AnnotatedVerseViewModel.ViewState.Error.PreviewLoadingError -> {
                viewModel.markErrorAsShown(error)

                // Very unlikely to fail, so just falls back to open the verse.
                openVerse(error.verseToPreview)
            }
            is AnnotatedVerseViewModel.ViewState.Error.SortOrderSavingError -> {
                dialog(
                    cancelable = true,
                    title = R.string.dialog_title_error,
                    message = R.string.dialog_message_failed_to_save_sort_order,
                    onPositive = { _, _ -> saveSortOrder(error.sortOrder) },
                    onDismiss = { viewModel.markErrorAsShown(error) }
                )
            }
            is AnnotatedVerseViewModel.ViewState.Error.VerseOpeningError -> {
                dialog(
                    cancelable = true,
                    title = R.string.dialog_title_error,
                    message = R.string.dialog_message_failed_to_select_verse,
                    onPositive = { _, _ -> openVerse(error.verseToOpen) },
                    onDismiss = { viewModel.markErrorAsShown(error) }
                )
            }
            null -> {
                // Do nothing
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(viewBinding.toolbar) {
            setTitle(toolbarText)
            sortOrderUpdated = ::saveSortOrder
        }
    }

    private fun saveSortOrder(@Constants.SortOrder sortOrder: Int) {
        viewModel.saveSortOrder(sortOrder)
    }

    override fun openVerse(verseToOpen: VerseIndex) {
        viewModel.openVerse(verseToOpen)
    }

    protected open fun extrasForOpeningVerse(): Bundle? = null
}
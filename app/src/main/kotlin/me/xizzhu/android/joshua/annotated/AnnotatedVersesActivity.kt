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
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.annotated.bookmarks.BookmarkItem
import me.xizzhu.android.joshua.annotated.highlights.HighlightItem
import me.xizzhu.android.joshua.annotated.notes.NoteItem
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.VerseAnnotation
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.databinding.ActivityAnnotatedBinding
import me.xizzhu.android.joshua.infra.BaseActivityV2
import me.xizzhu.android.joshua.preview.VersePreviewItem
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.listDialog

abstract class AnnotatedVersesActivity<V : VerseAnnotation, VM : AnnotatedVersesViewModel<V>>(
    @StringRes private val toolbarText: Int
) : BaseActivityV2<ActivityAnnotatedBinding, AnnotatedVersesViewModel.ViewAction, AnnotatedVersesViewModel.ViewState, VM>(), BookmarkItem.Callback, HighlightItem.Callback, NoteItem.Callback, VersePreviewItem.Callback {
    override fun inflateViewBinding(): ActivityAnnotatedBinding = ActivityAnnotatedBinding.inflate(layoutInflater)

    override fun onViewActionEmitted(viewAction: AnnotatedVersesViewModel.ViewAction) = when (viewAction) {
        AnnotatedVersesViewModel.ViewAction.OpenReadingScreen -> navigator.navigate(this, Navigator.SCREEN_READING, extrasForOpeningVerse())
    }

    override fun onViewStateUpdated(viewState: AnnotatedVersesViewModel.ViewState) = with(viewBinding) {
        viewState.settings?.let { verseList.setSettings(it) }

        toolbar.setSortOrder(viewState.sortOrder)

        if (viewState.loading) {
            loadingSpinner.fadeIn()
            verseList.isVisible = false
        } else {
            loadingSpinner.isVisible = false
            verseList.fadeIn()
        }

        verseList.setItems(viewState.items)

        viewState.preview?.let { preview ->
            listDialog(
                title = preview.title,
                settings = preview.settings,
                items = preview.items,
                selected = preview.currentPosition,
                onDismiss = { viewModel.markPreviewAsClosed() }
            )
        }

        when (val error = viewState.error) {
            is AnnotatedVersesViewModel.ViewState.Error.AnnotatedVersesLoadingError -> {
                dialog(
                    cancelable = false,
                    title = R.string.dialog_title_error,
                    message = R.string.dialog_message_failed_to_load_annotated_verses,
                    onPositive = { _, _ -> viewModel.loadAnnotatedVerses() },
                    onNegative = { _, _ -> finish() },
                    onDismiss = { viewModel.markErrorAsShown(error) }
                )
            }
            is AnnotatedVersesViewModel.ViewState.Error.PreviewLoadingError -> {
                viewModel.markErrorAsShown(error)

                // Very unlikely to fail, so just falls back to open the verse.
                openVerse(error.verseToPreview)
            }
            is AnnotatedVersesViewModel.ViewState.Error.SortOrderSavingError -> {
                dialog(
                    cancelable = true,
                    title = R.string.dialog_title_error,
                    message = R.string.dialog_message_failed_to_save_sort_order,
                    onPositive = { _, _ -> saveSortOrder(error.sortOrder) },
                    onDismiss = { viewModel.markErrorAsShown(error) }
                )
            }
            is AnnotatedVersesViewModel.ViewState.Error.VerseOpeningError -> {
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

    override fun showPreview(verseIndex: VerseIndex) {
        viewModel.loadPreview(verseIndex)
    }

    protected open fun extrasForOpeningVerse(): Bundle? = null
}

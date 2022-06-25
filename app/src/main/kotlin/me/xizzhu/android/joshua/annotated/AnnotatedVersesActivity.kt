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
import android.view.View
import androidx.annotation.StringRes
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.annotated.bookmarks.BookmarkItem
import me.xizzhu.android.joshua.annotated.highlights.HighlightItem
import me.xizzhu.android.joshua.annotated.notes.NoteItem
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.VerseAnnotation
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.databinding.ActivityAnnotatedBinding
import me.xizzhu.android.joshua.infra.*
import me.xizzhu.android.joshua.preview.VersePreviewItem
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.listDialog
import javax.inject.Inject

abstract class AnnotatedVersesActivity<V : VerseAnnotation, VM : AnnotatedVersesViewModel<V>>(
        @StringRes private val toolbarText: Int
) : BaseActivityV2<ActivityAnnotatedBinding, VM>(), BookmarkItem.Callback, HighlightItem.Callback, NoteItem.Callback, VersePreviewItem.Callback {
    @Inject
    lateinit var annotatedVersesViewModel: VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        annotatedVersesViewModel.viewAction().onEach(::onViewAction).launchIn(lifecycleScope)
        annotatedVersesViewModel.viewState().onEach(::onViewState).launchIn(lifecycleScope)
        initializeToolbar()
    }

    private fun onViewAction(viewAction: AnnotatedVersesViewModel.ViewAction) = when (viewAction) {
        AnnotatedVersesViewModel.ViewAction.OpenReadingScreen -> navigator.navigate(this, Navigator.SCREEN_READING, extrasForOpeningVerse())
        AnnotatedVersesViewModel.ViewAction.ShowLoadAnnotatedVersesFailedError -> {
            dialog(
                    false, R.string.dialog_title_error, R.string.dialog_message_failed_to_load_annotated_verses,
                    { _, _ -> annotatedVersesViewModel.loadAnnotatedVerses() }, { _, _ -> finish() }
            )
        }
        is AnnotatedVersesViewModel.ViewAction.ShowOpenPreviewFailedError -> {
            dialog(true, R.string.dialog_title_error, R.string.dialog_message_failed_to_load_verses, { _, _ -> showPreview(viewAction.verseIndex) })
        }
        is AnnotatedVersesViewModel.ViewAction.ShowOpenVerseFailedError -> {
            dialog(true, R.string.dialog_title_error, R.string.dialog_message_failed_to_select_verse, { _, _ -> openVerse(viewAction.verseToOpen) })
        }
        is AnnotatedVersesViewModel.ViewAction.ShowPreview -> {
            listDialog(viewAction.previewViewData.title, viewAction.previewViewData.settings, viewAction.previewViewData.items, viewAction.previewViewData.currentPosition)
            Unit
        }
        is AnnotatedVersesViewModel.ViewAction.ShowSaveSortOrderFailedError -> {
            dialog(true, R.string.dialog_title_error, R.string.dialog_message_failed_to_save_sort_order, { _, _ -> saveSortOrder(viewAction.sortOrderToSave) })
        }
    }

    private fun onViewState(viewState: AnnotatedVersesViewModel.ViewState) = with(viewBinding) {
        viewState.settings?.let { verseList.setSettings(it) }

        if (viewState.loading) {
            loadingSpinner.fadeIn()
            verseList.visibility = View.GONE
        } else {
            loadingSpinner.visibility = View.GONE
            verseList.fadeIn()
        }

        verseList.setItems(viewState.annotatedVerseItems)
        toolbar.setSortOrder(viewState.sortOrder)
    }

    private fun initializeToolbar() {
        with(viewBinding.toolbar) {
            setTitle(toolbarText)
            sortOrderUpdated = ::saveSortOrder
        }
    }

    private fun saveSortOrder(@Constants.SortOrder sortOrder: Int) {
        annotatedVersesViewModel.saveSortOrder(sortOrder)
    }

    override fun inflateViewBinding(): ActivityAnnotatedBinding = ActivityAnnotatedBinding.inflate(layoutInflater)

    override fun viewModel(): VM = annotatedVersesViewModel

    override fun openVerse(verseToOpen: VerseIndex) {
        annotatedVersesViewModel.openVerse(verseToOpen)
    }

    override fun showPreview(verseIndex: VerseIndex) {
        annotatedVersesViewModel.showPreview(verseIndex)
    }

    protected open fun extrasForOpeningVerse(): Bundle? = null
}

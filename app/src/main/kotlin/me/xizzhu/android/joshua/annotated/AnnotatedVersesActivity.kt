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
import me.xizzhu.android.joshua.infra.BaseActivity
import me.xizzhu.android.joshua.infra.onEach
import me.xizzhu.android.joshua.infra.onFailure
import me.xizzhu.android.joshua.infra.onSuccess
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.joshua.ui.fadeIn
import javax.inject.Inject

abstract class AnnotatedVersesActivity<V : VerseAnnotation, VM : AnnotatedVersesViewModel<V>>(
        @StringRes private val toolbarText: Int
) : BaseActivity<ActivityAnnotatedBinding, VM>(), BookmarkItem.Callback, HighlightItem.Callback, NoteItem.Callback {
    @Inject
    lateinit var annotatedVersesViewModel: VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        observeSettings()
        observeSortOrder()
        observeAnnotatedVerses()
        initializeToolbar()
    }

    private fun observeSettings() {
        annotatedVersesViewModel.settings().onEach { viewBinding.verseList.setSettings(it) }.launchIn(lifecycleScope)
    }

    private fun observeSortOrder() {
        annotatedVersesViewModel.sortOrder().onEach { viewBinding.toolbar.setSortOrder(it) }.launchIn(lifecycleScope)
    }

    private fun observeAnnotatedVerses() {
        annotatedVersesViewModel.annotatedVerses()
                .onEach(
                        onLoading = {
                            with(viewBinding) {
                                loadingSpinner.fadeIn()
                                verseList.visibility = View.GONE
                            }
                        },
                        onSuccess = {
                            with(viewBinding) {
                                verseList.setItems(it.items)
                                verseList.fadeIn()
                                loadingSpinner.visibility = View.GONE
                            }
                        },
                        onFailure = {
                            viewBinding.loadingSpinner.visibility = View.GONE
                            dialog(
                                    false, R.string.dialog_load_annotated_verses_error,
                                    { _, _ -> annotatedVersesViewModel.loadAnnotatedVerses() }, { _, _ -> finish() }
                            )
                        }
                )
                .launchIn(lifecycleScope)
    }

    private fun initializeToolbar() {
        with(viewBinding.toolbar) {
            setTitle(toolbarText)
            sortOrderUpdated = ::saveSortOrder
        }
    }

    private fun saveSortOrder(@Constants.SortOrder sortOrder: Int) {
        annotatedVersesViewModel.saveSortOrder(sortOrder)
                .onFailure { dialog(true, R.string.dialog_save_sort_order_error, { _, _ -> saveSortOrder(sortOrder) }) }
                .launchIn(lifecycleScope)
    }

    override fun inflateViewBinding(): ActivityAnnotatedBinding = ActivityAnnotatedBinding.inflate(layoutInflater)

    override fun viewModel(): VM = annotatedVersesViewModel

    override fun openVerse(verseToOpen: VerseIndex) {
        annotatedVersesViewModel.saveCurrentVerseIndex(verseToOpen)
                .onSuccess { navigator.navigate(this, Navigator.SCREEN_READING, extrasForOpeningVerse()) }
                .onFailure { dialog(true, R.string.dialog_verse_selection_error, { _, _ -> openVerse(verseToOpen) }) }
                .launchIn(lifecycleScope)
    }

    protected open fun extrasForOpeningVerse(): Bundle? = null
}

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

package me.xizzhu.android.joshua.strongnumber

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.databinding.ActivityStrongNumberBinding
import me.xizzhu.android.joshua.infra.*
import me.xizzhu.android.joshua.preview.VersePreviewItem
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.listDialog

@AndroidEntryPoint
class StrongNumberActivity : BaseActivityV2<ActivityStrongNumberBinding, StrongNumberViewModel>(), StrongNumberItem.Callback, VersePreviewItem.Callback {
    companion object {
        private const val KEY_STRONG_NUMBER = "me.xizzhu.android.joshua.KEY_STRONG_NUMBER"

        fun bundle(strongNumber: String): Bundle = Bundle().apply { putString(KEY_STRONG_NUMBER, strongNumber) }
    }

    private val strongNumberViewModel: StrongNumberViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        strongNumberViewModel.viewAction().onEach(::onViewAction).launchIn(lifecycleScope)
        strongNumberViewModel.viewState().onEach(::onViewState).launchIn(lifecycleScope)
        loadStrongNumber()
    }

    private fun onViewAction(viewAction: StrongNumberViewModel.ViewAction) = when (viewAction) {
        StrongNumberViewModel.ViewAction.OpenReadingScreen -> navigator.navigate(this, Navigator.SCREEN_READING)
        StrongNumberViewModel.ViewAction.ShowLoadStrongNumberFailedError -> {
            dialog(false, R.string.dialog_title_error, R.string.dialog_message_failed_to_load_strong_numbers,
                    { _, _ -> loadStrongNumber() }, { _, _ -> finish() })
        }
        is StrongNumberViewModel.ViewAction.ShowOpenPreviewFailedError -> {
            dialog(true, R.string.dialog_title_error, R.string.dialog_message_failed_to_load_verses,
                    { _, _ -> showPreview(viewAction.verseIndex) })
        }
        is StrongNumberViewModel.ViewAction.ShowOpenVerseFailedError -> {
            dialog(true, R.string.dialog_title_error, R.string.dialog_message_failed_to_select_verse, { _, _ -> openVerse(viewAction.verseToOpen) })
        }
        is StrongNumberViewModel.ViewAction.ShowPreview -> {
            listDialog(viewAction.previewViewData.title, viewAction.previewViewData.settings, viewAction.previewViewData.items, viewAction.previewViewData.currentPosition)
            Unit
        }
    }

    private fun onViewState(viewState: StrongNumberViewModel.ViewState) = with(viewBinding) {
        viewState.settings?.let { strongNumberList.setSettings(it) }

        if (viewState.loading) {
            loadingSpinner.fadeIn()
            strongNumberList.visibility = View.GONE
        } else {
            loadingSpinner.visibility = View.GONE
            strongNumberList.fadeIn()
        }

        strongNumberList.setItems(viewState.strongNumberItems)
    }

    private fun loadStrongNumber() {
        strongNumberViewModel.loadStrongNumber(intent.getStringExtra(KEY_STRONG_NUMBER) ?: "")
    }

    override fun inflateViewBinding(): ActivityStrongNumberBinding = ActivityStrongNumberBinding.inflate(layoutInflater)

    override fun viewModel(): StrongNumberViewModel = strongNumberViewModel

    override fun openVerse(verseToOpen: VerseIndex) {
        strongNumberViewModel.openVerse(verseToOpen)
    }

    override fun showPreview(verseIndex: VerseIndex) {
        strongNumberViewModel.showPreview(verseIndex)
    }
}

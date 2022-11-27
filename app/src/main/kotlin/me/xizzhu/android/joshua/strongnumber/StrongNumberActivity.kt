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
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.AndroidEntryPoint
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.databinding.ActivityStrongNumberBinding
import me.xizzhu.android.joshua.infra.BaseActivityV2
import me.xizzhu.android.joshua.preview.VersePreviewItem
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.listDialog

@AndroidEntryPoint
class StrongNumberActivity : BaseActivityV2<ActivityStrongNumberBinding, StrongNumberViewModel.ViewAction, StrongNumberViewModel.ViewState, StrongNumberViewModel>(), StrongNumberItem.Callback, VersePreviewItem.Callback {
    companion object {
        private const val KEY_STRONG_NUMBER = "me.xizzhu.android.joshua.KEY_STRONG_NUMBER"

        fun bundle(strongNumber: String): Bundle = Bundle().apply { putString(KEY_STRONG_NUMBER, strongNumber) }

        fun strongNumber(savedStateHandle: SavedStateHandle): String = savedStateHandle.get<String>(KEY_STRONG_NUMBER).orEmpty()
    }

    override val viewModel: StrongNumberViewModel by viewModels()

    override val viewBinding: ActivityStrongNumberBinding by lazy { ActivityStrongNumberBinding.inflate(layoutInflater) }

    override fun initializeView() {
        // TODO
    }

    override fun onViewActionEmitted(viewAction: StrongNumberViewModel.ViewAction) = when (viewAction) {
        is StrongNumberViewModel.ViewAction.OpenReadingScreen -> navigator.navigate(this, Navigator.SCREEN_READING)
    }

    override fun onViewStateUpdated(viewState: StrongNumberViewModel.ViewState) = with(viewBinding) {
        viewState.settings?.let { strongNumberList.setSettings(it) }

        if (viewState.loading) {
            loadingSpinner.fadeIn()
            strongNumberList.isVisible = false
        } else {
            loadingSpinner.isVisible = false
            strongNumberList.fadeIn()
        }

        strongNumberList.setItems(viewState.items)

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
            is StrongNumberViewModel.ViewState.Error.PreviewLoadingError -> {
                viewModel.markErrorAsShown(error)

                // Very unlikely to fail, so just falls back to open the verse.
                openVerse(error.verseToPreview)
            }
            is StrongNumberViewModel.ViewState.Error.StrongNumberLoadingError -> {
                dialog(
                    cancelable = false,
                    title = R.string.dialog_title_error,
                    message = R.string.dialog_message_failed_to_load_strong_numbers,
                    onPositive = { _, _ -> viewModel.loadStrongNumber() },
                    onNegative = { _, _ -> finish() },
                    onDismiss = { viewModel.markErrorAsShown(error) }
                )
            }
            is StrongNumberViewModel.ViewState.Error.VerseOpeningError -> {
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

    override fun openVerse(verseToOpen: VerseIndex) {
        viewModel.openVerse(verseToOpen)
    }

    override fun showPreview(verseIndex: VerseIndex) {
        viewModel.loadPreview(verseIndex)
    }
}

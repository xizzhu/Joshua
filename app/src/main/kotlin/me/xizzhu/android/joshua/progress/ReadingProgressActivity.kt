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

package me.xizzhu.android.joshua.progress

import androidx.activity.viewModels
import androidx.core.view.isVisible
import dagger.hilt.android.AndroidEntryPoint
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.databinding.ActivityReadingProgressBinding
import me.xizzhu.android.joshua.infra.BaseActivityV2
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.joshua.ui.fadeIn

@AndroidEntryPoint
class ReadingProgressActivity : BaseActivityV2<ActivityReadingProgressBinding, ReadingProgressViewModel.ViewAction, ReadingProgressViewModel.ViewState, ReadingProgressViewModel>(), ReadingProgressDetailItem.Callback {
    override val viewModel: ReadingProgressViewModel by viewModels()

    override val viewBinding: ActivityReadingProgressBinding by lazy { ActivityReadingProgressBinding.inflate(layoutInflater) }

    override fun onViewActionEmitted(viewAction: ReadingProgressViewModel.ViewAction) = when (viewAction) {
        ReadingProgressViewModel.ViewAction.OpenReadingScreen -> navigator.navigate(this, Navigator.SCREEN_READING)
    }

    override fun onViewStateUpdated(viewState: ReadingProgressViewModel.ViewState) = with(viewBinding) {
        viewState.settings?.let { readingProgressList.setSettings(it) }

        if (viewState.loading) {
            loadingSpinner.fadeIn()
            readingProgressList.isVisible = false
        } else {
            loadingSpinner.isVisible = false
            readingProgressList.fadeIn()
        }

        readingProgressList.setItems(viewState.items)

        when (val error = viewState.error) {
            is ReadingProgressViewModel.ViewState.Error.ReadingProgressLoadingError -> {
                dialog(
                    cancelable = false,
                    title = R.string.dialog_title_error,
                    message = R.string.dialog_message_failed_to_load_reading_progress,
                    onPositive = { _, _ -> viewModel.loadReadingProgress() },
                    onNegative = { _, _ -> finish() },
                    onDismiss = { viewModel.markErrorAsShown(error) }
                )
            }
            is ReadingProgressViewModel.ViewState.Error.VerseOpeningError -> {
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

    override fun onStart() {
        super.onStart()
        viewModel.loadReadingProgress()
    }

    override fun openVerse(verseToOpen: VerseIndex) {
        viewModel.openVerse(verseToOpen)
    }
}

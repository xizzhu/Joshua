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
import me.xizzhu.android.joshua.databinding.ActivityReadingProgressBinding
import me.xizzhu.android.joshua.infra.*
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.joshua.ui.fadeIn

@AndroidEntryPoint
class ReadingProgressActivity : BaseActivityV2<ActivityReadingProgressBinding, ReadingProgressViewModel>(), ReadingProgressDetailItem.Callback {
    private val readingProgressViewModel: ReadingProgressViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        readingProgressViewModel.viewAction().onEach(::onViewAction).launchIn(lifecycleScope)
        readingProgressViewModel.viewState().onEach(::onViewState).launchIn(lifecycleScope)
    }

    private fun onViewAction(viewAction: ReadingProgressViewModel.ViewAction) = when (viewAction) {
        ReadingProgressViewModel.ViewAction.OpenReadingScreen -> {
            navigator.navigate(this, Navigator.SCREEN_READING)
        }
        ReadingProgressViewModel.ViewAction.ShowLoadReadingProgressFailedError -> {
            dialog(false, R.string.dialog_title_error, R.string.dialog_message_failed_to_load_reading_progress,
                    { _, _ -> loadReadingProgress() }, { _, _ -> finish() })
        }
        is ReadingProgressViewModel.ViewAction.ShowOpenVerseFailedError -> {
            dialog(true, R.string.dialog_title_error, R.string.dialog_message_failed_to_select_verse, { _, _ -> openVerse(viewAction.verseToOpen) })
        }
    }

    private fun onViewState(viewState: ReadingProgressViewModel.ViewState) = with(viewBinding) {
        viewState.settings?.let { readingProgressList.setSettings(it) }
        if (viewState.loading) {
            loadingSpinner.fadeIn()
            readingProgressList.visibility = View.GONE
        } else {
            readingProgressList.fadeIn()
            loadingSpinner.visibility = View.GONE
        }
        readingProgressList.setItems(viewState.readingProgressItems)
    }

    override fun onStart() {
        super.onStart()
        loadReadingProgress()
    }

    private fun loadReadingProgress() {
        readingProgressViewModel.loadReadingProgress()
    }

    override fun inflateViewBinding(): ActivityReadingProgressBinding = ActivityReadingProgressBinding.inflate(layoutInflater)

    override fun viewModel(): ReadingProgressViewModel = readingProgressViewModel

    override fun openVerse(verseToOpen: VerseIndex) {
        readingProgressViewModel.openVerse(verseToOpen)
    }
}

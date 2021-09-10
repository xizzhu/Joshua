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
import me.xizzhu.android.joshua.infra.BaseActivity
import me.xizzhu.android.joshua.infra.onEach
import me.xizzhu.android.joshua.infra.onFailure
import me.xizzhu.android.joshua.infra.onSuccess
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.joshua.ui.fadeIn

@AndroidEntryPoint
class ReadingProgressActivity : BaseActivity<ActivityReadingProgressBinding>(), ReadingProgressDetailItem.Callback {
    private val readingProgressViewModel: ReadingProgressViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        observeSettings()
        observeReadingProgress()
    }

    private fun observeSettings() {
        readingProgressViewModel.settings().onEach { viewBinding.readingProgressList.setSettings(it) }.launchIn(lifecycleScope)
    }

    private fun observeReadingProgress() {
        readingProgressViewModel.readingProgress()
                .onEach(
                        onLoading = {
                            with(viewBinding) {
                                loadingSpinner.fadeIn()
                                readingProgressList.visibility = View.GONE
                            }
                        },
                        onSuccess = {
                            with(viewBinding) {
                                readingProgressList.setItems(it.items)
                                readingProgressList.fadeIn()
                                loadingSpinner.visibility = View.GONE
                            }
                        },
                        onFailure = {
                            viewBinding.loadingSpinner.visibility = View.GONE
                            dialog(false, R.string.dialog_load_reading_progress_error, { _, _ -> loadReadingProgress() }, { _, _ -> finish() })
                        }
                )
                .launchIn(lifecycleScope)
    }

    override fun onStart() {
        super.onStart()
        loadReadingProgress()
    }

    private fun loadReadingProgress() {
        readingProgressViewModel.loadReadingProgress()
    }

    override fun inflateViewBinding(): ActivityReadingProgressBinding = ActivityReadingProgressBinding.inflate(layoutInflater)

    override fun openVerse(verseToOpen: VerseIndex) {
        readingProgressViewModel.saveCurrentVerseIndex(verseToOpen)
                .onSuccess { navigator.navigate(this, Navigator.SCREEN_READING) }
                .onFailure { dialog(true, R.string.dialog_verse_selection_error, { _, _ -> openVerse(verseToOpen) }) }
                .launchIn(lifecycleScope)
    }
}

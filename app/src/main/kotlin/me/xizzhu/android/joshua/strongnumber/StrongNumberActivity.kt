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
import me.xizzhu.android.joshua.infra.BaseActivity
import me.xizzhu.android.joshua.infra.onEach
import me.xizzhu.android.joshua.infra.onFailure
import me.xizzhu.android.joshua.infra.onSuccess
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.listDialog
import me.xizzhu.android.joshua.ui.recyclerview.VersePreviewItem

@AndroidEntryPoint
class StrongNumberActivity : BaseActivity<ActivityStrongNumberBinding, StrongNumberViewModel>(), StrongNumberItem.Callback, VersePreviewItem.Callback {
    companion object {
        private const val KEY_STRONG_NUMBER = "me.xizzhu.android.joshua.KEY_STRONG_NUMBER"

        fun bundle(strongNumber: String): Bundle = Bundle().apply { putString(KEY_STRONG_NUMBER, strongNumber) }
    }

    private val strongNumberViewModel: StrongNumberViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        observeSettings()
        observeStrongNumber()
        loadStrongNumber()
    }

    private fun observeSettings() {
        strongNumberViewModel.settings().onEach { viewBinding.strongNumberList.setSettings(it) }.launchIn(lifecycleScope)
    }

    private fun observeStrongNumber() {
        strongNumberViewModel.strongNumber()
                .onEach(
                        onLoading = {
                            with(viewBinding) {
                                loadingSpinner.fadeIn()
                                strongNumberList.visibility = View.GONE
                            }
                        },
                        onSuccess = {
                            with(viewBinding) {
                                strongNumberList.setItems(it.items)
                                strongNumberList.fadeIn()
                                loadingSpinner.visibility = View.GONE
                            }
                        },
                        onFailure = {
                            viewBinding.loadingSpinner.visibility = View.GONE
                            dialog(false, R.string.dialog_title_error, R.string.dialog_message_failed_to_load_strong_numbers, { _, _ -> loadStrongNumber() }, { _, _ -> finish() })
                        }
                )
                .launchIn(lifecycleScope)
    }

    private fun loadStrongNumber() {
        strongNumberViewModel.loadStrongNumber(intent.getStringExtra(KEY_STRONG_NUMBER) ?: "")
    }

    override fun inflateViewBinding(): ActivityStrongNumberBinding = ActivityStrongNumberBinding.inflate(layoutInflater)

    override fun viewModel(): StrongNumberViewModel = strongNumberViewModel

    override fun openVerse(verseToOpen: VerseIndex) {
        strongNumberViewModel.saveCurrentVerseIndex(verseToOpen)
                .onSuccess { navigator.navigate(this, Navigator.SCREEN_READING) }
                .onFailure { dialog(true, R.string.dialog_title_error, R.string.dialog_message_failed_to_select_verse, { _, _ -> openVerse(verseToOpen) }) }
                .launchIn(lifecycleScope)
    }

    override fun showPreview(verseIndex: VerseIndex) {
        strongNumberViewModel.loadVersesForPreview(verseIndex)
                .onSuccess { preview -> listDialog(preview.title, preview.settings, preview.items, preview.currentPosition) }
                .onFailure { openVerse(verseIndex) } // Very unlikely to fail, so just falls back to open the verse.
                .launchIn(lifecycleScope)
    }
}

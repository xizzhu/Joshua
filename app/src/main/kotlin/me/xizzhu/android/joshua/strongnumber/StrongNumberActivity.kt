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
import kotlinx.coroutines.asExecutor
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.provider.CoroutineDispatcherProvider
import me.xizzhu.android.joshua.databinding.ActivityStrongNumberBinding
import me.xizzhu.android.joshua.infra.BaseActivityV2
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.listDialog
import javax.inject.Inject
import me.xizzhu.android.joshua.preview.Preview
import me.xizzhu.android.joshua.preview.PreviewAdapter

@AndroidEntryPoint
class StrongNumberActivity : BaseActivityV2<ActivityStrongNumberBinding, StrongNumberViewModel.ViewAction, StrongNumberViewModel.ViewState, StrongNumberViewModel>() {
    companion object {
        private const val KEY_STRONG_NUMBER = "me.xizzhu.android.joshua.KEY_STRONG_NUMBER"

        fun bundle(strongNumber: String): Bundle = Bundle().apply { putString(KEY_STRONG_NUMBER, strongNumber) }

        fun strongNumber(savedStateHandle: SavedStateHandle): String = savedStateHandle.get<String>(KEY_STRONG_NUMBER).orEmpty()
    }

    @Inject
    lateinit var coroutineDispatcherProvider: CoroutineDispatcherProvider

    private lateinit var adapter: StrongNumberAdapter

    override val viewModel: StrongNumberViewModel by viewModels()

    override val viewBinding: ActivityStrongNumberBinding by lazy(LazyThreadSafetyMode.NONE) { ActivityStrongNumberBinding.inflate(layoutInflater) }

    override fun initializeView() {
        adapter = StrongNumberAdapter(
            inflater = layoutInflater,
            executor = coroutineDispatcherProvider.default.asExecutor()
        ) { viewEvent ->
            when (viewEvent) {
                is StrongNumberAdapter.ViewEvent.OpenVerse -> viewModel.openVerse(viewEvent.verseToOpen)
                is StrongNumberAdapter.ViewEvent.ShowPreview -> viewModel.loadPreview(viewEvent.verseToPreview)
            }
        }
        viewBinding.strongNumberList.adapter = adapter
    }

    override fun onViewActionEmitted(viewAction: StrongNumberViewModel.ViewAction) = when (viewAction) {
        is StrongNumberViewModel.ViewAction.OpenReadingScreen -> navigator.navigate(this, Navigator.SCREEN_READING)
    }

    override fun onViewStateUpdated(viewState: StrongNumberViewModel.ViewState): Unit = with(viewBinding) {
        if (viewState.loading) {
            loadingSpinner.fadeIn()
            strongNumberList.isVisible = false
        } else {
            loadingSpinner.isVisible = false
            strongNumberList.fadeIn()
        }

        adapter.submitList(viewState.items)

        viewState.preview?.handle()
        viewState.error?.handle()
    }

    private fun Preview.handle() {
        val previewAdapter = PreviewAdapter(
            inflater = layoutInflater,
            executor = coroutineDispatcherProvider.default.asExecutor()
        ) { viewEvent ->
            when (viewEvent) {
                is PreviewAdapter.ViewEvent.OpenVerse -> viewModel.openVerse(viewEvent.verseToOpen)
            }
        }
        listDialog(
            title = title,
            adapter = previewAdapter,
            scrollToPosition = currentPosition,
            onDismiss = viewModel::markPreviewAsClosed,
        )
        previewAdapter.submitList(items)
    }

    private fun StrongNumberViewModel.ViewState.Error.handle() = when (this) {
        is StrongNumberViewModel.ViewState.Error.PreviewLoadingError -> {
            viewModel.markErrorAsShown(this)

            // Very unlikely to fail, so just falls back to open the verse.
            viewModel.openVerse(verseToPreview)
        }
        is StrongNumberViewModel.ViewState.Error.StrongNumberLoadingError -> {
            dialog(
                cancelable = false,
                title = R.string.dialog_title_error,
                message = R.string.dialog_message_failed_to_load_strong_numbers,
                onPositive = { _, _ -> viewModel.loadStrongNumber() },
                onNegative = { _, _ -> finish() },
                onDismiss = { viewModel.markErrorAsShown(this) }
            )
        }
        is StrongNumberViewModel.ViewState.Error.VerseOpeningError -> {
            dialog(
                cancelable = true,
                title = R.string.dialog_title_error,
                message = R.string.dialog_message_failed_to_select_verse,
                onPositive = { _, _ -> viewModel.openVerse(verseToOpen) },
                onDismiss = { viewModel.markErrorAsShown(this) }
            )
        }
    }
}

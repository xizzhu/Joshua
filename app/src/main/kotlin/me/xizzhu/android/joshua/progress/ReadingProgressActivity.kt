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
import kotlinx.coroutines.asExecutor
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.provider.CoroutineDispatcherProvider
import me.xizzhu.android.joshua.databinding.ActivityReadingProgressBinding
import me.xizzhu.android.joshua.infra.BaseActivityV2
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.joshua.ui.fadeIn
import javax.inject.Inject

@AndroidEntryPoint
class ReadingProgressActivity : BaseActivityV2<ActivityReadingProgressBinding, ReadingProgressViewModel.ViewAction, ReadingProgressViewModel.ViewState, ReadingProgressViewModel>() {
    @Inject
    lateinit var coroutineDispatcherProvider: CoroutineDispatcherProvider

    private lateinit var adapter: ReadingProgressAdapter

    override val viewModel: ReadingProgressViewModel by viewModels()

    override val viewBinding: ActivityReadingProgressBinding by lazy(LazyThreadSafetyMode.NONE) { ActivityReadingProgressBinding.inflate(layoutInflater) }

    override fun initializeView() {
        adapter = ReadingProgressAdapter(
            inflater = layoutInflater,
            executor = coroutineDispatcherProvider.default.asExecutor()
        ) { viewEvent ->
            when (viewEvent) {
                is ReadingProgressAdapter.ViewEvent.ExpandOrCollapseBook -> viewModel.expandOrCollapseBook(viewEvent.bookIndex)
                is ReadingProgressAdapter.ViewEvent.OpenVerse -> viewModel.openVerse(viewEvent.verseToOpen)
            }
        }
        viewBinding.readingProgressList.adapter = adapter
    }

    override fun onViewActionEmitted(viewAction: ReadingProgressViewModel.ViewAction) = when (viewAction) {
        ReadingProgressViewModel.ViewAction.OpenReadingScreen -> navigator.navigate(this, Navigator.SCREEN_READING)
    }

    override fun onViewStateUpdated(viewState: ReadingProgressViewModel.ViewState): Unit = with(viewBinding) {
        if (viewState.loading) {
            loadingSpinner.fadeIn()
            readingProgressList.isVisible = false
        } else {
            loadingSpinner.isVisible = false
            readingProgressList.fadeIn()
        }

        adapter.submitList(viewState.items)

        viewState.error?.handle()
    }

    private fun ReadingProgressViewModel.ViewState.Error.handle() = when (this) {
        is ReadingProgressViewModel.ViewState.Error.ReadingProgressLoadingError -> {
            dialog(
                cancelable = false,
                title = R.string.dialog_title_error,
                message = R.string.dialog_message_failed_to_load_reading_progress,
                onPositive = { _, _ -> viewModel.loadReadingProgress() },
                onNegative = { _, _ -> navigator.goBack(this@ReadingProgressActivity) },
                onDismiss = { viewModel.markErrorAsShown(this) }
            )
        }
        is ReadingProgressViewModel.ViewState.Error.VerseOpeningError -> {
            dialog(
                cancelable = true,
                title = R.string.dialog_title_error,
                message = R.string.dialog_message_failed_to_select_verse,
                onPositive = { _, _ -> viewModel.openVerse(verseToOpen) },
                onDismiss = { viewModel.markErrorAsShown(this) }
            )
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.loadReadingProgress()
    }
}

/*
 * Copyright (C) 2023 Xizhi Zhu
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

package me.xizzhu.android.joshua.reading.toolbar

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.databinding.FragmentReadingToolbarBinding
import me.xizzhu.android.joshua.infra.BaseFragment
import me.xizzhu.android.joshua.ui.toast

@AndroidEntryPoint
class ReadingToolbarFragment : BaseFragment<FragmentReadingToolbarBinding, ReadingToolbarViewModel.ViewAction, ReadingToolbarViewModel.ViewState, ReadingToolbarViewModel>() {
    val toolbar: ReadingToolbar by lazy(LazyThreadSafetyMode.NONE) { viewBinding.toolbar }

    override val viewModel: ReadingToolbarViewModel by viewModels()

    override val viewBinding: FragmentReadingToolbarBinding by lazy(LazyThreadSafetyMode.NONE) { FragmentReadingToolbarBinding.inflate(layoutInflater) }

    override fun initializeView() {
        viewBinding.toolbar.initialize { viewEvent ->
            when (viewEvent) {
                is ReadingToolbar.ViewEvent.OpenBookmarks -> navigator.navigate(requireActivity(), Navigator.SCREEN_BOOKMARKS)
                is ReadingToolbar.ViewEvent.OpenHighlights -> navigator.navigate(requireActivity(), Navigator.SCREEN_HIGHLIGHTS)
                is ReadingToolbar.ViewEvent.OpenNotes -> navigator.navigate(requireActivity(), Navigator.SCREEN_NOTES)
                is ReadingToolbar.ViewEvent.OpenReadingProgress -> navigator.navigate(requireActivity(), Navigator.SCREEN_READING_PROGRESS)
                is ReadingToolbar.ViewEvent.OpenSearch -> navigator.navigate(requireActivity(), Navigator.SCREEN_SEARCH)
                is ReadingToolbar.ViewEvent.OpenSettings -> navigator.navigate(requireActivity(), Navigator.SCREEN_SETTINGS)
                is ReadingToolbar.ViewEvent.OpenTranslations -> navigator.navigate(requireActivity(), Navigator.SCREEN_TRANSLATIONS)
                is ReadingToolbar.ViewEvent.RemoveParallelTranslation -> viewModel.removeParallelTranslation(viewEvent.translationToRemove)
                is ReadingToolbar.ViewEvent.RequestParallelTranslation -> viewModel.requestParallelTranslation(viewEvent.translationToRequest)
                is ReadingToolbar.ViewEvent.SelectCurrentTranslation -> viewModel.selectTranslation(viewEvent.translationToSelect)
                is ReadingToolbar.ViewEvent.TitleClicked -> {
                    // TODO
                }
            }
        }
    }

    override fun onViewActionEmitted(viewAction: ReadingToolbarViewModel.ViewAction) {}

    override fun onViewStateUpdated(viewState: ReadingToolbarViewModel.ViewState): Unit = with(viewBinding) {
        toolbar.title = viewState.title
        toolbar.setTranslationItems(viewState.translationItems)

        viewState.error?.handle()
    }

    private fun ReadingToolbarViewModel.ViewState.Error.handle() = when (this) {
        is ReadingToolbarViewModel.ViewState.Error.ParallelTranslationRemovalError,
        is ReadingToolbarViewModel.ViewState.Error.ParallelTranslationRequestingError,
        is ReadingToolbarViewModel.ViewState.Error.TranslationSelectionError -> {
            toast(R.string.toast_unknown_error)
            viewModel.markErrorAsShown(this)
        }
    }
}

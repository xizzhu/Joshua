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

package me.xizzhu.android.joshua.reading.chapter

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.databinding.FragmentChapterSelectionBinding
import me.xizzhu.android.joshua.infra.BaseFragment
import me.xizzhu.android.joshua.ui.dialog

@AndroidEntryPoint
class ChapterSelectionFragment : BaseFragment<FragmentChapterSelectionBinding, ChapterSelectionViewModel.ViewAction, ChapterSelectionViewModel.ViewState, ChapterSelectionViewModel>() {
    override val viewModel: ChapterSelectionViewModel by viewModels()

    override val viewBinding: FragmentChapterSelectionBinding by lazy(LazyThreadSafetyMode.NONE) { FragmentChapterSelectionBinding.inflate(layoutInflater) }

    override fun initializeView() {
        viewBinding.chapterSelectionView.initialize { viewEvent ->
            when (viewEvent) {
                is ChapterSelectionView.ViewEvent.SelectChapter -> viewModel.selectChapter(bookToSelect = viewEvent.bookIndex, chapterToSelect = viewEvent.chapterIndex)
            }
        }
    }

    override fun onViewActionEmitted(viewAction: ChapterSelectionViewModel.ViewAction) {}

    override fun onViewStateUpdated(viewState: ChapterSelectionViewModel.ViewState) {
        with(viewBinding.chapterSelectionView) {
            setChapterSelectionItems(chapterSelectionItems = viewState.chapterSelectionItems)
            setCurrentChapter(currentBookIndex = viewState.currentBookIndex, currentChapterIndex = viewState.currentChapterIndex)
        }

        expandCurrentBook()

        viewState.error?.handle()
    }

    private fun ChapterSelectionViewModel.ViewState.Error.handle() = when (this) {
        is ChapterSelectionViewModel.ViewState.Error.ChapterSelectionError -> {
            requireActivity().dialog(
                cancelable = true,
                title = R.string.dialog_title_error,
                message = R.string.dialog_message_failed_to_select_chapter,
                onPositive = { _, _ -> viewModel.selectChapter(bookToSelect = bookToSelect, chapterToSelect = chapterToSelect) },
                onDismiss = { viewModel.markErrorAsShown(this) },
            )
        }
    }

    fun expandCurrentBook() {
        viewBinding.chapterSelectionView.expandCurrentBook()
    }
}

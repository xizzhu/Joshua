/*
 * Copyright (C) 2020 Xizhi Zhu
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

import android.content.DialogInterface
import androidx.annotation.UiThread
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.activity.BaseSettingsPresenter
import me.xizzhu.android.joshua.infra.arch.ViewHolder
import me.xizzhu.android.joshua.infra.arch.combineOnSuccess
import me.xizzhu.android.joshua.reading.ReadingActivity
import me.xizzhu.android.joshua.reading.ReadingViewModel
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.logger.Log

data class ChapterListViewHolder(
        val readingDrawerLayout: ReadingDrawerLayout, val chapterListView: ChapterListView
) : ViewHolder

class ChapterListPresenter(
        private val readingActivity: ReadingActivity, readingViewModel: ReadingViewModel,
        lifecycle: Lifecycle, lifecycleCoroutineScope: LifecycleCoroutineScope = lifecycle.coroutineScope
) : BaseSettingsPresenter<ChapterListViewHolder, ReadingViewModel>(readingViewModel, lifecycle, lifecycleCoroutineScope) {
    @UiThread
    override fun onBind() {
        super.onBind()

        viewHolder.chapterListView.setOnChapterSelectedListener { bookIndex, chapterIndex -> selectChapter(bookIndex, chapterIndex) }
    }

    private fun selectChapter(bookIndex: Int, chapterIndex: Int) {
        lifecycleScope.launch {
            try {
                viewModel.saveCurrentVerseIndex(VerseIndex(bookIndex, chapterIndex, 0))
            } catch (e: Exception) {
                Log.e(tag, "Failed to select chapter", e)
                readingActivity.dialog(true, R.string.dialog_chapter_selection_error,
                        DialogInterface.OnClickListener { _, _ -> selectChapter(bookIndex, chapterIndex) })
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun observeBookNames() {
        viewModel.currentVerseIndex()
                .combineOnSuccess(viewModel.bookNames()) { currentVerseIndex, bookNames ->
                    viewHolder.run {
                        chapterListView.setData(currentVerseIndex, bookNames)
                        readingDrawerLayout.hide()
                    }
                }.launchIn(lifecycleScope)
    }
}

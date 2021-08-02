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

package me.xizzhu.android.joshua.reading.chapter

import androidx.annotation.UiThread
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.activity.BaseSettingsPresenter
import me.xizzhu.android.joshua.infra.arch.ViewHolder
import me.xizzhu.android.joshua.reading.ReadingActivity
import me.xizzhu.android.joshua.reading.ReadingViewModel
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.logger.Log

data class ChapterListViewHolder(
        val readingDrawerLayout: ReadingDrawerLayout, val chapterListView: ChapterListView
) : ViewHolder

class ChapterListPresenter(
        readingViewModel: ReadingViewModel, readingActivity: ReadingActivity,
        coroutineScope: CoroutineScope = readingActivity.lifecycleScope
) : BaseSettingsPresenter<ChapterListViewHolder, ReadingViewModel, ReadingActivity>(readingViewModel, readingActivity, coroutineScope) {
    @UiThread
    override fun onBind() {
        super.onBind()

        viewHolder.chapterListView.initialize(::selectChapter)
    }

    private fun selectChapter(bookIndex: Int, chapterIndex: Int) {
        coroutineScope.launch {
            try {
                viewModel.saveCurrentVerseIndex(VerseIndex(bookIndex, chapterIndex, 0))
            } catch (e: Exception) {
                Log.e(tag, "Failed to select chapter", e)
                activity.dialog(true, R.string.dialog_chapter_selection_error,
                        { _, _ -> selectChapter(bookIndex, chapterIndex) })
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun observeChapterList() {
        viewModel.chapterList()
                .onEach { chapterList ->
                    viewHolder.chapterListView.setData(chapterList.currentVerseIndex, chapterList.bookNames)
                    viewHolder.readingDrawerLayout.hide()
                }.catch { e ->
                    Log.e(tag, "Error when observing chapter list view data", e)
                }.launchIn(coroutineScope)
    }
}

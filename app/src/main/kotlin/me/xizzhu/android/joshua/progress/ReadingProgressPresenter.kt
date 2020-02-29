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

package me.xizzhu.android.joshua.progress

import android.content.DialogInterface
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.activity.BaseSettingsPresenter
import me.xizzhu.android.joshua.infra.arch.*
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.CommonRecyclerView
import me.xizzhu.android.joshua.ui.toast
import me.xizzhu.android.logger.Log

data class ReadingProgressViewHolder(
        val loadingSpinner: ProgressBar, val readingProgressListView: CommonRecyclerView
) : ViewHolder

class ReadingProgressPresenter(
        private val navigator: Navigator, readingProgressViewModel: ReadingProgressViewModel,
        readingProgressActivity: ReadingProgressActivity,
        coroutineScope: CoroutineScope = readingProgressActivity.lifecycleScope
) : BaseSettingsPresenter<ReadingProgressViewHolder, ReadingProgressViewModel, ReadingProgressActivity>(
        readingProgressViewModel, readingProgressActivity, coroutineScope
) {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val expanded: Array<Boolean> = Array(Bible.BOOK_COUNT) { it == 0 }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun observeSettings() {
        viewModel.settings().onEach { viewHolder.readingProgressListView.setSettings(it) }.launchIn(coroutineScope)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun loadReadingProgress() {
        viewModel.readingProgress()
                .onStart {
                    viewHolder.loadingSpinner.fadeIn()
                    viewHolder.readingProgressListView.visibility = View.GONE
                }.onEach { readingProgress ->
                    viewHolder.readingProgressListView.setItems(readingProgress.toItems())
                    viewHolder.readingProgressListView.fadeIn()
                    viewHolder.loadingSpinner.visibility = View.GONE
                }.catch { e ->
                    Log.e(tag, "Failed to load reading progress", e)
                    viewHolder.loadingSpinner.visibility = View.GONE
                    activity.dialog(false, R.string.dialog_load_reading_progress_error,
                            DialogInterface.OnClickListener { _, _ -> loadReadingProgress() },
                            DialogInterface.OnClickListener { _, _ -> activity.finish() })
                }.launchIn(coroutineScope)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun ReadingProgressViewData.toItems(): List<BaseItem> {
        var totalChaptersRead = 0
        val chaptersReadPerBook = Array(Bible.BOOK_COUNT) { i ->
            val chapterCount = Bible.getChapterCount(i)
            ArrayList<Boolean>(chapterCount).apply { repeat(chapterCount) { add(false) } }
        }
        val chaptersReadCountPerBook = Array(Bible.BOOK_COUNT) { 0 }
        for (chapter in readingProgress.chapterReadingStatus) {
            if (chapter.readCount > 0) {
                chaptersReadPerBook[chapter.bookIndex][chapter.chapterIndex] = true
                chaptersReadCountPerBook[chapter.bookIndex]++
                ++totalChaptersRead
            }
        }

        var finishedBooks = 0
        var finishedOldTestament = 0
        var finishedNewTestament = 0
        val detailItems = ArrayList<ReadingProgressDetailItem>(Bible.BOOK_COUNT)
        for ((bookIndex, chaptersRead) in chaptersReadPerBook.withIndex()) {
            val chaptersReadCount = chaptersReadCountPerBook[bookIndex]
            if (chaptersReadCount == Bible.getChapterCount(bookIndex)) {
                ++finishedBooks
                if (bookIndex < Bible.OLD_TESTAMENT_COUNT) {
                    ++finishedOldTestament
                } else {
                    ++finishedNewTestament
                }
            }
            detailItems.add(ReadingProgressDetailItem(bookNames[bookIndex], bookIndex, chaptersRead,
                    chaptersReadCount, this@ReadingProgressPresenter::onBookClicked,
                    this@ReadingProgressPresenter::openChapter, expanded[bookIndex]))
        }
        return mutableListOf<BaseItem>(ReadingProgressSummaryItem(readingProgress.continuousReadingDays,
                totalChaptersRead, finishedBooks, finishedOldTestament, finishedNewTestament))
                .apply { addAll(detailItems) }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun openChapter(bookIndex: Int, chapterIndex: Int) {
        coroutineScope.launch {
            try {
                viewModel.saveCurrentVerseIndex(VerseIndex(bookIndex, chapterIndex, 0))
                navigator.navigate(activity, Navigator.SCREEN_READING)
            } catch (e: Exception) {
                Log.e(tag, "Failed to open chapter for reading", e)
                activity.toast(R.string.toast_unknown_error)
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun onBookClicked(bookIndex: Int, expanded: Boolean) {
        this.expanded[bookIndex] = expanded
    }
}

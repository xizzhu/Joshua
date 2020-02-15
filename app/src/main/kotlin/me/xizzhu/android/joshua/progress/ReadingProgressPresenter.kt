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
import androidx.annotation.UiThread
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.ReadingProgress
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.activity.BaseSettingsPresenter
import me.xizzhu.android.joshua.infra.arch.*
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.CommonRecyclerView
import me.xizzhu.android.joshua.ui.toast
import me.xizzhu.android.logger.Log

data class ReadingProgressViewHolder(val loadingSpinner: ProgressBar,
                                     val readingProgressListView: CommonRecyclerView) : ViewHolder

class ReadingProgressPresenter(private val readingProgressActivity: ReadingProgressActivity,
                               private val navigator: Navigator,
                               readingProgressViewModel: ReadingProgressViewModel,
                               lifecycleCoroutineScope: LifecycleCoroutineScope)
    : BaseSettingsPresenter<ReadingProgressViewHolder, ReadingProgressViewModel>(readingProgressViewModel, lifecycleCoroutineScope) {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val expanded: Array<Boolean> = Array(Bible.BOOK_COUNT) { it == 0 }

    @UiThread
    override fun onBind(viewHolder: ReadingProgressViewHolder) {
        super.onBind(viewHolder)

        viewModel.settings().onEachSuccess { viewHolder.readingProgressListView.setSettings(it) }.launchIn(lifecycleScope)
        loadReadingProgress()
    }

    private fun loadReadingProgress() {
        lifecycleScope.launchWhenStarted {
            try {
                with(viewHolder) {
                    loadingSpinner.fadeIn()

                    readingProgressListView.visibility = View.GONE
                    readingProgressListView.setItems(viewModel.readingProgress().toItems(viewModel.bookNames()))
                    readingProgressListView.fadeIn()
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to load reading progress", e)
                readingProgressActivity.dialog(false, R.string.dialog_load_reading_progress_error,
                        DialogInterface.OnClickListener { _, _ -> loadReadingProgress() },
                        DialogInterface.OnClickListener { _, _ -> readingProgressActivity.finish() })
            } finally {
                viewHolder.loadingSpinner.visibility = View.GONE
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun ReadingProgress.toItems(bookNames: List<String>): List<BaseItem> {
        var totalChaptersRead = 0
        val chaptersReadPerBook = Array(Bible.BOOK_COUNT) { i ->
            val chapterCount = Bible.getChapterCount(i)
            ArrayList<Boolean>(chapterCount).apply { repeat(chapterCount) { add(false) } }
        }
        val chaptersReadCountPerBook = Array(Bible.BOOK_COUNT) { 0 }
        for (chapter in chapterReadingStatus) {
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
        return mutableListOf<BaseItem>(ReadingProgressSummaryItem(continuousReadingDays, totalChaptersRead, finishedBooks,
                finishedOldTestament, finishedNewTestament)).apply { addAll(detailItems) }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun openChapter(bookIndex: Int, chapterIndex: Int) {
        lifecycleScope.launch {
            try {
                viewModel.saveCurrentVerseIndex(VerseIndex(bookIndex, chapterIndex, 0))
                navigator.navigate(readingProgressActivity, Navigator.SCREEN_READING)
            } catch (e: Exception) {
                Log.e(tag, "Failed to open chapter for reading", e)
                readingProgressActivity.toast(R.string.toast_unknown_error)
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun onBookClicked(bookIndex: Int, expanded: Boolean) {
        this.expanded[bookIndex] = expanded
    }
}

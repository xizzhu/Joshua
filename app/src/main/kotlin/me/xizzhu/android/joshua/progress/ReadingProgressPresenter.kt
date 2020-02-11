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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.arch.*
import me.xizzhu.android.joshua.infra.interactors.BaseSettingsAwarePresenter
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.joshua.ui.recyclerview.CommonRecyclerView
import me.xizzhu.android.joshua.ui.toast
import me.xizzhu.android.logger.Log

data class ReadingProgressViewHolder(val loadingSpinner: ProgressBar,
                                     val readingProgressListView: CommonRecyclerView) : ViewHolder

class ReadingProgressPresenter(private val readingProgressActivity: ReadingProgressActivity,
                               private val navigator: Navigator,
                               readingProgressInteractor: ReadingProgressInteractor,
                               dispatcher: CoroutineDispatcher = Dispatchers.Main)
    : BaseSettingsAwarePresenter<ReadingProgressViewHolder, ReadingProgressInteractor>(readingProgressInteractor, dispatcher) {
    private val expanded: Array<Boolean> = Array(Bible.BOOK_COUNT) { it == 0 }

    @UiThread
    override fun onCreate(viewHolder: ReadingProgressViewHolder) {
        super.onCreate(viewHolder)
        interactor.settings().onEachSuccess { viewHolder.readingProgressListView.setSettings(it) }.launchIn(coroutineScope)
    }

    @UiThread
    override fun onStart() {
        super.onStart()
        loadReadingProgress()
    }

    private fun loadReadingProgress() {
        coroutineScope.launch {
            try {
                viewHolder?.loadingSpinner?.fadeIn()

                viewHolder?.readingProgressListView?.run {
                    visibility = View.GONE

                    val bookNames = interactor.bookNames()
                    val readingProgress = interactor.readingProgress()
                    val items = readingProgress.toReadingProgressItems(bookNames, expanded,
                            this@ReadingProgressPresenter::onBookClicked,
                            this@ReadingProgressPresenter::openChapter)
                    setItems(items)

                    fadeIn()
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to load reading progress", e)
                readingProgressActivity.dialog(false, R.string.dialog_load_reading_progress_error,
                        DialogInterface.OnClickListener { _, _ -> loadReadingProgress() },
                        DialogInterface.OnClickListener { _, _ -> readingProgressActivity.finish() })
            } finally {
                viewHolder?.loadingSpinner?.visibility = View.GONE
            }
        }
    }

    @VisibleForTesting
    fun openChapter(bookIndex: Int, chapterIndex: Int) {
        coroutineScope.launch {
            try {
                interactor.saveCurrentVerseIndex(VerseIndex(bookIndex, chapterIndex, 0))
                navigator.navigate(readingProgressActivity, Navigator.SCREEN_READING)
            } catch (e: Exception) {
                Log.e(tag, "Failed to open chapter for reading", e)
                readingProgressActivity.toast(R.string.toast_unknown_error)
            }
        }
    }

    private fun onBookClicked(bookIndex: Int, expanded: Boolean) {
        this.expanded[bookIndex] = expanded
    }
}

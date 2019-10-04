/*
 * Copyright (C) 2019 Xizhi Zhu
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
import androidx.annotation.UiThread
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.activity.BaseSettingsAwarePresenter
import me.xizzhu.android.joshua.infra.arch.ViewData
import me.xizzhu.android.joshua.infra.arch.ViewHolder
import me.xizzhu.android.joshua.ui.DialogHelper
import me.xizzhu.android.joshua.ui.fadeIn
import me.xizzhu.android.logger.Log

data class ReadingProgressViewHolder(val readingProgressListView: ReadingProgressListView) : ViewHolder

class ReadingProgressPresenter(private val readingProgressActivity: ReadingProgressActivity,
                               private val navigator: Navigator,
                               readingProgressInteractor: ReadingProgressInteractor,
                               dispatcher: CoroutineDispatcher = Dispatchers.Main)
    : BaseSettingsAwarePresenter<ReadingProgressViewHolder, ReadingProgressInteractor>(readingProgressInteractor, dispatcher) {
    private val expanded: Array<Boolean> = Array(Bible.BOOK_COUNT) { it == 0 }

    @UiThread
    override fun onStart() {
        super.onStart()

        observeSettings()
        loadReadingProgress()
    }

    private fun observeSettings() {
        coroutineScope.launch {
            interactor.settings().collect {
                if (it.status == ViewData.STATUS_SUCCESS) {
                    viewHolder?.readingProgressListView?.onSettingsUpdated(it.data)
                }
            }
        }
    }

    private fun loadReadingProgress() {
        coroutineScope.launch {
            try {
                viewHolder?.readingProgressListView?.let { readingProgressListView ->
                    readingProgressListView.visibility = View.GONE
                    val (bookNames, readingProgress) = interactor.readReadingProgress()
                    readingProgressListView.setReadingProgressItems(readingProgress.toReadingProgressItems(bookNames, expanded,
                            this@ReadingProgressPresenter::onBookClicked,
                            this@ReadingProgressPresenter::openChapter))
                    readingProgressListView.fadeIn()
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to load reading progress")

                DialogHelper.showDialog(readingProgressActivity, true, R.string.dialog_load_reading_progress_error,
                        DialogInterface.OnClickListener { _, _ -> loadReadingProgress() })
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
                // TODO
            }
        }
    }

    private fun onBookClicked(bookIndex: Int, expanded: Boolean) {
        this.expanded[bookIndex] = expanded
    }
}

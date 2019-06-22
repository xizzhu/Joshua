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

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.ui.recyclerview.toReadingProgressItems
import me.xizzhu.android.joshua.utils.BaseSettingsPresenter
import me.xizzhu.android.logger.Log

class ReadingProgressPresenter(private val readingProgressInteractor: ReadingProgressInteractor)
    : BaseSettingsPresenter<ReadingProgressView>(readingProgressInteractor) {
    private val expanded: Array<Boolean> = Array(Bible.BOOK_COUNT) { it == 0 }

    override fun onViewAttached() {
        super.onViewAttached()
        loadReadingProgress()
    }

    fun loadReadingProgress() {
        coroutineScope.launch(Dispatchers.Main) {
            try {
                val currentTranslation = readingProgressInteractor.readCurrentTranslation()
                val bookNames = readingProgressInteractor.readBookNames(currentTranslation)
                val readingProgress = readingProgressInteractor.readReadingProgress()
                        .toReadingProgressItems(bookNames, expanded,
                                this@ReadingProgressPresenter::onBookClicked,
                                this@ReadingProgressPresenter::openChapter)
                view?.onReadingProgressLoaded(readingProgress)

                readingProgressInteractor.notifyLoadingFinished()
            } catch (e: Exception) {
                Log.e(tag, "Failed to load reading progress")
                view?.onReadingProgressLoadFailed()
            }
        }
    }

    @VisibleForTesting
    fun openChapter(bookIndex: Int, chapterIndex: Int) {
        coroutineScope.launch(Dispatchers.Main) {
            try {
                readingProgressInteractor.openChapter(VerseIndex(bookIndex, chapterIndex, 0))
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

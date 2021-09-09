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

package me.xizzhu.android.joshua.progress

import android.app.Application
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.infra.BaseViewModel
import me.xizzhu.android.joshua.infra.onFailure
import me.xizzhu.android.joshua.infra.viewData
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.utils.firstNotEmpty
import me.xizzhu.android.logger.Log
import javax.inject.Inject

class ReadingProgressViewData(val items: List<BaseItem>)

@HiltViewModel
class ReadingProgressViewModel @Inject constructor(
        private val bibleReadingManager: BibleReadingManager,
        private val readingProgressManager: ReadingProgressManager,
        settingsManager: SettingsManager,
        application: Application
) : BaseViewModel(settingsManager, application) {
    private val readingProgress: MutableStateFlow<ViewData<ReadingProgressViewData>?> = MutableStateFlow(null)
    private val expanded: Array<Boolean> = Array(Bible.BOOK_COUNT) { it == 0 }

    fun readingProgress(): Flow<ViewData<ReadingProgressViewData>> = readingProgress.filterNotNull()

    fun loadReadingProgress() {
        viewModelScope.launch {
            try {
                readingProgress.value = ViewData.Loading()
                readingProgress.value = ViewData.Success(ReadingProgressViewData(
                        items = buildReadingProgressItems(
                                readingProgress = readingProgressManager.read(),
                                bookNames = bibleReadingManager.readBookNames(bibleReadingManager.currentTranslation().firstNotEmpty())
                        )
                ))
            } catch (e: Exception) {
                Log.e(tag, "Error occurred while loading reading progress", e)
                readingProgress.value = ViewData.Failure(e)
            }
        }
    }

    private fun buildReadingProgressItems(readingProgress: ReadingProgress, bookNames: List<String>): List<BaseItem> {
        val items = ArrayList<BaseItem>(1 + Bible.BOOK_COUNT)
        items.add(ReadingProgressSummaryItem(0, 0, 0, 0, 0))

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
            items.add(ReadingProgressDetailItem(
                    bookNames[bookIndex], bookIndex, chaptersRead, chaptersReadCount, ::onBookClicked, expanded[bookIndex]
            ))
        }

        items[0] = ReadingProgressSummaryItem(
                readingProgress.continuousReadingDays, totalChaptersRead, finishedBooks, finishedOldTestament, finishedNewTestament
        )

        return items
    }

    private fun onBookClicked(bookIndex: Int, expanded: Boolean) {
        this.expanded[bookIndex] = expanded
    }

    fun saveCurrentVerseIndex(verseToOpen: VerseIndex): Flow<ViewData<Unit>> = viewData {
        bibleReadingManager.saveCurrentVerseIndex(verseToOpen)
    }.onFailure { Log.e(tag, "Failed to select verse and open reading activity", it) }
}

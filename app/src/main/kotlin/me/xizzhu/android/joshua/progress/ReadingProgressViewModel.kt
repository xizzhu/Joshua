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

package me.xizzhu.android.joshua.progress

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.ReadingProgress
import me.xizzhu.android.joshua.core.ReadingProgressManager
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.provider.CoroutineDispatcherProvider
import me.xizzhu.android.joshua.infra.BaseViewModelV2
import me.xizzhu.android.joshua.utils.firstNotEmpty
import me.xizzhu.android.logger.Log
import javax.inject.Inject

@HiltViewModel
class ReadingProgressViewModel @Inject constructor(
    private val bibleReadingManager: BibleReadingManager,
    private val readingProgressManager: ReadingProgressManager,
    private val settingsManager: SettingsManager,
    private val coroutineDispatcherProvider: CoroutineDispatcherProvider
) : BaseViewModelV2<ReadingProgressViewModel.ViewAction, ReadingProgressViewModel.ViewState>(
    initialViewState = ViewState(
        loading = false,
        items = emptyList(),
        error = null,
    )
) {
    sealed class ViewAction {
        object OpenReadingScreen : ViewAction()
    }

    data class ViewState(
        val loading: Boolean,
        val items: List<ReadingProgressItem>,
        val error: Error?,
    ) {
        sealed class Error {
            object ReadingProgressLoadingError : Error()
            data class VerseOpeningError(val verseToOpen: VerseIndex) : Error()
        }
    }

    private val expanded: Array<Boolean> = Array(Bible.BOOK_COUNT) { it == 0 }

    fun loadReadingProgress() {
        viewModelScope.launch(coroutineDispatcherProvider.default) {
            runCatching {
                updateViewState { it.copy(loading = true, items = emptyList()) }

                val items = buildReadingProgressItems(
                    settings = settingsManager.settings().first(),
                    readingProgress = readingProgressManager.read(),
                    bookNames = bibleReadingManager.readBookNames(bibleReadingManager.currentTranslation().firstNotEmpty()),
                )
                updateViewState { it.copy(loading = false, items = items) }
            }.onFailure { e ->
                Log.e(tag, "Error occurred while loading reading progress", e)
                updateViewState { it.copy(loading = false, error = ViewState.Error.ReadingProgressLoadingError) }
            }
        }
    }

    private fun buildReadingProgressItems(settings: Settings, readingProgress: ReadingProgress, bookNames: List<String>): List<ReadingProgressItem> {
        val items = ArrayList<ReadingProgressItem>(1 + Bible.BOOK_COUNT)
        items.add(ReadingProgressItem.Summary(settings, 0, 0, 0, 0, 0))

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
            items.add(ReadingProgressItem.Book(
                settings = settings,
                bookName = bookNames[bookIndex],
                bookIndex = bookIndex,
                chaptersRead = chaptersRead,
                chaptersReadCount = chaptersReadCount,
                expanded = expanded[bookIndex]
            ))
        }

        items[0] = ReadingProgressItem.Summary(
            settings, readingProgress.continuousReadingDays, totalChaptersRead, finishedBooks, finishedOldTestament, finishedNewTestament
        )

        return items
    }

    fun expandOrCollapseBook(bookIndex: Int) {
        updateViewState { current ->
            (current.items.getOrNull(bookIndex + 1) as? ReadingProgressItem.Book)?.let { bookItem ->
                val expanded = bookItem.expanded.not()
                this.expanded[bookIndex] = expanded
                val newBookItem = bookItem.copy(expanded = expanded)
                current.copy(items = ArrayList(current.items).apply { set(bookIndex + 1, newBookItem) })
            }
        }
    }

    fun openVerse(verseToOpen: VerseIndex) {
        viewModelScope.launch(coroutineDispatcherProvider.default) {
            runCatching {
                bibleReadingManager.saveCurrentVerseIndex(verseToOpen)
                emitViewAction(ViewAction.OpenReadingScreen)
            }.onFailure { e ->
                Log.e(tag, "Failed to save current verse", e)
                updateViewState { it.copy(error = ViewState.Error.VerseOpeningError(verseToOpen)) }
            }
        }
    }

    fun markErrorAsShown(error: ViewState.Error) {
        updateViewState { current -> if (current.error == error) current.copy(error = null) else null }
    }
}

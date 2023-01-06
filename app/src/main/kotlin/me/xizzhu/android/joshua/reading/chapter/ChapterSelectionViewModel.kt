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

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.Bible
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.BaseViewModelV2
import me.xizzhu.android.logger.Log

@HiltViewModel
class ChapterSelectionViewModel @Inject constructor(
    private val bibleReadingManager: BibleReadingManager,
) : BaseViewModelV2<ChapterSelectionViewModel.ViewAction, ChapterSelectionViewModel.ViewState>(
    initialViewState = ViewState(
        currentBookIndex = -1,
        currentChapterIndex = -1,
        chapterSelectionItems = emptyList(),
        error = null,
    )
) {
    sealed class ViewAction

    data class ViewState(
        val currentBookIndex: Int,
        val currentChapterIndex: Int,
        val chapterSelectionItems: List<ChapterSelectionItem>,
        val error: Error?,
    ) {
        sealed class Error {
            data class ChapterSelectionError(val bookToSelect: Int, val chapterToSelect: Int) : Error()
        }
    }

    init {
        bibleReadingManager.currentTranslation().onEach { currentTranslation ->
            val chapterSelectionItems = bibleReadingManager.readBookNames(currentTranslation).mapIndexed { index, bookName ->
                ChapterSelectionItem(bookIndex = index, bookName = bookName, chapterCount = Bible.getChapterCount(index))
            }
            updateViewState { it.copy(chapterSelectionItems = chapterSelectionItems) }
        }.launchIn(viewModelScope)

        bibleReadingManager.currentVerseIndex().onEach { currentVerseIndex ->
            updateViewState { it.copy(currentBookIndex = currentVerseIndex.bookIndex, currentChapterIndex = currentVerseIndex.chapterIndex) }
        }.launchIn(viewModelScope)
    }

    fun selectChapter(bookToSelect: Int, chapterToSelect: Int) {
        viewModelScope.launch {
            runCatching {
                bibleReadingManager.saveCurrentVerseIndex(VerseIndex(bookIndex = bookToSelect, chapterIndex = chapterToSelect, verseIndex = 0))
            }.onFailure { e ->
                Log.e(tag, "Failed to select chapter [bookToSelect=$bookToSelect, chapterToSelect=$chapterToSelect]", e)
                updateViewState { it.copy(error = ViewState.Error.ChapterSelectionError(bookToSelect = bookToSelect, chapterToSelect = chapterToSelect)) }
            }
        }
    }

    fun markErrorAsShown(error: ViewState.Error) {
        updateViewState { current -> if (current.error == error) current.copy(error = null) else null }
    }
}

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

package me.xizzhu.android.joshua.reading.chapter

import android.content.DialogInterface
import androidx.annotation.UiThread
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.arch.ViewHolder
import me.xizzhu.android.joshua.infra.arch.ViewPresenter
import me.xizzhu.android.joshua.infra.arch.combineOnSuccess
import me.xizzhu.android.joshua.reading.ReadingActivity
import me.xizzhu.android.joshua.ui.DialogHelper
import me.xizzhu.android.logger.Log

data class ChapterListViewHolder(val readingDrawerLayout: ReadingDrawerLayout, val chapterListView: ChapterListView) : ViewHolder

class ChapterListPresenter(private val readingActivity: ReadingActivity,
                           chapterListInteractor: ChapterListInteractor,
                           dispatcher: CoroutineDispatcher = Dispatchers.Main)
    : ViewPresenter<ChapterListViewHolder, ChapterListInteractor>(chapterListInteractor, dispatcher) {
    @UiThread
    override fun onCreate(viewHolder: ChapterListViewHolder) {
        super.onCreate(viewHolder)

        viewHolder.chapterListView.setOnChapterSelectedListener { bookIndex, chapterIndex -> selectChapter(bookIndex, chapterIndex) }

        interactor.currentVerseIndex()
                .combineOnSuccess(interactor.bookNames()) { currentVerseIndex, bookNames ->
                    viewHolder.run {
                        chapterListView.setData(currentVerseIndex, bookNames)
                        readingDrawerLayout.hide()
                    }
                }.launchIn(coroutineScope)
    }

    private fun selectChapter(bookIndex: Int, chapterIndex: Int) {
        coroutineScope.launch {
            try {
                interactor.saveCurrentVerseIndex(VerseIndex(bookIndex, chapterIndex, 0))
            } catch (e: Exception) {
                Log.e(tag, "Failed to select chapter", e)
                DialogHelper.showDialog(readingActivity, true, R.string.dialog_chapter_selection_error,
                        DialogInterface.OnClickListener { _, _ -> selectChapter(bookIndex, chapterIndex) })
            }
        }
    }
}

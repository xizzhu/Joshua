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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.filter
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.logger.Log
import me.xizzhu.android.joshua.reading.ReadingInteractor
import me.xizzhu.android.joshua.utils.MVPPresenter

class ChapterListPresenter(private val readingInteractor: ReadingInteractor) : MVPPresenter<ChapterView>() {
    override fun onViewAttached() {
        super.onViewAttached()

        launch(Dispatchers.Main) {
            val currentTranslation = readingInteractor.observeCurrentTranslation()
            receiveChannels.add(currentTranslation)
            currentTranslation.filter { it.isNotEmpty() }
                    .consumeEach { view?.onBookNamesUpdated(readingInteractor.readBookNames(it)) }
        }
        launch(Dispatchers.Main) {
            val currentVerseIndex = readingInteractor.observeCurrentVerseIndex()
            receiveChannels.add(currentVerseIndex)
            currentVerseIndex.filter { it.isValid() }
                    .consumeEach { view?.onCurrentVerseIndexUpdated(it) }
        }
    }

    fun selectChapter(bookIndex: Int, chapterIndex: Int) {
        launch(Dispatchers.Main) {
            try {
                readingInteractor.saveCurrentVerseIndex(VerseIndex(bookIndex, chapterIndex, 0))
            } catch (e: Exception) {
                Log.e(tag, e, "Failed to select chapter")
                view?.onChapterSelectionFailed(bookIndex, chapterIndex)
            }
        }
    }
}

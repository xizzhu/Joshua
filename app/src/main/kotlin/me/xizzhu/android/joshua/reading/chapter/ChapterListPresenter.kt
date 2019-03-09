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
import kotlinx.coroutines.channels.filter
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.reading.ReadingInteractor
import me.xizzhu.android.joshua.utils.MVPPresenter
import me.xizzhu.android.joshua.utils.onEach

class ChapterListPresenter(private val readingInteractor: ReadingInteractor) : MVPPresenter<ChapterView>() {
    override fun onViewAttached() {
        super.onViewAttached()

        launch(Dispatchers.Main) {
            receiveChannels.add(readingInteractor.observeCurrentTranslation()
                    .filter { it.isNotEmpty() }
                    .onEach {
                        view?.onBookNamesUpdated(readingInteractor.readBookNames(it))
                    })
        }
        launch(Dispatchers.Main) {
            receiveChannels.add(readingInteractor.observeCurrentVerseIndex()
                    .filter { it.isValid() }
                    .onEach {
                        view?.onCurrentVerseIndexUpdated(it)
                    })
        }
    }

    fun selectChapter(bookIndex: Int, chapterIndex: Int) {
        launch(Dispatchers.Main) {
            try {
                readingInteractor.saveCurrentVerseIndex(VerseIndex(bookIndex, chapterIndex, 0))
            } catch (e: Exception) {
                view?.onChapterSelectionFailed(bookIndex, chapterIndex)
            }
        }
    }
}

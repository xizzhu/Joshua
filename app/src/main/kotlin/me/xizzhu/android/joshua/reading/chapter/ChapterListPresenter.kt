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
import kotlinx.coroutines.withContext
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.reading.ReadingManager
import me.xizzhu.android.joshua.utils.MVPPresenter

class ChapterListPresenter(private val readingManager: ReadingManager,
                           private val listener: Listener) : MVPPresenter<ChapterView>() {
    interface Listener {
        fun onChapterSelected()
    }

    override fun onViewAttached() {
        super.onViewAttached()

        launch(Dispatchers.Main) {
            val currentTranslation = readingManager.observeCurrentTranslation()
            receiveChannels.add(currentTranslation)
            currentTranslation
                    .filter { it.isNotEmpty() }
                    .consumeEach {
                        view?.onBookNamesUpdated(withContext(Dispatchers.IO) { readingManager.readBookNames(it) })
                    }
        }
        launch(Dispatchers.Main) {
            val currentVerse = readingManager.observeCurrentVerseIndex()
            receiveChannels.add(currentVerse)
            currentVerse.filter { it.isValid() }
                    .consumeEach {
                        view?.onCurrentVerseIndexUpdated(it)
                    }
        }
    }

    fun updateCurrentVerseIndex(verseIndex: VerseIndex) {
        launch(Dispatchers.IO) {
            readingManager.saveCurrentVerseIndex(verseIndex)
        }
        listener.onChapterSelected()
    }
}

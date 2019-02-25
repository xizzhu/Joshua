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

package me.xizzhu.android.joshua.reading.verse

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.reading.ReadingViewController
import me.xizzhu.android.joshua.utils.MVPPresenter
import me.xizzhu.android.joshua.utils.onEach
import java.lang.Exception

class VersePresenter(private val readingViewController: ReadingViewController) : MVPPresenter<VerseView>() {
    override fun onViewAttached() {
        super.onViewAttached()

        launch(Dispatchers.Main) {
            receiveChannels.add(readingViewController.observeCurrentTranslation()
                    .filter { it.isNotEmpty() }
                    .onEach {
                        view?.onCurrentTranslationUpdated(it)
                    })
        }
        launch(Dispatchers.Main) {
            receiveChannels.add(readingViewController.observeCurrentVerseIndex()
                    .filter { it.isValid() }
                    .onEach {
                        view?.onCurrentVerseIndexUpdated(it)
                    })
        }
    }

    fun updateCurrentVerseIndex(verseIndex: VerseIndex) {
        launch(Dispatchers.IO) {
            readingViewController.saveCurrentVerseIndex(verseIndex)
        }
    }

    fun loadVerses(translationShortName: String, bookIndex: Int, chapterIndex: Int) {
        launch(Dispatchers.Main) {
            try {
                val verses = withContext(Dispatchers.IO) {
                    readingViewController.readVerses(translationShortName, bookIndex, chapterIndex)
                }
                view?.onVersesLoaded(bookIndex, chapterIndex, verses)
            } catch (e: Exception) {
                view?.onError(e)
            }
        }
    }
}

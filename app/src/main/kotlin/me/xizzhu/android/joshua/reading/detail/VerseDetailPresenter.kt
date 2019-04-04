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

package me.xizzhu.android.joshua.reading.detail

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.first
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.reading.ReadingInteractor
import me.xizzhu.android.joshua.utils.MVPPresenter

class VerseDetailPresenter(private val readingInteractor: ReadingInteractor) : MVPPresenter<VerseDetailView>() {
    private var verse: Verse? = null

    override fun onViewAttached() {
        super.onViewAttached()

        launch(Dispatchers.Main) {
            val verseDetailOpenState = readingInteractor.observeVerseDetailOpenState()
            receiveChannels.add(verseDetailOpenState)
            verseDetailOpenState.consumeEach { verseIndex ->
                if (verseIndex.isValid()) {
                    view?.show()

                    val verse = async {
                        readingInteractor.readVerse(
                                readingInteractor.observeCurrentTranslation().first(), verseIndex)
                    }
                    val bookmarked = async { readingInteractor.readBookmark(verseIndex) }
                    this@VerseDetailPresenter.verse = verse.await()
                    view?.showVerse(VerseDetail(verse.await(), bookmarked.await().isValid()))
                } else {
                    view?.hide()
                }
            }
        }
    }

    fun hide() {
        launch(Dispatchers.Main) { readingInteractor.closeVerseDetail() }
    }

    fun addBookmark(verseIndex: VerseIndex) {
        launch(Dispatchers.Main) {
            verse?.let { v ->
                readingInteractor.addBookmark(verseIndex)
                view?.showVerse(VerseDetail(v, true))
            }
        }
    }

    fun removeBookmark(verseIndex: VerseIndex) {
        launch(Dispatchers.Main) {
            verse?.let { v ->
                readingInteractor.removeBookmark(verseIndex)
                view?.showVerse(VerseDetail(v, false))
            }
        }
    }
}

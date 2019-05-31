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

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.first
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.logger.Log
import me.xizzhu.android.joshua.reading.ReadingInteractor
import me.xizzhu.android.joshua.ui.recyclerview.VerseTextItem
import me.xizzhu.android.joshua.utils.BaseSettingsPresenter

class VerseDetailPresenter(private val readingInteractor: ReadingInteractor)
    : BaseSettingsPresenter<VerseDetailView>(readingInteractor) {
    @VisibleForTesting
    var verseDetail: VerseDetail? = null
    private var updateBookmarkJob: Job? = null
    private var updateNoteJob: Job? = null

    override fun onViewAttached() {
        super.onViewAttached()

        launch(Dispatchers.Main) {
            readingInteractor.observeVerseDetailOpenState().consumeEach {
                if (it.first.isValid()) {
                    view?.show(it.second)
                    loadVerseDetail(it.first)
                } else {
                    view?.hide()
                }
            }
        }
    }

    fun loadVerseDetail(verseIndex: VerseIndex) {
        launch(Dispatchers.Main) {
            view?.onVerseDetailLoaded(VerseDetail.INVALID)

            try {
                val verseDetail = coroutineScope {
                    val bookmarkAsync = async { readingInteractor.readBookmark(verseIndex) }
                    val noteAsync = async { readingInteractor.readNote(verseIndex) }

                    val verse = readingInteractor.readVerseWithParallel(
                            readingInteractor.observeCurrentTranslation().first(), verseIndex)
                    val verseTextItems = mutableListOf<VerseTextItem>().apply {
                        add(VerseTextItem(verseIndex, verse.text))
                    }
                    verse.parallel.forEach { verseTextItems.add(VerseTextItem(verseIndex, it)) }

                    verseDetail = VerseDetail(verseIndex, verseTextItems,
                            bookmarkAsync.await().isValid(), noteAsync.await().note)
                    return@coroutineScope verseDetail!!
                }

                view?.onVerseDetailLoaded(verseDetail)
            } catch (e: Exception) {
                Log.e(tag, e, "Failed to load verse detail")
                view?.onVerseDetailLoadFailed(verseIndex)
            }
        }
    }

    fun hide() {
        launch(Dispatchers.Main) { readingInteractor.closeVerseDetail() }
    }

    fun updateBookmark() {
        updateBookmarkJob?.cancel()
        updateBookmarkJob = launch(Dispatchers.Main) {
            verseDetail?.let { detail ->
                if (detail.bookmarked) {
                    readingInteractor.removeBookmark(detail.verseIndex)
                    verseDetail = detail.copy(bookmarked = false)
                } else {
                    readingInteractor.addBookmark(detail.verseIndex)
                    verseDetail = detail.copy(bookmarked = true)
                }
                view?.onVerseDetailLoaded(verseDetail!!)

                updateBookmarkJob = null
            }
        }
    }

    fun updateNote(note: String) {
        updateNoteJob?.cancel()
        updateNoteJob = launch(Dispatchers.Main) {
            verseDetail?.let { detail ->
                if (note.isEmpty()) {
                    readingInteractor.removeNote(detail.verseIndex)
                } else {
                    readingInteractor.saveNote(detail.verseIndex, note)
                }
                verseDetail = detail.copy(note = note)

                updateNoteJob = null
            }
        }
    }
}

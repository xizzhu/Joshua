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

import androidx.annotation.ColorInt
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.reading.ReadingInteractor
import me.xizzhu.android.joshua.ui.TranslationInfoComparator
import me.xizzhu.android.joshua.utils.activities.BaseSettingsPresenter
import me.xizzhu.android.joshua.utils.supervisedAsync
import me.xizzhu.android.logger.Log

class VerseDetailPresenter(private val readingInteractor: ReadingInteractor)
    : BaseSettingsPresenter<VerseDetailView>(readingInteractor) {
    companion object {
        private val TAG: String = VerseDetailPresenter::class.java.simpleName
    }

    private val translationComparator = TranslationInfoComparator(
            TranslationInfoComparator.SORT_ORDER_LANGUAGE_THEN_SHORT_NAME)

    @VisibleForTesting
    var verseDetail: VerseDetail? = null
    private var updateBookmarkJob: Job? = null
    private var updateHighlightJob: Job? = null
    private var updateNoteJob: Job? = null

    override fun onViewAttached() {
        super.onViewAttached()

        coroutineScope.launch(Dispatchers.Main) {
            readingInteractor.observeVerseDetailOpenState().collect {
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
        coroutineScope.launch(Dispatchers.Main) {
            view?.onVerseDetailLoaded(VerseDetail.INVALID)

            try {
                val bookmarkAsync = supervisedAsync { readingInteractor.readBookmark(verseIndex) }
                val highlightAsync = supervisedAsync { readingInteractor.readHighlight(verseIndex) }
                val noteAsync = supervisedAsync { readingInteractor.readNote(verseIndex) }

                val currentTranslation = readingInteractor.observeCurrentTranslation().first()
                val parallelTranslations = readingInteractor.observeDownloadedTranslations().first()
                        .sortedWith(translationComparator)
                        .filter { it.shortName != currentTranslation }
                        .map { it.shortName }
                val verse = readingInteractor.readVerse(currentTranslation, parallelTranslations, verseIndex)
                val verseTextItems = mutableListOf<VerseTextItem>().apply {
                    add(VerseTextItem(verseIndex, verse.text,
                            readingInteractor.readBookNames(verse.text.translationShortName)[verseIndex.bookIndex],
                            this@VerseDetailPresenter::onVerseClicked,
                            this@VerseDetailPresenter::onVerseLongClicked))
                }
                verse.parallel.forEach {
                    verseTextItems.add(VerseTextItem(verseIndex, it,
                            readingInteractor.readBookNames(it.translationShortName)[verseIndex.bookIndex],
                            this@VerseDetailPresenter::onVerseClicked,
                            this@VerseDetailPresenter::onVerseLongClicked))
                }

                verseDetail = VerseDetail(verseIndex, verseTextItems,
                        bookmarkAsync.await().isValid(), highlightAsync.await().color,
                        noteAsync.await().note)

                view?.onVerseDetailLoaded(verseDetail!!)
            } catch (e: Exception) {
                Log.e(tag, "Failed to load verse detail", e)
                view?.onVerseDetailLoadFailed(verseIndex)
            }
        }
    }

    @VisibleForTesting
    fun onVerseClicked(translation: String) {
        coroutineScope.launch(Dispatchers.Main) {
            try {
                if (translation != readingInteractor.observeCurrentTranslation().first()) {
                    readingInteractor.saveCurrentTranslation(translation)
                    readingInteractor.closeVerseDetail()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to select translation", e)
                view?.onVerseTextClickFailed()
            }
        }
    }

    @VisibleForTesting
    fun onVerseLongClicked(verse: Verse) {
        coroutineScope.launch(Dispatchers.Main) {
            if (readingInteractor.copyToClipBoard(listOf(verse))) {
                view?.onVerseTextCopied()
            } else {
                view?.onVerseTextClickFailed()
            }
        }
    }

    fun hide() {
        readingInteractor.closeVerseDetail()
    }

    fun updateBookmark() {
        updateBookmarkJob?.cancel()
        updateBookmarkJob = coroutineScope.launch(Dispatchers.Main) {
            verseDetail?.let { detail ->
                if (detail.bookmarked) {
                    readingInteractor.removeBookmark(detail.verseIndex)
                    verseDetail = detail.copy(bookmarked = false)
                } else {
                    readingInteractor.addBookmark(detail.verseIndex)
                    verseDetail = detail.copy(bookmarked = true)
                }
                view?.onVerseDetailLoaded(verseDetail!!)
            }

            updateBookmarkJob = null
        }
    }

    @ColorInt
    fun currentHighlightColor(): Int = verseDetail?.highlightColor ?: Highlight.COLOR_NONE

    fun updateHighlight(@ColorInt highlightColor: Int) {
        updateHighlightJob?.cancel()
        updateHighlightJob = coroutineScope.launch(Dispatchers.Main) {
            verseDetail?.let { detail ->
                if (highlightColor == Highlight.COLOR_NONE) {
                    readingInteractor.removeHighlight(detail.verseIndex)
                } else {
                    readingInteractor.saveHighlight(detail.verseIndex, highlightColor)
                }
                verseDetail = detail.copy(highlightColor = highlightColor)
                view?.onVerseDetailLoaded(verseDetail!!)
            }

            updateHighlightJob = null
        }
    }

    fun updateNote(note: String) {
        updateNoteJob?.cancel()
        updateNoteJob = coroutineScope.launch(Dispatchers.Main) {
            verseDetail?.let { detail ->
                if (note.isEmpty()) {
                    readingInteractor.removeNote(detail.verseIndex)
                } else {
                    readingInteractor.saveNote(detail.verseIndex, note)
                }
                verseDetail = detail.copy(note = note)
            }

            updateNoteJob = null
        }
    }
}

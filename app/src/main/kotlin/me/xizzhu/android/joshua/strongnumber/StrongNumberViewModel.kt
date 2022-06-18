/*
 * Copyright (C) 2022 Xizhi Zhu
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

package me.xizzhu.android.joshua.strongnumber

import android.app.Application
import android.text.SpannableStringBuilder
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.*
import me.xizzhu.android.joshua.infra.BaseViewModelV2
import me.xizzhu.android.joshua.preview.PreviewViewData
import me.xizzhu.android.joshua.preview.loadPreviewV2
import me.xizzhu.android.joshua.preview.toVersePreviewItems
import me.xizzhu.android.joshua.ui.createTitleSpans
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.TextItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import me.xizzhu.android.joshua.ui.setSpans
import me.xizzhu.android.joshua.ui.toCharSequence
import me.xizzhu.android.joshua.utils.firstNotEmpty
import me.xizzhu.android.logger.Log
import javax.inject.Inject

@HiltViewModel
class StrongNumberViewModel @Inject constructor(
        private val bibleReadingManager: BibleReadingManager,
        private val strongNumberManager: StrongNumberManager,
        settingsManager: SettingsManager,
        application: Application
) : BaseViewModelV2<StrongNumberViewModel.ViewAction, StrongNumberViewModel.ViewState>(
        settingsManager = settingsManager,
        application = application,
        initialViewState = ViewState(
                settings = null,
                loading = false,
                strongNumberItems = emptyList()
        )
) {
    sealed class ViewAction {
        object OpenReadingScreen : ViewAction()
        object ShowLoadStrongNumberFailedError : ViewAction()
        class ShowOpenPreviewFailedError(val verseIndex: VerseIndex) : ViewAction()
        class ShowOpenVerseFailedError(val verseToOpen: VerseIndex) : ViewAction()
        class ShowPreview(val previewViewData: PreviewViewData) : ViewAction()
    }

    data class ViewState(
            val settings: Settings?,
            val loading: Boolean,
            val strongNumberItems: List<BaseItem>
    )

    init {
        settings().onEach { settings -> emitViewState { it.copy(settings = settings) } }.launchIn(viewModelScope)
    }

    fun loadStrongNumber(sn: String) {
        if (sn.isEmpty()) {
            emitViewAction(ViewAction.ShowLoadStrongNumberFailedError)
            return
        }

        viewModelScope.launch {
            try {
                emitViewState { currentViewState ->
                    currentViewState.copy(loading = true, strongNumberItems = emptyList())
                }

                val currentTranslation = bibleReadingManager.currentTranslation().firstNotEmpty()
                val strongNumberItems = buildStrongNumberItems(
                        strongNumber = strongNumberManager.readStrongNumber(sn),
                        verses = bibleReadingManager.readVerses(currentTranslation, strongNumberManager.readVerseIndexes(sn)).values
                                .sortedBy { with(it.verseIndex) { bookIndex * 100000 + chapterIndex * 1000 + verseIndex } },
                        bookNames = bibleReadingManager.readBookNames(currentTranslation),
                        bookShortNames = bibleReadingManager.readBookShortNames(currentTranslation)
                )
                emitViewState { currentViewState ->
                    currentViewState.copy(loading = false, strongNumberItems = strongNumberItems)
                }
            } catch (e: Exception) {
                Log.e(tag, "Error occurred which loading Strong's Numbers", e)
                emitViewAction(ViewAction.ShowLoadStrongNumberFailedError)
                emitViewState { currentViewState ->
                    currentViewState.copy(loading = false, strongNumberItems = emptyList())
                }
            }
        }
    }

    private fun buildStrongNumberItems(
            strongNumber: StrongNumber,
            verses: List<Verse>,
            bookNames: List<String>,
            bookShortNames: List<String>): List<BaseItem> {
        val items: ArrayList<BaseItem> = ArrayList()

        val title = SpannableStringBuilder().append(strongNumber.sn)
                .setSpans(createTitleSpans())
                .append(' ').append(strongNumber.meaning)
                .toCharSequence()
        items.add(TextItem(title))

        var currentBookIndex = -1
        verses.forEach { verse ->
            if (verse.text.text.isEmpty()) return@forEach

            val verseIndex = verse.verseIndex
            val bookName = bookNames[verseIndex.bookIndex]
            if (verseIndex.bookIndex != currentBookIndex) {
                items.add(TitleItem(bookName, false))
                currentBookIndex = verseIndex.bookIndex
            }
            items.add(StrongNumberItem(verseIndex, bookShortNames[verseIndex.bookIndex], verse.text.text))
        }

        return items
    }

    fun openVerse(verseToOpen: VerseIndex) {
        viewModelScope.launch {
            try {
                bibleReadingManager.saveCurrentVerseIndex(verseToOpen)
                emitViewAction(ViewAction.OpenReadingScreen)
            } catch (e: Exception) {
                Log.e(tag, "Failed to save current verse", e)
                emitViewAction(ViewAction.ShowOpenVerseFailedError(verseToOpen))
            }
        }
    }

    fun showPreview(verseIndex: VerseIndex) {
        viewModelScope.launch {
            try {
                emitViewAction(ViewAction.ShowPreview(
                        previewViewData = loadPreviewV2(bibleReadingManager, settingsManager, verseIndex, ::toVersePreviewItems)
                ))
            } catch (e: Exception) {
                Log.e(tag, "Failed to load verses for preview", e)
                emitViewAction(ViewAction.ShowOpenPreviewFailedError(verseIndex))
            }
        }
    }
}

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

import android.text.SpannableStringBuilder
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.provider.CoroutineDispatcherProvider
import me.xizzhu.android.joshua.core.Settings
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.StrongNumber
import me.xizzhu.android.joshua.core.StrongNumberManager
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
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
    private val settingsManager: SettingsManager,
    private val coroutineDispatcherProvider: CoroutineDispatcherProvider,
    private val savedStateHandle: SavedStateHandle
) : BaseViewModelV2<StrongNumberViewModel.ViewAction, StrongNumberViewModel.ViewState>(
    initialViewState = ViewState(
        settings = null,
        loading = false,
        items = emptyList(),
        preview = null,
        error = null,
    )
) {
    sealed class ViewAction {
        object OpenReadingScreen : ViewAction()
    }

    data class ViewState(
        val settings: Settings?,
        val loading: Boolean,
        val items: List<BaseItem>,
        val preview: PreviewViewData?,
        val error: Error?,
    ) {
        sealed class Error {
            data class PreviewLoadingError(val verseToPreview: VerseIndex) : Error()
            object StrongNumberLoadingError : Error()
            data class VerseOpeningError(val verseToOpen: VerseIndex) : Error()
        }
    }

    init {
        settingsManager.settings().onEach { settings -> updateViewState { it.copy(settings = settings) } }.launchIn(viewModelScope)
        loadStrongNumber()
    }

    fun loadStrongNumber() {
        val sn = StrongNumberActivity.strongNumber(savedStateHandle)
        if (sn.isEmpty()) {
            updateViewState { it.copy(loading = false, items = emptyList(), error = ViewState.Error.StrongNumberLoadingError) }
            return
        }

        viewModelScope.launch(coroutineDispatcherProvider.default) {
            runCatching {
                updateViewState { it.copy(loading = true, error = null) }

                val currentTranslation = bibleReadingManager.currentTranslation().firstNotEmpty()
                val items = buildStrongNumberItems(
                    strongNumber = strongNumberManager.readStrongNumber(sn),
                    verses = bibleReadingManager.readVerses(currentTranslation, strongNumberManager.readVerseIndexes(sn)).values
                        .sortedBy { with(it.verseIndex) { bookIndex * 100000 + chapterIndex * 1000 + verseIndex } },
                    bookNames = bibleReadingManager.readBookNames(currentTranslation),
                    bookShortNames = bibleReadingManager.readBookShortNames(currentTranslation)
                )
                updateViewState { it.copy(loading = false, items = items) }
            }.onFailure { e ->
                Log.e(tag, "Error occurred which loading Strong's Numbers", e)
                updateViewState { it.copy(loading = false, error = ViewState.Error.StrongNumberLoadingError) }
            }
        }
    }

    private fun buildStrongNumberItems(
        strongNumber: StrongNumber,
        verses: List<Verse>,
        bookNames: List<String>,
        bookShortNames: List<String>
    ): List<BaseItem> {
        val items: ArrayList<BaseItem> = ArrayList()

        val title = SpannableStringBuilder().append(strongNumber.sn).setSpans(createTitleSpans()).append(' ').append(strongNumber.meaning).toCharSequence()
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
        viewModelScope.launch(coroutineDispatcherProvider.default) {
            runCatching {
                bibleReadingManager.saveCurrentVerseIndex(verseToOpen)
                emitViewAction(ViewAction.OpenReadingScreen)
            }.onFailure { e ->
                Log.e(tag, "Failed to save current verse", e)
                updateViewState { it.copy(error = ViewState.Error.VerseOpeningError(verseToOpen)) }
            }
        }
    }

    fun loadPreview(verseToPreview: VerseIndex) {
        viewModelScope.launch(coroutineDispatcherProvider.default) {
            runCatching {
                val preview = loadPreviewV2(bibleReadingManager, settingsManager, verseToPreview, ::toVersePreviewItems)
                updateViewState { it.copy(preview = preview) }
            }.onFailure { e ->
                Log.e(tag, "Failed to load verses for preview", e)
                updateViewState { it.copy(error = ViewState.Error.PreviewLoadingError(verseToPreview)) }
            }
        }
    }

    fun markPreviewAsClosed() {
        updateViewState { it.copy(preview = null) }
    }

    fun markErrorAsShown(error: ViewState.Error) {
        updateViewState { current -> if (current.error == error) current.copy(error = null) else null }
    }
}

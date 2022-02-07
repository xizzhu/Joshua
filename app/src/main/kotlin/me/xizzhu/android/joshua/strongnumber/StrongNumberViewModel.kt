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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.StrongNumber
import me.xizzhu.android.joshua.core.StrongNumberManager
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.BaseViewModel
import me.xizzhu.android.joshua.infra.viewData
import me.xizzhu.android.joshua.infra.onFailure
import me.xizzhu.android.joshua.preview.PreviewViewData
import me.xizzhu.android.joshua.preview.loadPreview
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

class StrongNumberViewData(val items: List<BaseItem>)

@HiltViewModel
class StrongNumberViewModel @Inject constructor(
        private val bibleReadingManager: BibleReadingManager,
        private val strongNumberManager: StrongNumberManager,
        settingsManager: SettingsManager,
        application: Application
) : BaseViewModel(settingsManager, application) {
    private val strongNumber: MutableStateFlow<ViewData<StrongNumberViewData>?> = MutableStateFlow(null)

    fun strongNumber(): Flow<ViewData<StrongNumberViewData>> = strongNumber.filterNotNull()

    fun loadStrongNumber(sn: String) {
        if (sn.isEmpty()) {
            strongNumber.value = ViewData.Failure(IllegalStateException("Requested Strong number is empty"))
            return
        }

        viewModelScope.launch {
            try {
                strongNumber.value = ViewData.Loading()

                val currentTranslation = bibleReadingManager.currentTranslation().firstNotEmpty()
                strongNumber.value = ViewData.Success(StrongNumberViewData(
                        items = buildStrongNumberItems(
                                strongNumber = strongNumberManager.readStrongNumber(sn),
                                verses = bibleReadingManager.readVerses(currentTranslation, strongNumberManager.readVerseIndexes(sn)).values
                                        .sortedBy { with(it.verseIndex) { bookIndex * 100000 + chapterIndex * 1000 + verseIndex } },
                                bookNames = bibleReadingManager.readBookNames(currentTranslation),
                                bookShortNames = bibleReadingManager.readBookShortNames(currentTranslation)
                        )
                ))
            } catch (e: Exception) {
                Log.e(tag, "Error occurred which loading Strong's Numbers", e)
                strongNumber.value = ViewData.Failure(e)
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

    fun saveCurrentVerseIndex(verseToOpen: VerseIndex): Flow<ViewData<Unit>> = viewData {
        bibleReadingManager.saveCurrentVerseIndex(verseToOpen)
    }.onFailure { Log.e(tag, "Failed to save current verse", it) }

    fun loadVersesForPreview(verseIndex: VerseIndex): Flow<ViewData<PreviewViewData>> =
            loadPreview(bibleReadingManager, settingsManager, verseIndex, ::toVersePreviewItems)
                    .onFailure { Log.e(tag, "Failed to load verses for preview", it) }
}

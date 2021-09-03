/*
 * Copyright (C) 2021 Xizhi Zhu
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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.core.BibleReadingManager
import me.xizzhu.android.joshua.core.SettingsManager
import me.xizzhu.android.joshua.core.StrongNumber
import me.xizzhu.android.joshua.core.StrongNumberManager
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.BaseViewModel
import me.xizzhu.android.joshua.infra.viewData
import me.xizzhu.android.joshua.infra.onFailure
import me.xizzhu.android.joshua.ui.createTitleSizeSpan
import me.xizzhu.android.joshua.ui.createTitleStyleSpan
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.TextItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import me.xizzhu.android.joshua.ui.setSpan
import me.xizzhu.android.joshua.ui.toCharSequence
import me.xizzhu.android.joshua.utils.firstNotEmpty
import me.xizzhu.android.logger.Log

class StrongNumberViewData(val items: List<BaseItem>)

class StrongNumberViewModel(
        private val navigator: Navigator,
        private val bibleReadingManager: BibleReadingManager,
        private val strongNumberManager: StrongNumberManager,
        settingsManager: SettingsManager,
        strongNumberActivity: StrongNumberActivity,
        coroutineScope: CoroutineScope = strongNumberActivity.lifecycleScope
) : BaseViewModel<StrongNumberActivity>(settingsManager, strongNumberActivity, coroutineScope) {
    private val strongNumber: MutableStateFlow<ViewData<StrongNumberViewData>?> = MutableStateFlow(null)

    private fun buildStrongNumberItems(
            strongNumber: StrongNumber,
            verses: List<Verse>,
            bookNames: List<String>,
            bookShortNames: List<String>): List<BaseItem> {
        val items: ArrayList<BaseItem> = ArrayList()

        items.add(TextItem(formatStrongNumber(strongNumber)))

        var currentBookIndex = -1
        verses.forEach { verse ->
            val verseIndex = verse.verseIndex
            val bookName = bookNames[verseIndex.bookIndex]
            if (verseIndex.bookIndex != currentBookIndex) {
                items.add(TitleItem(bookName, false))
                currentBookIndex = verseIndex.bookIndex
            }

            items.add(StrongNumberItem(verseIndex, bookShortNames[verseIndex.bookIndex], verse.text.text, ::openVerse))
        }

        return items
    }

    private fun formatStrongNumber(strongNumber: StrongNumber): CharSequence =
            SpannableStringBuilder().append(strongNumber.sn)
                    .setSpan(createTitleStyleSpan(), createTitleSizeSpan())
                    .append(' ').append(strongNumber.meaning)
                    .toCharSequence()

    private fun openVerse(verseToOpen: VerseIndex): Flow<ViewData<Unit>> = viewData {
        bibleReadingManager.saveCurrentVerseIndex(verseToOpen)
        navigator.navigate(activity, Navigator.SCREEN_READING)
    }.onFailure { Log.e(tag, "Failed to select verse and open reading activity", it) }

    fun strongNumber(): Flow<ViewData<StrongNumberViewData>> = strongNumber.filterNotNull()

    fun loadStrongNumber(sn: String) {
        if (sn.isEmpty()) {
            strongNumber.value = ViewData.Failure(IllegalStateException("Requested Strong number is empty"))
            return
        }

        coroutineScope.launch {
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
        }
    }
}

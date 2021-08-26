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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.core.StrongNumber
import me.xizzhu.android.joshua.core.Verse
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.infra.BaseViewModel
import me.xizzhu.android.joshua.ui.createTitleSizeSpan
import me.xizzhu.android.joshua.ui.createTitleStyleSpan
import me.xizzhu.android.joshua.ui.dialog
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.TextItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import me.xizzhu.android.joshua.ui.setSpan
import me.xizzhu.android.joshua.ui.toCharSequence
import me.xizzhu.android.logger.Log

class StrongNumberViewData(val items: List<BaseItem>)

class StrongNumberViewModel(
        private val navigator: Navigator,
        strongNumberInteractor: StrongNumberInteractor,
        strongNumberActivity: StrongNumberActivity,
        coroutineScope: CoroutineScope = strongNumberActivity.lifecycleScope
) : BaseViewModel<StrongNumberInteractor, StrongNumberActivity>(strongNumberInteractor, strongNumberActivity, coroutineScope) {
    private val strongNumber: MutableStateFlow<String?> = MutableStateFlow(null)
    private val strongNumberViewData: MutableStateFlow<ViewData<StrongNumberViewData>?> = MutableStateFlow(null)

    init {
        strongNumber
                .filterNotNull()
                .onEach { sn ->
                    if (sn.isEmpty()) {
                        strongNumberViewData.value = ViewData.Failure(IllegalStateException("Requested Strong number is empty"))
                        return@onEach
                    }

                    strongNumberViewData.value = ViewData.Success(StrongNumberViewData(
                            items = buildStrongNumberItems(
                                    strongNumber = interactor.strongNumber(sn),
                                    verses = interactor.verses(sn),
                                    bookNames = interactor.bookNames(),
                                    bookShortNames = interactor.bookShortNames()
                            )
                    ))
                }
                .launchIn(coroutineScope)
    }

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

            items.add(VerseStrongNumberItem(verseIndex, bookShortNames[verseIndex.bookIndex],
                    verse.text.text, ::openVerse))
        }

        return items
    }

    private fun formatStrongNumber(strongNumber: StrongNumber): CharSequence =
            SpannableStringBuilder().append(strongNumber.sn)
                    .setSpan(createTitleStyleSpan(), createTitleSizeSpan())
                    .append(' ').append(strongNumber.meaning)
                    .toCharSequence()

    private fun openVerse(verseToOpen: VerseIndex) {
        coroutineScope.launch {
            try {
                interactor.saveCurrentVerseIndex(verseToOpen)
                navigator.navigate(activity, Navigator.SCREEN_READING)
            } catch (e: Exception) {
                Log.e(tag, "Failed to select verse and open reading activity", e)
                activity.dialog(true, R.string.dialog_verse_selection_error,
                        { _, _ -> openVerse(verseToOpen) })
            }
        }
    }

    fun strongNumber(): Flow<ViewData<StrongNumberViewData>> = strongNumberViewData.filterNotNull()

    fun loadStrongNumber(sn: String) {
        strongNumber.value = sn
    }
}

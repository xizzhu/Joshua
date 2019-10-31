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

package me.xizzhu.android.joshua.annotated.highlights.list

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.annotated.BaseAnnotatedVersesPresenter
import me.xizzhu.android.joshua.annotated.formatDate
import me.xizzhu.android.joshua.annotated.highlights.HighlightsActivity
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.infra.arch.dataOnSuccessOrThrow
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import java.util.*
import kotlin.collections.ArrayList

class HighlightsListPresenter(private val highlightsActivity: HighlightsActivity,
                              navigator: Navigator,
                              highlightsListInteractor: HighlightsListInteractor,
                              dispatcher: CoroutineDispatcher = Dispatchers.Main)
    : BaseAnnotatedVersesPresenter<Highlight, HighlightsListInteractor>(
        highlightsActivity, navigator, R.string.text_no_highlights, highlightsListInteractor, dispatcher) {
    override suspend fun List<Highlight>.toBaseItemsByDate(): List<BaseItem> {
        val bookNames = interactor.bookNames().dataOnSuccessOrThrow("Failed to load book names")
        val bookShortNames = interactor.bookShortNames().dataOnSuccessOrThrow("Failed to load book short names")

        val calendar = Calendar.getInstance()
        var previousYear = -1
        var previousDayOfYear = -1

        val items: ArrayList<BaseItem> = ArrayList()
        forEach { highlight ->
            calendar.timeInMillis = highlight.timestamp
            val currentYear = calendar.get(Calendar.YEAR)
            val currentDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
            if (currentYear != previousYear || currentDayOfYear != previousDayOfYear) {
                items.add(TitleItem(highlight.timestamp.formatDate(highlightsActivity.resources, calendar), false))

                previousYear = currentYear
                previousDayOfYear = currentDayOfYear
            }

            val verse = interactor.verse(highlight.verseIndex).dataOnSuccessOrThrow("Failed to load verse")
            items.add(HighlightItem(highlight.verseIndex, bookNames[highlight.verseIndex.bookIndex],
                    bookShortNames[highlight.verseIndex.bookIndex], verse.text.text,
                    highlight.color, Constants.SORT_BY_DATE, this@HighlightsListPresenter::openVerse))
        }
        return items
    }

    override suspend fun List<Highlight>.toBaseItemsByBook(): List<BaseItem> {
        val bookNames = interactor.bookNames().dataOnSuccessOrThrow("Failed to load book names")
        val bookShortNames = interactor.bookShortNames().dataOnSuccessOrThrow("Failed to load book short names")

        val items: ArrayList<BaseItem> = ArrayList()
        var currentBookIndex = -1
        forEach { highlight ->
            val bookName = bookNames[highlight.verseIndex.bookIndex]
            if (highlight.verseIndex.bookIndex != currentBookIndex) {
                items.add(TitleItem(bookName, false))
                currentBookIndex = highlight.verseIndex.bookIndex
            }

            val verse = interactor.verse(highlight.verseIndex).dataOnSuccessOrThrow("Failed to load book short names")
            items.add(HighlightItem(highlight.verseIndex, bookName, bookShortNames[highlight.verseIndex.bookIndex],
                    verse.text.text, highlight.color, Constants.SORT_BY_BOOK, this@HighlightsListPresenter::openVerse))
        }
        return items
    }
}

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

import android.content.res.Resources
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.annotated.AnnotatedVersePresenter
import me.xizzhu.android.joshua.annotated.highlights.HighlightsInteractor
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.annotated.formatDate
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.TextItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import me.xizzhu.android.joshua.utils.supervisedAsync
import me.xizzhu.android.logger.Log
import java.util.*
import kotlin.collections.ArrayList

class HighlightsPresenter(private val highlightsInteractor: HighlightsInteractor, private val resources: Resources)
    : AnnotatedVersePresenter(highlightsInteractor) {
    override fun load(sortOrder: Int) {
        coroutineScope.launch(Dispatchers.Main) {
            try {
                highlightsInteractor.notifyLoadingStarted()
                view?.onLoadingStarted()

                val highlights = highlightsInteractor.readHighlights(sortOrder)
                if (highlights.isEmpty()) {
                    view?.onItemsLoaded(listOf(TextItem(resources.getString(R.string.text_no_highlights))))
                } else {
                    val currentTranslation = highlightsInteractor.readCurrentTranslation()
                    val bookNamesAsync = supervisedAsync { highlightsInteractor.readBookNames(currentTranslation) }
                    val bookShortNames = highlightsInteractor.readBookShortNames(currentTranslation)
                    val bookNames = bookNamesAsync.await()
                    val items = when (sortOrder) {
                        Constants.SORT_BY_DATE -> toBaseItemsByDate(highlights, currentTranslation, bookNames, bookShortNames)
                        Constants.SORT_BY_BOOK -> toBaseItemsByBook(highlights, currentTranslation, bookNames, bookShortNames)
                        else -> throw IllegalArgumentException("Unsupported sort order - $sortOrder")
                    }
                    view?.onItemsLoaded(items)
                }

                view?.onLoadingCompleted()
            } catch (e: Exception) {
                Log.e(tag, "Failed to load highlights", e)
                view?.onLoadingFailed(sortOrder)
            } finally {
                highlightsInteractor.notifyLoadingFinished()
            }
        }
    }

    private suspend fun toBaseItemsByDate(highlights: List<Highlight>, currentTranslation: String,
                                          bookNames: List<String>, bookShortNames: List<String>): List<BaseItem> {
        val calendar = Calendar.getInstance()
        var previousYear = -1
        var previousDayOfYear = -1

        val items: ArrayList<BaseItem> = ArrayList()
        for (highlight in highlights) {
            calendar.timeInMillis = highlight.timestamp
            val currentYear = calendar.get(Calendar.YEAR)
            val currentDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
            if (currentYear != previousYear || currentDayOfYear != previousDayOfYear) {
                items.add(TitleItem(highlight.timestamp.formatDate(resources, calendar), false))

                previousYear = currentYear
                previousDayOfYear = currentDayOfYear
            }

            items.add(HighlightItem(highlight.verseIndex, bookNames[highlight.verseIndex.bookIndex],
                    bookShortNames[highlight.verseIndex.bookIndex],
                    highlightsInteractor.readVerse(currentTranslation, highlight.verseIndex).text.text,
                    highlight.color, Constants.SORT_BY_DATE, this@HighlightsPresenter::openVerse))
        }
        return items
    }

    private suspend fun toBaseItemsByBook(highlights: List<Highlight>, currentTranslation: String,
                                          bookNames: List<String>, bookShortNames: List<String>): List<BaseItem> {
        val items: ArrayList<BaseItem> = ArrayList()
        var currentBookIndex = -1
        for (highlight in highlights) {
            val verse = highlightsInteractor.readVerse(currentTranslation, highlight.verseIndex)
            val bookName = bookNames[highlight.verseIndex.bookIndex]
            if (highlight.verseIndex.bookIndex != currentBookIndex) {
                items.add(TitleItem(bookName, false))
                currentBookIndex = highlight.verseIndex.bookIndex
            }

            items.add(HighlightItem(highlight.verseIndex, bookName, bookShortNames[highlight.verseIndex.bookIndex],
                    verse.text.text, highlight.color, Constants.SORT_BY_BOOK, this@HighlightsPresenter::openVerse))
        }
        return items
    }
}

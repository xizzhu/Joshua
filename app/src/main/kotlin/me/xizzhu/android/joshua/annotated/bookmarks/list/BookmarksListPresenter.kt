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

package me.xizzhu.android.joshua.annotated.bookmarks.list

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import me.xizzhu.android.joshua.Navigator
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.annotated.BaseAnnotatedVersesPresenter
import me.xizzhu.android.joshua.annotated.bookmarks.BookmarksActivity
import me.xizzhu.android.joshua.annotated.formatDate
import me.xizzhu.android.joshua.core.Bookmark
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import java.util.*
import kotlin.collections.ArrayList

class BookmarksListPresenter(private val bookmarksActivity: BookmarksActivity,
                             navigator: Navigator,
                             bookmarksListInteractor: BookmarksListInteractor,
                             dispatcher: CoroutineDispatcher = Dispatchers.Main)
    : BaseAnnotatedVersesPresenter<Bookmark, BookmarksListInteractor>(
        bookmarksActivity, navigator, R.string.text_no_bookmark, bookmarksListInteractor, dispatcher) {
    override suspend fun List<Bookmark>.toBaseItemsByDate(): List<BaseItem> {
        val bookNames = interactor.bookNames()
        val bookShortNames = interactor.bookShortNames()

        val calendar = Calendar.getInstance()
        var previousYear = -1
        var previousDayOfYear = -1

        val items: ArrayList<BaseItem> = ArrayList()
        forEach { bookmark ->
            calendar.timeInMillis = bookmark.timestamp
            val currentYear = calendar.get(Calendar.YEAR)
            val currentDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
            if (currentYear != previousYear || currentDayOfYear != previousDayOfYear) {
                items.add(TitleItem(bookmark.timestamp.formatDate(bookmarksActivity.resources, calendar), false))

                previousYear = currentYear
                previousDayOfYear = currentDayOfYear
            }

            items.add(BookmarkItem(bookmark.verseIndex, bookNames[bookmark.verseIndex.bookIndex],
                    bookShortNames[bookmark.verseIndex.bookIndex],
                    interactor.verse(bookmark.verseIndex).text.text,
                    Constants.SORT_BY_DATE, this@BookmarksListPresenter::openVerse))
        }
        return items
    }

    override suspend fun List<Bookmark>.toBaseItemsByBook(): List<BaseItem> {
        val bookNames = interactor.bookNames()
        val bookShortNames = interactor.bookShortNames()

        val items: ArrayList<BaseItem> = ArrayList()
        var currentBookIndex = -1
        forEach { bookmark ->
            val verse = interactor.verse(bookmark.verseIndex)
            val bookName = bookNames[bookmark.verseIndex.bookIndex]
            if (bookmark.verseIndex.bookIndex != currentBookIndex) {
                items.add(TitleItem(bookName, false))
                currentBookIndex = bookmark.verseIndex.bookIndex
            }

            items.add(BookmarkItem(bookmark.verseIndex, bookName, bookShortNames[bookmark.verseIndex.bookIndex],
                    verse.text.text, Constants.SORT_BY_BOOK, this@BookmarksListPresenter::openVerse))
        }
        return items
    }
}

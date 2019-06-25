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

package me.xizzhu.android.joshua.bookmarks.list

import android.content.res.Resources
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.bookmarks.BookmarksInteractor
import me.xizzhu.android.joshua.core.Bookmark
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.ui.formatDate
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.TextItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import me.xizzhu.android.joshua.utils.BaseSettingsPresenter
import me.xizzhu.android.logger.Log
import java.util.*
import kotlin.collections.ArrayList

class BookmarksPresenter(private val bookmarksInteractor: BookmarksInteractor, private val resources: Resources)
    : BaseSettingsPresenter<BookmarksView>(bookmarksInteractor) {
    override fun onViewAttached() {
        super.onViewAttached()

        coroutineScope.launch(Dispatchers.Main) {
            bookmarksInteractor.observeBookmarksSortOrder().consumeEach { loadBookmarks(it) }
        }
    }

    fun loadBookmarks(@Constants.SortOrder sortOrder: Int) {
        coroutineScope.launch(Dispatchers.Main) {
            try {
                bookmarksInteractor.notifyLoadingStarted()
                view?.onBookmarksLoadingStarted()

                val bookmarks = bookmarksInteractor.readBookmarks(sortOrder)
                if (bookmarks.isEmpty()) {
                    view?.onBookmarksLoaded(listOf(TextItem(resources.getString(R.string.text_no_bookmark))))
                } else {
                    val items = when (sortOrder) {
                        Constants.SORT_BY_DATE -> bookmarks.toBaseItemsByDate()
                        Constants.SORT_BY_BOOK -> bookmarks.toBaseItemsByBook()
                        else -> throw IllegalArgumentException("Unsupported sort order - $sortOrder")
                    }
                    view?.onBookmarksLoaded(items)
                }

                bookmarksInteractor.notifyLoadingFinished()
                view?.onBookmarksLoadingCompleted()
            } catch (e: Exception) {
                Log.e(tag, "Failed to load bookmarks", e)
                view?.onBookmarksLoadFailed(sortOrder)
            }
        }
    }

    @VisibleForTesting
    suspend fun List<Bookmark>.toBaseItemsByDate(): List<BaseItem> {
        val calendar = Calendar.getInstance()
        var previousYear = -1
        var previousDayOfYear = -1

        val currentTranslation = bookmarksInteractor.readCurrentTranslation()
        val items: ArrayList<BaseItem> = ArrayList()
        for (bookmark in this) {
            calendar.timeInMillis = bookmark.timestamp
            val currentYear = calendar.get(Calendar.YEAR)
            val currentDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
            if (currentYear != previousYear || currentDayOfYear != previousDayOfYear) {
                items.add(TitleItem(bookmark.timestamp.formatDate(resources, calendar), false))

                previousYear = currentYear
                previousDayOfYear = currentDayOfYear
            }

            items.add(BookmarkItem(bookmark.verseIndex,
                    bookmarksInteractor.readVerse(currentTranslation, bookmark.verseIndex).text,
                    bookmark.timestamp, Constants.SORT_BY_DATE, this@BookmarksPresenter::selectVerse))
        }
        return items
    }

    @VisibleForTesting
    suspend fun List<Bookmark>.toBaseItemsByBook(): List<BaseItem> {
        val currentTranslation = bookmarksInteractor.readCurrentTranslation()
        val items: ArrayList<BaseItem> = ArrayList()
        var currentBookIndex = -1
        for (bookmark in this) {
            val verse = bookmarksInteractor.readVerse(currentTranslation, bookmark.verseIndex)
            if (bookmark.verseIndex.bookIndex != currentBookIndex) {
                items.add(TitleItem(verse.text.bookName, false))
                currentBookIndex = bookmark.verseIndex.bookIndex
            }

            items.add(BookmarkItem(bookmark.verseIndex, verse.text, bookmark.timestamp,
                    Constants.SORT_BY_BOOK, this@BookmarksPresenter::selectVerse))
        }
        return items
    }

    fun selectVerse(verseToSelect: VerseIndex) {
        coroutineScope.launch(Dispatchers.Main) {
            try {
                bookmarksInteractor.openReading(verseToSelect)
            } catch (e: Exception) {
                Log.e(tag, "Failed to select verse and open reading activity", e)
                view?.onVerseSelectionFailed(verseToSelect)
            }
        }
    }
}

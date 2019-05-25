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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.R
import me.xizzhu.android.joshua.bookmarks.BookmarksInteractor
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.logger.Log
import me.xizzhu.android.joshua.ui.formatDate
import me.xizzhu.android.joshua.ui.recyclerview.BaseItem
import me.xizzhu.android.joshua.ui.recyclerview.BookmarkItem
import me.xizzhu.android.joshua.ui.recyclerview.TextItem
import me.xizzhu.android.joshua.ui.recyclerview.TitleItem
import me.xizzhu.android.joshua.utils.BaseSettingsPresenter
import java.util.*
import kotlin.collections.ArrayList

class BookmarksPresenter(private val bookmarksInteractor: BookmarksInteractor, private val resources: Resources)
    : BaseSettingsPresenter<BookmarksView>(bookmarksInteractor) {
    override fun onViewAttached() {
        super.onViewAttached()

        launch(Dispatchers.Main) {
            bookmarksInteractor.observeBookmarksSortOrder().consumeEach { loadBookmarks(it) }
        }
    }

    fun loadBookmarks(@Constants.SortOrder sortOrder: Int) {
        launch(Dispatchers.Main) {
            try {
                bookmarksInteractor.notifyLoadingStarted()
                view?.onBookmarksLoadingStarted()

                val bookmarks = bookmarksInteractor.readBookmarks(sortOrder)
                if (bookmarks.isEmpty()) {
                    view?.onBookmarksLoaded(listOf(TextItem(resources.getString(R.string.text_no_bookmark))))
                } else {
                    // TODO handles sort order

                    val calendar = Calendar.getInstance()
                    var previousYear = -1
                    var previousDayOfYear = -1

                    val currentTranslation = bookmarksInteractor.readCurrentTranslation()
                    val items: ArrayList<BaseItem> = ArrayList()
                    for (bookmark in bookmarks) {
                        calendar.timeInMillis = bookmark.timestamp
                        val currentYear = calendar.get(Calendar.YEAR)
                        val currentDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
                        if (currentYear != previousYear || currentDayOfYear != previousDayOfYear) {
                            items.add(TitleItem(bookmark.timestamp.formatDate(resources)))

                            previousYear = currentYear
                            previousDayOfYear = currentDayOfYear
                        }

                        items.add(BookmarkItem(bookmark.verseIndex,
                                bookmarksInteractor.readVerse(currentTranslation, bookmark.verseIndex).text,
                                bookmark.timestamp, this@BookmarksPresenter::selectVerse))
                    }
                    view?.onBookmarksLoaded(items)
                }

                bookmarksInteractor.notifyLoadingFinished()
                view?.onBookmarksLoadingCompleted()
            } catch (e: Exception) {
                Log.e(tag, e, "Failed to load bookmarks")
                view?.onBookmarksLoadFailed(sortOrder)
            }
        }
    }

    fun selectVerse(verseToSelect: VerseIndex) {
        launch(Dispatchers.Main) {
            try {
                bookmarksInteractor.selectVerse(verseToSelect)
                bookmarksInteractor.openReading()
            } catch (e: Exception) {
                Log.e(tag, e, "Failed to select verse and open reading activity")
                view?.onVerseSelectionFailed(verseToSelect)
            }
        }
    }
}

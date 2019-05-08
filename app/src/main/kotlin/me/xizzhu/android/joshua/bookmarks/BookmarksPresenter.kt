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

package me.xizzhu.android.joshua.bookmarks

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.core.VerseIndex
import me.xizzhu.android.joshua.core.logger.Log
import me.xizzhu.android.joshua.ui.recyclerview.BookmarkItem
import me.xizzhu.android.joshua.utils.BaseSettingsPresenter

class BookmarksPresenter(private val bookmarksInteractor: BookmarksInteractor)
    : BaseSettingsPresenter<BookmarksView>(bookmarksInteractor) {
    override fun onViewAttached() {
        super.onViewAttached()
        loadBookmarks()
    }

    fun loadBookmarks() {
        launch(Dispatchers.Main) {
            try {
                val bookmarks = bookmarksInteractor.readBookmarks()
                if (bookmarks.isEmpty()) {
                    view?.onNoBookmarksAvailable()
                } else {
                    val currentTranslation = bookmarksInteractor.readCurrentTranslation()
                    val bookmarkItems: ArrayList<BookmarkItem> = ArrayList(bookmarks.size)
                    for (bookmark in bookmarks) {
                        bookmarkItems.add(BookmarkItem(bookmark.verseIndex,
                                bookmarksInteractor.readVerse(currentTranslation, bookmark.verseIndex).text,
                                bookmark.timestamp))
                    }
                    view?.onBookmarksLoaded(bookmarkItems)
                }

                bookmarksInteractor.notifyLoadingFinished()
            } catch (e: Exception) {
                Log.e(tag, e, "Failed to load bookmarks")
                view?.onBookmarksLoadFailed()
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

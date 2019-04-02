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
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.xizzhu.android.joshua.core.logger.Log
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
                view?.onBookmarksLoaded(withContext(Dispatchers.Default) {
                    val bookmarksAsync = async { bookmarksInteractor.readBookmarks() }
                    val currentTranslation = withContext(Dispatchers.Default) { bookmarksInteractor.readCurrentTranslation() }
                    val bookmarks: ArrayList<BookmarkForDisplay> = ArrayList()
                    for (bookmark in bookmarksAsync.await()) {
                        bookmarks.add(BookmarkForDisplay(bookmark.verseIndex,
                                bookmarksInteractor.readVerse(currentTranslation, bookmark.verseIndex).text,
                                bookmark.timestamp))
                    }
                    return@withContext bookmarks
                })

                bookmarksInteractor.notifyLoadingFinished()
            } catch (e: Exception) {
                Log.e(tag, e, "Failed to load bookmarks")
                view?.onBookmarksLoadFailed()
            }
        }
    }
}

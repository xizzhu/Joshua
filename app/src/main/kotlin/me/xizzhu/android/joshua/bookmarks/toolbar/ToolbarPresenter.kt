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

package me.xizzhu.android.joshua.bookmarks.toolbar

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.first
import kotlinx.coroutines.launch
import me.xizzhu.android.joshua.bookmarks.BookmarksInteractor
import me.xizzhu.android.joshua.core.Constants
import me.xizzhu.android.joshua.core.logger.Log
import me.xizzhu.android.joshua.utils.MVPPresenter

class ToolbarPresenter(private val bookmarksInteractor: BookmarksInteractor) : MVPPresenter<ToolbarView>() {
    override fun onViewAttached() {
        super.onViewAttached()

        launch(Dispatchers.Main) {
            view?.onBookmarkSortOrderLoaded(bookmarksInteractor.observeBookmarksSortOrder().first())
        }
    }

    fun updateBookmarksSortOrder(@Constants.SortOrder sortOrder: Int) {
        launch(Dispatchers.Main) {
            try {
                bookmarksInteractor.saveBookmarksSortOrder(sortOrder)
            } catch (e: Exception) {
                Log.e(tag, e, "Failed to update sort method")
                // TODO
            }
        }
    }
}

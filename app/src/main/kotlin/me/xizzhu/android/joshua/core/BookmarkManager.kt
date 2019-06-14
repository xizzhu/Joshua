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

package me.xizzhu.android.joshua.core

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import me.xizzhu.android.joshua.core.repository.BookmarkRepository
import me.xizzhu.android.logger.Log

data class Bookmark(val verseIndex: VerseIndex, val timestamp: Long) {
    fun isValid(): Boolean = timestamp > 0L
}

class BookmarkManager(private val bookmarkRepository: BookmarkRepository) {
    companion object {
        private val TAG = BookmarkManager::class.java.simpleName
    }

    private val bookmarksSortOrder: BroadcastChannel<Int> = ConflatedBroadcastChannel()

    suspend fun observeSortOrder(): ReceiveChannel<Int> {
        return bookmarksSortOrder.openSubscription().apply {
            if (isEmpty) {
                try {
                    bookmarksSortOrder.send(bookmarkRepository.readSortOrder())
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to initialize bookmark sort order", e)
                    bookmarksSortOrder.send(Constants.DEFAULT_SORT_ORDER)
                }
            }
        }
    }

    suspend fun saveSortOrder(@Constants.SortOrder sortOrder: Int) {
        bookmarkRepository.saveSortOrder(sortOrder)
        bookmarksSortOrder.send(sortOrder)
    }

    suspend fun read(@Constants.SortOrder sortOrder: Int): List<Bookmark> = bookmarkRepository.read(sortOrder)

    suspend fun read(bookIndex: Int, chapterIndex: Int): List<Bookmark> = bookmarkRepository.read(bookIndex, chapterIndex)

    suspend fun read(verseIndex: VerseIndex): Bookmark = bookmarkRepository.read(verseIndex)

    suspend fun save(bookmark: Bookmark) {
        bookmarkRepository.save(bookmark)
    }

    suspend fun remove(verseIndex: VerseIndex) {
        bookmarkRepository.remove(verseIndex)
    }
}

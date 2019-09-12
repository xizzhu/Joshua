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

import kotlinx.coroutines.*
import me.xizzhu.android.joshua.utils.mergeSort

class BackupManager(private val serializer: Serializer,
                    private val bookmarkManager: BookmarkManager,
                    private val highlightManager: HighlightManager,
                    private val noteManager: NoteManager,
                    private val readingProgressManager: ReadingProgressManager) {
    interface Serializer {
        fun serialize(data: Data): String

        fun deserialize(content: String): Data
    }

    data class Data(val bookmarks: List<Bookmark>, val highlights: List<Highlight>,
                    val notes: List<Note>, val readingProgress: ReadingProgress)

    suspend fun prepareForBackup(): String = withContext(Dispatchers.Default) {
        val bookmarksAsync = async { bookmarkManager.read(Constants.SORT_BY_BOOK) }
        val highlightsAsync = async { highlightManager.read(Constants.SORT_BY_BOOK) }
        val notesAsync = async { noteManager.read(Constants.SORT_BY_BOOK) }
        val readingProgressAsync = async { readingProgressManager.read() }
        return@withContext serializer.serialize(
                Data(bookmarksAsync.await(), highlightsAsync.await(), notesAsync.await(), readingProgressAsync.await()))
    }

    suspend fun restore(content: String) {
        withContext(Dispatchers.Default) {
            val data = serializer.deserialize(content)
            arrayOf(loadAndMergeReadingProgressAsync(data.readingProgress)).forEach { it.await() }

            // TODO merge and store data
        }
    }

    private fun CoroutineScope.loadAndMergeReadingProgressAsync(backupReadingProgress: ReadingProgress): Deferred<Unit> = async {
        val currentReadingProgress = readingProgressManager.read()
        val continuousReadingDays: Int
        val lastReadingTimestamp: Long
        if (currentReadingProgress.lastReadingTimestamp >= backupReadingProgress.lastReadingTimestamp) {
            continuousReadingDays = currentReadingProgress.continuousReadingDays
            lastReadingTimestamp = currentReadingProgress.lastReadingTimestamp
        } else {
            continuousReadingDays = backupReadingProgress.continuousReadingDays
            lastReadingTimestamp = backupReadingProgress.lastReadingTimestamp
        }

        val chapterReadingStatus = mergeSort(currentReadingProgress.chapterReadingStatus,
                backupReadingProgress.chapterReadingStatus.toMutableList().apply { sortBy { it.bookIndex * 1000 + it.chapterIndex } },
                { left, right -> left.bookIndex * 1000 + left.chapterIndex - (right.bookIndex * 1000 + right.chapterIndex) },
                { left, right -> if (left.lastReadingTimestamp > right.lastReadingTimestamp) left else right })

        readingProgressManager.save(ReadingProgress(continuousReadingDays, lastReadingTimestamp, chapterReadingStatus))
    }
}

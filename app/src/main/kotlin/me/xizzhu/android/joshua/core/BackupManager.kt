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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class BackupManager(private val serializerFactory: () -> Serializer,
                    private val deserializerFactory: () -> Deserializer,
                    private val bookmarkManager: BookmarkManager,
                    private val highlightManager: HighlightManager,
                    private val noteManager: NoteManager,
                    private val readingProgressManager: ReadingProgressManager) {
    interface Serializer {
        fun withBookmarks(bookmarks: List<Bookmark>): Serializer

        fun withHighlights(highlights: List<Highlight>): Serializer

        fun withNotes(notes: List<Note>): Serializer

        fun withReadingProgress(readingProgress: ReadingProgress): Serializer

        fun serialize(): String
    }

    interface Deserializer {
        fun withContent(content: String): Deserializer


        fun deserialize(): Data
    }

    data class Data(val bookmarks: List<Bookmark>, val highlights: List<Highlight>,
                    val notes: List<Note>, val readingProgress: ReadingProgress)

    suspend fun prepareForBackup(): String = withContext(Dispatchers.Default) {
        val bookmarksAsync = async { bookmarkManager.read(Constants.SORT_BY_DATE) }
        val highlightsAsync = async { highlightManager.read(Constants.SORT_BY_DATE) }
        val notesAsync = async { noteManager.read(Constants.SORT_BY_DATE) }
        val readingProgressAsync = async { readingProgressManager.readReadingProgress() }
        return@withContext serializerFactory()
                .withBookmarks(bookmarksAsync.await())
                .withHighlights(highlightsAsync.await())
                .withNotes(notesAsync.await())
                .withReadingProgress(readingProgressAsync.await())
                .serialize()
    }

    suspend fun restore(content: String) {
        withContext(Dispatchers.Default) {
            val data = deserializerFactory().withContent(content).deserialize()
        }
    }
}

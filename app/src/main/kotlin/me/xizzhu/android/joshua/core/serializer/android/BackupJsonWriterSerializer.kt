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

package me.xizzhu.android.joshua.core.serializer.android

import android.util.JsonWriter
import me.xizzhu.android.joshua.core.BackupManager
import me.xizzhu.android.joshua.core.Bookmark
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.VerseIndex
import java.io.BufferedWriter
import java.io.StringWriter

class BackupJsonWriterSerializer : BackupManager.Serializer {
    companion object {
        private const val KEY_BOOKMARKS = "bookmarks"
        private const val KEY_HIGHLIGHTS = "highlights"

        private const val KEY_BOOK_INDEX = "bookIndex"
        private const val KEY_CHAPTER_INDEX = "chapterIndex"
        private const val KEY_VERSE_INDEX = "verseIndex"
        private const val KEY_TIMESTAMP = "timestamp"
        private const val KEY_COLOR = "color"
    }

    private val writer = StringWriter()
    private val jsonWriter = JsonWriter(BufferedWriter(writer)).apply {
        setIndent("  ")
        beginObject()
    }

    override fun withBookmarks(bookmarks: List<Bookmark>): BackupManager.Serializer {
        with(jsonWriter) {
            name(KEY_BOOKMARKS)
            beginArray()
            bookmarks.forEach { bookmark ->
                beginObject()
                withVerseIndex(bookmark.verseIndex)
                name(KEY_TIMESTAMP).value(bookmark.timestamp)
                endObject()
            }
            endArray()
        }

        return this
    }

    private fun withVerseIndex(verseIndex: VerseIndex) {
        with(jsonWriter) {
            name(KEY_BOOK_INDEX).value(verseIndex.bookIndex)
            name(KEY_CHAPTER_INDEX).value(verseIndex.chapterIndex)
            name(KEY_VERSE_INDEX).value(verseIndex.verseIndex)
        }
    }

    override fun withHighlights(highlights: List<Highlight>): BackupManager.Serializer {
        with(jsonWriter) {
            name(KEY_HIGHLIGHTS)
            beginArray()
            highlights.forEach { highlight ->
                beginObject()
                withVerseIndex(highlight.verseIndex)
                name(KEY_COLOR).value(highlight.color)
                name(KEY_TIMESTAMP).value(highlight.timestamp)
                endObject()
            }
            endArray()
        }

        return this
    }

    override fun serialize(): String {
        jsonWriter.endObject()
        jsonWriter.close()
        return writer.toString()
    }
}

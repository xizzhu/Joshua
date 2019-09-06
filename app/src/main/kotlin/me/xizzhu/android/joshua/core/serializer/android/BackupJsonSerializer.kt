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
import me.xizzhu.android.joshua.core.*
import java.io.BufferedWriter
import java.io.StringWriter

private object Constants {
    const val KEY_BOOKMARKS = "bookmarks"
    const val KEY_HIGHLIGHTS = "highlights"
    const val KEY_NOTES = "notes"
    const val KEY_READING_PROGRESS = "readingProgress"

    const val KEY_BOOK_INDEX = "bookIndex"
    const val KEY_CHAPTER_INDEX = "chapterIndex"
    const val KEY_VERSE_INDEX = "verseIndex"
    const val KEY_TIMESTAMP = "timestamp"
    const val KEY_COLOR = "color"
    const val KEY_NOTE = "note"
    const val KEY_CONTINUOUS_READING_DAYS = "continuousReadingDays"
    const val KEY_LAST_READING_TIMESTAMP = "lastReadingTimestamp"
    const val KEY_CHAPTER_READING_STATUS = "chapterReadingStatus"
    const val KEY_READ_COUNT = "readCount"
    const val KEY_TIME_SPENT_IN_MILLIS = "timeSpentInMillis"
}

class BackupJsonSerializer : BackupManager.Serializer {
    private val writer = StringWriter()
    private val jsonWriter = JsonWriter(BufferedWriter(writer)).apply {
        setIndent("  ")
        beginObject()
    }

    override fun withBookmarks(bookmarks: List<Bookmark>): BackupManager.Serializer {
        with(jsonWriter) {
            name(Constants.KEY_BOOKMARKS)
            beginArray()
            bookmarks.forEach { bookmark ->
                beginObject()
                withVerseIndex(bookmark.verseIndex)
                name(Constants.KEY_TIMESTAMP).value(bookmark.timestamp)
                endObject()
            }
            endArray()
        }

        return this
    }

    private fun withVerseIndex(verseIndex: VerseIndex) {
        with(jsonWriter) {
            name(Constants.KEY_BOOK_INDEX).value(verseIndex.bookIndex)
            name(Constants.KEY_CHAPTER_INDEX).value(verseIndex.chapterIndex)
            name(Constants.KEY_VERSE_INDEX).value(verseIndex.verseIndex)
        }
    }

    override fun withHighlights(highlights: List<Highlight>): BackupManager.Serializer {
        with(jsonWriter) {
            name(Constants.KEY_HIGHLIGHTS)
            beginArray()
            highlights.forEach { highlight ->
                beginObject()
                withVerseIndex(highlight.verseIndex)
                name(Constants.KEY_COLOR).value(highlight.color)
                name(Constants.KEY_TIMESTAMP).value(highlight.timestamp)
                endObject()
            }
            endArray()
        }

        return this
    }

    override fun withNotes(notes: List<Note>): BackupManager.Serializer {
        with(jsonWriter) {
            name(Constants.KEY_NOTES)
            beginArray()
            notes.forEach { note ->
                beginObject()
                withVerseIndex(note.verseIndex)
                name(Constants.KEY_NOTE).value(note.note)
                name(Constants.KEY_TIMESTAMP).value(note.timestamp)
                endObject()
            }
            endArray()
        }

        return this
    }

    override fun withReadingProgress(readingProgress: ReadingProgress): BackupManager.Serializer {
        with(jsonWriter) {
            name(Constants.KEY_READING_PROGRESS)
            beginObject()
            name(Constants.KEY_CONTINUOUS_READING_DAYS).value(readingProgress.continuousReadingDays)
            name(Constants.KEY_LAST_READING_TIMESTAMP).value(readingProgress.lastReadingTimestamp)
            name(Constants.KEY_CHAPTER_READING_STATUS)
            beginArray()
            readingProgress.chapterReadingStatus.forEach { chapterReadingStatus ->
                beginObject()
                name(Constants.KEY_BOOK_INDEX).value(chapterReadingStatus.bookIndex)
                name(Constants.KEY_CHAPTER_INDEX).value(chapterReadingStatus.chapterIndex)
                name(Constants.KEY_READ_COUNT).value(chapterReadingStatus.readCount)
                name(Constants.KEY_TIME_SPENT_IN_MILLIS).value(chapterReadingStatus.timeSpentInMillis)
                name(Constants.KEY_LAST_READING_TIMESTAMP).value(chapterReadingStatus.lastReadingTimestamp)
                endObject()
            }
            endArray()
            endObject()
        }

        return this
    }

    override fun serialize(): String {
        jsonWriter.endObject()
        jsonWriter.close()
        return writer.toString()
    }
}

class BackupJsonDeserializer : BackupManager.Deserializer {
    private var content: String? = null

    override fun withContent(content: String): BackupManager.Deserializer {
        this.content = content
        return this
    }

    override fun deserialize(): BackupManager.Data =
            content?.let {
                val bookmarks = mutableListOf<Bookmark>()
                val highlights = mutableListOf<Highlight>()
                val notes = mutableListOf<Note>()
                val continuousReadingDays = 0
                val lastReadingTimestamp = 0L
                val chapterReadingStatus = mutableListOf<ReadingProgress.ChapterReadingStatus>()

                // TODO

                return@let BackupManager.Data(bookmarks, highlights, notes,
                        ReadingProgress(continuousReadingDays, lastReadingTimestamp, chapterReadingStatus))
            } ?: throw IllegalStateException("Missing content")
}

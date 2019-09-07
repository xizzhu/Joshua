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

import android.util.JsonReader
import android.util.JsonWriter
import me.xizzhu.android.joshua.core.*
import java.io.BufferedWriter
import java.io.StringReader
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
                var bookmarks: List<Bookmark>? = null
                val highlights = mutableListOf<Highlight>()
                val notes = mutableListOf<Note>()
                var readingProgress: ReadingProgress? = null

                with(JsonReader(StringReader(it))) {
                    beginObject()
                    while (hasNext()) {
                        when (nextName()) {
                            Constants.KEY_BOOKMARKS -> bookmarks = readBookMarks()
                            Constants.KEY_HIGHLIGHTS -> {
                                // TODO
                                skipValue()
                            }
                            Constants.KEY_NOTES -> {
                                // TODO
                                skipValue()
                            }
                            Constants.KEY_READING_PROGRESS -> readingProgress = readReadingProgress()
                            else -> skipValue()
                        }
                    }
                    endObject()
                    close()
                }

                if (bookmarks == null) throw IllegalStateException("Missing bookmarks")
                if (readingProgress == null) throw IllegalStateException("Missing reading progress")

                return@let BackupManager.Data(bookmarks!!, highlights, notes, readingProgress!!)
            } ?: throw IllegalStateException("Missing content")

    private fun JsonReader.readBookMarks(): List<Bookmark> {
        val bookmarks = mutableListOf<Bookmark>()
        beginArray()
        while (hasNext()) {
            readBookmark().let { if (it.isValid()) bookmarks.add(it) }
        }
        endArray()
        return bookmarks
    }

    private fun JsonReader.readBookmark(): Bookmark {
        var bookIndex = -1
        var chapterIndex = -1
        var verseIndex = -1
        var timestamp = -1L
        beginObject()
        while (hasNext()) {
            when (nextName()) {
                Constants.KEY_BOOK_INDEX -> bookIndex = nextInt()
                Constants.KEY_CHAPTER_INDEX -> chapterIndex = nextInt()
                Constants.KEY_VERSE_INDEX -> verseIndex = nextInt()
                Constants.KEY_TIMESTAMP -> timestamp = nextLong()
                else -> skipValue()
            }
        }
        endObject()
        return Bookmark(VerseIndex(bookIndex, chapterIndex, verseIndex), timestamp)
    }

    private fun JsonReader.readReadingProgress(): ReadingProgress {
        var continuousReadingDays = 0
        var lastReadingTimestamp = 0L
        val chapterReadingStatus = mutableListOf<ReadingProgress.ChapterReadingStatus>()
        beginObject()
        while (hasNext()) {
            when (nextName()) {
                Constants.KEY_CONTINUOUS_READING_DAYS -> continuousReadingDays = nextInt()
                Constants.KEY_LAST_READING_TIMESTAMP -> lastReadingTimestamp = nextLong()
                Constants.KEY_CHAPTER_READING_STATUS -> {
                    beginArray()
                    while (hasNext()) {
                        readChapterReadingStatus().let { if (it.isValid()) chapterReadingStatus.add(it) }
                    }
                    endArray()
                }
                else -> skipValue()
            }
        }
        endObject()
        return ReadingProgress(continuousReadingDays, lastReadingTimestamp, chapterReadingStatus)
    }

    private fun JsonReader.readChapterReadingStatus(): ReadingProgress.ChapterReadingStatus {
        var bookIndex = -1
        var chapterIndex = -1
        var readCount = -1
        var timeSpentInMillis = -1L
        var lastReadingTimestamp = -1L
        beginObject()
        while (hasNext()) {
            when (nextName()) {
                Constants.KEY_BOOK_INDEX -> bookIndex = nextInt()
                Constants.KEY_CHAPTER_INDEX -> chapterIndex = nextInt()
                Constants.KEY_READ_COUNT -> readCount = nextInt()
                Constants.KEY_TIME_SPENT_IN_MILLIS -> timeSpentInMillis = nextLong()
                Constants.KEY_LAST_READING_TIMESTAMP -> lastReadingTimestamp = nextLong()
                else -> skipValue()
            }
        }
        endObject()
        return ReadingProgress.ChapterReadingStatus(bookIndex, chapterIndex, readCount, timeSpentInMillis, lastReadingTimestamp)
    }
}

/*
 * Copyright (C) 2022 Xizhi Zhu
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
    override fun serialize(data: BackupManager.Data): String {
        val writer = StringWriter()
        JsonWriter(BufferedWriter(writer)).apply { setIndent("  ") }
                .beginObject()
                .withBookmarks(data.bookmarks)
                .withHighlights(data.highlights)
                .withNotes(data.notes)
                .withReadingProgress(data.readingProgress)
                .endObject()
                .close()
        return writer.toString()
    }

    private fun JsonWriter.withBookmarks(bookmarks: List<Bookmark>): JsonWriter {
        name(Constants.KEY_BOOKMARKS)
        beginArray()
        bookmarks.forEach { bookmark ->
            beginObject()
            withVerseIndex(bookmark.verseIndex)
            name(Constants.KEY_TIMESTAMP).value(bookmark.timestamp)
            endObject()
        }
        endArray()
        return this
    }

    private fun JsonWriter.withVerseIndex(verseIndex: VerseIndex): JsonWriter {
        name(Constants.KEY_BOOK_INDEX).value(verseIndex.bookIndex)
        name(Constants.KEY_CHAPTER_INDEX).value(verseIndex.chapterIndex)
        name(Constants.KEY_VERSE_INDEX).value(verseIndex.verseIndex)
        return this
    }

    private fun JsonWriter.withHighlights(highlights: List<Highlight>): JsonWriter {
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
        return this
    }

    private fun JsonWriter.withNotes(notes: List<Note>): JsonWriter {
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
        return this
    }

    private fun JsonWriter.withReadingProgress(readingProgress: ReadingProgress): JsonWriter {
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
        return this
    }

    override fun deserialize(content: String): BackupManager.Data {
        var bookmarks: List<Bookmark>? = null
        var highlights: List<Highlight>? = null
        var notes: List<Note>? = null
        var readingProgress: ReadingProgress? = null

        with(JsonReader(StringReader(content))) {
            beginObject()
            while (hasNext()) {
                when (nextName()) {
                    Constants.KEY_BOOKMARKS -> bookmarks = readBookMarks()
                    Constants.KEY_HIGHLIGHTS -> highlights = readHighlights()
                    Constants.KEY_NOTES -> notes = readNotes()
                    Constants.KEY_READING_PROGRESS -> readingProgress = readReadingProgress()
                    else -> skipValue()
                }
            }
            endObject()
            close()
        }

        if (bookmarks == null) throw IllegalStateException("Missing bookmarks")
        if (highlights == null) throw IllegalStateException("Missing highlights")
        if (notes == null) throw IllegalStateException("Missing notes")
        if (readingProgress?.isValid() != true) throw IllegalStateException("Missing reading progress")

        return BackupManager.Data(bookmarks!!, highlights!!, notes!!, readingProgress!!)
    }

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

    private fun JsonReader.readHighlights(): List<Highlight> {
        val highlights = mutableListOf<Highlight>()
        beginArray()
        while (hasNext()) {
            readHighlight().let { if (it.isValid()) highlights.add(it) }
        }
        endArray()
        return highlights
    }

    private fun JsonReader.readHighlight(): Highlight {
        var bookIndex = -1
        var chapterIndex = -1
        var verseIndex = -1
        var color = Highlight.COLOR_NONE
        var timestamp = -1L
        beginObject()
        while (hasNext()) {
            when (nextName()) {
                Constants.KEY_BOOK_INDEX -> bookIndex = nextInt()
                Constants.KEY_CHAPTER_INDEX -> chapterIndex = nextInt()
                Constants.KEY_VERSE_INDEX -> verseIndex = nextInt()
                Constants.KEY_COLOR -> color = nextInt()
                Constants.KEY_TIMESTAMP -> timestamp = nextLong()
                else -> skipValue()
            }
        }
        endObject()
        return Highlight(VerseIndex(bookIndex, chapterIndex, verseIndex), color, timestamp)
    }

    private fun JsonReader.readNotes(): List<Note> {
        val notes = mutableListOf<Note>()
        beginArray()
        while (hasNext()) {
            readNote().let { if (it.isValid()) notes.add(it) }
        }
        endArray()
        return notes
    }

    private fun JsonReader.readNote(): Note {
        var bookIndex = -1
        var chapterIndex = -1
        var verseIndex = -1
        var note = ""
        var timestamp = -1L
        beginObject()
        while (hasNext()) {
            when (nextName()) {
                Constants.KEY_BOOK_INDEX -> bookIndex = nextInt()
                Constants.KEY_CHAPTER_INDEX -> chapterIndex = nextInt()
                Constants.KEY_VERSE_INDEX -> verseIndex = nextInt()
                Constants.KEY_NOTE -> note = nextString()
                Constants.KEY_TIMESTAMP -> timestamp = nextLong()
                else -> skipValue()
            }
        }
        endObject()
        return Note(VerseIndex(bookIndex, chapterIndex, verseIndex), note, timestamp)
    }

    private fun JsonReader.readReadingProgress(): ReadingProgress {
        var continuousReadingDays = -1
        var lastReadingTimestamp = -1L
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

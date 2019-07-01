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

package me.xizzhu.android.joshua.core.repository.local.android.db

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.annotation.WorkerThread
import me.xizzhu.android.joshua.core.Highlight
import me.xizzhu.android.joshua.core.VerseIndex

class HighlightDao(sqliteHelper: SQLiteOpenHelper) {
    companion object {
        private const val TABLE_HIGHLIGHT = "highlight"
        private const val COLUMN_BOOK_INDEX = "bookIndex"
        private const val COLUMN_CHAPTER_INDEX = "chapterIndex"
        private const val COLUMN_VERSE_INDEX = "verseIndex"
        private const val COLUMN_COLOR = "color"
        private const val COLUMN_TIMESTAMP = "timestamp"

        @WorkerThread
        fun createTable(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE $TABLE_HIGHLIGHT (" +
                    "$COLUMN_BOOK_INDEX INTEGER NOT NULL, " +
                    "$COLUMN_CHAPTER_INDEX INTEGER NOT NULL, " +
                    "$COLUMN_VERSE_INDEX INTEGER NOT NULL, " +
                    "$COLUMN_COLOR INTEGER NOT NULL, " +
                    "$COLUMN_TIMESTAMP INTEGER NOT NULL, " +
                    "PRIMARY KEY ($COLUMN_BOOK_INDEX, $COLUMN_CHAPTER_INDEX, $COLUMN_VERSE_INDEX));")
        }
    }

    private val db by lazy { sqliteHelper.writableDatabase }

    fun read(bookIndex: Int, chapterIndex: Int): List<Highlight> {
        db.query(TABLE_HIGHLIGHT, arrayOf(COLUMN_VERSE_INDEX, COLUMN_COLOR, COLUMN_TIMESTAMP),
                "$COLUMN_BOOK_INDEX = ? AND $COLUMN_CHAPTER_INDEX = ?",
                arrayOf(bookIndex.toString(), chapterIndex.toString()),
                null, null, "$COLUMN_VERSE_INDEX ASC").use {
            val highlights = ArrayList<Highlight>(it.count)
            val verseIndex = it.getColumnIndex(COLUMN_VERSE_INDEX)
            val color = it.getColumnIndex(COLUMN_COLOR)
            val timestamp = it.getColumnIndex(COLUMN_TIMESTAMP)
            while (it.moveToNext()) {
                highlights.add(Highlight(VerseIndex(bookIndex, chapterIndex, it.getInt(verseIndex)),
                        it.getInt(color), it.getLong(timestamp)))
            }
            return highlights
        }
    }

    fun read(verseIndex: VerseIndex): Highlight {
        db.query(TABLE_HIGHLIGHT, arrayOf(COLUMN_COLOR, COLUMN_TIMESTAMP),
                "$COLUMN_BOOK_INDEX = ? AND $COLUMN_CHAPTER_INDEX = ? AND $COLUMN_VERSE_INDEX = ?",
                arrayOf(verseIndex.bookIndex.toString(), verseIndex.chapterIndex.toString(), verseIndex.verseIndex.toString()),
                null, null, null).use {
            return if (it.moveToNext()) {
                Highlight(verseIndex, it.getInt(it.getColumnIndex(COLUMN_COLOR)), it.getLong(it.getColumnIndex(COLUMN_TIMESTAMP)))
            } else {
                Highlight(verseIndex, Highlight.COLOR_NONE, -1L)
            }
        }
    }

    fun save(highlight: Highlight) {
        val values = ContentValues(4)
        with(values) {
            put(COLUMN_BOOK_INDEX, highlight.verseIndex.bookIndex)
            put(COLUMN_CHAPTER_INDEX, highlight.verseIndex.chapterIndex)
            put(COLUMN_VERSE_INDEX, highlight.verseIndex.verseIndex)
            put(COLUMN_COLOR, highlight.color)
            put(COLUMN_TIMESTAMP, highlight.timestamp)
        }
        db.insertWithOnConflict(TABLE_HIGHLIGHT, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun remove(verseIndex: VerseIndex) {
        db.delete(TABLE_HIGHLIGHT, "$COLUMN_BOOK_INDEX = ? AND $COLUMN_CHAPTER_INDEX = ? AND $COLUMN_VERSE_INDEX = ?",
                arrayOf(verseIndex.bookIndex.toString(), verseIndex.chapterIndex.toString(), verseIndex.verseIndex.toString()))
    }
}

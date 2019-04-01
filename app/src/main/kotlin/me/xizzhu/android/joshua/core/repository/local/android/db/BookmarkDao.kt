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
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.annotation.WorkerThread
import me.xizzhu.android.joshua.core.Bookmark
import me.xizzhu.android.joshua.core.VerseIndex

class BookmarkDao(private val sqliteHelper: SQLiteOpenHelper) {
    companion object {
        private const val TABLE_BOOKMARK = "bookmark"
        private const val COLUMN_BOOK_INDEX = "bookIndex"
        private const val COLUMN_CHAPTER_INDEX = "chapterIndex"
        private const val COLUMN_VERSE_INDEX = "verseIndex"
        private const val COLUMN_TIMESTAMP = "timestamp"

        @WorkerThread
        fun createTable(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE $TABLE_BOOKMARK (" +
                    "$COLUMN_BOOK_INDEX INTEGER NOT NULL, " +
                    "$COLUMN_CHAPTER_INDEX INTEGER NOT NULL, " +
                    "$COLUMN_VERSE_INDEX INTEGER NOT NULL, " +
                    "$COLUMN_TIMESTAMP INTEGER NOT NULL, " +
                    "PRIMARY KEY ($COLUMN_BOOK_INDEX, $COLUMN_CHAPTER_INDEX, $COLUMN_VERSE_INDEX));")
        }
    }

    private val db by lazy { sqliteHelper.writableDatabase }

    fun read(): List<Bookmark> {
        var cursor: Cursor? = null
        try {
            cursor = db.query(TABLE_BOOKMARK, null, null, null, null, null, "$COLUMN_TIMESTAMP DESC")
            val bookmarks = ArrayList<Bookmark>(cursor.count)
            val bookIndex = cursor.getColumnIndex(COLUMN_BOOK_INDEX)
            val chapterIndex = cursor.getColumnIndex(COLUMN_CHAPTER_INDEX)
            val verseIndex = cursor.getColumnIndex(COLUMN_VERSE_INDEX)
            val timestamp = cursor.getColumnIndex(COLUMN_TIMESTAMP)
            while (cursor.moveToNext()) {
                bookmarks.add(Bookmark(
                        VerseIndex(cursor.getInt(bookIndex), cursor.getInt(chapterIndex), cursor.getInt(verseIndex)),
                        cursor.getLong(timestamp)))
            }
            return bookmarks
        } finally {
            cursor?.close()
        }
    }

    fun read(bookIndex: Int, chapterIndex: Int): List<Bookmark> {
        var cursor: Cursor? = null
        try {
            cursor = db.query(TABLE_BOOKMARK, arrayOf(COLUMN_VERSE_INDEX, COLUMN_TIMESTAMP),
                    "$COLUMN_BOOK_INDEX = ? AND $COLUMN_CHAPTER_INDEX = ?", arrayOf(bookIndex.toString(), chapterIndex.toString()),
                    null, null, "$COLUMN_VERSE_INDEX ASC")
            val bookmarks = ArrayList<Bookmark>(cursor.count)
            val verseIndex = cursor.getColumnIndex(COLUMN_VERSE_INDEX)
            val timestamp = cursor.getColumnIndex(COLUMN_TIMESTAMP)
            while (cursor.moveToNext()) {
                bookmarks.add(Bookmark(VerseIndex(bookIndex, chapterIndex, cursor.getInt(verseIndex)),
                        cursor.getLong(timestamp)))
            }
            return bookmarks
        } finally {
            cursor?.close()
        }
    }

    fun save(bookmark: Bookmark) {
        val values = ContentValues(4)
        values.put(COLUMN_BOOK_INDEX, bookmark.verseIndex.bookIndex)
        values.put(COLUMN_CHAPTER_INDEX, bookmark.verseIndex.chapterIndex)
        values.put(COLUMN_VERSE_INDEX, bookmark.verseIndex.verseIndex)
        values.put(COLUMN_TIMESTAMP, bookmark.timestamp)
        db.insertWithOnConflict(TABLE_BOOKMARK, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun remove(verseIndex: VerseIndex) {
        db.delete(TABLE_BOOKMARK, "$COLUMN_BOOK_INDEX = ? AND $COLUMN_CHAPTER_INDEX = ? AND $COLUMN_VERSE_INDEX = ?",
                arrayOf(verseIndex.bookIndex.toString(), verseIndex.chapterIndex.toString(), verseIndex.verseIndex.toString()))
    }
}

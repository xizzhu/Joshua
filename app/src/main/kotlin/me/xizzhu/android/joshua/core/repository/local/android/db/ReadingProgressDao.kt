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
import me.xizzhu.android.joshua.core.ReadingProgress

class ReadingProgressDao(private val sqliteHelper: SQLiteOpenHelper) {
    companion object {
        private const val TABLE_READING_PROGRESS = "readingProgress"
        private const val COLUMN_BOOK_INDEX = "bookIndex"
        private const val COLUMN_CHAPTER_INDEX = "chapterIndex"
        private const val COLUMN_READ_COUNT = "readCount"
        private const val COLUMN_LAST_READING_TIMESTAMP = "lastReadingTimestamp"

        @WorkerThread
        fun createTable(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE $TABLE_READING_PROGRESS (" +
                    "$COLUMN_BOOK_INDEX INTEGER NOT NULL, $COLUMN_CHAPTER_INDEX INTEGER NOT NULL, " +
                    "$COLUMN_LAST_READING_TIMESTAMP INTEGER NOT NULL, $COLUMN_READ_COUNT INTEGER NOT NULL, " +
                    "PRIMARY KEY ($COLUMN_BOOK_INDEX, $COLUMN_CHAPTER_INDEX));")
        }
    }

    private val db by lazy { sqliteHelper.writableDatabase }

    @WorkerThread
    fun save(chapterReadingStatus: ReadingProgress.ChapterReadingStatus) {
        val values = ContentValues(4)
        values.put(COLUMN_BOOK_INDEX, chapterReadingStatus.bookIndex)
        values.put(COLUMN_CHAPTER_INDEX, chapterReadingStatus.chapterIndex)
        values.put(COLUMN_READ_COUNT, chapterReadingStatus.readCount)
        values.put(COLUMN_LAST_READING_TIMESTAMP, chapterReadingStatus.lastReadingTimestamp)
        db.insertWithOnConflict(TABLE_READING_PROGRESS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    @WorkerThread
    fun read(bookIndex: Int, chapterIndex: Int): ReadingProgress.ChapterReadingStatus {
        var cursor: Cursor? = null
        try {
            cursor = db.query(TABLE_READING_PROGRESS, arrayOf(COLUMN_READ_COUNT, COLUMN_LAST_READING_TIMESTAMP),
                    "$COLUMN_BOOK_INDEX = ? AND $COLUMN_CHAPTER_INDEX = ?",
                    arrayOf(bookIndex.toString(), chapterIndex.toString()), null, null, null)
            val readCount: Int
            val lastReadingTimestamp: Long
            if (cursor.count > 0 && cursor.moveToNext()) {
                readCount = cursor.getInt(cursor.getColumnIndex(COLUMN_READ_COUNT))
                lastReadingTimestamp = cursor.getLong(cursor.getColumnIndex(COLUMN_LAST_READING_TIMESTAMP))
            } else {
                readCount = 0
                lastReadingTimestamp = 0L
            }
            return ReadingProgress.ChapterReadingStatus(bookIndex, chapterIndex, readCount, lastReadingTimestamp)
        } finally {
            cursor?.close()
        }
    }
}

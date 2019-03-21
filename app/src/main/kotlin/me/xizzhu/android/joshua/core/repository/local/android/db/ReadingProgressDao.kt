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
        private const val COLUMN_TIME_SPENT_IN_MILLS = "timeSpentInMills"
        private const val COLUMN_LAST_READING_TIMESTAMP = "lastReadingTimestamp"

        @WorkerThread
        fun createTable(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE $TABLE_READING_PROGRESS (" +
                    "$COLUMN_BOOK_INDEX INTEGER NOT NULL, $COLUMN_CHAPTER_INDEX INTEGER NOT NULL, " +
                    "$COLUMN_READ_COUNT INTEGER NOT NULL, $COLUMN_TIME_SPENT_IN_MILLS INTEGER NOT NULL, " +
                    "$COLUMN_LAST_READING_TIMESTAMP INTEGER NOT NULL, " +
                    "PRIMARY KEY ($COLUMN_BOOK_INDEX, $COLUMN_CHAPTER_INDEX));")
        }
    }

    private val db by lazy { sqliteHelper.writableDatabase }

    @WorkerThread
    fun save(chapterReadingStatus: ReadingProgress.ChapterReadingStatus) {
        val values = ContentValues(5)
        values.put(COLUMN_BOOK_INDEX, chapterReadingStatus.bookIndex)
        values.put(COLUMN_CHAPTER_INDEX, chapterReadingStatus.chapterIndex)
        values.put(COLUMN_READ_COUNT, chapterReadingStatus.readCount)
        values.put(COLUMN_TIME_SPENT_IN_MILLS, chapterReadingStatus.timeSpentInMillis)
        values.put(COLUMN_LAST_READING_TIMESTAMP, chapterReadingStatus.lastReadingTimestamp)
        db.insertWithOnConflict(TABLE_READING_PROGRESS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    @WorkerThread
    fun read(): List<ReadingProgress.ChapterReadingStatus> {
        var cursor: Cursor? = null
        try {
            cursor = db.query(TABLE_READING_PROGRESS, arrayOf(COLUMN_BOOK_INDEX, COLUMN_CHAPTER_INDEX,
                    COLUMN_READ_COUNT, COLUMN_TIME_SPENT_IN_MILLS, COLUMN_LAST_READING_TIMESTAMP),
                    null, null, null, null, null)
            val count = cursor.count
            if (count == 0) {
                return emptyList()
            }

            val bookIndex = cursor.getColumnIndex(COLUMN_BOOK_INDEX)
            val chapterIndex = cursor.getColumnIndex(COLUMN_CHAPTER_INDEX)
            val readCount = cursor.getColumnIndex(COLUMN_READ_COUNT)
            val timeSpentInMills = cursor.getColumnIndex(COLUMN_TIME_SPENT_IN_MILLS)
            val lastReadingTimestamp = cursor.getColumnIndex(COLUMN_LAST_READING_TIMESTAMP)
            val result = ArrayList<ReadingProgress.ChapterReadingStatus>(count)
            while (cursor.moveToNext()) {
                result.add(ReadingProgress.ChapterReadingStatus(cursor.getInt(bookIndex), cursor.getInt(chapterIndex),
                        cursor.getInt(readCount), cursor.getLong(timeSpentInMills), cursor.getLong(lastReadingTimestamp)))
            }
            return result
        } finally {
            cursor?.close()
        }
    }

    @WorkerThread
    fun read(bookIndex: Int, chapterIndex: Int): ReadingProgress.ChapterReadingStatus {
        var cursor: Cursor? = null
        try {
            cursor = db.query(TABLE_READING_PROGRESS, arrayOf(COLUMN_READ_COUNT, COLUMN_TIME_SPENT_IN_MILLS, COLUMN_LAST_READING_TIMESTAMP),
                    "$COLUMN_BOOK_INDEX = ? AND $COLUMN_CHAPTER_INDEX = ?",
                    arrayOf(bookIndex.toString(), chapterIndex.toString()), null, null, null)
            val readCount: Int
            val timeSpentInMills: Long
            val lastReadingTimestamp: Long
            if (cursor.count > 0 && cursor.moveToNext()) {
                readCount = cursor.getInt(cursor.getColumnIndex(COLUMN_READ_COUNT))
                timeSpentInMills = cursor.getLong(cursor.getColumnIndex(COLUMN_TIME_SPENT_IN_MILLS))
                lastReadingTimestamp = cursor.getLong(cursor.getColumnIndex(COLUMN_LAST_READING_TIMESTAMP))
            } else {
                readCount = 0
                timeSpentInMills = 0L
                lastReadingTimestamp = 0L
            }
            return ReadingProgress.ChapterReadingStatus(bookIndex, chapterIndex, readCount, timeSpentInMills, lastReadingTimestamp)
        } finally {
            cursor?.close()
        }
    }
}

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
import me.xizzhu.android.joshua.core.ReadingProgress

class ReadingProgressDao(sqliteHelper: SQLiteOpenHelper) {
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
        val values = ContentValues(5).apply {
            put(COLUMN_BOOK_INDEX, chapterReadingStatus.bookIndex)
            put(COLUMN_CHAPTER_INDEX, chapterReadingStatus.chapterIndex)
            put(COLUMN_READ_COUNT, chapterReadingStatus.readCount)
            put(COLUMN_TIME_SPENT_IN_MILLS, chapterReadingStatus.timeSpentInMillis)
            put(COLUMN_LAST_READING_TIMESTAMP, chapterReadingStatus.lastReadingTimestamp)
        }
        db.insertWithOnConflict(TABLE_READING_PROGRESS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    @WorkerThread
    fun read(): List<ReadingProgress.ChapterReadingStatus> {
        db.query(TABLE_READING_PROGRESS, arrayOf(COLUMN_BOOK_INDEX, COLUMN_CHAPTER_INDEX,
                COLUMN_READ_COUNT, COLUMN_TIME_SPENT_IN_MILLS, COLUMN_LAST_READING_TIMESTAMP),
                null, null, null, null, "$COLUMN_BOOK_INDEX ASC, $COLUMN_CHAPTER_INDEX ASC").use {
            val result = ArrayList<ReadingProgress.ChapterReadingStatus>(it.count)
            if (it.count > 0) {
                val bookIndex = it.getColumnIndex(COLUMN_BOOK_INDEX)
                val chapterIndex = it.getColumnIndex(COLUMN_CHAPTER_INDEX)
                val readCount = it.getColumnIndex(COLUMN_READ_COUNT)
                val timeSpentInMills = it.getColumnIndex(COLUMN_TIME_SPENT_IN_MILLS)
                val lastReadingTimestamp = it.getColumnIndex(COLUMN_LAST_READING_TIMESTAMP)
                while (it.moveToNext()) {
                    result.add(ReadingProgress.ChapterReadingStatus(it.getInt(bookIndex), it.getInt(chapterIndex),
                            it.getInt(readCount), it.getLong(timeSpentInMills), it.getLong(lastReadingTimestamp)))
                }
            }
            return result
        }
    }

    @WorkerThread
    fun read(bookIndex: Int, chapterIndex: Int): ReadingProgress.ChapterReadingStatus {
        db.query(TABLE_READING_PROGRESS, arrayOf(COLUMN_READ_COUNT, COLUMN_TIME_SPENT_IN_MILLS, COLUMN_LAST_READING_TIMESTAMP),
                "$COLUMN_BOOK_INDEX = ? AND $COLUMN_CHAPTER_INDEX = ?",
                arrayOf(bookIndex.toString(), chapterIndex.toString()), null, null, null).use {
            val readCount: Int
            val timeSpentInMills: Long
            val lastReadingTimestamp: Long
            if (it.count > 0 && it.moveToNext()) {
                readCount = it.getInt(it.getColumnIndex(COLUMN_READ_COUNT))
                timeSpentInMills = it.getLong(it.getColumnIndex(COLUMN_TIME_SPENT_IN_MILLS))
                lastReadingTimestamp = it.getLong(it.getColumnIndex(COLUMN_LAST_READING_TIMESTAMP))
            } else {
                readCount = 0
                timeSpentInMills = 0L
                lastReadingTimestamp = 0L
            }
            return ReadingProgress.ChapterReadingStatus(bookIndex, chapterIndex, readCount, timeSpentInMills, lastReadingTimestamp)
        }
    }
}
